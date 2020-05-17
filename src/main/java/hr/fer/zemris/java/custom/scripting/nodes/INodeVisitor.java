package hr.fer.zemris.java.custom.scripting.nodes;

/**
 * Visitor that goes through all nodes recursively and processes them.
 * 
 * @author Ante Gazibaric
 * @version 1.0
 *
 */
public interface INodeVisitor {
	
	/**
	 * Method that is called when visitor visits text node.
	 * 
	 * @param node text node
	 */
	public void visitTextNode(TextNode node);

	/**
	 * Method that is called when visitor visits for loop node.
	 * 
	 * @param node for loop node
	 */
	public void visitForLoopNode(ForLoopNode node);

	/**
	 * Method that is called when visitor visits echo node.
	 * 
	 * @param node echo node
	 */
	public void visitEchoNode(EchoNode node);

	/**
	 * Method that is called when visitor visits document node.
	 * 
	 * @param node document node
	 */
	public void visitDocumentNode(DocumentNode node);

}
