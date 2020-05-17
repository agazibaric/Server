package hr.fer.zemris.java.custom.scripting.nodes;

/**
 * Class that represents document node.
 * 
 * @author Ante Gazibarić
 * @version 1.0
 */
public class DocumentNode extends Node {
	
	/**
	 * Default constructor
	 */
	public DocumentNode() {
		super();
	}
	
	@Override
	public void accept(INodeVisitor visitor) {
		visitor.visitDocumentNode(this);
	}

	@Override
	public String getText() {
		return "";
	}
	
}
