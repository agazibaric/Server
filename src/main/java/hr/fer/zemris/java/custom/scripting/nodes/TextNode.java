package hr.fer.zemris.java.custom.scripting.nodes;

/**
 * Class represents text node and its properties.
 * 
 * @author Ante Gazibarić
 * @version 1.0
 */
public class TextNode extends Node {

	/**
	 * content of text node
	 */
	private String text;
	
	/**
	 * Constructor for creating new <code>TextNode</code>.
	 * 
	 * @param text content of text node that is stored
	 */
	public TextNode(String text) {
		this.text = text;
	}
	
	@Override
	public String getText() {
		return text;
	}
	
	@Override
	public void accept(INodeVisitor visitor) {
		visitor.visitTextNode(this);
	}
	
}
