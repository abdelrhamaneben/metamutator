package metamutator;

import java.util.EnumSet;

import spoon.processing.AbstractProcessor;

import spoon.reflect.code.CtCodeSnippetStatement;
import spoon.reflect.code.CtDo;

/**
 * inserts a mutation hotspot for DO statement
 */
public class DoExpressionMetaMutator 
				extends AbstractProcessor<CtDo> {

	public static final String PREFIX = "_doExpressionMetaMutator";
	public enum NbRound {
		Rounds3,
		Rounds100
		
	};
	private static final EnumSet<NbRound> roundsSet = EnumSet
			.of(NbRound.Rounds3, NbRound.Rounds100);
	
	public static int thisIndex = 0;
	
	/**
	 * Stop Do loop on 3 or 100 Rounds
	 */
	@Override
	public void process(CtDo candidate) {
		thisIndex++;
		String constanteName = "_doExpressionMetaMutator"+thisIndex+"_Constante";
		String expression = "int "+ constanteName +" = 1";
		CtCodeSnippetStatement DeclareRoundStatement = getFactory().Core()
				.createCodeSnippetStatement();
		DeclareRoundStatement.setValue(expression);
		
		String expression2 = "if((" + PREFIX + thisIndex + ".is(\""+NbRound.Rounds3.toString() + "\")) && "+ constanteName +" == 3) "
							+ "{break;}"
							+ "else if("+ constanteName +" == 100){break;}"
							+ " "+ constanteName +"++";
		CtCodeSnippetStatement ifRoundStatement = getFactory().Core()
				.createCodeSnippetStatement();
		ifRoundStatement.setValue(expression2);
		
		candidate.insertBefore(DeclareRoundStatement);
		candidate.getBody().insertAfter(ifRoundStatement);
		Selector.generateSelector(candidate, NbRound.Rounds3.toString(), thisIndex, roundsSet, PREFIX);
	}
}
