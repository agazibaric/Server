package hr.fer.zemris.java.webserver.workers;

import java.util.Iterator;
import java.util.Set;

import hr.fer.zemris.java.webserver.IWebWorker;
import hr.fer.zemris.java.webserver.RequestContext;

/**
 * Server worker that can calculates sum of given parameters in client's request.
 * 
 * @author Ante Gazibaric
 * @version 1.0
 *
 */
public class SumWorker implements IWebWorker {
	
	/**
	 * Default value of A parameter.
	 */
	private static final String DEFAULT_A = "1";
	/**
	 * Default value of B parameter.
	 */
	private static final String DEFAULT_B = "2";
	/**
	 * String that represents name of A parameter.
	 */
	private static final String A = "a";
	/**
	 * String that represents name of B parameter.
	 */
	private static final String B = "b";
	/**
	 * String that represents name of calculated sum.
	 */
	private static final String SUM = "zbroj";
	/**
	 * Default sum value.
	 */
	private static final String DEFAULT_SUM = "3";

	@Override
	public void processRequest(RequestContext context) throws Exception {
		
		Set<String> names = context.getParameterNames();
		int size = names.size();
		if (size == 0) {
			setParameters(DEFAULT_A, DEFAULT_B, context);
		} else if (size == 1) {
			if (names.contains(A)) {
				setParameters(context.getParameter(A), DEFAULT_B, context);
			} else if (names.contains(B)) {
				setParameters(DEFAULT_A, context.getParameter(B), context);
			}
		} else {
			Iterator<String> it = names.iterator();
			setParameters(context.getParameter(it.next()), context.getParameter(it.next()), context);
		}
		
		context.getIDispatcher().dispatchRequest("/private/calc.smscr");
	}
	
	/**
	 * Method sets parameters and calculated sum as temporary parameters in given {@code context.
	 * 
	 * @param valueA  A value
	 * @param valueB  B value
	 * @param context context that stores temporary parameters
	 */
	private void setParameters(String valueA, String valueB, RequestContext context) {
		String sum = getSum(valueA, valueB);
		if (sum == null) {
			valueA = DEFAULT_A;
			valueB = DEFAULT_B;
			sum = DEFAULT_SUM;
		}
		context.setTemporaryParameter(A, valueA);
		context.setTemporaryParameter(B, valueB);
		context.setTemporaryParameter(SUM, sum);
	}
	
	/**
	 * Method returns sum of given values as String
	 * or default string value given values are not integers.
	 * 
	 * @param valueA A value
	 * @param valueB B value
	 * @return       sum of A value and B value as string
	 */
	private String getSum(String valueA, String valueB) {
		try {
			Integer a = Integer.parseInt(valueA);
			Integer b = Integer.parseInt(valueB);
			return String.valueOf(a + b);
		} catch (NumberFormatException ex) {
			return null;
		}
		
	}

}
