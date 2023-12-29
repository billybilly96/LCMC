package ast;

public class AndNode implements Node {
	
	private Node left;
	private Node right;
	
	public AndNode(Node l, Node r) {
		left = l;
		right = r;
	}
	
	public String toPrint(String s) {
		return s + "And\n" + left.toPrint(s + "  ") + right.toPrint(s + "  "); 
	}

	public Node typeCheck() {
		if (!(left.typeCheck() instanceof BoolTypeNode) || !(right.typeCheck() instanceof BoolTypeNode)) {
			System.out.println("No booleans in And operation");
			System.exit(0);	
		}
		return new BoolTypeNode();
	}

	public String codeGeneration() {
		// "a" AND "b" equivale a moltiplicare "a" e "b"; se il risultato è 1 allora è true, altrimenti è false
		return left.codeGeneration() + right.codeGeneration() + "mult\n"; 		
	}
}

