package metamutator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.google.common.collect.Sets;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtCFlowBreak;
import spoon.reflect.code.CtCase;
import spoon.reflect.code.CtCodeSnippetExpression;
import spoon.reflect.code.CtCodeSnippetStatement;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtContinue;
import spoon.reflect.code.CtDo;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFor;
import spoon.reflect.code.CtForEach;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtLoop;
import spoon.reflect.code.CtNewClass;
import spoon.reflect.code.CtOperatorAssignment;
import spoon.reflect.code.CtReturn;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtSwitch;
import spoon.reflect.code.CtSynchronized;
import spoon.reflect.code.CtThrow;
import spoon.reflect.code.CtTry;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.code.CtWhile;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtEnum;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.Filter;
import spoon.reflect.visitor.filter.ReturnOrThrowFilter;
import spoon.support.reflect.code.CtExpressionImpl;
import spoon.support.reflect.code.CtLambdaImpl;
import spoon.support.reflect.code.CtLiteralImpl;
import spoon.reflect.code.CtAssert;
import spoon.reflect.code.CtAssignment;

public class StatementDeletionMetaMutator 
extends AbstractProcessor<CtStatement> {

	public static final String PREFIX =  "_StatementDeletionMutatorHotSpot";
	private static int index = 0;
	
	public enum ACTIVABLE {
		// NO CHANGE
		ENABLED,
		// Absolute Value	
		DISABLED
	};
	
	private static final EnumSet<ACTIVABLE> ActivableSet = EnumSet
			.of(ACTIVABLE.ENABLED, ACTIVABLE.DISABLED);
	

	private Set<CtElement> hotSpots = Sets.newHashSet();
	
	//break? si boucle infini
	//ctcflowbreak ? kesako?
	//invocation?	private static final List<Class> MODIFIABLE_STATEMENTS = new ArrayList<Class>(
	private static final List<Class> MODIFIABLE_STATEMENTS = new ArrayList<Class>(
			Arrays.asList(CtAssert.class, CtContinue.class, CtDo.class, CtFor.class, CtForEach.class,
					CtIf.class, CtLoop.class, CtSwitch.class, CtThrow.class, CtWhile.class)
			);
	
	
	//break? si boucle infini
	//ctcflowbreak ? kesako?
	private static final List<Class> UNMODIFIABLE_STATEMENTS = new ArrayList<Class>(
			Arrays.asList(CtAssignment.class, CtBlock.class, CtCFlowBreak.class, CtClass.class,
					CtCodeSnippetStatement.class, CtConstructorCall.class, CtEnum.class, CtInvocation.class,
					CtLocalVariable.class, CtNewClass.class, CtOperatorAssignment.class, CtReturn.class, 
					CtSynchronized.class, CtUnaryOperator.class)
			);
	
	
	
	//Templates of returned expressions, used when a function have a return in a statement which can be deleted.
	private static final Map<Class, CtLiteral> returnedExpressions;
	    static {
	        HashMap<Class, CtLiteral> map = new HashMap<Class, CtLiteral>();
	        map.put(byte.class, new CtLiteralImpl<Byte>().setValue(0));
	        map.put(short.class, new CtLiteralImpl<Short>().setValue(0));
	        map.put(int.class, new CtLiteralImpl<Integer>().setValue(0));
	        map.put(long.class, new CtLiteralImpl<Long>().setValue(0L));
	        map.put(float.class, new CtLiteralImpl<Float>().setValue(0.0f));
	        map.put(double.class,new CtLiteralImpl<Double>().setValue(0.0d));
	        map.put(boolean.class,new CtLiteralImpl<Boolean>().setValue(false));
	        map.put(char.class,new CtLiteralImpl<Character>().setValue('\u0000'));
	        map.put(void.class,null);
	        returnedExpressions = Collections.unmodifiableMap(map);
	    }

	
	@Override
	public boolean isToBeProcessed(CtStatement element) {
		for(Class c : MODIFIABLE_STATEMENTS){
			if(c.isInstance(element)){
				return true;
			}
		}
		return false;
	}
	
	
	@Override
	public void process(CtStatement element) {
		mutateOperator(element);
	}
	
	
	private void mutateOperator(final CtStatement expression) {
		

		System.out.println("---------------------------------------------------------------------");
		System.out.println(expression);
		System.out.println("---------------------------------------------------------------------");
		
		if (alreadyInHotsSpot(expression)) {
			System.out
					.println(String
							.format("Expression '%s' ignored because it is included in previous hot spot",
									expression));
			return;
		}
		int thisIndex = ++index;
		
		ACTIVABLE kind = ACTIVABLE.ENABLED;
		String expressionContent =  String.format("("+ PREFIX + "%s.is(%s))",
				thisIndex, kind.getClass().getCanonicalName()+"."+kind.name());
		
		//create IfChoice with right condition
		CtIf ifChoice = getFactory().Core().createIf();
		CtCodeSnippetExpression expIf = getFactory().Code().createCodeSnippetExpression(expressionContent);
		ifChoice.setCondition(expIf);
			
		
		
		//create block from a clone of expression
		CtStatement exp2 = getFactory().Core().clone(expression);
		CtBlock thenBlock = getFactory().Code().createCtBlock(exp2);
		
		//set if and replace the expression with the new if
		ifChoice.setThenStatement(thenBlock);
		expression.replace(ifChoice);
		
		//to be sure
		ifChoice.getParent().updateAllParentsBelow();
		
		//if there are return or throws, set else with value of return.
		Filter<CtCFlowBreak> filter = new ReturnOrThrowFilter();
		if(!thenBlock.getElements(filter).isEmpty()){
			
			//search the first parent method
			CtElement parent = thenBlock.getParent();
			while(parent != null && !(parent instanceof CtMethod)){
				parent = parent.getParent();
			}
			//we go out of while with null of a CtMethod. If null, return without method in parents...?
			if(parent == null){
				return;
			}
			CtMethod parentMethod = (CtMethod) parent;

			
			CtLiteral returnedExpression = null;
			Class classOfReturn = parentMethod.getType().getActualClass();
			
			if(returnedExpressions.containsKey(classOfReturn)){
				CtLiteral templateExpression = returnedExpressions.get(classOfReturn);
				returnedExpression = getFactory().Core().clone(templateExpression);
			}else{
				returnedExpression = new CtLiteralImpl().setValue(null);
			}
			
			
			CtReturn theReturn = getFactory().Core().createReturn();
			theReturn.setReturnedExpression(returnedExpression);
			ifChoice.setElseStatement(theReturn);
		}
		
		Selector.generateSelector(expression, ACTIVABLE.ENABLED, thisIndex, ActivableSet, PREFIX);
		
		hotSpots.add(expression);

	}
	
	private boolean alreadyInHotsSpot(CtElement element) {
		CtElement parent = element.getParent();
		while (!isTopLevel(parent) && parent != null) {
			if (hotSpots.contains(parent))
				return true;

			parent = parent.getParent();
		}

		return false;
	}

	private boolean isTopLevel(CtElement parent) {
		return parent instanceof CtClass && ((CtClass) parent).isTopLevel();
	}
}