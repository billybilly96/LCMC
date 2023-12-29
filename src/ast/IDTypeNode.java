package ast;

public class IDTypeNode implements Node {
	  
	public String toPrint(String s) {
		return s + "IdType\n";  
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
