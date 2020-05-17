package hr.fer.zemris.java.custom.scripting.exec;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Stack;

import hr.fer.zemris.java.custom.scripting.elems.Element;
import hr.fer.zemris.java.custom.scripting.elems.ElementConstantDouble;
import hr.fer.zemris.java.custom.scripting.elems.ElementConstantInteger;
import hr.fer.zemris.java.custom.scripting.elems.ElementFunction;
import hr.fer.zemris.java.custom.scripting.elems.ElementOperator;
import hr.fer.zemris.java.custom.scripting.elems.ElementString;
import hr.fer.zemris.java.custom.scripting.elems.ElementVariable;
import hr.fer.zemris.java.custom.scripting.nodes.DocumentNode;
import hr.fer.zemris.java.custom.scripting.nodes.EchoNode;
import hr.fer.zemris.java.custom.scripting.nodes.ForLoopNode;
import hr.fer.zemris.java.custom.scripting.nodes.INodeVisitor;
import hr.fer.zemris.java.custom.scripting.nodes.TextNode;
import hr.fer.zemris.java.webserver.RequestContext;

/**
 * SmartScriptEngine is class that can interpret and execute smart scripts.
 * 
 * @author Ante Gazibaric
 * @version 1.0
 *
 */
public class SmartScriptEngine {

	/**
	 * Main document node.
	 */
	private DocumentNode documentNode;
	/**
	 * Context where it stores and from which it gets values.
	 */
	private RequestContext requestContext;
	/**
	 * Multistack object.
	 */
	private ObjectMultistack multistack = new ObjectMultistack();
	/**
	 * Node visitor object used for visiting all nodes 
	 * and performing correct actions.
	 */
	private INodeVisitor visitor = new INodeVisitor() {

		@Override
		public void visitTextNode(TextNode node) {
			try {
				requestContext.write(node.getText());
			} catch (IOException e) {
			}
		}

		@Override
		public void visitForLoopNode(ForLoopNode node) {
			String varName = node.getVariable().getName();
			
			String startValue = node.getStartExpression().asText();
			multistack.push(varName, new ValueWrapper(startValue));
			String endValue = node.getEndExpression().asText();
			String stepValue = node.getStepExpression().asText();
			while (multistack.peek(varName).numComare(endValue) <= 0) {
				for (int i = 0, n = node.numberOfChildren(); i < n; i++) {
					node.getChild(i).accept(this);
				}
				multistack.peek(varName).add(stepValue);
			}
			multistack.pop(varName);
		}

		@Override
		public void visitEchoNode(EchoNode node) {
			Stack<Object> temporaryStack = new Stack<>();
			for (Element element : node.getElements()) {
				if (element instanceof ElementConstantInteger) {
					temporaryStack.push(((ElementConstantInteger)element).getValue());
				} else if (element instanceof ElementConstantDouble) {
					temporaryStack.push(((ElementConstantDouble)element).getValue());
				} else if (element instanceof ElementString) {
					temporaryStack.push(((ElementString)element).getValue());
				} else if (element instanceof ElementVariable){
					temporaryStack.push(multistack.peek(((ElementVariable) element).getName()).getValue());
				} else if (element instanceof ElementOperator) {
					temporaryStack.push(doOperation((ElementOperator) element, temporaryStack));
				} else if (element instanceof ElementFunction) {
					performFunction((ElementFunction) element, temporaryStack);
				}
			}
			writeEchoResult(temporaryStack);
		}
			
		/**
		 * Method performs operation that ElementOperator {@code element} determines.
		 * 
		 * @param element        element that determines operation that will be performed
		 * @param temporaryStack stack used for getting operators for operation that is performed
		 * @return
		 */
		private Object doOperation(ElementOperator element, Stack<Object> temporaryStack) {
			String operation = element.getValue();
			ValueWrapper value1 = new ValueWrapper(temporaryStack.pop());
			ValueWrapper value2 = new ValueWrapper(temporaryStack.pop());
			
			if (operation.equals("+")) {
				value1.add(value2.getValue());
			} else if (operation.equals("-")) {
				value1.substract(value2.getValue());
			} else if (operation.equals("*")) {
				value1.multiply(value2.getValue());
			} else if (operation.equals("/")) {
				value1.divide(value2.getValue());
			} 
			
			return value1.getValue();
		}
		
		/**
		 * Method performs function that is determined by given ElementFunction {@code element}.
		 * 
		 * @param element        element that determines function that will be performed
		 * @param temporaryStack stack used for getting values on which function will be performed
		 */
		private void performFunction(ElementFunction element, Stack<Object> temporaryStack) {
			String functionName = element.getValue();
			if (functionName.equals("sin")) {
				Double arg = getDoubleValueFrom(temporaryStack.pop());
				temporaryStack.push(Math.sin(Math.toRadians(arg)));
			} else if (functionName.equals("decfmt")) {
				String format = temporaryStack.pop().toString();
				Double number = getDoubleValueFrom(temporaryStack.pop());
				DecimalFormat formatter = new DecimalFormat("#" + format);
				temporaryStack.push(formatter.format(number));
			} else if (functionName.equals("dup")) {
				temporaryStack.push(temporaryStack.peek());
			} else if (functionName.equals("swap")) {
				Object first = temporaryStack.pop();
				Object second = temporaryStack.pop();
				temporaryStack.push(first);
				temporaryStack.push(second);
			} else if (functionName.equals("setMimeType")) {
				requestContext.setMimeType(temporaryStack.pop().toString());
			} else if (functionName.equals("paramGet")) {
				String defValue = temporaryStack.pop().toString();
				String name = temporaryStack.pop().toString();
				String value = requestContext.getParameter(name);
				temporaryStack.push(value == null ? defValue : value);
			} else if (functionName.equals("pparamGet")) {
				String defValue = temporaryStack.pop().toString();
				String name = temporaryStack.pop().toString();
				String value = requestContext.getPersistentParameter(name);
				temporaryStack.push(value == null ? defValue : value);
			} else if (functionName.equals("pparamSet")) {
				String name = temporaryStack.pop().toString();
				String value = temporaryStack.pop().toString();
				requestContext.setPersistentParameter(name, value);
			} else if (functionName.equals("pparamDel")) {
				String name = temporaryStack.pop().toString();
				requestContext.removePersistentParameter(name);
			} else if (functionName.equals("tparamGet")) {
				Object defValue = temporaryStack.pop().toString();
				String name = temporaryStack.pop().toString();
				String value = requestContext.getTemporaryParameter(name);
				temporaryStack.push(value == null ? defValue : value);
			} else if (functionName.equals("tparamSet")) {
				String name = temporaryStack.pop().toString();
				String value = temporaryStack.pop().toString();
				requestContext.setTemporaryParameter(name, value);
			} else if (functionName.equals("tparamDel")) {
				String name = temporaryStack.pop().toString();
				requestContext.removeTemporaryParameter(name);
			}
		}
		
		/**
		 * Method writes ECHO TAG result from given {@code temporaryStack}.
		 * 
		 * @param temporaryStack stack that contains result of ECHO TAG
		 */
		private void writeEchoResult(Stack<Object> temporaryStack) {
			Object[] elements = temporaryStack.toArray();
			for (int i = 0; i < elements.length; i++) {
				try {
					requestContext.write(elements[i].toString());
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
		
		/**
		 * Method returns double value from given {@code value}.
		 * 
		 * @param value represents double value
		 * @return      double value that given {@code value} represents
		 */
		private Double getDoubleValueFrom(Object value) {
			return Double.parseDouble(value.toString());
			
		}

		@Override
		public void visitDocumentNode(DocumentNode node) {
			for (int i = 0, n = node.numberOfChildren(); i < n; i++) {
				node.getChild(i).accept(this);
			}
		}

	};
	
	/**
	 * Constructor that creates new {@link SmartScriptEngine} object.
	 * 
	 * @param documentNode   {@link #documentNode}
	 * @param requestContext {@link #requestContext}
	 */
	public SmartScriptEngine(DocumentNode documentNode, RequestContext requestContext) {
		this.documentNode = documentNode;
		this.requestContext = requestContext;
	}

	/**
	 * Method executes {@link SmartScriptEngine}.
	 */
	public void execute() {
		documentNode.accept(visitor);
	}

}
