package ast;

public class IntTypeNode implements Node {
  
	public String toPrint(String s) {
		return s + "IntType\n";  
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