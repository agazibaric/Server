package hr.fer.zemris.java.webserver.workers;

import hr.fer.zemris.java.webserver.IWebWorker;
import hr.fer.zemris.java.webserver.RequestContext;

/**
 * Server worker that shows home page.
 * 
 * @author Ante Gazibaric
 * @version 1.0
 *
 */
public class Home implements IWebWorker {

	@Override
	public void processRequest(RequestContext context) throws Exception {
		
		String colorValue = context.getPersistentParameter("bgcolor");
		colorValue = colorValue == null ? "7F7F7F" : colorValue;
		context.setTemporaryParameter("background", colorValue);
		context.getIDispatcher().dispatchRequest("/private/home.smscr");
		
	}

}
