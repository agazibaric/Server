package hr.fer.zemris.java.custom.scripting.lexer;

/**
 * Class represents different types of <code>Token2</code>.
 * 
 * @author Ante Gazibaric
 * @version 1.0
 */
public enum TokenType {
	/**
	 * represents end of document
	 */
	EOF,
	/**
	 * represents <code>String</code> input
	 */
	TEXT,
	/**
	 * represents variable
	 */
	VAR,
	/**
	 * represents integer
	 */
	INTEGER,
	/**
	 * represents floating point number
	 */
	DOUBLE,
	/**
	 * represents keywords that are specific for language
	 */
	KEYWORD,
	/**
	 * represents arithmetic operators
	 */
	OPERATOR,
	/**
	 * represents start of tag
	 */
	START_TAG,
	/**
	 * represents end of tag
	 */
	END_TAG,
	/**
	 * represents AT symbol '@'
	 */
	AT,
	/**
	 * represents equals symbol '='
	 */
	EQUALS
}