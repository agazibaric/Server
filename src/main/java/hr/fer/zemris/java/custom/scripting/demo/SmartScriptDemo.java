package hr.fer.zemris.java.custom.scripting.demo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hr.fer.zemris.java.custom.scripting.exec.SmartScriptEngine;
import hr.fer.zemris.java.custom.scripting.parser.SmartScriptParser;
import hr.fer.zemris.java.webserver.RequestContext;
import hr.fer.zemris.java.webserver.RequestContext.RCCookie;

/**
 * Class represents demo of {@link SmartScriptEngine}.
 * Uncomment methods in main method if needed.
 * 
 * @author Ante Gazibaric
 * @version 1.0
 *
 */
public class SmartScriptDemo {

	/**
	 * Main method. Accepts no arguments.
	 * 
	 * @param args command-line arguments
	 */
	public static void main(String[] args)  {

		try {
			   demo1();
			// demo2();
			// demo3();
			// demo4();
			// demo5();
		} catch (IOException ex) {
			System.out.println("Can not read file");
		}
	}
	
	/**
	 * Demo of smart script osnovni.smscr.
	 * 
	 * @throws IOException if reading of file fails
	 */
	private static void demo1() throws IOException {
		byte[] bytes = Files.readAllBytes(Paths.get("./src/main/resources/osnovni.smscr"));
		String documentBody = new String(bytes);
		Map<String,String> parameters = new HashMap<String, String>();
		Map<String,String> persistentParameters = new HashMap<String, String>();
		List<RCCookie> cookies = new ArrayList<RequestContext.RCCookie>();
		// create engine and execute it
		new SmartScriptEngine(
		new SmartScriptParser(documentBody).getDocumentNode(),
		new RequestContext(System.out, parameters, persistentParameters, cookies)
		).execute();
	}
	
	/**
	 * Demo of smart script zbrajanje.smscr.
	 * 
	 * @throws IOException if reading of file fails
	 */
	@SuppressWarnings("unused")
	private static void demo2() throws IOException {
		byte[] bytes = Files.readAllBytes(Paths.get("./src/main/resources/zbrajanje.smscr"));
		String documentBody = new String(bytes);
		Map<String,String> parameters = new HashMap<String, String>();
		Map<String,String> persistentParameters = new HashMap<String, String>();
		List<RCCookie> cookies = new ArrayList<RequestContext.RCCookie>();
		parameters.put("a", "4");
		parameters.put("b", "2");
		// create engine and execute it
		new SmartScriptEngine(
		new SmartScriptParser(documentBody).getDocumentNode(),
		new RequestContext(System.out, parameters, persistentParameters, cookies)
		).execute();
	}
	
	/**
	 * Demo of smart script brojPoziva.smscr.
	 * 
	 * @throws IOException if reading of file fails
	 */
	@SuppressWarnings("unused")
	private static void demo3() throws IOException {
		byte[] bytes = Files.readAllBytes(Paths.get("./src/main/resources/brojPoziva.smscr"));
		String documentBody = new String(bytes);
		Map<String,String> parameters = new HashMap<String, String>();
		Map<String,String> persistentParameters = new HashMap<String, String>();
		List<RCCookie> cookies = new ArrayList<RequestContext.RCCookie>();
		persistentParameters.put("brojPoziva", "3");
		RequestContext rc = new RequestContext(System.out, parameters, persistentParameters,
		cookies);
		new SmartScriptEngine(
		new SmartScriptParser(documentBody).getDocumentNode(), rc
		).execute();
		System.out.println("Vrijednost u mapi: " + rc.getPersistentParameter("brojPoziva"));
	}
	
	/**
	 * Demo of smart script fibonacci.smscr.
	 * 
	 * @throws IOException if reading of file fails
	 */
	@SuppressWarnings("unused")
	private static void demo4() throws IOException {
		byte[] bytes = Files.readAllBytes(Paths.get("./src/main/resources/fibonacci.smscr"));
		String documentBody = new String(bytes);
		Map<String,String> parameters = new HashMap<String, String>();
		Map<String,String> persistentParameters = new HashMap<String, String>();
		List<RCCookie> cookies = new ArrayList<RequestContext.RCCookie>();
		// create engine and execute it
		new SmartScriptEngine(
		new SmartScriptParser(documentBody).getDocumentNode(),
		new RequestContext(System.out, parameters, persistentParameters, cookies)
		).execute();
	}
	
	/**
	 * Demo of smart script fibonaccih.smscr.
	 * 
	 * @throws IOException if reading of file fails
	 */
	@SuppressWarnings("unused")
	private static void demo5() throws IOException {
		byte[] bytes = Files.readAllBytes(Paths.get("./src/main/resources/fibonaccih.smscr"));
		String documentBody = new String(bytes);
		Map<String,String> parameters = new HashMap<String, String>();
		Map<String,String> persistentParameters = new HashMap<String, String>();
		List<RCCookie> cookies = new ArrayList<RequestContext.RCCookie>();
		// create engine and execute it
		new SmartScriptEngine(
		new SmartScriptParser(documentBody).getDocumentNode(),
		new RequestContext(System.out, parameters, persistentParameters, cookies)
		).execute();
	}
	
}
