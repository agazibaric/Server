package hr.fer.zemris.java.webserver;

/**
 * Interface represents general form of server dispatcher
 * that offers method through which you can dispatch your request to the server.
 * 
 * @author Ante Gazibaric
 *
 */
public interface IDispatcher {

	/**
	 * Method that dispatches request to the server.
	 * 
	 * @param urlPath    path that represents requested path.
	 * @throws Exception if there's invalid situation in processing given {@code urlPath}
	 */
	void dispatchRequest(String urlPath) throws Exception;
	
}
