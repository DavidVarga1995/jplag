package jplag.java15;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import jplag.java15.grammar.JavaParser;
import jplag.java15.grammar.Token;
import org.apache.commons.io.FileUtils;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Parser extends jplag.Parser implements JavaTokenConstants {
	private String actFile;
	private static final Logger LOGGER = Logger.getLogger(Parser.class.getName());
	private final boolean useMethodSeparators;

	private jplag.Structure struct;

	public Parser(boolean useMethodSeparators) {
		this.useMethodSeparators = useMethodSeparators;
	}

	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Only one parameter allowed.");
			System.exit(-1);
		}
		Parser parser = new Parser(true);
		parser.setProgram(new jplag.StrippedProgram());
		jplag.Structure struct = parser.parse(null, args);
		try {
			BufferedReader reader = new BufferedReader(new FileReader(FileUtils.getFile(args[0])));
			int lineNr = 1;
			int token = 0;
			String line;
			while ((line = reader.readLine()) != null) {
				if (token < struct.size()) {
					boolean first = true;
					while (struct.tokens[token] != null && struct.tokens[token].getLine() == lineNr) {
						if (!first) {
							System.out.println();
						}
						JavaToken tok = (JavaToken) struct.tokens[token];
						System.out.print(JavaToken.type2string(tok.type) + " (" + tok.getLine() + "," + tok.getColumn() + ","
								+ tok.getLength() + ")\t");
						first = false;
						token++;
					}
					if (first)
						System.out.print("                \t");
				}
				System.out.println(line);
				lineNr++;
			}
			reader.close();
		} catch (IOException e) {
			// e.printStackTrace();
			LOGGER.log(Level.SEVERE, "Exception occur", e);
		}
	}

	public jplag.Structure parse(File dir, String[] files) {
		struct = new jplag.Structure();
		errors = 0;

		// This will be (re)initialised in parseFile()

		for (String file : files) {
			actFile = file;
			getProgram().print(null, "Parsing file " + file + "\n");
			if (!JavaParser.parseFile(dir, file, null, this))
				errors++;

			struct.addToken(new JavaToken(FILE_END, actFile, -1, -1, -1));
		}

		this.parseEnd();
		return struct;
	}

	public void add(int type, Token token) {
		if (type == SEPARATOR_TOKEN && !useMethodSeparators)
			return;

		JavaToken tok = new JavaToken(type, actFile, token.beginLine, token.beginColumn, token.image.length());
		struct.addToken(tok);

		/*
		 * getProgram().print(null,token.beginLine+"\t"+
		 * JavaToken.type2string(type)+"\t"+ token.image);
		 */
	}
}
