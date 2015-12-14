package metamutator;

import spoon.Launcher;

public class Main {
	
	public static void main(String[] args) throws Exception {
		
		System.out.println("------AssertionsTest-------");
		MutantSearchSpaceExplorator.runMetaProgramWith(Class.forName(args[0]));
		//xSystem.out.println("------CSVFormatTest-------");
		//MutantSearchSpaceExplorator.runMetaProgramWith(CSVFormatTest.class);
		//System.out.println("------CSVParserTest-------");
		//MutantSearchSpaceExplorator.runMetaProgramWith(CSVParserTest.class);
		//System.out.println("------CSVFileParserTest-------");
		//MutantSearchSpaceExplorator.runMetaProgramWith(CSVFileParserTest.class);
		//System.out.println("------CSVFormatPredefinedTest-------");
		//MutantSearchSpaceExplorator.runMetaProgramWith(CSVFormatPredefinedTest.class);
		//System.out.println("------CSVRecordTest-------");
		//MutantSearchSpaceExplorator.runMetaProgramWith(CSVRecordTest.class);
		//System.out.println("------CSVPrinterTest-------");
		//MutantSearchSpaceExplorator.runMetaProgramWith(CSVPrinterTest.class);
		//System.out.println("------ExtendedBufferedReaderTest-------");
		//MutantSearchSpaceExplorator.runMetaProgramWith(ExtendedBufferedReaderTest.class);
		//System.out.println("------LexerTest-------");
		//MutantSearchSpaceExplorator.runMetaProgramWith(LexerTest.class);
		//System.out.println("------FercGovTest-------");
		//MutantSearchSpaceExplorator.runMetaProgramWith(FercGovTest.class);
		
		
		Launcher l = new Launcher();
        //l.addInputResource("/local/dufaux/workspaceJEE/commons-csv/src/main");
        l.addProcessor(new StatementDeletionMetaMutator());
        //l.addProcessor(new LoopExpressionMetaMutator());
    	l.run(new String[]{"-i","/local/dufaux/workspaceJEE/commons-csv/src/main/java","--source-classpath","/local/dufaux/workspaceJEE/commons-csv/target/classes"});
		
    	System.out.println("Project spooned");
	}
}
