package ast;

public class EmptyNode implements Node {

	public String toPrint(String s) {
		return s + "Null\n";
	}

	public Node typeCheck() {
		// ritorna tipo EmptyTypeNode
		return new EmptyTypeNode();
	}

	public String codeGeneration() {
		// sarà sicuramente diverso da object pointer di ogni oggetto creato
		return "push -1\n";
	}

}
