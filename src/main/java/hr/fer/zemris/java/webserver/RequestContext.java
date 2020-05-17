package hr.fer.zemris.java.webserver;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Class represents context for server request.
 * 
 * @author Ante Gazibaric
 * @version 1.0
 *
 */
public class RequestContext {

	/**
	 * Output stream where it writes given data.
	 */
	private OutputStream outputStream;
	/**
	 * Charset used for decoding given data.
	 */
	private Charset charset;
	/**
	 * Character encoding.
	 */
	private String encoding = "UTF-8";
	/**
	 * Status code.
	 */
	private int statusCode = 200;
	/**
	 * Status text.
	 */
	private String statusText = "OK";
	/**
	 * Mime type of data that is written.
	 */
	private String mimeType = "text/html";
	/**
	 * Parameters of client request.
	 */
	private Map<String, String> parameters;
	/**
	 * Temporary parameters of request.
	 */
	private Map<String, String> temporaryParameters;
	/**
	 * Persistent parameters of request.
	 */
	private Map<String, String> persistentParameters;
	/**
	 * Cookies of client request.
	 */
	private List<RCCookie> outputCookies; 
	/**
	 * Flag that shows if header has been generated.
	 */
	private boolean headerGenerated = false;
	/**
	 * Dispatcher object.
	 */
	private IDispatcher dispatcher;
	/**
	 * Length of content that is written.
	 */
	private Long contentLength;
	
	/**
	 * Constructor that creates new {@link RequestContext} object.
	 * 
	 * @param outputStream         {@link #outputStream}
	 * @param parameters           {@link #parameters}
	 * @param persistentParameters {@link #persistentParameters}
	 * @param outputCookies        {@link #outputCookies}
	 * @param temporaryParameters  {@link #temporaryParameters}
	 * @param dispatcher           {@link #dispatcher}
	 */
	public RequestContext(OutputStream outputStream, Map<String, String> parameters,
			Map<String, String> persistentParameters, List<RCCookie> outputCookies,
			Map<String,String> temporaryParameters, IDispatcher dispatcher) {
		this.outputStream = Objects.requireNonNull(outputStream, "Output stream must not be null");
		this.parameters = parameters == null ? new HashMap<>() : parameters;
		this.persistentParameters = persistentParameters == null ? new HashMap<>() : persistentParameters;
		this.outputCookies = outputCookies == null ? new ArrayList<>() : outputCookies;
		this.temporaryParameters = temporaryParameters == null ? new HashMap<>() : temporaryParameters;
		this.dispatcher = dispatcher;
	}
	
	/**
	 * Constructor that creates new {@link RequestContext} object.
	 * 
	 * @param outputStream         {@link #outputStream}
	 * @param parameters           {@link #parameters}
	 * @param persistentParameters {@link #persistentParameters}
	 * @param outputCookies        {@link #outputCookies}
	 */
	public RequestContext(OutputStream outputStream, Map<String, String> parameters,
			Map<String, String> persistentParameters, List<RCCookie> outputCookies) {
		this(outputStream, parameters, persistentParameters, outputCookies, null, null);
	}
	
	/**
	 * Method sets content length that is written to output stream.
	 * 
	 * @param contentLength content length taht is written to output stream.
	 */
	public void setContentLength (Long contentLength) {
		checkHeaderGenerated();
		this.contentLength = contentLength;
	}

	/**
	 * Method returns parameter associated to the given {@code name}.
	 *  
	 * @param name name to which parameter is associated
	 * @return     parameter that is associated to the given {@code name}
	 */
	public String getParameter(String name) {
		return parameters.get(name);
	}
	
	/**
	 * Method returns set of parameters.
	 * 
	 * @return set of parameters
	 */
	public Set<String> getParameterNames() {
		return Collections.unmodifiableSet(parameters.keySet());
	}

	/**
	 * Method returns persistent parameter associated to the given {@code name}.
	 *  
	 * @param name name to which persistent parameter is associated
	 * @return     persistent parameter that is associated to the given {@code name}
	 */
	public String getPersistentParameter(String name) {
		return persistentParameters.get(name);
	}

	/**
	 * Method returns set of persistent parameters.
	 * 
	 * @return set of persistent parameters
	 */
	public Set<String> getPersistentParameterNames() {
		return Collections.unmodifiableSet(persistentParameters.keySet());
	}
	
	/**
	 * Method adds persistent parameter of given value that is associated to the given name.
	 * 
	 * @param name  name to which given {@code value} is associated
	 * @param value represents value of persistent parameter
	 */
	public void setPersistentParameter(String name, String value) {
		persistentParameters.put(name, value);
	}

	/**
	 * Method removes persistent parameter that is associated to the given {@code name}.
	 * 
	 * @param name name to which removed parameter is associated
	 */
	public void removePersistentParameter(String name) {
		persistentParameters.remove(name);
	}

	/**
	 * Method returns temporary parameter associated to the given {@code name}.
	 *  
	 * @param name name to which temporary parameter is associated
	 * @return     temporary parameter that is associated to the given {@code name}
	 */
	public String getTemporaryParameter(String name) {
		return temporaryParameters.get(name);
	}
	
	/**
	 * Method adds temporary parameter of given value that is associated to the given name.
	 * 
	 * @param name  name to which given {@code value} is associated
	 * @param value represents value of temporary parameter
	 */
	public void setTemporaryParameter(String name, String value) {
		temporaryParameters.put(name, value);
	}
	
	/**
	 * Method returns set of temporary parameters.
	 * 
	 * @return set of temporary parameters
	 */
	public Set<String> getTemporaryParameterNames() {
		return Collections.unmodifiableSet(temporaryParameters.keySet());
	}
	
	/**
	 * Method removes temporary parameter that is associated to the given {@code name}.
	 * 
	 * @param name name to which removed parameter is associated
	 */
	public void removeTemporaryParameter(String name) {
		temporaryParameters.remove(name);
	}
	
	/**
	 * Method returns encoding value.
	 * 
	 * @return encoding value
	 */
	public String getEncoding() {
		return encoding;
	}

	/**
	 * Method sets encoding to the given value {@code encoding}.
	 * 
	 * @param encoding new encoding value
	 */
	public void setEncoding(String encoding) {
		checkHeaderGenerated();
		this.encoding = encoding;
	}

	/**
	 * Method returns status code.
	 * 
	 * @return status code
	 */
	public int getStatusCode() {
		return statusCode;
	}

	/**
	 * Method sets status code.
	 * 
	 * @param statusCode new status code
	 */
	public void setStatusCode(int statusCode) {
		checkHeaderGenerated();
		this.statusCode = statusCode;
	}

	/**
	 * Method returns status text.
	 * 
	 * @return status text
	 */
	public String getStatusText() {
		return statusText;
	}

	/**
	 * Method sets status text.
	 * 
	 * @param statusText new status text
	 */
	public void setStatusText(String statusText) {
		checkHeaderGenerated();
		this.statusText = statusText;
	}

	/**
	 * Method returns mime type.
	 * 
	 * @return mime type
	 */
	public String getMimeType() {
		return mimeType;
	}

	/**
	 * Method sets mime type.
	 * 
	 * @param mimeType new mime type
	 */
	public void setMimeType(String mimeType) {
		checkHeaderGenerated();
		this.mimeType = mimeType;
	}
	
	/**
	 * Method adds given {@code cookie} to the list of cookies.
	 * 
	 * @param cookie cookie that is added
	 */
	public void addRCCookie(RCCookie cookie) {
		outputCookies.add(cookie);
	}
	
	/**
	 * Method checks if header has been generated 
	 * and throws {@code RuntimeException} if it has been.
	 * Otherwise it does nothing.
	 * 
	 * @throws RuntimeException if header has been generated
	 */
	private void checkHeaderGenerated() {
		if (headerGenerated) 
			throw new RuntimeException("You must not edit properties after header has been generated");	
	}

	/**
	 * Method writes given {@code data} to the output stream.
	 * 
	 * @param data         data that is written
	 * @return             this object
	 * @throws IOException if writing data fails
	 */
	public RequestContext write(byte[] data) throws IOException {
		return write(data, 0, data.length);
	}
	
	/**
	 * Method writes given {@code text} to the output stream.
	 * 
	 * @param text         text that is written
	 * @return             this object
	 * @throws IOException if writing text fails
	 */
	public RequestContext write(String text) throws IOException {
		if (!headerGenerated) {
			generateHeader();
		}
		outputStream.write(text.getBytes(charset));
		outputStream.flush();
		return this;
	}
	
	/**
	 *  Method writes given {@code data} with length {@code len} from given {@code offset} to the output stream.
	 * 
	 * @param data
	 * @param offset
	 * @param len
	 * @return
	 * @throws IOException
	 */
	public RequestContext write(byte[] data, int offset, int len) throws IOException {
		if (!headerGenerated) {
			generateHeader();
		}
		outputStream.write(data, offset, len);
		outputStream.flush();
		return this;
	}
	
	/**
	 * Method returns dispatcher object.
	 * 
	 * @return IDispatcher object
	 */
	public IDispatcher getIDispatcher() {
		return dispatcher;
	}
	
	/**
	 * Method generates and writes request header to the output stream.
	 * 
	 * @throws IOException if writing header fails
	 */
	private void generateHeader() throws IOException {
		charset = Charset.forName(encoding);
		
		StringBuilder headerBuilder = new StringBuilder();
		headerBuilder.append("HTTP/1.1 " + statusCode + " " + statusText + "\r\n");
		if (contentLength != null) {
			headerBuilder.append("Content-Length: " + contentLength + "\r\n");
		}
		headerBuilder.append("Content-Type: " + mimeType);
		headerBuilder.append(mimeType.startsWith("text/") ? "; charset=" + charset.toString() : "");
		headerBuilder.append("\r\n");
		
		if (!outputCookies.isEmpty()) {
			outputCookies.forEach(c -> {
				headerBuilder.append("Set-Cookie: " + c.name + "=\"" + c.value + "\"");
				headerBuilder.append(c.domain == null ? "" : "; Domain=" + c.domain);
				headerBuilder.append(c.path == null ? "" : "; Path=" + c.path);
				headerBuilder.append(c.maxAge == null ? "" : "; Max-Age=" + c.maxAge);
				headerBuilder.append("; HttpOnly");
				headerBuilder.append("\r\n");
			});
		}
		headerBuilder.append("\r\n");
		outputStream.write(headerBuilder.toString().getBytes(charset));
		outputStream.flush();
		headerGenerated = true;
	}

	/**
	 * Class represents client request cookie.
	 * 
	 * @author Ante Gazibaric
	 * @version 1.0
	 *
	 */
	public static class RCCookie {
		
		/**
		 * Cookie's name.
		 */
		private String name;
		/**
		 * Cookie's value.
		 */
		private String value;
		/**
		 * Cookie's domain.
		 */
		private String domain;
		/**
		 * Cookie's path.
		 */
		private String path;
		/**
		 * Maximum age of cookie.
		 */
		private Integer maxAge;
		
		/**
		 * Constructor that creates new {@link RCCookie} object.
		 * 
		 * @param name   name of cookie
		 * @param value  value of cookie
		 * @param maxAge maximum age of cookie
		 * @param domain domain of cookie
		 * @param path   path of cookie
		 */
		public RCCookie(String name, String value, Integer maxAge, String domain, String path) {
			this.name = name;
			this.value = value;
			this.domain = domain;
			this.path = path;
			this.maxAge = maxAge;
		}

		/**
		 * Method returns cookie's name.
		 * 
		 * @return Cookie's name
		 */
		public String getName() {
			return name;
		}

		/**
		 * Method returns cookie's value.
		 * 
		 * @return cookie's value
		 */
		public String getValue() {
			return value;
		}

		/**
		 * Method returns cookie's domain.
		 * 
		 * @return cookie's domain
		 */
		public String getDomain() {
			return domain;
		}

		/**
		 * Method returns cookie's path.
		 * 
		 * @return cookie's path
		 */
		public String getPath() {
			return path;
		}

		/**
		 * Method returns cookie's maximum age.
		 * 
		 * @return cookie's maximum age
		 */
		public Integer getMaxAge() {
			return maxAge;
		}
		
	}
	
}
