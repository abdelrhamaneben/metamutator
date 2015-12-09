package metamutator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtCFlowBreak;
import spoon.reflect.code.CtCodeSnippetExpression;
import spoon.reflect.code.CtCodeSnippetStatement;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtNewClass;
import spoon.reflect.code.CtOperatorAssignment;
import spoon.reflect.code.CtReturn;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtSynchronized;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtEnum;
import spoon.reflect.visitor.Filter;
import spoon.reflect.visitor.filter.ReturnOrThrowFilter;
import spoon.support.reflect.code.CtExpressionImpl;
import spoon.support.reflect.code.CtIfImpl;
import spoon.support.reflect.code.CtLiteralImpl;
import spoon.support.reflect.code.CtStatementImpl;
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
	//invocation?
	private static final List<Class> UNMODIFIABLE_STATEMENTS = new ArrayList<Class>(
			Arrays.asList(CtAssignment.class, CtBlock.class, CtReturn.class, CtCFlowBreak.class, CtClass.class,
					CtCodeSnippetStatement.class, CtConstructorCall.class, CtEnum.class, CtInvocation.class,
					CtLocalVariable.class, CtNewClass.class, CtOperatorAssignment.class, CtReturn.class, 
					CtSynchronized.class, CtUnaryOperator.class)
			);
	
	
	@Override
	public boolean isToBeProcessed(CtStatement element) {
		for(Class c : UNMODIFIABLE_STATEMENTS){
			if(c.isInstance(element)){
				/*System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
				System.out.println(element);
				System.out.println(c);*/
				return false;
			}
		}
		System.out.println("---------------------------------------------------------------------");
		System.out.println(element);
		System.out.println("---------------------------------------------------------------------");
		return true;
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
			CtReturn theReturn = getFactory().Core().createReturn();
			CtExpression returnedExpression = null; //TO DO, COMMAND DESIGN PATTERN? 
			/*
			 *Byte.class, Short.class, Integer.class, Long.class, 
			 *Float.class, Double.class, Boolean.class, Character.class
			 */
			theReturn.setReturnedExpression(returnedExpression);
			ifChoice.setElseStatement(theReturn);
		}
		
		
		
		/*
		System.out.println(thenBlock.getElements(filter).size());
		for(CtElement ele : thenBlock.getElements(filter)){
			System.out.println("----------------- CT FLOW BREAK------------------- ");
			System.out.println(ele);
		}*/
		

		//expression.setParent(thenBlock);
		//thenBlock.addStatement(expression);
		
		//choice.setThenStatement(expression);
		
		//expression.replace(choice);
		
		System.out.println("############################## CHOICE ################################");
		System.out.println(expression.getParent());
		System.out.println("############################## CHOICE ################################");
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