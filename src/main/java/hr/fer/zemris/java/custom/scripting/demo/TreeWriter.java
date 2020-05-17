package hr.fer.zemris.java.custom.scripting.demo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import hr.fer.zemris.java.custom.scripting.nodes.DocumentNode;
import hr.fer.zemris.java.custom.scripting.nodes.EchoNode;
import hr.fer.zemris.java.custom.scripting.nodes.ForLoopNode;
import hr.fer.zemris.java.custom.scripting.nodes.INodeVisitor;
import hr.fer.zemris.java.custom.scripting.nodes.TextNode;
import hr.fer.zemris.java.custom.scripting.parser.SmartScriptParser;

/**
 * Class that writes to standard output result of parsing smart script from given path.
 * 
 * @author Ante Gazibaric
 * @version 1.0
 *
 */
public class TreeWriter {
	
	/**
	 * Main method.
	 * It accepts one argument that represents path to the smart script.
	 * 
	 * @param args command-line arguments
	 */
	public static void main(String[] args) {
		
		if (args.length != 1) {
			System.out.println("One argument is expected");
			return;
		}
		
		Path filePath = Paths.get(args[0]);
		
		String smartScript = null;
		try {
			smartScript = getSmartScriptFromPath(filePath);
		} catch (IOException ex) {
			System.out.println("Unable to read file: " + filePath);
		}
		
		SmartScriptParser parser = new SmartScriptParser(smartScript);
		WriterVisitor visitor = new WriterVisitor();
		parser.getDocumentNode().accept(visitor);
		System.out.println(visitor.getText());
		
		
	}
	
	/**
	 * Method returns content of smart script as string got from given {@code path}.
	 * 
	 * @param path         path of smart script file
	 * @return             content of smart script as string got from given {@code path}
	 * @throws IOException if reading smart script file fails
	 */
	private static String getSmartScriptFromPath(Path path) throws IOException {
		if (!Files.isReadable(path) || !Files.isRegularFile(path)) {
			System.out.println("Invalid given path. Was: " + path);
		}
		return new String(Files.readAllBytes(path));
	}
	
	/**
	 * Class represents visitor that goes through all nodes 
	 * and prints out nodes content as smart script.
	 * 
	 * @author Ante Gazibaric
	 * @version 1.0
	 *
	 */
	private static class WriterVisitor implements INodeVisitor {

		/**
		 * Smart script content.
		 */
		private String text;
		
		/**
		 * Constructor that creates new {@link WriterVisitor} object.
		 */
		public WriterVisitor() {
			text = "";
		}
		
		@Override
		public void visitTextNode(TextNode node) {
			text = text.concat(node.getText());
		}

		@Override
		public void visitForLoopNode(ForLoopNode node) {
			text = text.concat(node.getText());
			for (int i = 0, n = node.numberOfChildren(); i < n; i++) {
				node.getChild(i).accept(this);	
			}
			text = text.concat("{$END$}");
		}

		@Override
		public void visitEchoNode(EchoNode node) {
			text = text.concat(node.getText());
		}

		@Override
		public void visitDocumentNode(DocumentNode node) {
			for (int i = 0, n = node.numberOfChildren(); i < n; i++) {
				node.getChild(i).accept(this);	
			}
		}
		
		/**
		 * Method returns text that visitor generated.
		 * 
		 * @return text that visitor generated
		 */
		public String getText() {
			return text;
		}
		
	}

}
