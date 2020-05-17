package hr.fer.zemris.java.webserver.workers;

import java.io.IOException;

import hr.fer.zemris.java.webserver.IWebWorker;
import hr.fer.zemris.java.webserver.RequestContext;

/**
 * Server worker that can set background color.
 * 
 * @author Ante Gazibaric
 * @version 1.0
 *
 */
public class BgColorWorker implements IWebWorker {

	@Override
	public void processRequest(RequestContext context) throws Exception {
		context.setMimeType("text/html");
		String message = null;
		String color = context.getParameter("bgcolor");
		if (color.matches("[A-Fa-f0-9]{6}")) {
			message = "Color is updated.";
			context.setPersistentParameter("bgcolor", color);
		} else {
			message = "Color is not updated.";
		}
		
		try {
			context.write("<html><body>");
			context.write("<h1>" + message + "</h1>");
			context.write("<p><a href=\"http://www.localhost.com:5721/index2.html\">index2.html</a></p>");
			context.write("</body></html>");
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
	}

}
