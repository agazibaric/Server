package hr.fer.zemris.java.custom.scripting.exec;

/**
 * Class allows you to store value of a number. </br>
 * Number can be either of a type {@code Integer} or {@code Double}. </br>
 * Class offers several operation that you can perform on object. </br>
 * Operation on object upon which is called can change its value and also type of the value that object stores. </br>
 * 
 * @author Ante Gazibarić
 * @version 1.0
 *
 */
public class ValueWrapper {

	/** value of a number */
	private Object value;
	
	/**
	 * Constructor that creates new {@code ValueWrapper} object.
	 * 
	 * @param value object that is stored in {@code ValueWrapper}
	 */
	public ValueWrapper(Object value) {
		this.value = value;
	}

	/**
	 * Method adds to this object's value given {@code incValue}. </br>
	 * Given value is allowed to be of a type {@code Integer}, {@code Double} or </br>
	 * {@code String} if it's convertible to previous two types.
	 * 
	 * @param incValue value that is added to this object's value
	 */
	public void add(Object incValue) {
		this.value = getProcessedValue(this.value);
		incValue = getProcessedValue(incValue);
		
		calculateNewValue(incValue, Operation.ADD);
	}
	
	/**
	 * Method subtracts given {@code incValue} from this object's value. </br>
	 * Given value is allowed to be of a type {@code Integer}, {@code Double} or </br>
	 * {@code String} if it's convertible to previous two types.
	 * 
	 * @param incValue value that is subtracted from this object's value
	 */
	public void substract(Object decValue) {
		this.value = getProcessedValue(this.value);
		decValue = getProcessedValue(decValue);
		
		calculateNewValue(decValue, Operation.SUB);
		
	}
	
	/**
	 * Method multiplies this object's value by given {@code incValue}. </br>
	 * Given value is allowed to be of a type {@code Integer}, {@code Double} or </br>
	 * {@code String} if it's convertible to previous two types.
	 * 
	 * @param incValue value that is multiplying this object's value
	 */
	public void multiply(Object mulValue) {
		this.value = getProcessedValue(this.value);
		mulValue = getProcessedValue(mulValue);
		
		calculateNewValue(mulValue, Operation.MUL);
	}
	
	/**
	 * Method divides this object's value by given {@code incValue}. </br>
	 * Given value is allowed to be of a type {@code Integer}, {@code Double} or </br>
	 * {@code String} if it's convertible to previous two types.
	 * 
	 * @param incValue value that is dividing this object's value
	 */
	public void divide(Object divValue) {
		this.value = getProcessedValue(this.value);
		divValue = getProcessedValue(divValue);
		
		calculateNewValue(divValue, Operation.DIV);
	}
	
	/**
	 * Method compares this object's value with given {@code withValue}.
	 * 
	 * @param withValue value that is compared with
	 * @return number grater then zero if this object's value is greater than given value. </br>
	 * 		   number less then zero if this object's value is less than given value. </br>
	 * 		   zero if values are the same.
	 */
	public int numComare(Object withValue) {
		Object value1 = getProcessedValue(this.value);
		Object value2 = getProcessedValue(withValue);
		
		if (value1 instanceof Double || value2 instanceof Double) {
			Double v1 = Double.parseDouble(value1.toString());
			Double v2 = Double.parseDouble(value2.toString());
			return v1.compareTo(v2);
		}
		
		Integer v1 = (Integer) value1;
		Integer v2 = (Integer) value2;
		return v1.compareTo(v2);
	}
	
	/**
	 * Helper method for calculating new value 
	 * and deciding what type of newly calculated value will be. </br>
	 * 
	 * @param other     value with which operation is performed
	 * @param operation type of operation
	 */
	private void calculateNewValue(Object other, Operation operation) {
		if (other instanceof Double) {
			Double value2 = (Double) other;
			calculateNewDoubleValue(Double.parseDouble(this.value.toString()), value2, operation);
			return;
		}

		Integer value2 = (Integer) other;
		if (this.value instanceof Double) {
			calculateNewDoubleValue((Double) this.value, Double.valueOf(value2.toString()), operation);
		} else {
			calculateNewIntegerValue((Integer) this.value, value2, operation);
		}
		
	}
	
	/**
	 * Method that accepts two {@code Integer} values and performs given {@code operation} </br>
	 * whose result is stored in this object's value as a {@code Integer}.
	 * 
	 * @param value1    first {@code Integer} value
	 * @param value2	second {@code Integer} value
	 * @param operation operation that is performed on given values
	 * @throws          IllegalArgumentException if given operation is unsupported </br>
	 * 					or you tried  dividing by zero
	 */
	private void calculateNewIntegerValue(Integer value1, Integer value2, Operation operation) {
		switch (operation) {
		case ADD:
			this.value = value1 + value2;
			break;
		case SUB:
			this.value = value1 - value2;
			break;
		case  MUL:
			this.value = value1 * value2;
			break;
		case DIV:
			try {
				this.value = value1 / value2;
			} catch (ArithmeticException ex) {
				throw new IllegalArgumentException("Can not divide by zero");
			}
			break;
		default:
			throw new IllegalArgumentException("Unsupported operation");
		}
		
	}
	
	/**
	 * Method that accepts two {@code Double} values and performs given {@code operation} </br>
	 * whose result is stored in this object's value as a {@code Double}.
	 * 
	 * @param value1    first {@code Double} value
	 * @param value2	second {@code Double} value
	 * @param operation operation that is performed on given values
	 * @throws          IllegalArgumentException if given operation is unsupported
	 */
	private void calculateNewDoubleValue(Double value1, Double value2, Operation operation) {
		switch (operation) {
		case ADD:
			this.value = value1 + value2;
			break;
		case SUB:
			this.value = value1 - value2;
			break;
		case  MUL:
			this.value = value1 * value2;
			break;
		case DIV:
			this.value = value1 / value2;
			break;
		default:
			throw new IllegalArgumentException("Unsupported operation");
		}
		
	}
	
	/**
	 * Method processes given value by the rules that this class defines. </br>
	 * 
	 * @param value value that is processed
	 * @return      {@code Object} that represents processed value
	 */
	private Object getProcessedValue(Object value) {
		if (value == null) 
			return Integer.valueOf(0);
		
		checkValueValidity(value); 
		
		try {
			if (value instanceof String) {
				String stringValue = (String) value;
				if (stringValue.contains(".") || stringValue.contains("E")) {
					Double doubleValue = Double.parseDouble(stringValue);
					return doubleValue;
				}
				Integer integerValue = Integer.parseInt(stringValue);
				return integerValue;
			}
		} catch (NumberFormatException ex) {
			throw new RuntimeException("Invalid string representation. You entered: " + value.toString());
		}
		
		return value;
	}
	
	/**
	 * Method checks if given value is of a valid type. </br>
	 * Valid types are: {@code Integer}, {@code Double} and {@code String}.
	 * 
	 * @param value value whose type is checked
	 */
	private void checkValueValidity(Object value) {
		if (!(value instanceof Integer) && 
			!(value instanceof Double) && 
			!(value instanceof String))
			throw new RuntimeException("Given value is not of a valid type");
	}
	
	/**
	 * Method returns value that this objects stores.
	 * 
	 * @return value of a this object
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * Method sets object's value to the given {@code value}.
	 * 
	 * @param value new value
	 */
	public void setValue(Object value) {
		this.value = value;
	}
	
}
