package jplag.python3;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import jplag.Structure;
import jplag.python3.grammar.Python3Lexer;
import jplag.python3.grammar.Python3Parser;
import jplag.python3.grammar.Python3Parser.File_inputContext;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.commons.io.FileUtils;

public class Parser extends jplag.Parser implements Python3TokenConstants {

    private Structure struct = new Structure();
    private String currentFile;

    private static final Logger LOGGER = Logger.getLogger(Parser.class.getName());

    public static void main(String[] args) {
        if (args.length < 1) {
            LOGGER.log(Level.INFO, "Only one or more files as parameter allowed.");
            System.exit(-1);
        }
        Parser parser = new Parser();
        parser.setProgram(new jplag.StrippedProgram());
        jplag.Structure struct = parser.parse(null, args);
        try (BufferedReader reader = new BufferedReader(new FileReader(FileUtils.getFile(args[0])))) {
            int lineNr = 1;
            int token = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                if (token < struct.size()) {
                    boolean first = true;
                    while (struct.getTokens()[token] != null
                            && struct.getTokens()[token].getLine() == lineNr) {
                        if (!first) {
                            LOGGER.log(Level.INFO, "");
                        }
                        Python3Token tok = (Python3Token) struct.getTokens()[token];
                        String info = Python3Token.type2string(tok.type) + " ("
                                + tok.getLine() + "," + tok.getColumn() + "," + tok.getLength() + ")\t";
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
            LOGGER.log(Level.SEVERE, "Exception occire in main: ", e);
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
            struct.addToken(new Python3Token(FILE_END, file, -1, -1, -1));
        }
        this.parseEnd();
        return struct;
    }

    public boolean parseFile(File dir, String file) {

        ANTLRInputStream input;
        try (BufferedInputStream fis = new BufferedInputStream(new FileInputStream(FileUtils.getFile(dir, file)))) {

            currentFile = file;
            input = new ANTLRInputStream(fis);

            // create a lexer that feeds off of input CharStream
            Python3Lexer lexer = new Python3Lexer(input);

            // create a buffer of tokens pulled from the lexer
            CommonTokenStream tokens = new CommonTokenStream(lexer);

            // create a parser that feeds off the tokens buffer
            Python3Parser parser = new Python3Parser(tokens);
            File_inputContext in = parser.file_input();

            ParseTreeWalker ptw = new ParseTreeWalker();
            for (int i = 0; i < in.getChildCount(); i++) {
                ParseTree pt = in.getChild(i);
                ptw.walk(new JplagPython3Listener(this), pt);
            }

        } catch (IOException e) {
            getProgram().addError("Parsing Error in '" + file + "':\n" + e.getMessage() + "\n");
            return false;
        }

        return true;
    }

    public void add(int type, org.antlr.v4.runtime.Token tok) {
        struct.addToken(new Python3Token(type, (currentFile == null ? "null" : currentFile), tok.getLine(),
                tok.getCharPositionInLine() + 1, tok.getText().length()));
    }

    public void addEnd(int type, org.antlr.v4.runtime.Token tok) {
        struct.addToken(new Python3Token(type, (currentFile == null ? "null" : currentFile), tok.getLine(),
                struct.getTokens()[struct.size()-1].getColumn() + 1,0));
    }
}
