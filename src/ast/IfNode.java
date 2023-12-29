package ast;

import lib.FOOLlib;

public class IfNode implements Node {

	private Node cond;
	private Node th;
	private Node el;

	public IfNode(Node c, Node t, Node e) {
		cond = c;
		th = t;
		el = e;
	}

	public String toPrint(String s) {
		return s + "If\n" + cond.toPrint(s + "  ") 
				 + th.toPrint(s + "  ")   
				 + el.toPrint(s + "  "); 
	}

	/*
	 * OTTIMIZZAZIONE: type checking con Lowest Common Ancestor
	 */
	public Node typeCheck() {		
		if (!(cond.typeCheck() instanceof BoolTypeNode)) {
			System.out.println("Non boolean condition in If");
			System.exit(0);		
		}
		Node t = th.typeCheck();  
		Node e = el.typeCheck();  
		if (FOOLlib.isSubtype(t, e)) {
			return e;
		}
		if (FOOLlib.isSubtype(e, t)) {
			return t;
		}
		// Siccome le due espressioni non sono una sottotipo dell'altro, controllo se hanno un Lowest Common Ancestor
		Node lowestCommonAncestor = FOOLlib.lowestCommonAncestor(t, e);
		// se ritorna null il typechecking fallisce
		if (lowestCommonAncestor == null) {
			System.out.println("Incompatible types in then-else branches");
			System.exit(0);
		} else {
			// il type checking ha avuto successo, restituisco il tipo ritornato
			return lowestCommonAncestor;
		}
		// type checking fallito
		return null; 
	}

	public String codeGeneration() {
		String l1 = FOOLlib.freshLabel();
		String l2 = FOOLlib.freshLabel();
		return cond.codeGeneration()
			   + "push 1\n"		     
			   + "beq "+ l1 + "\n"
			   + el.codeGeneration()
			   + "b " + l2 + "\n"
			   + l1 + ": \n"
			   + th.codeGeneration()
			   + l2 + ": \n";
	}

}  