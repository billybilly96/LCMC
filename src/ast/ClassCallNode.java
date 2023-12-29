package ast;

import java.util.ArrayList;

import lib.FOOLlib;

public class ClassCallNode implements Node {
	
	private String classID;
	private String classMethod;
	private int nestingLevel;
	private STentry entry;
	private STentry methodEntry;
	private ArrayList<Node> parlist = new ArrayList<Node>();

	public ClassCallNode(String id1, String id2, STentry e, STentry m, ArrayList<Node> p, int n) {
		classID = id1;
		classMethod = id2;
		entry = e;
		methodEntry = m;
		parlist = p;
		nestingLevel = n;
	}

	public String toPrint(String s) {
		String parlstr = "";
  		for (Node par : parlist) {
  			parlstr += par.toPrint(s + "  ");
  		}
		return s + "ClassCall:" + classID + "." + classMethod + " at nestinglevel " + nestingLevel + "\n"
				+ entry.toPrint(s + "  ") + methodEntry.toPrint(s + "  ") + parlstr;
	}

	public Node typeCheck() {
		ArrowTypeNode arrowType = null;
		// se l'entry è un ArrowTypeNode setto il suo valore a t
		if (methodEntry.getType() instanceof ArrowTypeNode) {
			arrowType = (ArrowTypeNode) methodEntry.getType();
		} else {
			System.out.println("Invocation of a non-method " + classMethod);
			System.exit(0);
		}
		ArrayList<Node> params = arrowType.getParList();
		// check sul numero di parametri
		if (params.size() != parlist.size()) {
			System.out.println("Wrong number of parameters in the invocation of " + classMethod);
			System.exit(0);
		}
		// check sul tipo dei parametri
		for (int i = 0; i < parlist.size(); i++) {
			if (!(FOOLlib.isSubtype((parlist.get(i)).typeCheck(), params.get(i)))) {
				System.out.println("Wrong type for " + (i + 1) + "-th parameter in the invocation of " + classMethod);
				System.exit(0);
			}
		}
		return arrowType.getRet();
	}

	public String codeGeneration() {
		String parCode = "";
		for (int i = parlist.size() - 1; i >= 0; i--) {
			parCode += parlist.get(i).codeGeneration();
		}
		String getAR = "";
		for (int i = 0; i < nestingLevel - entry.getNestinglevel(); i++) {
			getAR += "lw\n";
		}			
		return // control link
				"lfp\n"				
				// allocazione valori parametri
				+ parCode 				
				// setto l'Access Link nell'AR del metodo ID2 invocato
				+ "push " + entry.getOffset() + "\n" // recupero valore dell'ID1 (object pointer) dall'AR dove è dichiarato
				+ "lfp\n" + getAR // con risalita della catena statica
				+ "add\n" 
				+ "lw\n"
				// jump al metodo
				+ "push " + entry.getOffset() + "\n" 
				+ "lfp\n" + getAR  // risalgo la catena statica per ottenere l'indirizzo dell'AR in cui e' dichiarato l'oggetto
				+ "add\n" 
				+ "lw\n"// ho l'object pointer sullo stack						
				+ "lw\n" // salto al dispatch pointer (indirizzo primo metodo in Dispatch Table)
				+ "push " + methodEntry.getOffset() + "\n" 
				+ "add\n" // recupero l'indirizzo del metodo a cui saltare
				+ "lw\n" // carico l'indirizzo del metodo a cui saltare
				+ "js\n"; //effettua il salto
	}

}
