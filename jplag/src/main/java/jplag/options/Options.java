/*
 * Author  Emeric Kwemou on 30.01.2005
 * this small package provide an interface to generate options of a jplag.Program instance
 * all type of options will be derivated from the abstract class Options. A simple implementation
 * is for exmaple a class that initialize commandline options.
 *
 *
 */
package jplag.options;

import jplag.Language;
import jplag.Program;
import jplag.clustering.SimilarityMatrix;
import jplag.options.util.ZipUtil;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @Author Emeric Kwemou 30.01.2005
 */
public abstract class Options {

    private static final Logger LOGGER = Logger.getLogger(ZipUtil.class.getName());

    protected boolean suffixesSet = false;

    protected boolean minTokenMatchSet = false;

    /**
     * The available languages - the first language in the array is the default language.
     */
    protected static String[] languages = {// @formatter:off
            "java19", "jplag.java19.Language",
            "java17", "jplag.java17.Language",
            "java15", "jplag.java15.Language",
            "java15dm", "jplag.java15.LanguageWithDelimitedMethods",
            "java12", "jplag.java.Language",
            "java11", "jplag.javax.Language",
            "python3", "jplag.python3.Language",
            "c/c++", "jplag.cpp.Language",
            "c#-1.2", "jplag.csharp.Language",
            "char", "jplag.chars.Language",
            "text", "jplag.text.Language",
            "scheme", "jplag.scheme.Language",
    };// @formatter:on

    public static final int MIN_CLUSTER = 1;
    public static final int MAX_CLUSTER = 2;
    public static final int AVR_CLUSTER = 3;

    // Program OPTIONS
    public boolean verboseQuiet = false;

    public boolean verboseLong = false;

    public boolean verboseParser = false;

    public boolean verboseDetails = false;

    // use to detect a language in the initilalisation
    public boolean languageIsFound = false;

    public String subDir = null;

    public String rootDir = null;

    public String originalDir = null;

    public String title = "";

    public String outputFile = null;

    public String excludeFile = null;

    public String includeFile = null;

    public boolean readSubdirs = false;

    public int storeMatches = 30;

    public boolean storePercent = false; // is the number "store_matches"

    // a percentage?
    public static final int MAX_RESULT_PAIRS = 1000;

    public String resultDir = "result";

    public static final int COMPMODE_NORMAL = 0;
    public static final int COMPMODE_REVISION = 1;

    public int comparisonMode = COMPMODE_NORMAL;

    public int minTokenMatch;

    public String[] suffixes;

    public boolean exp = false; // EXPERIMENT

    public boolean diffReport = false; // special "diff" report

    public jplag.filter.Filter filter = null;

    public String filtername = "."; // == no filter

    public String basecode = "";

    public String commandLine = "";

    // "Ronald Kostoff" specials
    public boolean externalSearch = false;

    public boolean skipParse = false;

    public boolean clustering = false;

    public boolean useBasecode = false;

    public String languageName = null;

    public String countryTag = "en";

    public float[] threshold = null;

    public int[] themewords = {15};

    public int clusterType = 0;

    public int compare = 0; // 0 = deactivated

    public SimilarityMatrix similarity = null;

    public jplag.Language language;

    //compare list of files options
    public boolean fileListMode = false;

    public List<String> fileList = new ArrayList<>();

    // "FINAL" OPTIONS
    public boolean debugParser = false;

    // END OPTIONS

    public void setProgress() {
    }

    public void setState() {
    }

    public abstract void initializeSecondStep(Program program)
            throws jplag.ExitException;

    // TODO control how the exclusion file is handled by the Program

    public static void usage() {
        String info = Program.NAME_LONG
                + ", Copyright (c) 2004-2017 KIT - IPD Tichy, Guido Malpohl, and others.\n"
                + "Usage: JPlag [ options ] <root-dir> [-c file1 file2 ...]\n"
                + " <root-dir>        The root-directory that contains all submissions.\n\n"
                + "options are:\n"
                + " -v[qlpd]        (Verbose)\n"
                + "                 q: (Quiet) no output\n"
                + "                 l: (Long) detailed output\n"
                + "                 p: print all (p)arser messages\n"
                + "                 d: print (d)etails about each submission\n"
                + " -d              (Debug) parser. Non-parsable files will be stored.\n"
                + " -S <dir>        Look in directories <root-dir>/*/<dir> for programs.\n"
                + "                 (default: <root-dir>/*)\n"
                + " -s              (Subdirs) Look at files in subdirs too (default: deactivated)\n\n"
                + " -p <suffixes>   <suffixes> is a comma-separated list of all filename suffixes\n"
                + "                 that are included. (\"-p ?\" for defaults)\n\n"
                + " -o <file>       (Output) The Parserlog will be saved to <file>\n"
                + " -x <file>       (eXclude) All files named in <file> will be ignored\n"
                + " -t <n>          (Token) Tune the sensitivity of the comparison. A smaller\n"
                + "                 <n> increases the sensitivity.\n"
                + " -m <n>          (Matches) Number of matches that will be saved (default:20)\n"
                + " -m <p>%         All matches with more than <p>% similarity will be saved.\n"
                + " -r <dir>        (Result) Name of directory in which the web pages will be\n"
                + "                 stored (default: result)\n"
                + " -bc <dir>       Name of the directory which contains the basecode (common framework)\n"
                + " -c [files]      Compare a list of files. Should be the last one.\n"
                + " -l <language>   (Language) Supported Languages:\n                 ";
        LOGGER.log(Level.INFO, "{0}", info.replaceAll("[\r\n]",""));
        for (int i = 0; i < languages.length - 2; i += 2) {
            info = languages[i] + (i == 0 ? " (default), " : ", ");
            LOGGER.log(Level.INFO, "{0}", info.replaceAll("[\r\n]",""));
        }
        info = languages[languages.length - 2];
        LOGGER.log(Level.INFO, "{0}", info.replaceAll("[\r\n]",""));
    }

    protected static void printAllLanguages() throws jplag.ExitException {
        for (int i = 0; i < languages.length - 1; i += 2)
            try {
                Language langClass = (Language) Class.forName(languages[i + 1]).getDeclaredConstructor().newInstance();
                String info = languages[i];
                LOGGER.log(Level.INFO, "{0}", info.replaceAll("[\r\n]",""));
                String[] suffixes = langClass.suffixes();
                for (int j = 0; j < suffixes.length; j++) {
                    info = suffixes[j] + (j + 1 < suffixes.length ? "," : "\n");
                    LOGGER.log(Level.INFO, "{0}", info.replaceAll("[\r\n]",""));
                }
                info = String.valueOf(langClass.min_token_match());
                LOGGER.log(Level.INFO, "{0}", info);
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException
                    | InvocationTargetException | NoSuchMethodException ex) {
                LOGGER.log(Level.SEVERE, "Exception occur in print all languages", ex);
            }
        throw new jplag.ExitException("printAllLanguages exited");
    }

    public String getClusterTyp() {
        if (this.clusterType == MIN_CLUSTER)
            return "min";
        else if (this.clusterType == MAX_CLUSTER)
            return "max";
        else if (this.clusterType == AVR_CLUSTER)
            return "avr";
        else
            return "";
    }

    public String getCountryTag() {
        return this.countryTag;
    }

    /**
     * @return Returns the title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title The title to set.
     */
    public void setTitle(String title) {
        this.title = title;
    }
}
