package ast;

public interface Node {

	String toPrint(String s);

	Node typeCheck(); 

	String codeGeneration();

}  