package metamutator;


import java.util.ArrayList;
import java.util.EnumSet;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtCodeSnippetExpression;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtVariableReference;

/**
 * inserts a mutation hotspot for each Numeric Variable
 */
public class NumericExpressionMetaMutator 
				extends AbstractProcessor<CtVariableRead> {

	public static final String PREFIX = "_numericExpressionMetaMutator";
	public enum UNARY {
		// NO CHANGE
		INIT,
		// Absolute Value	
		ABS,
		// Unary minus
		MINUS,
		// Increment
		INC,
		// Decrement
		DEC
	};
	private static final EnumSet<UNARY> absSet = EnumSet
			.of(UNARY.ABS, UNARY.MINUS, UNARY.INC, UNARY.DEC);
	
	public static int thisIndex = 0;
	/**
	 * Accept Numeric Variable
	 */
	@Override
	public boolean isToBeProcessed(CtVariableRead candidate) {
		System.out.println(candidate.getSignature());
		// SKIP not declared variable and Finale variable
		if(candidate.getVariable() == null) return false;
		System.out.println("Not null variable");
		if(candidate.getVariable().getModifiers().contains(ModifierKind.FINAL)) return false;
		System.out.println("Not FINAL");
		candidate.getVariable().getType();
		if(this.isNumber(candidate.getVariable())){
			System.out.println("spooned");
			return true;
		}
		return false;
	}
	/**
	 * 
	 * @param ctVariableReference
	 * @return
	 */
	private boolean isNumber(CtVariableReference ctVariableReference) {
		return ctVariableReference.getType().getSimpleName().equals("int")
			|| ctVariableReference.getType().getSimpleName().equals("long")
			|| ctVariableReference.getType().getSimpleName().equals("byte")
			|| ctVariableReference.getType().getSimpleName().equals("char")
		|| ctVariableReference.getType().getSimpleName().equals("float")
		|| ctVariableReference.getType().getSimpleName().equals("double")
		|| Number.class.isAssignableFrom(ctVariableReference.getType().getActualClass());
	}
	
	/**
	 * Add AbsoluteValue, Plus, Minus, Increment or Decrement Unary Operator on Numeric Variable 
	 */
	@Override
	public void process(CtVariableRead candidate) {
		thisIndex++;
		
		String expression = "(";
		for(UNARY unary : absSet){
			if(unary.equals(UNARY.INIT)) continue;
			expression += PREFIX+thisIndex + ".is(\"" + unary.toString() + "\")?( " + UnaryEquivalent(unary)  + candidate.getVariable().getSimpleName() + ")):";
		}
		expression += "(" + candidate.getVariable().getSimpleName() + "))";
		CtCodeSnippetExpression<Boolean> codeSnippet = getFactory().Core()
				.createCodeSnippetExpression();
		codeSnippet.setValue(expression);
		candidate.replace(codeSnippet);
		Selector.generateSelector(candidate, UNARY.INIT.toString(), thisIndex, absSet, PREFIX);
	}
	
	private String UnaryEquivalent(UNARY value) {
		switch(value) {
		case ABS : return "Math.abs(";
		case MINUS : return "-(";
		case INC : return "(++";
		case DEC : return "(--";
		default : return "(";
		}
	}
}
