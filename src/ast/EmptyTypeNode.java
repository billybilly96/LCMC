package ast;

public class EmptyTypeNode implements Node {
	
	// non deve essere nell'AST
	public String toPrint(String s) {
		return s + "EmptyType\n";
	}
  
	// non utilizzato
	public Node typeCheck() {
		return null; 
	}
 
	// non utilizzato
	public String codeGeneration() {
		return "";
	}
}
