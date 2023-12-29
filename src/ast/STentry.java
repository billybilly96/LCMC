package ast;

public class STentry {

	private int nestingLevel;
	private Node type;
	private int offset;
	private boolean method;

	public STentry(int n, int o) {
		nestingLevel = n;
		offset = o; 
	} 

	public STentry(int n, Node t, int o) {
		nestingLevel = n;
		type = t;
		offset = o;
	}

	public STentry(int n, int o, boolean m) {
		nestingLevel = n;
		offset = o;
		method = m;
	}

	public void addType(Node t) {
		type = t; 
	}

	public Node getType() {
		return type;
	}

	public int getOffset() {
		return offset;
	}

	public int getNestinglevel() {
		return nestingLevel;
	}
	
	public boolean isMethod() {
		return method;
	}

	public String toPrint(String s) {
		return s + "STentry: nestlev " + nestingLevel + "\n" +
			   s + "STentry: type\n " + type.toPrint(s + "  ") +
			   s + "STentry: offset " + offset + "\n" + 
			   s  + "STentry: isMethod " + method + "\n";    
	} 

}  