package ast;

import java.util.ArrayList;

import lib.FOOLlib;

public class CallNode implements Node {

	private String id;
	private STentry entry;
	private ArrayList<Node> parlist = new ArrayList<Node>();
	private int nestingLevel;

	public CallNode(String i, STentry e, ArrayList<Node> p, int n) {
		id = i;
		entry = e;
		parlist = p;
		nestingLevel = n;
	}

	public String toPrint(String s) {
		String parlstr = "";
		for (Node par : parlist) { 
			parlstr += par.toPrint(s + "  ");
		}
		return s + "Call:" + id + " at nesting level " + nestingLevel + "\n" + entry.toPrint(s + "  ") + parlstr;
	}

	public Node typeCheck() { 
		ArrowTypeNode arrowType = null;
		// se l'entry è un ArrowTypeNode setto il suo valore ad arrowType
		if (entry.getType() instanceof ArrowTypeNode) {
			arrowType = (ArrowTypeNode) entry.getType();
		} else {
			System.out.println("Invocation of a non-function " + id);
			System.exit(0);
		}
		ArrayList<Node> params = arrowType.getParList();
		// check sul numero di parametri
		if (params.size() != parlist.size()) {
			System.out.println("Wrong number of parameters in the invocation of " + id);
			System.exit(0);
		}
		// check sul tipo dei parametri
		for (int i = 0; i < parlist.size(); i++) {
			if (!(FOOLlib.isSubtype((parlist.get(i)).typeCheck(), params.get(i)))) {
				System.out.println("Wrong type for " + (i + 1) + "-th parameter in the invocation of " + id);
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
		// controllo se ID è un metodo
		if (entry.isMethod()) {
			// CODICE ESTENSIONE OBJECT-ORIENTED
			return  // control link
	    			"lfp\n" 					  
					// allocazione parametri
	    			+ parCode  	    			 
	    			// access link	 
	    			+ "lfp\n" 
	    			+ getAR			    			
	    			// jump al metodo (aggiungo 1 alla differenza di nesting level, raggiungendo così la Dispatch Table)
	    			+ "push " + entry.getOffset() + "\n"			 
	    			+ "lfp\n" 
	    			+ getAR
	    			+ "lw\n"
	    			+ "add\n"
	    			+ "lw\n"
	    			+ "js\n";			
		} else {
			// CODICE ESTENSIONE HIGHER-OLDER
			return  // control link
	    			"lfp\n" 
					// allocazione parametri
	    			+ parCode    			 
	    			// access link
	    			+ "push " 
	    			+ entry.getOffset() + "\n"		 
	    			+ "lfp\n" 
	    			+ getAR			 
	    			+ "add\n"
	    			+ "lw \n"      			
	    			// jump alla funzione 
	    			// nell'higher order le funzioni occupano 2 spazi e l'indirizzo della funzione è nel secondo
	    			+ "push " 
	    			+ (entry.getOffset() - 1) + "\n"
	    			+ "lfp\n" + getAR // risalgo la catena statica per ottenre l'indirizzo dell'AR in cui è dichiarata la funzxione (access link)
	    			+ "add\n"
	    			+ "lw\n"  // carica sullo stack l'indirizzo della funzione
	    	        + "js\n"; //effettua il salto
		}
	}  
}  