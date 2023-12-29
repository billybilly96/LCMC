package ast;

import lib.*;

public class MinusNode implements Node {

	private Node left;
	private Node right;

	public MinusNode(Node l, Node r) {
		left = l;
		right = r;
	}

	public String toPrint(String s) {
		return s + "Minus\n" + left.toPrint(s + "  ") + right.toPrint(s + "  "); 
	}

	public Node typeCheck() {
		// posso avere in sottrazione anche due booleani
		if (!(FOOLlib.isSubtype(left.typeCheck(), new IntTypeNode()) && FOOLlib.isSubtype(right.typeCheck(), new IntTypeNode()))) {
			System.out.println("No integers/booleans in minus");
			System.exit(0);	
		}
		return new IntTypeNode(); 
	}

	public String codeGeneration() {
		return left.codeGeneration() + right.codeGeneration() + "sub\n";
	}

}  