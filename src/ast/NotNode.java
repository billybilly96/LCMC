package ast;

public class NotNode implements Node {

	private Node exp;

	public NotNode(Node e) {
		exp = e;
	}

	public String toPrint(String s) {
		return s + "Not\n" + exp.toPrint(s + "  ");
	}

	public Node typeCheck() {
		if (!(exp.typeCheck() instanceof BoolTypeNode)) {
			System.out.println("No Boolean in Not operation");
			System.exit(0);	
		}
		return new BoolTypeNode(); 
	}

	public String codeGeneration() {
		return "push 1\n"					// pusho 1
				+ exp.codeGeneration()		   
				+ "sub\n";					// e faccio la sottrazione (in questo modo faccio il Not del valore)
	}

}
