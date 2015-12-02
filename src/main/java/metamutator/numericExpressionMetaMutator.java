package metamutator;


import java.util.ArrayList;
import java.util.EnumSet;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtStatement;
import spoon.support.reflect.code.CtAssignmentImpl;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtCodeSnippetExpression;
import spoon.reflect.code.CtVariableRead;

/**
 * inserts a mutation hotspot for each binary operator
 */
public class numericExpressionMetaMutator 
				extends AbstractProcessor<CtVariableRead> {

	public static final String PREFIX = "_numericExpressionMetaMutator";
	public enum ABS {
		   ABS, NABS
		   };
	private static final EnumSet absSet = EnumSet
			.of(ABS.ABS, ABS.NABS);
	
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
		
		String expression = "";
		expression = "(" +PREFIX+thisIndex + ".is(\"" + ABS.ABS.toString() + "\") ?"
						+ "( Math.abs(" + candidate.getVariable().getSimpleName() + ")):"
						+ "(" + candidate.getVariable().getSimpleName() + "))";
		CtCodeSnippetExpression<Boolean> codeSnippet = getFactory().Core()
				.createCodeSnippetExpression();
		codeSnippet.setValue(expression);
		candidate.replace(codeSnippet);
		Selector.generateSelector(candidate, ABS.NABS.toString(), thisIndex, absSet, PREFIX);
	}
	
}
