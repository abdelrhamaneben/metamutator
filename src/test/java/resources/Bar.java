package resources;

import metamutator.Selector;

public class Bar {
	
	private int number;
	private String sentence;
	private boolean bool;

	
	public Bar(){
		
	}
	
	public Bar(int n, String s, boolean b){
		this.number =n;
		this.sentence =s;
		this.bool = b;
	}
	
	public boolean addition(int b){
		int other = 10;
		other = other + b;
		if(other > 20){
			sentence = "other sup";
		}
		else{
			sentence = "other sub";
		}
		
		return b == number;
	}
	
	public int returnMax10(int a) {
		if(a < 10){
			return a;
		}
		else{
			return 10;
		}
	}
	
	public void ThrowException() throws Exception{
		throw new Exception();
	}
	
}
