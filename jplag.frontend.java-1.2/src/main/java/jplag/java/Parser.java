package jplag.java;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import jplag.InputState;
import jplag.ParserToken;
import jplag.Structure;
import jplag.java.grammar.JLexer;
import jplag.java.grammar.JRecognizer;
import org.apache.commons.io.FileUtils;

public class Parser extends jplag.Parser implements JavaTokenConstants {
    private Structure struct;
    private String currentFile;

    private static final Logger LOGGER = Logger.getLogger(Parser.class.getName());

    public static void main(String[] args) {
        if (args.length != 1) {
            LOGGER.log(Level.INFO, "Only one parameter allowed.");
            System.exit(-1);
        }
        Parser parser = new Parser();
        parser.setProgram(new jplag.StrippedProgram());
        jplag.Structure struct = parser.parse(null, args);
        try (BufferedReader reader = new BufferedReader(new FileReader(FileUtils.getFile(args[0])))){
            int lineNr = 1;
            int token = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                if (token < struct.size()) {
                    boolean first = true;
                    while (struct.getTokens()[token] != null && struct.getTokens()[token].getLine() == lineNr) {
                        if (!first) {
                            LOGGER.log(Level.INFO, "");
                        }
                        JavaToken tok = (JavaToken) struct.getTokens()[token];
                        String info = JavaToken.type2string(tok.type) + " (" + tok.getLine() + ","
                                + tok.getColumn() + "," + tok.getLength() + ")\t";
                        LOGGER.log(Level.INFO, "{0}", info);
                        first = false;
                        token++;
                    }
                    if (first) {
                        LOGGER.log(Level.INFO, "                \t");
                    }
                }
                LOGGER.log(Level.INFO, line.replaceAll("[\r\n]",""));
                lineNr++;
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Exception occure in main: ", e);
        }
    }

    public jplag.Structure parse(File dir, String[] files) {
        struct = new Structure();
        errors = 0;
        for (String file : files) {
            getProgram().print(null, "Parsing file " + file + "\n");
            if (!parseFile(dir, file)) {
                errors++;
            }
            struct.addToken(new JavaToken(FILE_END, file, -1, -1, -1));
        }
        this.parseEnd();
        return struct;
    }

    public boolean parseFile(File dir, String file) {
        InputState inputState = null;
        try {
            FileInputStream fis = new FileInputStream(FileUtils.getFile(dir, file));
            currentFile = file;
            // Create a scanner that reads from the input stream passed to us
            inputState = new InputState(fis);
            JLexer lexer = new JLexer(inputState);
            lexer.setFilename(file);
            lexer.setTokenObjectClass("jplag.ParserToken");

            // Create a parser that reads from the scanner
            JRecognizer parser = new JRecognizer(lexer);
            parser.setFilename(file);
            parser.parser = this;

            // start parsing at the compilationUnit rule
            parser.compilationUnit();

            // close file
            fis.close();
        } catch (Exception e) {
            getProgram().addError(
                    "  Parsing Error in '" + file + "':\n" + "  Parse error at line "
                            + (inputState != null ? "" + inputState.getLine() : "UNKNOWN") + ", column "
                            + (inputState != null ? "" + inputState.tokColumn : "UNKNOWN") + ": " + e.getMessage() + "\n");
            return false;
        }

        return true;
    }

    public void add(int type, antlr.Token tok) {
        ParserToken ptok = (ParserToken) tok;
        struct.addToken(new JavaToken(type, currentFile, ptok.getLine(), ptok.getColumn(), ptok.getLength()));
    }
}
