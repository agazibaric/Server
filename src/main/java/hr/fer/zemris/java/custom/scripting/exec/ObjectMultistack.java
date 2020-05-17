package hr.fer.zemris.java.custom.scripting.exec;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ObjectMultistack {

	/** map used for storing elements */
	private Map<String, MultistackEntry> map;

	/**
	 * Constructor that creates new {@code ObjectMultistack} object.
	 */
	public ObjectMultistack() {
		map = new HashMap<>();
	}

	/**
	 * Method that pushes given {@code valueWrapper} onto the stack
	 * that is associated to the given {@code name}.
	 * 
	 * @param name         key to which given value is associated
	 * @param valueWrapper value that is pushed onto the stack that is associated to the given key
	 */
	public void push(String name, ValueWrapper valueWrapper) {
		Objects.requireNonNull(name, "Key must not be null");
		Objects.requireNonNull(valueWrapper, "Value must not be null");

		MultistackEntry newEntry = new MultistackEntry(valueWrapper, map.get(name));
		map.put(name, newEntry);
	}

	/**
	 * Method that pops given {@code valueWrapper} from the stack
	 * that is associated to the given {@code name}.
	 * 
	 * @param name key to which returned value is associated
	 */
	public ValueWrapper pop(String name) {
		Objects.requireNonNull(name, "Key must not be null");

		MultistackEntry entry = map.get(name);
		if (entry == null)
			throw new ObjectMultistackException("Stack for given key is empty");

		ValueWrapper value = entry.value;
		map.put(name, entry.next);
		return value;
	}

	/** 
	 * Method that returns last value that is pushed onto the stack </br>
	 * which is associated to the given {@code name}.
	 * 
	 * 
	 * @param name key to which returned value is associated
	 * @return     value that is last pushed onto the stack associated to the given key
	 */
	public ValueWrapper peek(String name) {
		Objects.requireNonNull(name, "Key must not be null");

		MultistackEntry entry = map.get(name);
		if (entry == null)
			throw new ObjectMultistackException("Stack for given key is empty");

		return entry.value;
	}

	/**
	 * Method checks if stack associated to the given {@code name} is empty.
	 * 
	 * @param name key whose stack is checked
	 * @return     {@code true} if stack associated to the given key contains no elements,
	 *             {@code false} otherwise
	 */
	public boolean isEmpty(String name) {
		Objects.requireNonNull(name, "Key must not be null");
		return map.get(name) != null;
	}

	/**
	 * Private class for creating virtual stack-like storage.
	 * 
	 * @author Ante Gazibarić
	 * @version 1.0
	 *
	 */
	private static class MultistackEntry {

		/** value that is stored */
		private ValueWrapper value;
		/** reference to the next object */
		private MultistackEntry next;

		/**
		 * Constructor that creates new {@code MultistackEntry} object.
		 * 
		 * @param value value that is stored
		 * @param next  reference to the next {@code MultistackEntry} object
		 */
		public MultistackEntry(ValueWrapper value, MultistackEntry next) {
			this.value = value;
			this.next = next;
		}

	}
	
}
