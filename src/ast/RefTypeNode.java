package ast;

public class RefTypeNode implements Node {
	
	private String classID;
	
	public RefTypeNode(String id) {
		classID = id;
	}
	
	public String getClassId() {
		return classID;
	}
	  
	public String toPrint(String s) {
		return s + classID + "\n";
	}
  
	// non utilizzato
	public Node typeCheck() {
		return null; 
	}
 
	// non utilizzato
	public String codeGeneration() {
		return "";
	}

}
