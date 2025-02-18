/*
 * @Author  Emeric Kwemou on 12.02.2005
 *
 * 
 */
package jplag.options;

import jplag.Language;
import jplag.Program;

import java.lang.reflect.Constructor;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @Changed by Emeric Kwemou 12.02.2005
 * 
 */

public class OptionContainer extends Options {
	private String languageName = "java12";// Default
	private boolean found1 = false;
	private static final Logger LOGGER = Logger.getLogger(OptionContainer.class.getName());
	public OptionContainer() {
	}

	public final void setLanguage(String language) {
		this.languageName = language;
	}

	public final void initializeSecondStep(Program program) throws jplag.ExitException {
		for (int j = 0; j < languages.length - 1; j += 2)
			if (languageName.equals(languages[j]))
				try {
					// Changed by Emeric Kwemou 13.01.05
					Constructor<?>[] laguageConstructors = Class.forName(languages[j + 1]).getDeclaredConstructors();
					Constructor<?> cons = laguageConstructors[0];
					Object[] ob = { program };
					// All Language have to have a program as Constructor Parameter
					// ->public Language(Program prog)
					Language tmp = (Language) cons.newInstance(ob);
					this.language = tmp;
					System.out.println("Language accepted ...................##########################################    " + tmp.name());
					this.minTokenMatch = this.language.min_token_match();
					this.suffixes = this.language.suffixes();
					found1 = true;
				} catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
					System.out.println(e.getMessage() + "oui");
				} catch (Exception e) {
					// e.printStackTrace();
					LOGGER.log(Level.SEVERE, "Exception occur", e);
					throw new jplag.ExitException("Language instantiation failed!");
				}
		if (!found1) {
			throw new jplag.ExitException("Unknown language: " + languageName);
		}

		// defaults
		if (!minTokenMatchSet)
			this.minTokenMatch = this.language.min_token_match();
		if (!suffixesSet)
			this.suffixes = this.language.suffixes();
	}
}
