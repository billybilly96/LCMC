package ast;

import java.util.ArrayList;
import java.util.List;

import lib.FOOLlib;

public class ClassNode implements Node, DecNode {
	
	private ArrayList<FieldNode> fields = new ArrayList<>();
	private ArrayList<MethodNode> methods = new ArrayList<>();
	private Node symType;
	private String id;
	private STentry superEntry;
	private ArrayList<String> dispatchTable;
	
	public ClassNode(String i) {
		id = i;
	}
	
	public void setSuperEntry(STentry s) {
		superEntry = s;
	}

	public STentry getSuperEntry() {
		return superEntry;
	}
	
	public void addField(FieldNode field) {
		fields.add(field);
	}
	
	public void addMethod(MethodNode method) {
		methods.add(method);
	}
	
	public void setSymType(Node type) {
		symType = type;
	}

	public Node getSymType() {
		return symType;
	}

	public String toPrint(String s) {
		String fieldList = "";
		for (Node field : fields) {
			fieldList += field.toPrint(s + "  ");
		}
		String methodList = "";
		for (Node method : methods) {
			methodList += method.toPrint(s + "  ");
		}
		return s + "Class:" + id + "\n" + fieldList + methodList;
	}

	/* 
	 * OTTIMIZZAZIONE: effettua il controllo di correttezza (subtyping) solo per
	 * 				   i campi e i metodi su cui è stato fatto overriding
	 */
	public Node typeCheck() {
		// si richiama sui figli che sono metodi
		methods.forEach(method -> method.typeCheck());
		// in caso di ereditarietà controlla che l'overriding sia corretto
		if (superEntry != null) {
			// legge il ClassType in superEntry
			ClassTypeNode superType = (ClassTypeNode)superEntry.getType();
			List<Node> superClassFields = superType.getFields();
			List<Node> superClassMethods = superType.getMethods();	
			
			// CONTROLLO SUI CAMPI
			fields.forEach(field -> {
				// calcola la posizione che corrisponde al suo offset nell'array allFields
				int pos = (-field.getOffset() - 1);
				// controllo solamente se c'è override (controllando che la posizione sia < della lunghezza di allFields)
				if (pos < superClassFields.size()) {
					FieldNode fieldParent = (FieldNode)superClassFields.get(pos);
					// controllo che il tipo del figlio sia sottotipo del tipo in allFields in tale posizione
					if (!FOOLlib.isSubtype(field, fieldParent)) {
						System.out.println("Incompatible value in overriding field");
						System.exit(0);
					}
				}
			});
			
			// CONTROLLO SUI METODI
			methods.forEach(method -> {
				// calcola la posizione che corrisponde al suo offset nell'array allMethods
				int pos = method.getOffset();
				// controllo solamente se c'è override (controllando che la posizione sia < della lunghezza di allMethods)
				if (pos < superClassMethods.size()) {
					MethodNode methodParent = (MethodNode)superClassMethods.get(pos);
					// controllo che il tipo del figlio sia sottotipo del tipo in allMethods in tale posizione
					if (!FOOLlib.isSubtype(method, methodParent)) {
						System.out.println("Incompatible value in overriding method");
						System.exit(0);
					}
				}
			});
		}
		return null;
	}

	// Ritorna codice che alloca su heap la Dispatch Table della classe e lascia il dispatch pointer sullo stack
	public String codeGeneration() {
		// se non si eredita creo una Dispatch Table vuota
		if (superEntry == null) {
			dispatchTable = new ArrayList<>();
		} else {
			// altrimenti la creo copiando la Dispatch Table della classe da cui si eredita (copia di tutto il contenuto)
			dispatchTable = new ArrayList<>(FOOLlib.getDispatchTables().get(-superEntry.getOffset() - 2));
		}
		// considero in ordine di apparizione i miei figli metodi
		methods.forEach(method -> {
			// invoco la sua codeGeneration
			method.codeGeneration();
			// leggo la sua etichetta
			String label = method.getLabel();
			// leggo il suo offset
			int offset = method.getOffset();
			// aggiorno la Dispatch Table considerando eventuali ovverride
			if (offset < dispatchTable.size()) {
				// rimpiazzo in caso di override dei metodi
				dispatchTable.set(offset, label); 
			} else {
				dispatchTable.add(offset, label);
			}
		});
		// aggiungo una nuova Dispatch Table a dispatchTables
		FOOLlib.addDispatchTable(dispatchTable);
		// metto il valore di $hp sullo stack (sarà il dispatch pointer da ritornare alla fine)
		String code = "lhp\n";
		// scorro la Dispatch Table
		for (String label : dispatchTable) {
			// memorizzo l'etichetta a indirizzo in $hp
			code += "push " + label + "\n"
			+ "lhp\n"
			+ "sw\n"
			// incremento valore di $hp
			+ "push 1 \n"
			+ "lhp\n"
			+ "add\n"
			+ "shp\n"; // queste ultime 4 istruzioni servono ad incrementare il valore di hp
		}
		return code;
	}

}
