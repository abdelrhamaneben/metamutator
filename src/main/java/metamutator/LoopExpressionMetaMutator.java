package metamutator;

import java.util.EnumSet;

import spoon.processing.AbstractProcessor;

import spoon.reflect.code.CtCodeSnippetStatement;
import spoon.reflect.code.CtDo;
import spoon.reflect.code.CtLoop;

/**
 * inserts a mutation hotspot for DO statement
 */
public class LoopExpressionMetaMutator 
				extends AbstractProcessor<CtLoop> {

	public static final String PREFIX = "_doExpressionMetaMutator";
	public enum NbRound {
		NoRound,
		Rounds3,
		Rounds100
		
	};
	private static final EnumSet<NbRound> roundsSet = EnumSet
			.of(NbRound.NoRound,NbRound.Rounds3, NbRound.Rounds100);
	
	public static int thisIndex = 0;
	
	/**
	 * Stop Do loop on 3 or 100 Rounds
	 */
	@Override
	public void process(CtLoop candidate) {
		thisIndex++;
		String constanteName = "_doExpressionMetaMutator"+thisIndex+"_Constante";
		String expression = "int "+ constanteName +" = 1";
		CtCodeSnippetStatement DeclareRoundStatement = getFactory().Core()
				.createCodeSnippetStatement();
		DeclareRoundStatement.setValue(expression);
		
		String expression2 = "if((" + PREFIX + thisIndex + ".is(\""+NbRound.Rounds3.toString() + "\")) && "+ constanteName +" == 3) "
							+ "{break;}"
							+ "if((" + PREFIX + thisIndex + ".is(\""+NbRound.NoRound.toString() + "\"))) { break;}"
							+ "else if("+ constanteName +" == 100){break;}"
							+ " "+ constanteName +"++";
		CtCodeSnippetStatement ifRoundStatement = getFactory().Core()
				.createCodeSnippetStatement();
		ifRoundStatement.setValue(expression2);
		
		candidate.insertBefore(DeclareRoundStatement);
		candidate.getBody().insertAfter(ifRoundStatement);
		Selector.generateSelector(candidate, NbRound.NoRound.toString(), thisIndex, roundsSet, PREFIX);
	}
}
