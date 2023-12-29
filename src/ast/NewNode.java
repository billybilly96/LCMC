package ast;

import java.util.ArrayList;
import java.util.List;

import lib.FOOLlib;

public class NewNode implements Node {
	
	private String id;
	private STentry entry;
	private ArrayList<Node> parlist = new ArrayList<>();

	public NewNode(String i, STentry e, ArrayList<Node> p) {
		id = i;
		entry = e;
		parlist = p;		
	}

	public String toPrint(String s) {
		String parlstr = ""; 
		for (Node p : parlist) {
			parlstr += p.toPrint(s + "  ");
		}
		return s + "NewNode: " + id + "\n" + parlstr;
	}

	public Node typeCheck() {
		if (entry.getType() instanceof ClassTypeNode) {
			// recupera i tipi dei parametri tramite allFields del ClassTypeNode in campo entry
			List<Node> fields = ((ClassTypeNode)entry.getType()).getFields();
			// check sul numero dei parametri
			if (fields.size() != parlist.size()) {
				System.out.println("Wrong number of parameters in instancing of " + id);
				System.exit(0);
			}
			// check sul tipo dei parametri
			for (int i = 0; i < parlist.size(); i++) {
				if (!(FOOLlib.isSubtype((this.parlist.get(i)).typeCheck(), ((FieldNode)fields.get(i)).getSymType()))) {
					System.out.println("Wrong type for " + (i + 1) + "-th parameter in the instancing of " + id);
					System.exit(0);
				}
			}
		} else {
			System.out.println("Error in invocation of a non class " + id);
			System.exit(0);
		}
		// torna un RefTypeNode
		return new RefTypeNode(id);
	}

	public String codeGeneration() {
		String code = "";
		int classOffset = FOOLlib.MEMSIZE + entry.getOffset();
		// si richiama su tutti i parametri in ordine di apparizione (ognuno metto il suo valore sullo stack)
		for (int i = 0; i < parlist.size(); i++) {
			code += parlist.get(i).codeGeneration();
		}	
		// prendo tutti i valori dei parametri, uno alla volta, e li metto nello heap, incrementando hp dopo ogni singola copia
		for (int i = parlist.size() - 1; i >= 0; i--) {
			code += "lhp\n" + // carico hp sullo stack
					"sw\n" +  // rimuovo hp e il valore del parametro dallo stack e inserisco il valore del parametro nella posizione indicata da hp
					"push 1\n" +
					"lhp\n" +
					"add\n" +
					"shp\n"; // queste ultime 4 istruzioni servono ad incrementare il valore di hp
		}		
		return code += "push " + classOffset + "\n" +
					   "lw\n" + // carico sullo stack il dispatch pointer
					   "lhp\n" +
					   "sw\n" + // scrivo il dispatch pointer all'indirizzo puntato da hp
					   "lhp\n" + // carico sullo stack il valore del registro hp (scrivo object pointer)
					   "push 1\n" +
					   "lhp\n" +
					   "add\n" +
					   "shp\n"; // queste ultime 4 istruzioni servono ad incrementare il valore di hp				
	}
	
}
