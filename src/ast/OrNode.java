package ast;

import lib.FOOLlib;

public class OrNode implements Node {
	
	private Node left;
	private Node right;

	public OrNode(Node l, Node r) {
		left = l;
		right = r; 
	}
	
	public String toPrint(String s) {
		return s + "Or\n" + left.toPrint(s + "  ") + right.toPrint(s + "  "); 
	}

	public Node typeCheck() {
		if (!(left.typeCheck() instanceof BoolTypeNode) || !(right.typeCheck() instanceof BoolTypeNode)) {
			System.out.println("No booleans in Or operation");
			System.exit(0);	
		}
		return new BoolTypeNode();
	}

	public String codeGeneration() {
		String l1= FOOLlib.freshLabel();
		String l2= FOOLlib.freshLabel();
		return left.codeGeneration() + 
			   right.codeGeneration() 
			   + "add\n" + "\n" 				// sommo a e b
			   + "push 0\n"						// e pusho 0
			   + "beq " + l1 + "\n"				// se sono uguali allora è false e salto a l1 e pusho 0
			   + "push 1\n"						// altrimento pusho 1 e salto a l2
			   + "b " + l2 + "\n" 
			   + l1 + ": \n"		
			   + "push 0\n"
			   + l2 + ": \n"; 		
	}
}
