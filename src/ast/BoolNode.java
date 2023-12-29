package ast;

public class BoolNode implements Node {
	
	private boolean val;
  
	public BoolNode(boolean v) {
		val = v; 
	}
  
	public String toPrint(String s) {
		return val ? (s + "Bool:true\n") : (s + "Bool:false\n");
	}
  
	public Node typeCheck() {
		return new BoolTypeNode(); 
	}
	  
	public String codeGeneration() {
		return "push " + (val ? 1 : 0) + "\n";
	}
      
}  