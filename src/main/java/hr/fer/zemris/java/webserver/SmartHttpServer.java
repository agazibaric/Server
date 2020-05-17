package hr.fer.zemris.java.webserver;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import hr.fer.zemris.java.custom.scripting.exec.SmartScriptEngine;
import hr.fer.zemris.java.custom.scripting.parser.SmartScriptParser;
import hr.fer.zemris.java.webserver.RequestContext.RCCookie;

/**
 * SmartHttpServer is server that offers basic server functionalities. </br>
 * Client can request following links:
 * <li><a href="http://www.localhost.com:5721/sample.txt">http://www.localhost.com:5721/sample.txt</a></li>
 * <li><a href="http://www.localhost.com:5721/index.html">http://www.localhost.com:5721/index.html</a></li>
 * <li><a href="http://www.localhost.com:5721/index2.html">http://www.localhost.com:5721/index2.html</a></li>
 * <li><a href="http://www.localhost.com:5721/fruits.png">http://www.localhost.com:5721/fruits.png</a></li>
 * <li><a href="http://www.localhost.com:5721/scripts/osnovni.smscr">http://www.localhost.com:5721/scripts/osnovni.smscr</a></li>
 * <li><a href="http://www.localhost.com:5721/scripts/zbrajanje.smscr?a=3&b=7">http://www.localhost.com:5721/scripts/zbrajanje.smscr?a=3&b=7</a></li>
 * <li><a href="http://www.localhost.com:5721/scripts/brojPoziva.smscr">http://www.localhost.com:5721/scripts/brojPoziva.smscr</a></li>
 * <li><a href="http://www.localhost.com:5721/scripts/fibonacci.smscr">http://www.localhost.com:5721/scripts/fibonacci.smscr</a></li>
 * <li><a href="http://www.localhost.com:5721/hello">http://www.localhost.com:5721/hello</a></li>
 * <li><a href="http://www.localhost.com:5721/hello?name=john">http://www.localhost.com:5721/hello?name=john</a></li>
 * <li><a href="http://www.localhost.com:5721/cw">http://www.localhost.com:5721/cw</a></li>
 * <li><a href="http://www.localhost.com:5721/ext/EchoParams?name1=value1&name2=value2&name3=value3">http://www.localhost.com:5721/ext/EchoParams?name1=value1&name2=value2&name3=value3</a></li>
 * <li><a href="http://www.localhost.com:5721/calc?a=11&b=22">http://www.localhost.com:5721/calc?a=11&b=22</a><li>
 * 
 * @author Ante Gazibaric
 * @version 1.0
 *
 */
public class SmartHttpServer {

	/**
	 * Server's address.
	 */
	private String address;
	/**
	 * Server's domain name.
	 */
	private String domainName;
	/**
	 * Server's port value.
	 */
	private int port;
	/**
	 * Number of server's worker threads.
	 */
	private int workerThreads;
	/**
	 * Number of seconds that session cookies are valid.
	 */
	private int sessionTimeout;
	/**
	 * Server main thread.
	 */
	private ServerThread serverThread;
	/**
	 * Pool of server worker threads.
	 */
	private ExecutorService threadPool;
	/**
	 * Server's document root.
	 */
	private Path documentRoot;
	/**
	 * Map of mime types.
	 */
	private Map<String, String> mimeTypes = new HashMap<>();
	/**
	 * Map of server's workers.
	 */
	private Map<String, IWebWorker> workersMap = new HashMap<>();
	/**
	 * Map of sessions.
	 */
	private Map<String, SessionMapEntry> sessions = new HashMap<>();
	/**
	 * Random object used for generating session SID.
	 */
	private volatile Random sessionRandom = new Random();
	/**
	 * Time for which sessions thread checker sleeps.
	 */
	private static final long SESSIONS_THREAD_SLEEP = 300_000; // 5 minutes

	/**
	 * Main method.
	 * Accepts one argument that represents path to the server's properties file.
	 * 
	 * @param args command-line arguments
	 */
	public static void main(String[] args) {
		
		if (args.length != 1) {
			System.out.println("One argument is expected: 'config path'");
			return;
		}
		
		SmartHttpServer server = new SmartHttpServer(args[0]);
		server.start();
		
	}

	/**
	 * Constructor that creates new {@link SmartHttpServer} object.
	 * 
	 * @param configFileName string representation of server's properties file
	 */
	public SmartHttpServer(String configFileName) {
		initServer(configFileName);
	}
	
	/**
	 * Method initializes {@link SmartHttpServer} object.
	 * 
	 * @param configFileName string representation of server's properties file
	 */
	private void initServer(String configFileName) {
		Path configPath = Paths.get(configFileName);
		Properties properties = new Properties();
		try {
			properties.load(Files.newInputStream(configPath));
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		loadProperties(properties);
	}
	
	/**
	 * Method loads server's properties.
	 * 
	 * @param properties server's properties
	 */
	private void loadProperties(Properties properties) {
		address = properties.getProperty(ServerPropertiesKeys.ADDRESS);
		domainName = properties.getProperty(ServerPropertiesKeys.DOMAIN);
		port = Integer.parseInt(properties.getProperty(ServerPropertiesKeys.PORT));
		workerThreads = Integer.parseInt(properties.getProperty(ServerPropertiesKeys.THREADS));
		sessionTimeout = Integer.parseInt(properties.getProperty(ServerPropertiesKeys.TIMEOUT));
		documentRoot = Paths.get(properties.getProperty(ServerPropertiesKeys.DOCUMENT)).toAbsolutePath();
		loadMimeTypes(Paths.get(properties.getProperty(ServerPropertiesKeys.MIME)));
		loadWorkers(Paths.get(properties.getProperty(ServerPropertiesKeys.WORKERS)));
	}
	
	/**
	 * Method loads server's mime types from given path {@code mimePath}.
	 * 
	 * @param mimePath path to the server's mime.properties file
	 */
	private void loadMimeTypes(Path mimePath) {
		Properties mimeProp = new Properties();
		try {
			mimeProp.load(Files.newInputStream(mimePath));
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		for (String key : mimeProp.stringPropertyNames()) {
			mimeTypes.put(key, mimeProp.getProperty(key));
		}
	}
	
	/**
	 * Method loads server's workers from given path {@code workersPath}.
	 * 
	 * @param workersPath path to the server's workers.properties file
	 */
	private void loadWorkers(Path workersPath) {
		Properties workersProp = new Properties();
		try {
			workersProp.load(Files.newInputStream(workersPath));

			for (String path : workersProp.stringPropertyNames()) {
				if (workersMap.containsKey(path))
					throw new RuntimeException("Paths for workers must be unique. Duplicate path: " + path);

				String fqcn = workersProp.getProperty(path);
				Class<?> referenceToClass = this.getClass().getClassLoader().loadClass(fqcn);
				@SuppressWarnings("deprecation")
				Object newObject = referenceToClass.newInstance();
				IWebWorker iww = (IWebWorker) newObject;
				workersMap.put(path, iww);
			}

		} catch (IOException | ClassNotFoundException | 
				IllegalAccessException | InstantiationException ex) {
			ex.printStackTrace();
		}

	}

	/**
	 * Method that starts server thread if it already hasn't been started.
	 */
	protected synchronized void start() {
		if (serverThread == null) {
			serverThread = new ServerThread();
		}
		if (!serverThread.isAlive()) {
			threadPool = Executors.newFixedThreadPool(workerThreads);
			serverThread.start();
		}
		startSessionsCheckThread();
	}

	/**
	 * Method stops server thread if it's running and shuts down thread pool.
	 */
	protected synchronized void stop() {
		if (serverThread != null && serverThread.isAlive()) {
			serverThread.stopThread();
			threadPool.shutdown();
		}
	}
	
	/**
	 * Method starts thread that removes sessions from list
	 * with expired time until they are valid.
	 */
	private void startSessionsCheckThread() {
		Thread sessionsCheckThread = new Thread(() -> {
			while (true) {
				sessions.entrySet().removeIf(e -> e.getValue().validUntil < System.currentTimeMillis() / 1000);
				try {
					Thread.sleep(SESSIONS_THREAD_SLEEP);
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}
			}
		});
		sessionsCheckThread.setDaemon(true);
		sessionsCheckThread.start();
	}

	/**
	 * Thread represents server's main thread that accepts and server's clients.
	 * 
	 * @author Ante Gazibaric
	 * @version 1.0
	 *
	 */
	protected class ServerThread extends Thread {
		
		/**
		 * Flag that shows if server's thread is active
		 */
		private boolean isRunning = true;
		
		/**
		 * Method stops the thread.
		 */
		public void stopThread() {
			isRunning = false;
		}
		
		@Override
		public void run() {
				ServerSocket serverSocket;
				try {
					serverSocket = new ServerSocket();
					serverSocket.bind(new InetSocketAddress(address, port));
					while (isRunning) {
						Socket client = serverSocket.accept();
						ClientWorker cw = new ClientWorker(client);
						threadPool.submit(cw);
					}
					serverSocket.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
		}
	}

	/**
	 * Class represents client's worker that extends {@link Runnable}.
	 * It processes client request and sends him back wanted content if request is valid.
	 * 
	 * @author Ante Gazibaric
	 * @version 1.0
	 *
	 */
	private class ClientWorker implements Runnable, IDispatcher {
		
		/**
		 * Client's socket.
		 */
		private Socket csocket;
		/**
		 * Client's input stream.
		 */
		private PushbackInputStream istream;
		/**
		 * Client's output stream.
		 */
		private OutputStream ostream;
		/**
		 * Requested HTTP protocol version.
		 */
		private String version;
		/**
		 * Requested method.
		 */
		private String method;
		/**
		 * Requested host.
		 */
		private String host;
		/**
		 * Requested parameters.
		 */
		private Map<String, String> params = new HashMap<>();
		/**
		 * Temporary parameters.
		 */
		private Map<String, String> tempParams = new HashMap<>();
		/**
		 * Persistent parameters.
		 */
		private Map<String, String> permPrams = new HashMap<>();
		/**
		 * List of session's cookies.
		 */
		private List<RCCookie> outputCookies = new ArrayList<>();
		/**
		 * Session ID.
		 */
		private String SID;
		/**
		 * Mime type of requested file.
		 */
		private String mimeType;
		/**
		 * Session's requested context.
		 */
		private RequestContext context; 

		/**
		 * Constructor that creates new {@link ClientWorker} object.
		 * 
		 * @param csocket client's socket
		 */
		public ClientWorker(Socket csocket) {
			this.csocket = csocket;
		}

		@Override
		public void run() {
			try {
				istream = new PushbackInputStream(csocket.getInputStream());
				ostream = new BufferedOutputStream(csocket.getOutputStream());
				
				List<String> request = readRequest(istream);
				
				String[] firstLine = request.isEmpty() ? null : request.get(0).split(" ");
				if (firstLine == null || firstLine.length != 3) {
					sendError(ostream, 400, "Bad request");
					return;
				}
				
				method = firstLine[0].toUpperCase();
				if (!method.equals("GET")) {
					sendError(ostream, 400, "Bad request");
					return;
				}
				
				version = firstLine[2].toUpperCase();
				if (!version.equals("HTTP/1.1") && !version.equals("HTTP/1.0")) {
					sendError(ostream, 400, "HTTP version is not supported");
					return;
				}
				
				getHost(request);
				checkSession(request);
				String urlPath = getPathParameters(firstLine[1]);
				getMimeType(urlPath);
				internalDispatchRequest(urlPath, true);
				
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				try {
					csocket.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		
		/**
		 * Method checks client's request for cookies.
		 * 
		 * @param request client's request lines
		 */
		private void checkSession(List<String> request) {
			String sidCandidate = null;
			Map<String, String> sessionEntryMap = new ConcurrentHashMap<>();
			for (String header : request) {
				if (header.startsWith("Cookie:")) {
					int index = header.indexOf("Cookie:") + "Cookie:".length();
					String[] cookies = header.substring(index).trim().split(";");
					
					for (String cookie : cookies) {
						String[] cookiePair = cookie.split("=");
						String name = cookiePair[0];
						String value = cookiePair[1].substring(1, cookiePair[1].length() - 1); // Remove quotes
						sessionEntryMap.put(name, value);
						if (name.toLowerCase().equals("sid")) {
							sidCandidate = SID = value;
						}
					}
				}
			}
			
			if (sidCandidate == null) {
				addNewSession(host, generateRandomSid(), sessionEntryMap);
				return;
			}

			// Entry exists
			SessionMapEntry entry = sessions.get(sidCandidate);
			if (!entry.host.equals(this.host)) {
				addNewSession(this.host, generateRandomSid(), sessionEntryMap);
			} else if (entry.validUntil < System.currentTimeMillis() / 1000) {
				sessions.remove(sidCandidate);
				addNewSession(this.host, generateRandomSid(), sessionEntryMap);
			} else {
				// If it's valid entry, update validUntil
				entry.validUntil = sessionTimeout + System.currentTimeMillis() / 1000;
				this.permPrams = entry.map;
			}
		}
		
		/**
		 * Method adds new session to the sessions map.
		 * 
		 * @param host session's host
		 * @param sid  session's sid
		 * @param map  map of parameters
		 */
		private void addNewSession(String host, String sid, Map<String, String> map) {
			SessionMapEntry entry = new SessionMapEntry();
			entry.host = host;
			entry.sid = SID = sid;
			entry.map = map;
			entry.validUntil = sessionTimeout + System.currentTimeMillis() / 1000;
			sessions.put(SID, entry);
			outputCookies.add(new RCCookie("sid", SID, null, host, "/"));
			this.permPrams = map;
		}
		
		/**
		 * Method loads parameters from requested path 
		 * and returns requested URL path without parameters.
		 * 
		 * @param requestedPath requested path
		 * @return              URL path without parameters
		 * @throws Exception    if parsing fails
		 */
		private String getPathParameters(String requestedPath) throws Exception {
			String[] pathParts = requestedPath.split("\\?");
			if (pathParts.length == 2) {
				String paramString = pathParts[1];
				parseParameters(paramString);
			}
			return pathParts[0];
		}
		
		@Override
		public void dispatchRequest(String urlPath) throws Exception {
			internalDispatchRequest(urlPath, false);
		}
		
		/**
		 * Method that processes request.
		 * 
		 * @param urlPath    requested URL path without parameters
		 * @param directCall flag that shows if it's direct call from client
		 * @throws Exception if request process fails
		 */
		public void internalDispatchRequest(String urlPath, boolean directCall) throws Exception {
			checkRequestContext();
			
			if (urlPath.startsWith("/private") && directCall) {
				sendError(ostream, 404, "Requested path is not valid");
				return;
			}
			
			if (urlPath.startsWith("/ext/")) {
				writeEchoParams(urlPath.substring(urlPath.lastIndexOf("/") + 1));
				return;
			}
			
			if (workersMap.containsKey(urlPath)) {
				workersMap.get(urlPath).processRequest(context);
				return;
			}
			
			Path requestedFile = checkRequestedFile(urlPath);
			if (requestedFile == null)
				return;
			
			if (urlPath.endsWith(".smscr")) {
				writeSmartScript(requestedFile);
			} else {
				writeRegularFile(requestedFile);
			}
		}
		
		/**
		 * Method initializes context if it's not already.
		 */
		private void checkRequestContext() {
			if (context == null) {
				context = new RequestContext(ostream, params, permPrams, outputCookies, tempParams, this);
			}
		}
		
		/**
		 * Method processes echo parameters request.
		 * 
		 * @param className name of class that processes echo parameters request  
		 */
		private void writeEchoParams(String className) {
			String fqcn = getFQCNForWorker(className);
			try {
				Class<?> referenceToClass = this.getClass().getClassLoader().loadClass(fqcn);
				@SuppressWarnings("deprecation")
				Object newObject = referenceToClass.newInstance();
				IWebWorker iww = (IWebWorker) newObject;
				iww.processRequest(context);
			} catch (ClassNotFoundException ex) {
				return;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
		/**
		 * Method returns fully qualified class name of given worker name.
		 * 
		 * @param workerName worker name
		 * @return           fully qualified class name of given worker name
		 */
		private String getFQCNForWorker(String workerName) {
			return "hr.fer.zemris.java.webserver.workers." + workerName;
		}
		
		/**
		 * Method writes request file to client.
		 * 
		 * @param requestedFile file requested by client
		 * @throws IOException  if writing fails
		 */
		private void writeRegularFile(Path requestedFile) throws IOException {
			setRequestContext(context, requestedFile);
			byte[] data = Files.readAllBytes(requestedFile);
			context.write(data);
		}
		
		/**
		 * Method writes smart script to client.
		 * 
		 * @param requestedFile path of requested smart script file
		 * @throws IOException  if writing fails
		 */
		private void writeSmartScript(Path requestedFile) throws IOException {
			byte[] data = Files.readAllBytes(requestedFile);
			String smartScript = new String(data);
			new SmartScriptEngine(new SmartScriptParser(smartScript).getDocumentNode(), context).execute();
		}
		
		/**
		 * Method sets request context.
		 * 
		 * @param rc            request context that is set
		 * @param requestedFile requested file
		 * @throws IOException  if setting of context fails 
		 */
		private void setRequestContext(RequestContext rc, Path requestedFile) throws IOException {
			rc.setMimeType(mimeType);
			rc.setStatusCode(200);
			rc.setContentLength(Files.size(requestedFile));
		}
		
		/**
		 * Method checks if requested file is valid. 
		 * If it is it returns requested file's path.
		 * If it's not, then it writes error message to client and returns {@code null}.
		 * 
		 * @param urlPath      requested URL path
		 * @return             requested file path or {@code null} if requested path is not valid
		 * @throws IOException if writing to client fails
		 */
		private Path checkRequestedFile(String urlPath) throws IOException {
			Path requestedFile = documentRoot.resolve(urlPath.substring(1)).toAbsolutePath();
			if (!requestedFile.startsWith(documentRoot)) {
				sendError(ostream, 403, "Forbbiden");
				return null;
			}
			
			if (!Files.isReadable(requestedFile) || !Files.isRegularFile(requestedFile)) {
				sendError(ostream, 404, "Requested file is not valid");
				return null;
			}
			return requestedFile;
		}
		
		/**
		 * Method parses client's parameters from given {@code paramString}.
		 * 
		 * @param paramString client's request parameters
		 */
		private void parseParameters(String paramString) {
			if (paramString.isEmpty()) 
				return;
			
			String[] pairs = paramString.split("&");
			for (String pair : pairs) {
				String[] pairParts = pair.split("=");
				try {
					params.put(pairParts[0], pairParts[1]);
				} catch (ArrayIndexOutOfBoundsException ex) {
					return;
				}
			}
		}
		
		/**
		 * Method returns mime type of requested URL path.
		 * 
		 * @param urlPath requested URL path
		 */
		private void getMimeType(String urlPath) {
			for (String type : mimeTypes.keySet()) {
				if(urlPath.endsWith(type)) {
					this.mimeType = mimeTypes.get(type);
					return;
				}
			}
			this.mimeType = "application/octet-stream";
		}
		
		/**
		 * Method parses client request and sets host if it's given.
		 * 
		 * @param request client request
		 */
		private void getHost(List<String> request) {
			for (String header : request) {
				if (header.contains("Host:")) {
					int length = "Host:".length();
					int index = header.indexOf("Host:");
					host = header.substring(index + length).trim();
					if (host.contains(":")) {
						host = host.substring(0, host.indexOf(":")).trim();
					}
					return;
				}
			}
			this.host = domainName;
		}
		
		/**
		 * Method reads client request from given input stream {@code istream}.
		 * 
		 * @param istream      clinet's input stream
		 * @return             clinet's request list of lines
		 * @throws IOException if reading client request fails
		 */
		private List<String> readRequest(PushbackInputStream istream) throws IOException {
			String request = new String(getRequestByteArray(istream), StandardCharsets.US_ASCII);
			List<String> headers = new ArrayList<String>();
			
			String currentLine = null;
			for (String s : request.split("\n")) {
				if (s.isEmpty())
					break;
				char c = s.charAt(0);
				if (c == 9 || c == 32) {
					currentLine += s;
				} else {
					if (currentLine != null) {
						headers.add(currentLine);
					}
					currentLine = s;
				}
			}
			if (!currentLine.isEmpty()) {
				headers.add(currentLine);
			}
			return headers;
		}
		
		/**
		 * Method checks if client request is valid and returns its content in byte array.
		 * 
		 * @param istream      client input stream
		 * @return             byte array that represents client's request
		 * @throws IOException if reading client request fails
		 */
		private byte[] getRequestByteArray(PushbackInputStream istream) throws IOException {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			int state = 0;
			l: while (true) {
				int b = istream.read();
				if (b == -1)
					return null;
				if (b != 13) {
					bos.write(b);
				}
				switch (state) {
					case 0: 
						if(b==13) { state=1; } else if(b==10) state=4;
						break;
					case 1: 
						if(b==10) { state=2; } else state=0;
						break;
					case 2: 
						if(b==13) { state=3; } else state=0;
						break;
					case 3: 
						if(b==10) { break l; } else state=0;
						break;
					case 4: 
						if(b==10) { break l; } else state=0;
						break;
				}
			}
			return bos.toByteArray();
		}

		/**
		 * Method writes error to client output stream.
		 * 
		 * @param ostream      client's output stream
		 * @param statusCode   status code
		 * @param statusText   status text
		 * @throws IOException if writing error to client fails
		 */
		private void sendError(OutputStream ostream, int statusCode, String statusText) throws IOException {
			ostream.write(
					("HTTP/1.1 " + statusCode + " " + statusText + "\r\n" +
					"Server: simple java server\r\n" +
					"Content-Type: text/plain;charset=UTF-8\r\n" +
					"Content-Length: 0\r\n" +
					"Connection: close\r\n" +
					"\r\n").getBytes(StandardCharsets.US_ASCII)
				);
			ostream.flush();
		}
	}
	
	/**
	 * Method generates random SID value that contains 20 uppercased letters.
	 * 
	 * @return random SID value that contains 20 uppercased letters
	 */
	private String generateRandomSid() {
		int leftLimit = 65; // 'A'
	    int rightLimit = 90; // 'Z'
	    int length = 20;
		StringBuilder buffer = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			int value = leftLimit + (int) (this.sessionRandom.nextFloat() * (rightLimit - leftLimit + 1));
			buffer.append((char) value);
		}
		return buffer.toString();
	}
	
	/**
	 * Class that stores info about session entry.
	 * 
	 * @author Ante Gazibaric
	 * @version 1.0
	 *
	 */
	private static class SessionMapEntry {
		
		/**
		 * Session ID.
		 */
		@SuppressWarnings("unused")
		String sid;
		/**
		 * Session's host.
		 */
		String host;
		/**
		 * Session's time until it's valid
		 */
		long validUntil;
		/**
		 * Session's map of parameters.
		 */
		Map<String, String> map;
		
	}

}
