package ast;

import lib.FOOLlib;

public class GreaterEqualNode implements Node {
	
	private Node left;
	private Node right;
	  
	public GreaterEqualNode(Node l, Node r) {
		left = l;
		right = r; 
	}
  
	public String toPrint(String s) {
		return s + "Greater or Equal\n" + left.toPrint(s + "  ") + right.toPrint(s + "  "); 
	}
    
	public Node typeCheck() {
		Node l = left.typeCheck();  
		Node r = right.typeCheck();
		if (!(FOOLlib.isSubtype(l, r) || FOOLlib.isSubtype(r, l))) {
			System.out.println("Incompatible types in Greater or Equal operation");
			System.exit(0);	
		}  
		return new BoolTypeNode();
	}
  
	public String codeGeneration() {
		String l1= FOOLlib.freshLabel();
		String l2= FOOLlib.freshLabel();
		return right.codeGeneration() +
			   left.codeGeneration() 
			   + "bleq " + l1 + "\n" 		    // se "b" è minore o uguale ad "a" salto a l1
			   + "push 0\n" 					// altrimenti pusho 0 (false)
			   + "b " + l2 + "\n" 			    // e salto a l2
			   + l1 + ": \n" 				    // l1 pusha 1 (true)
			   + "push 1\n" 
			   + l2 + ": \n";					// l2 termina		   	   
	}

}
