package hr.fer.zemris.java.webserver.workers;

import java.io.IOException;

import hr.fer.zemris.java.webserver.IWebWorker;
import hr.fer.zemris.java.webserver.RequestContext;

/**
 * Server worker that shows given parameters of client's request in table.
 * 
 * @author Ante Gazibaric
 * @version 1.0
 *
 */
public class EchoParams implements IWebWorker {

	@Override
	public void processRequest(RequestContext context) throws Exception {
		context.setMimeType("text/html");
		try {
			context.write("<html><body>");
			context.write("<style> table, th, td { border: 1px solid black;} </style>");
			context.write("<table><tbody>");
			for (String name : context.getParameterNames()) {
				context.write("<tr><td>" + name + "</td><td>" + context.getParameter(name) + "</td></tr>");
			}
			context.write("</table></tbody>");
			context.write("</body></html>");
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
	}

}
