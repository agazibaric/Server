package hr.fer.zemris.java.custom.scripting.lexer;


import hr.fer.zemris.java.custom.collections.ArrayIndexedCollection;

/**
 * Class represents a lexical analyzer for language specified in assignment.
 * Input of lexical analyzer is original text. Output of lexical analyzer is
 * stream of tokens.
 * 
 * @author Ante Gazibarić
 * @version 1.0
 *
 */
public class SmartScriptLexer {

	/**
	 * input text arranged in char array
	 */
	private char[] data;
	/**
	 * current token
	 */
	private Token token;
	/**
	 * index of first unprocessed <code>char</code> in </code>data</code>
	 */
	private int currentIndex;
	/**
	 * current lexer state
	 */
	private LexerState state = LexerState.TEXT;
	/**
	 * collection of language keywords
	 */
	private static ArrayIndexedCollection keywords = new ArrayIndexedCollection();

	/**
	 * Constructor for creating new <code>Lexer</code>.
	 * 
	 * @param text
	 *            text input that is processed
	 */
	public SmartScriptLexer(String text) {
		if (text == null)
			throw new IllegalArgumentException("Text must not be null");

		data = text.toCharArray();
		initLexer();
	}

	/**
	 * Method returns current token.
	 * 
	 * @return <code>Token</code> current token
	 */
	public Token getToken() {
		return token;
	}

	/**
	 * Method used for setting next token from input data.
	 * 
	 * @return <code>Token</code> next token
	 */
	public Token nextToken() {
		if (this.state == LexerState.TEXT) {
			setCurrentTokenText();
		} else {
			setCurrentTokenTag();
		}
		return token;
	}

	/**
	 * Method used for setting new token when it is in TEXT mode
	 */
	private void setCurrentTokenText() {

		if (token != null && token.getType() == TokenType.EOF)
			throw new LexerException("There is no more tokens");

		if (currentIndex >= data.length) {
			token = new Token(TokenType.EOF, null);
			return;
		}

		// '{' + '$' represents start of TAG
		if (data[currentIndex] == '{') {
			if (currentIndex + 1 < data.length && data[currentIndex + 1] == '$') {
				token = new Token(TokenType.START_TAG, null);
				currentIndex += 2;
				return;
			}
		}

		int startingIndex = currentIndex;
		while (true) {
			if (currentIndex >= data.length)
				break;
			if(data[currentIndex] == '{') {
				if (currentIndex + 1 < data.length && data[currentIndex + 1] == '$') {
					break;
				}
			}

			if (data[currentIndex] == '\\') {
				currentIndex++;
			}
			currentIndex++;
		}
		String text = new String(data, startingIndex, currentIndex - startingIndex);
		token = new Token(TokenType.TEXT, text);
	}

	/**
	 * Method for setting new token from input data when it is in TAG mode
	 */
	private void setCurrentTokenTag() {

		if (token != null && token.getType() == TokenType.EOF)
			throw new LexerException("There is no more tokens");

		if (currentIndex >= data.length) {
			token = new Token(TokenType.EOF, null);
			return;
		}

		skipWhitespaces();
		
		if (data[currentIndex] == '$') {
			if (currentIndex + 1 < data.length && data[currentIndex + 1] == '}') {
				token = new Token(TokenType.END_TAG, null);
				currentIndex += 2;
				return;
			}
			throw new LexerException("Invalid symbol in TAG. Unexpected symbol: " + data[currentIndex]);
		}

		// characters that have special function
		TokenType mappedType = getTypeFromSpecialSymbols(
				data[currentIndex]); 
		if (mappedType != null) {
			token = new Token(mappedType, Character.valueOf(data[currentIndex]));
			currentIndex++;
			return;
		}
		// Variable: it's starts with letter and further it can contain letters, digits
		// and underscores.
		if (Character.isLetter(data[currentIndex])) {
			int beginningOfWord = currentIndex;
			currentIndex++;
			while (currentIndex < data.length && (Character.isLetter(data[currentIndex])
					|| Character.isDigit(data[currentIndex]) || data[currentIndex] == '_')) {
				currentIndex++;
			}
			String word = new String(data, beginningOfWord, currentIndex - beginningOfWord);
			if (keywords.contains(word.toUpperCase())) {
				token = new Token(TokenType.KEYWORD, word);
				return;
			}
			token = new Token(TokenType.VAR, word);
			return;
		}
		// Number: it can be integer or double
		if (Character.isDigit(data[currentIndex])) {
			int beginningOfNumber = currentIndex;
			currentIndex++;
			while (currentIndex < data.length && Character.isDigit(data[currentIndex])) {
				currentIndex++;
			}
			// It's a double
			if (data[currentIndex] == '.') {
				currentIndex++;
				while (currentIndex < data.length && Character.isDigit(data[currentIndex])) {
					currentIndex++;
				}
				String numberInput = new String(data, beginningOfNumber, currentIndex - beginningOfNumber);
				Double number;
				number = Double.parseDouble(numberInput);
				token = new Token(TokenType.DOUBLE, number);
				return;
			}
			// It's a integer
			String numberInput = new String(data, beginningOfNumber, currentIndex - beginningOfNumber);
			Integer number;
			number = Integer.parseInt(numberInput);
			token = new Token(TokenType.INTEGER, number);
			return;
		}

		// If line starts with double quote, it's a string (TEXT) in TAG
		
		if (data[currentIndex] == '\"') {
			StringBuilder textBuilder = new StringBuilder();
			currentIndex++;
			
			while (true) {

				if (currentIndex >= data.length)
					throw new IllegalArgumentException("Invalid input");

				// break when we come across ending double quote
				if (data[currentIndex] == '\"')
					break;
				
				if (data[currentIndex] == '\\') {
					if (currentIndex + 1 >= data.length)
						throw new IllegalArgumentException("Invalid input");
					
					textBuilder.append(getValidString(data[currentIndex + 1]));
					currentIndex += 2;
					
				} else {
					textBuilder.append(data[currentIndex++]);
				}
			}
			
			
    		String text = new String(textBuilder.toString());
			token = new Token(TokenType.TEXT, text);
			currentIndex++;
			return;
		}
		// Operators: +, -, *, /, ^
		if (isOperator(data[currentIndex])) {
			if (data[currentIndex] == '-') {
				// Check if it's a negative number
				int beginningIndex = currentIndex;
				int nextIndex = currentIndex + 1;
				if (nextIndex < data.length && Character.isDigit(data[nextIndex])) {
					nextIndex++;
					while (nextIndex < data.length && Character.isDigit(data[nextIndex])) {
						nextIndex++;
					}
					String negativeNumberInput = new String(data, beginningIndex, nextIndex - beginningIndex);
					Integer negativeNumber;
					negativeNumber = Integer.parseInt(negativeNumberInput);
					token = new Token(TokenType.INTEGER, negativeNumber);
					currentIndex = nextIndex;
					return;
				}
			}
			token = new Token(TokenType.OPERATOR, data[currentIndex]);
			currentIndex++;
			return;
		}

		// Everything else are invalid symbols in TAG
		throw new LexerException("Invalid symbol in TAG. You entered: " + data[currentIndex]);
	}
	
	private String getValidString(char c) {
		switch (c) {
		case 'n': 
			return "\n";
		case 'r':
			return "\r";
		case '\\':
			return "\\";
		case '"':
			return "\"";
		default:
			throw new LexerException("Invalid escape symbol in TAG. Was: " + c);
		}
	}

	/**
	 * Method checks if character is a valid operator. Operators are: '+', '-', '*',
	 * '/', '^'
	 * 
	 * @param c
	 *            character that is checked
	 * @return <code>true</code> if given character is operator, otherwise
	 *         <code>false</code>
	 */
	private boolean isOperator(char c) {
		return c == '+' || c == '-' || c == '*' || c == '/' || c == '^';
	}

	/**
	 * Method used for initializing <code>Lexer<code>.
	 */
	private void initLexer() {
		keywords.add("FOR");
		keywords.add("END");
	}

	/**
	 * Helper method for getting type of token if character is special symbol
	 * 
	 * @param c
	 *            character that is checked
	 * @return <code>TokenType2</code> of <code>c</code> if <code>c</code> is
	 *         special symbol, <code>null</code> otherwise
	 */
	private TokenType getTypeFromSpecialSymbols(char c) {
		if (c == '@') {
			return TokenType.AT;
		} else if (c == '=') {
			return TokenType.EQUALS;
		} else {
			return null;
		}
	}

	/**
	 * Method used for changing state of <code>Lexer</code>.
	 * 
	 * @param state
	 *            new lexer state
	 */
	public void setState(LexerState state) {
		this.state = state;
	}

	/**
	 * Private method for skipping any whitespaces. That includes: ' ', '\n', '\t',
	 * '\r'.
	 */
	private void skipWhitespaces() {
		while (currentIndex < data.length) {
			if (isWhitespace(data[currentIndex])) {
				currentIndex++;
				continue;
			}
			break;
		}
	}

	/**
	 * Method checks if given character is a whitespace. That includes: ' ', '\n',
	 * '\t', '\r'.
	 * 
	 * @param c
	 *            character that is checked
	 * @return <code>true</code> if <code>c</code> is a whitespace, otherwise
	 *         <code>false</code>
	 */
	private boolean isWhitespace(char c) {
		return c == ' ' || c == '\n' || c == '\r' || c == '\t';
	}

	/**
	 * Method returns current state of lexer.
	 * 
	 * @return <code>LexerState</code> of lexer
	 */
	public LexerState getState() {
		return state;
	}

}
