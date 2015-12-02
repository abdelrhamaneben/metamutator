package metamutator;


import java.util.ArrayList;
import java.util.EnumSet;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtCodeSnippetExpression;
import spoon.reflect.code.CtVariableRead;

/**
 * inserts a mutation hotspot for each binary operator
 */
public class numericExpressionMetaMutator 
				extends AbstractProcessor<CtVariableRead> {

	public static final String PREFIX = "_numericExpressionMetaMutator";
	public enum UNARY {
		// NO CHANGE
		INIT,
		// Absolute Value	
		ABS,
		// Unary plus
		PLUS,
		// Unary minus
		MINUS,
		// Increment
		INC,
		// Decrement
		DEC
	};
	private static final EnumSet<UNARY> absSet = EnumSet
			.of(UNARY.ABS, UNARY.PLUS, UNARY.MINUS, UNARY.INC, UNARY.DEC);
	
	public static int thisIndex = 0;
	/**
	 * Ne recupere que les valeurs numériques
	 */
	@Override
	public boolean isToBeProcessed(CtVariableRead candidate) {
		ArrayList<String> valideType = new ArrayList<String>();
		valideType.add("java.lang.Integer");
		valideType.add("java.lang.Double");
		valideType.add("java.lang.Float");
		valideType.add("java.lang.Long");
		
		if(valideType.contains(candidate.getVariable().getType().toString())){
			return true;
		}
		return false;
	}
	
	/**
	 * Ajoute la valeur absolue
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
		case PLUS : return "+(";
		case MINUS : return "-(";
		case INC : return "(++";
		case DEC : return "(--";
		default : return "(";
		}
	}
}
