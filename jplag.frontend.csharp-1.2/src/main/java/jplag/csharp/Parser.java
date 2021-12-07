package jplag.csharp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import jplag.StrippedProgram;
import jplag.Structure;
import jplag.UnicodeReader;
import jplag.csharp.grammar.CSharpLexer;
import jplag.csharp.grammar.CSharpParser;
import org.apache.commons.io.FileUtils;

public class Parser extends jplag.Parser implements CSharpTokenConstants {
	private Structure struct;
	private String currentFile;

	private static final Logger LOGGER = Logger.getLogger(Parser.class.getName());

	public static void main(String[] args) {
		if (args.length != 1) {
			LOGGER.log(Level.INFO, "Only one parameter allowed.");
			System.exit(-1);
		}
		Parser parser = new Parser();
		parser.setProgram(new StrippedProgram());

		File inputFile = FileUtils.getFile(args[0]);
		jplag.Structure struct = parser.parse(inputFile.getParentFile(), new String[] { inputFile.getName() });

		try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
			int lineNr = 1;
			int token = 0;
			String line;
			while ((line = reader.readLine()) != null) {
				if (token < struct.size()) {
					boolean first = true;
					while (struct.getTokens()[token] != null && struct.getTokens()[token].getLine() == lineNr) {
						if (!first){
							LOGGER.log(Level.INFO, "");
						}
						jplag.Token tok = struct.getTokens()[token];
						LOGGER.log(Level.INFO, CSharpToken.type2string(tok.type) + " (" + tok.getLine() + ","
								+ tok.getColumn() + "," + tok.getLength() + ")\t");
						first = false;
						token++;
					}
					if (first){
						LOGGER.log(Level.INFO, " \t");
					}
				} else{
					LOGGER.log(Level.INFO, " \t");
				}
				LOGGER.log(Level.INFO, line);
				lineNr++;
			}
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Exception occur in main", e);
		}
	}

	public jplag.Structure parse(File dir, String[] files) {
		struct = new Structure();
		errors = 0;
		for (String file : files) {
			if (!parseFile(dir, file))
				errors++;
			struct.addToken(new CSharpToken(FILE_END, file, -1, -1, -1));
		}
		this.parseEnd();
		return struct;
	}

	private boolean parseFile(File dir, String file) {
		try {
			FileInputStream fis = new FileInputStream(FileUtils.getFile(dir, file));
			currentFile = file;
			// Create a scanner that reads from the input stream passed to us
			CSharpLexer lexer = new CSharpLexer(new UnicodeReader(fis, "UTF-8"));
			lexer.setFilename(file);
			lexer.setTabSize(1);

			// Create a parser that reads from the scanner
			CSharpParser parser = new CSharpParser(lexer);
			parser.setFilename(file);
			parser.parser = this;//Added by emeric 22.01.05
			// start parsing at the compilationUnit rule
			parser.compilation_unit();

			// close file
			fis.close();
		} catch (Exception e) {
			getProgram().addError("  Parsing Error in '" + file + "':\n  " + e + "\n");
			return false;
		}
		return true;
	}

	public void add(int type, antlr.Token tok) {
		if (tok == null) {
			LOGGER.log(Level.SEVERE, "tok == null  ERROR!");
			return;
		}
		struct.addToken(new CSharpToken(type, currentFile, tok.getLine(), tok.getColumn(), tok.getText().length()));
	}

	public void add(int type, CSharpParser p) {
		add(type, p.getLastConsumedToken());
	}
}
