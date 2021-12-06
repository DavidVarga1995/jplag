/*
 * Created on 04.10.2005
 */
package jplag.options.util;

import jplag.Program;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Moritz Kroll
 */
public class TagParser {
	/**
	 * Replaces all "{<index>_description}" tags inside the message string
	 * with params[<index>-1].
	 * 
	 * Example:
	 * 		parse("blb {1_n/a} d {3_desc} sf {2_bla} d",
	 * 				new String[] {"#", blabla(), null});
	 * 		returns "blb # d null sf blab d", if blabla() returns "blab"
	 */

	private static final Logger LOGGER = Logger.getLogger(Program.class.getName());

	public static String parse(String message, String[] params) {
		String[] tokens = message.split("[{}]",-1);
		StringBuilder result = new StringBuilder(tokens[0]);
		
		for(int i=1;i<tokens.length;i+=2)		// Go to next tag position
		{
			try	{
				int ind=tokens[i].indexOf('_');
				String num=(ind==-1) ? tokens[i] : tokens[i].substring(0,ind);
				result.append(params[Integer.parseInt(num) - 1]);
			}
			catch(Exception ex) {
				if(ex instanceof NumberFormatException
						|| ex instanceof IndexOutOfBoundsException)
				{
					// ex.printStackTrace();
					LOGGER.log(Level.SEVERE, "Exception occur", ex);
					result.append("{ILLEGAL PARAMETER INDEX \"").append(tokens[i]).append("\"}");
				} 
				else throw (RuntimeException) ex;
			}
			if(i+1 < tokens.length) result.append(tokens[i + 1]);
		}
		return result.toString();
	}
}