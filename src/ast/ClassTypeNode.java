package ast;

import java.util.ArrayList;
import java.util.List;

public class ClassTypeNode implements Node {
	
	private ArrayList<Node> allFields = new ArrayList<>();	// tipi dei campi (inclusi quelli ereditati)
	private ArrayList<Node> allMethods = new ArrayList<>(); // tipi dei metodi (inclusi quelli ereditati)
	
	public ClassTypeNode() {}
	
	public ClassTypeNode(ArrayList<Node> f, ArrayList<Node> m) {
		allFields = f;
		allMethods = m;
	}

	public List<Node> getFields() {
		return allFields;
	}
	
	public List<Node> getMethods() {
		return allMethods;
	}

	public void addField(FieldNode field, int offset) {
		allFields.add(offset, field);
	}

	public void replaceField(FieldNode field, int offset) {
		allFields.set(offset, field);		
	}

	public void addMethod(MethodNode method, int offset) {
		allMethods.add(offset, method);		
	}
	
	public void replaceMethod(MethodNode method, int offset) {
		allMethods.set(offset, method);		
	}
	
	// copia di tutto il contenuto dell'oggetto ClassTypeNode
	public ClassTypeNode copy() {
		return new ClassTypeNode(new ArrayList<>(allFields), new ArrayList<>(allMethods));
	}
	
	public String toPrint(String s) {
		String fieldList = "";
		for (Node node : allFields) {
			fieldList += node.toPrint(s + "  ");
		}		
		String methodList = "";
		for (Node node : allMethods) {
			methodList += node.toPrint(s + "  ");
		}
		return s + "ClassType\n" + fieldList + methodList;
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
