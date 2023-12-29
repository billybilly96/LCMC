package ast;

import lib.FOOLlib;

public class EqualNode implements Node {

  private Node left;
  private Node right;
  
  public EqualNode(Node l, Node r) {
	  left = l;
	  right = r;
  }
  
  public String toPrint(String s) {
	  return s + "Equal\n" + left.toPrint(s + "  ") + right.toPrint(s + "  "); 
  }
    
  public Node typeCheck() {
	  Node l = left.typeCheck();  
	  Node r = right.typeCheck();
	  // controllo che non siano di tipo funzionale
	  if ((l instanceof ArrowTypeNode) || (r instanceof ArrowTypeNode)) {
	      System.out.println("Incompatible types in equal");
		  System.exit(0);	
	  }  
	  if (!(FOOLlib.isSubtype(l, r) || FOOLlib.isSubtype(r, l))) {
	      System.out.println("Incompatible types in equal");
		  System.exit(0);	
	  }  
	  return new BoolTypeNode();
  }
  
  public String codeGeneration() {
	  String l1 = FOOLlib.freshLabel();
	  String l2 = FOOLlib.freshLabel();
	  return left.codeGeneration() +
			 right.codeGeneration()
			 + "beq " + l1 + "\n"  			// se "a" e "b" sono uguali salto a l1
			 + "push 0\n" 	  				// altrimenti pusho 0 (false)
			 + "b " + l2 + "\n"    			// e salto a l2
			 + l1 + ": \n" 					// l1 pusha 1 (true)
			 + "push 1\n" 
			 + l2 + ": \n";					// l2 termina			 
  	}
  
}  