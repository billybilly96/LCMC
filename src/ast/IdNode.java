package ast;

public class IdNode implements Node {

	private String id;
	private STentry entry;
	private int nestingLevel;

	public IdNode(String i, STentry e, int n) {
		id = i;
		entry = e;
		nestingLevel = n;
	}

	public String toPrint(String s) {
		return s + "Id:" + id + " at nesting level " + nestingLevel + "\n" + entry.toPrint(s + "  ");
	}

	public Node typeCheck() {
		Node typeId = entry.getType();
		// ID può essere di tipo funzionale ma non deve essere un metodo, nè deve essere il nome di una classe
		if ((entry.getType() instanceof ClassTypeNode) || (entry.isMethod())) {
    		System.out.println("Wrong usage of identifier " + id);
    		System.exit(0);
    	}
		return typeId;
	}

	public String codeGeneration() {
		String getAR = "";
		for (int i = 0; i < nestingLevel - entry.getNestinglevel(); i++) {
			getAR += "lw\n";
		}
		// se è di tipo funzionale devo trovare 2 valori: AR in cui è dichiarata la variabile e l'indirizzo della funzione
		if (this.entry.getType() instanceof ArrowTypeNode) {
			return "push " +  
					entry.getOffset() + "\n" +			 
					"lfp\n" + getAR + // risalgo la catena statica per ottenere l'indirizzo dell'AR in cui e' dichiarata la variabile		
					"add\n" +
					"lw\n" +					
					"push " + 
					(entry.getOffset() - 1) + "\n" +	
					"lfp\n" + getAR + //risalgo la catena statica per ottenere l'indirizzo dell'AR in cui e' dichiarato l'indirizzo della funzione	
					"add\n" +
					"lw\n";
		} else {
			return "push " + 
				   entry.getOffset() + "\n" + 
				   "lfp\n" + getAR + //risalgo la catena statica per ottenere l'indirizzo dell'AR in cui e' dichiarata la variabile		 
				   "add\n" +
				   "lw\n"; 
		}	
			
	}

}  