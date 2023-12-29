package ast;

import java.util.ArrayList;

import lib.FOOLlib;

public class ProgLetInNode implements Node {

	private ArrayList<Node> classlist = new ArrayList<>();
	private ArrayList<Node> declist = new ArrayList<>();
	private Node exp;

	public ProgLetInNode(ArrayList<Node> d, Node e) {
		declist = d; 
		exp = e;
	}
	
	public ProgLetInNode(ArrayList<Node> c, ArrayList<Node> d, Node e) {
		classlist = c;
		declist = d;
		exp = e;
	}

	public String toPrint(String s) {
		String classStr = "";
		for (Node c : classlist) {
			classStr += c.toPrint(s + "  ");
		}
		String declStr = "";
		for (Node d : declist) {
			declStr += d.toPrint(s + "  ");
		}
		return s + "ProgLetIn\n" + classStr + declStr + exp.toPrint(s + "  "); 
	}

	public Node typeCheck() {
		classlist.forEach(c -> c.typeCheck());
		declist.forEach(d -> d.typeCheck());
		return exp.typeCheck(); 
	}

	public String codeGeneration() {
		String classCode = "";
		for (Node c : classlist) {
			classCode += c.codeGeneration();
		}
		String declCode = "";
		for (Node d : declist) {
			 declCode += d.codeGeneration();
		}
		return "push 0\n" + classCode + declCode + exp.codeGeneration() + "halt\n" + FOOLlib.getCode();
	}

}  