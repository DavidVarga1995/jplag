/*
 * Author  Emeric Kwemou on 30.01.2005
 */
package jplag.options;

import jplag.ExitException;
import jplag.Language;
import jplag.Program;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CommandLineOptions extends Options {
    private static final Logger LOGGER = Logger.getLogger(CommandLineOptions.class.getName());

    public CommandLineOptions(String[] args, String cmdInString)
            throws jplag.ExitException {

        initialize(args);

        // @Changed by Moritz Kroll 26.02.2005
        // set to default language if not specified
        // changed by Emeric 22-03-05 commandLine set to be compatible with
        // the web service requirements

        if (languageName == null)
            languageName = languages[0];
        if (cmdInString != null)
            commandLine = cmdInString;
    }

    private void initialize(String[] args) throws jplag.ExitException {
        int i = 0;
        try {
            for (i = 0; i < args.length; i++)
                if (args[i].startsWith("-"))
                    i = scanOption(args, i);
                else
                    this.rootDir = args[i];
        } catch (NumberFormatException e) {
            throw new jplag.ExitException("Bad parameter for option '"
                    + args[i] + "': " + args[i + 1] + " is not a "
                    + "positive integer!", ExitException.BAD_PARAMETER);
        }
        if (args.length == 0) {
            usage();
        } else {
            StringBuilder strBuilder = new StringBuilder(this.commandLine);
            for (i = 0; i < args.length; i++) {
                strBuilder.append(args[i]).append(" ");
            }
            this.commandLine = strBuilder.toString();
        }
    }

    private boolean scanOption(String arg) throws jplag.ExitException {
        if (arg.equals("-s")) {
            this.readSubdirs = true;
        } else if (arg.equals("-external")) { // hidden option!
            LOGGER.log(Level.INFO, "External search activated!");
            this.externalSearch = true;
        } else if (arg.equals("-skipparse")) { // hidden option!
            LOGGER.log(Level.INFO, "Skip parse activated!");
            this.skipParse = true;
        } else if (arg.equals("-diff")) { // hidden option!
            LOGGER.log(Level.INFO, "Diff-Report activated!");
            this.diffReport = true;
        } else if (arg.equals("-L")) { // hidden option!
            printAllLanguages();

        } else if (arg.startsWith("-v") && arg.length() > 2) {
            for (int i = 2; i < arg.length(); i++)
                switch (arg.charAt(i)) {
                    case 'q':
                        this.verboseQuiet = true;
                        break;
                    case 'l':
                        this.verboseLong = true;
                        break;
                    case 'p':
                        this.verboseParser = true;
                        break;
                    case 'd':
                        this.verboseDetails = true;
                        break;
                    case 's': // hidden Option
                        this.language = new jplag.javax.Language(null);// WARNING!!!!!BOMB
                        this.minTokenMatch = this.language.min_token_match();
                        this.suffixes = this.language.suffixes();
                        this.verboseQuiet = true;
                        this.exp = true;
                        break;
                    default:
                        return false;
                }
        } else
            return false;
        return true;
    }

    private boolean found1 = false;


    private int scanOption(String[] args, int i)
            throws NumberFormatException, jplag.ExitException {
        String arg = args[i];
        if (arg.equals("-S") && i + 1 < args.length) {
            subDir = args[i + 1];
            i++;
        } else if (arg.equals("-o") && i + 1 < args.length) {
            outputFile = args[i + 1];
            i++;
        } else if (arg.equals("-bc") && i + 1 < args.length) {
            // Will be validated later as root_dir is not set yet
            useBasecode = true;
            basecode = args[i + 1];
            i++;
        } else if (arg.equals("-d") && i + 1 < args.length) {
            // original directory - when used in the server environment.
            debugParser = true;
            originalDir = args[i + 1];
            i++;
        } else if (arg.equals("--") && i + 1 < args.length) {
            rootDir = args[i + 1];
            i++;
        } else if (arg.equals("-x") && i + 1 < args.length) {
            excludeFile = args[i + 1];
            i++;
        } else if (arg.equals("-clang") && i + 1 < args.length) {
            countryTag = args[i + 1];
            countryTag = countryTag.toLowerCase();
            i++;
        } else if (arg.equals("-i") && i + 1 < args.length) {
            includeFile = args[i + 1];
            i++;
        } else if (arg.equals("-t") && i + 1 < args.length) {
            minTokenMatch = Integer.parseInt(args[i + 1]);
            if (minTokenMatch < 1) {
                throw new jplag.ExitException(
                        "Illegal value: Minimum token length is less or " +
                                "equal zero!", ExitException.BAD_SENSITIVITY_OF_COMPARISON);
            }
            minTokenMatchSet = true;
            i++;
        } else if (arg.equals("-m") && i + 1 < args.length) {
            String tmp = args[i + 1];
            int index;
            if ((index = tmp.indexOf("%")) != -1) {
                storePercent = true;
                tmp = tmp.substring(0, index);
            }
            if ((storeMatches = Integer.parseInt(tmp)) < 0)
                throw new NumberFormatException();
            if (storeMatches > MAX_RESULT_PAIRS)
                storeMatches = MAX_RESULT_PAIRS;
            i++;
        } else if (arg.equals("-r") && i + 1 < args.length) {
            resultDir = args[i + 1];
            i++;
        } else if (arg.equals("-l") && i + 1 < args.length) {
            // Will be validated later when the language routines are chosen
            languageIsFound = true;
            languageName = args[i + 1].toLowerCase();
            i++;
        } else if (arg.equals("-p") && i + 1 < args.length) {
            String suffixstr = args[i + 1];
            if (!suffixstr.equals("")) {
                ArrayList<String> vsuffies = new ArrayList<>();
                StringTokenizer st = new StringTokenizer(suffixstr, ",");
                while (st.hasMoreTokens()) {
                    suffixstr = st.nextToken();
                    suffixstr = suffixstr.trim();
                    if (suffixstr.equals(""))
                        continue;
                    vsuffies.add(suffixstr);
                }
                suffixes = new String[vsuffies.size()];
                suffixes = vsuffies.toArray(suffixes);
                suffixesSet = true;
            }
            i++;
        } else if (arg.equals("-f") && i + 1 < args.length && this.exp) {
            this.filter = new jplag.filter.Filter(args[i + 1]);
            this.filtername = args[i + 1];
            i++; // EXPERIMENT!!
        } else if (arg.equals("-filter") && i + 1 < args.length) {
            LOGGER.log(Level.INFO, "Filter activated!");
            jplag.text.Parser parser = new jplag.text.Parser();
            // This Parser object doesn't have its "program" attribute
            // initialized but the initializeFilter method doesn't use it anyway
            try {
                parser.initializeFilter(args[i + 1]);
            } catch (java.io.FileNotFoundException e) {
                throw new jplag.ExitException("Filter file not found!",
                        ExitException.BAD_PARAMETER);
            }
            i++;
        } else if (arg.equals("-compmode") && i + 1 < args.length) {
            comparisonMode = Integer.parseInt(args[i + 1]);
            if (comparisonMode < COMPMODE_NORMAL || comparisonMode > COMPMODE_REVISION)
                throw new jplag.ExitException("Illegal comparison mode: \"" + comparisonMode + "\"");
            i++;
        } else if (arg.equals("-compare") && i + 1 < args.length) {
            if ((this.compare = Integer.parseInt(args[i + 1])) < 0)
                throw new NumberFormatException();
            String info = "Special comparison activated. Parameter: " + this.compare;
            LOGGER.log(Level.INFO, "{0}", info.replaceAll("[\r\n]",""));
            i++;
        } else if (arg.equals("-clustertype") && i + 1 < args.length) {
            this.clustering = true;
            String tmp = args[i + 1].toLowerCase();
            switch (tmp) {
                case "min":
                    this.clusterType = MIN_CLUSTER;
                    break;
                case "max":
                    this.clusterType = MAX_CLUSTER;
                    break;
                case "avr":
                    this.clusterType = AVR_CLUSTER;
                    break;
                default:
                    throw new ExitException("Illegal clustertype: \"" + tmp
                            + "\"\nAvailable types are 'min', 'max' and 'avr'!");
            }

            String info = "Clustering activated; type: " + args[i + 1].toUpperCase();
            LOGGER.log(Level.INFO, "{0}", info.replaceAll("[\r\n]",""));
            i++;
        } else if (arg.equals("-threshold") && i + 1 < args.length) {
            if (args[i + 1].equals("")) {
                throw new jplag.ExitException("Threshold-list is empty!");
            }
            try {
                int number = 0;
                String help;
                StringTokenizer st = new StringTokenizer(args[i + 1], ",");
                while (st.hasMoreTokens()) {
                    help = st.nextToken();
                    help = help.trim();
                    if (help.equals(""))
                        continue;
                    if (Float.parseFloat(help) < 0)
                        throw new NumberFormatException();
                    number++;
                }
                if (number == 0) {
                    throw new jplag.ExitException("No threshold given!");
                }
                this.threshold = new float[number];
                st = new StringTokenizer(args[i + 1], ",");
                number = 0;
                while (st.hasMoreTokens()) {
                    help = st.nextToken();
                    help = help.trim();
                    if (help.equals(""))
                        continue;
                    this.threshold[number++] = Float.parseFloat(help);
                }
            } catch (NoSuchElementException e) {
                throw new jplag.ExitException(
                        "Error parsing '-threshold' option!");
            }
            LOGGER.log(Level.INFO, "Thresholds: ");
            for (float v : this.threshold) {
                String info = v + " ";
                LOGGER.log(Level.INFO, "{0}", info.replaceAll("[\r\n]",""));
            }
            LOGGER.log(Level.INFO, "");
            i++;
        } else if (arg.equals("-themewords") && i + 1 < args.length) {
            if (args[i + 1].equals("")) {
                throw new jplag.ExitException("Themeword-list is empty!");
            }
            try {
                int number = 0;
                String help;
                StringTokenizer st = new StringTokenizer(args[i + 1], ",");
                while (st.hasMoreTokens()) {
                    help = st.nextToken();
                    help = help.trim();
                    if (help.equals(""))
                        continue;
                    if (Integer.parseInt(help) < 0)
                        throw new NumberFormatException();
                    number++;
                }
                if (number == 0) {
                    throw new jplag.ExitException("No themeword given!");
                }
                this.themewords = new int[number];
                st = new StringTokenizer(args[i + 1], ",");
                number = 0;
                while (st.hasMoreTokens()) {
                    help = st.nextToken();
                    help = help.trim();
                    if (help.equals(""))
                        continue;
                    this.themewords[number++] = Integer.parseInt(help);
                }
            } catch (NoSuchElementException e) {
                throw new jplag.ExitException(
                        "Error parsing '-themewords' option!");
            }
            LOGGER.log(Level.INFO, "Themewords: ");
            for (int themeword : this.themewords) {
                String info = themeword + " ";
                LOGGER.log(Level.INFO, "{0}", info.replaceAll("[\r\n]",""));
            }
            LOGGER.log(Level.INFO, "");
            i++;
        } else if (arg.equals("-title") && i + 1 < args.length) {
            if (args[i + 1].equals("")) {
                throw new jplag.ExitException("Title is empty!");
            }
            this.title = args[i + 1];
            i++;
        } else if (arg.equals("-c") && i + 2 < args.length) {
            this.fileListMode = true;
            while (i + 1 < args.length) {
                this.fileList.add(args[i + 1]);
                i++;
            }
        } else if (!scanOption(arg))
            throw new jplag.ExitException("Unknown option: " + arg,
                    ExitException.BAD_PARAMETER);

        if (!languageIsFound && i >= args.length - 2)
            throw new jplag.ExitException("No language found...",
                    ExitException.BAD_LANGUAGE_ERROR);

        return i;
    }

    public void initializeSecondStep(Program program) throws jplag.ExitException {

        for (int j = 0; j < languages.length - 1; j += 2)
            if (languageName.equals(languages[j]))
                try {
                    Constructor<?>[] languageConstructors = Class.forName(languages[j + 1]).getDeclaredConstructors();
                    Constructor<?> cons = languageConstructors[0];
                    Object[] ob = {program};
                    // All Language have to have a program as Constructor
                    // Parameter
                    // ->public Language(ProgramI prog)
                    Language tmp = (Language) cons.newInstance(ob);
                    this.language = tmp;
                    String info = "Language accepted: " + tmp.name() + "\nCommand line: " + this.commandLine;
                    LOGGER.log(Level.INFO, "{0}", info.replaceAll("[\r\n]",""));
                    found1 = true;
                } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                    LOGGER.log(Level.SEVERE, "Exception in initialize second step: {0}", e.getMessage().replaceAll("[\r\n]",""));
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Exception occur", e);
                    throw new jplag.ExitException("Illegal value: Language instantiation failed", ExitException.BAD_LANGUAGE_ERROR);
                }
        if (!found1) {
            throw new jplag.ExitException("Illegal value: Language instantiation failed: Unknown language \"" + languageName + "\"",
                    ExitException.BAD_LANGUAGE_ERROR);
        }

        // defaults
        if (!minTokenMatchSet)
            this.minTokenMatch = this.language.min_token_match();
        if (!suffixesSet)
            this.suffixes = this.language.suffixes();
        checkBasecodeOption();
    }

    /**
     * This method checks whether the basecode directory value is valid
     */
    private void checkBasecodeOption() throws jplag.ExitException {
        if (useBasecode) {
            if (basecode == null || basecode.equals("")) {
                throw new ExitException("Basecode option used but none " +
                        "specified!", ExitException.BAD_PARAMETER);
            }
            String baseC = rootDir + File.separator + basecode;
            if (!(FileUtils.getFile(rootDir)).exists()) {
                throw new ExitException("Root directory \"" + rootDir
                        + "\" doesn't exist!", ExitException.BAD_PARAMETER);
            }
            File f = FileUtils.getFile(baseC);
            if (!f.exists()) {    // Basecode dir doesn't exist.
                throw new ExitException("Basecode directory \"" + baseC
                        + "\" doesn't exist!", ExitException.BAD_PARAMETER);
            }
            if (subDir != null && subDir.length() != 0) {
                f = FileUtils.getFile(baseC, subDir);
                if (!f.exists()) {
                    throw new ExitException("Basecode directory doesn't contain"
                            + " the subdirectory \"" + subDir + "\"!",
                            ExitException.BAD_PARAMETER);
                }
            }
            String info = "Basecode directory \"" + baseC + "\" will be used";
            LOGGER.log(Level.INFO, "{0}", info.replaceAll("[\r\n]",""));
        }
    }

}
