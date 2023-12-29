package ast;

import java.util.ArrayList;

import lib.FOOLlib;

public class MethodNode implements Node, DecNode {
	
	private Node symType; 
	private String id;
	private Node type; 
	private int offset;
	private ArrayList<Node> parlist = new ArrayList<Node>(); 
	private ArrayList<Node> declist = new ArrayList<Node>();
	private Node exp; // corpo del metodo
	private String label; // etichetta associata
	
	public MethodNode(String i, Node t) {
		id = i;
		type = t;
	}
	
	public void setSymType(Node type) {
		symType = type;
	}

	public Node getSymType() {
		return symType;
	}
	
	public void setOffset(int methodOffset) {
		offset = methodOffset;		
	}
	
	public void addPar(Node par) { 
		parlist.add(par);  
	}
	
	public void addDec(ArrayList<Node> d) {
		declist = d;
	} 
	
	public void addBody(Node b) {
		exp = b;
	}

	public int getOffset() {
		return offset;
	}
	
	public String getLabel() {
		return label;
	}

	public String toPrint(String s) {
		String parlstr = "";
		for (Node par : parlist) {
			parlstr += par.toPrint(s + "  ");
		}
		String declstr = "";
		for (Node dec : declist) {
			declstr += dec.toPrint(s + "  ");
		}
		return s + "Method:" + id + "\n" + s + 
				"Offset: " + offset + "\n" + 
				symType.toPrint(s + "  ") + parlstr + declstr + exp.toPrint(s + "  ");
	}

	public Node typeCheck() {
		declist.forEach(dec -> dec.typeCheck());
		if (!FOOLlib.isSubtype(exp.typeCheck(), type)) {
			System.out.println("Incompatible type for return value at method " + id);
			System.exit(0); 
		}
		return null;
	}

	public String codeGeneration() {
		String declCode = "";
		String popDecl= "";
		String popParCode = "";		
		for (Node dec : declist) {
			declCode += dec.codeGeneration();
			// se è ArrowType (tipo funzionale) devo poppare un valore in più 
			if (((DecNode) dec).getSymType() instanceof ArrowTypeNode) {
				popDecl += "pop\n";
			}
			popDecl += "pop\n";
		}		
		for (Node par : parlist) {
			// se è ArrowType (tipo funzionale) devo poppare un valore in più
			if (((ParNode) par).getSymType() instanceof ArrowTypeNode) {
				popParCode += "pop\n";
			}
			popParCode += "pop\n";
		}
		// genera un'etichetta nuova per il suo indirizzo e la mette nel suo campo label
		label = FOOLlib.freshMethodLabel();
		// genera il codice del metodo e lo inserisce il FOOLlib con putCode()
		FOOLlib.putCode(
				label + ":\n" + 
				"cfp\n" + 								// setta $fp a $sp
		 		"lra\n" + 								// restituisce il return address in cima allo stack
				declCode + 								// inserisce le dichiarazioni locali
				exp.codeGeneration() + "srv\n" + 		// pop del return value (pop e store in rv)
				popDecl + 								// pop delle dichiarazioni
				"sra\n" + 								// pop e store del return address
				"pop\n" + 								// pop di AL
				popParCode + 							// pop dei parametri
				"sfp\n" + 								// setto $fp al valore del control link
				"lrv\n" + 								// risultato della funzione sullo stack (return value)
				"lra\n" + 								// push dell'indirizzo di ritorno (return address)
				"js\n" 									// salta a $ra
		);
		// ritorna codice vuoto
		return "";
	}

}
