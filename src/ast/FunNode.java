package ast;

import java.util.ArrayList;

import lib.FOOLlib;

public class FunNode implements Node, DecNode {

	private String id;
	private Node type; 
	private ArrayList<Node> parlist = new ArrayList<Node>(); 
	private ArrayList<Node> declist = new ArrayList<Node>(); 
	private Node exp;
	private Node symType; 

	public FunNode(String i, Node t) {
		id = i;
		type = t;
	}

	public void addDec(ArrayList<Node> d) {
		declist = d;
	}  

	public void addBody(Node e) {
		exp = e;
	}  

	public void addPar(Node par) { 
		parlist.add(par);  
	} 
	
	public void setSymType(Node type) {
		symType = type;
	}

	public Node getSymType() {
		return symType;
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
		return s + "Fun:" + id + "\n" + type.toPrint(s + "  ") + parlstr + declstr + exp.toPrint(s + "  "); 
	}

	public Node typeCheck() {
		declist.forEach(dec -> dec.typeCheck());
		if (!FOOLlib.isSubtype(exp.typeCheck(), type)) {
			System.out.println("Incompatible value for variable in function");
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
		String funl = FOOLlib.freshFunLabel();
		FOOLlib.putCode(
				funl + ":\n" + 
				"cfp\n" + 								// setta $fp a $sp
		 		"lra\n" + 								// restituisce il return address
				declCode + 								// inserisce le dichiarazioni locali
				exp.codeGeneration() + "srv\n" + 	// pop del return value
				popDecl + 								// pop delle dichiarazioni
				"sra\n" + 								// pop del return address
				"pop\n" + 								// pop di AL
				popParCode + 							// pop dei parametri
				"sfp\n" + 								// setto $fp al valore del control link
				"lrv\n" + 								// risultato della funzione sullo stack
				"lra\n" + 
				"js\n" 									// salta a $ra
				);
		return "lfp\n" + "push " + funl + "\n";
	}

}  