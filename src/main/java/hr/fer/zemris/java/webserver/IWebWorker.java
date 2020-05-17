package hr.fer.zemris.java.webserver;

/**
 * Class represents worker that can process request from client.
 * 
 * @author Ante Gazibaric
 * @version 1.0
 *
 */
public interface IWebWorker {
	
	/**
	 * Method processes request and uses context to write results 
	 * or to delegate processing job further.
	 * 
	 * @param context    context where it writes result of processed request
	 * @throws Exception if there's invalid situation in request processing
	 */
	public void processRequest(RequestContext context) throws Exception;
	
}
