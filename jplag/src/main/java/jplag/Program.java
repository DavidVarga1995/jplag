package jplag;

import jplag.clustering.Cluster;
import jplag.clustering.Clusters;
import jplag.clustering.SimilarityMatrix;
import jplag.options.Options;
import jplag.options.util.Messages;
import jplagUtils.PropertiesLoader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * This class coordinates the whole program flow.
 * The revision history can be found on https://svn.ipd.kit.edu/trac/jplag/wiki/JPlag/History
 *
 */

public class Program implements ProgramI {

    private static final Logger LOGGER = Logger.getLogger(Program.class.getName());

    private static final Properties versionProps = PropertiesLoader.loadProps("jplag/version.properties");
    public static final String NAME = "JPlag" + versionProps.getProperty("version", "devel");
    public static final String NAME_LONG = "JPlag (Version " + versionProps.getProperty("version", "devel") + ")";

    private final DateFormat dateFormat;
    private final DateFormat dateTimeFormat;

    private String currentSubmissionName = "<Unknown submission>";
    private ArrayList<String> errorVector = new ArrayList<>();
    private static final String SEC = " sec\n";
    private static final String MIN = " min ";
    private static final String MSEC = " msec\n\n";
    private static final String MSEC2 = " msec\n";
    private static final String TPC = "Time per comparison: ";
    private static final String NODIR = " is not a directory!";

    public final DateFormat getDateFormat() {
        return dateFormat;
    }

    public final void addError(String errorMsg) {
        errorVector.add("[" + currentSubmissionName + "]\n" + errorMsg);
        print(errorMsg, null);
    }

    public final void print(String normal, String lng) {
        if (options.verboseParser) {
            if (lng != null)
                myWrite(lng);
            else if (normal != null)
                myWrite(normal);
        }
        if (options.verboseQuiet)
            return;
        try {
            if (normal != null) {
                LOGGER.log(Level.INFO, "normal = {0}", normal.replaceAll("[\r\n]",""));
            }

            if (lng != null && options.verboseLong)
                LOGGER.log(Level.INFO, "lng = {0}", lng.replaceAll("[\r\n]",""));

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception occur in print: {0}", e.getMessage().replaceAll("[\r\n]",""));
        }
    }

    private Submission basecodeSubmission = null;

    // Used Objects of anothers jplag.Classes ,they muss be just one time
    // instantiate
    private Clusters clusters = null;

    public final Clusters getClusters() {
        return clusters;
    }

    private int errors = 0;
    private String invalidSubmissionNames = null;

    private HashSet<String> excluded = null;

    protected GSTiling gSTiling = new GSTiling(this);

    private final HashMap<String, AllBasecodeMatches> htBasecodeMatches = new HashMap<>(30);

    private ArrayList<String> included = null;

    // experiment end

    private final jplag.options.Options options;

    private final Report report;

    public final Report getReport() {
        return report;
    }

    private final Messages msg;

    public final Messages getMsg() {
        return msg;
    }

    private final Runtime runtime = Runtime.getRuntime();

    private ArrayList<Submission> submissions;

    private FileWriter writer = null;

    public Program(Options options) throws jplag.ExitException {
        this.options = options;
        this.options.initializeSecondStep(this);
        if (this.options.language == null)
            throw new ExitException("Language not initialized!", ExitException.BAD_LANGUAGE_ERROR);

        msg = new Messages(this.options.getCountryTag());

        if (this.options.getCountryTag().equals("de")) {
            dateFormat = new SimpleDateFormat("dd.MM.yyyy");
            dateTimeFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss 'GMT'");
        } else {
            dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'GMT'");
        }
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        dateTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        report = new Report(this, getLanguage());
    }

    /**
     * All submission with no errors are counted. (unsure if this is still
     * necessary.)
     */
    protected final int validSubmissions() {
        if (submissions == null)
            return 0;
        int size = 0;
        for (int i = submissions.size() - 1; i >= 0; i--) {
            if (submissions.get(i).getErrors())
                size++;
        }
        return size;
    }

    /**
     * Like the validSubmissions(), but this time all the submissions are
     * returned as a string, separated by "separator".
     */
    protected final String allValidSubmissions() {
        StringBuilder res = new StringBuilder();
        boolean firsterr = true;
        for (Submission subm : submissions) {
            if (subm.getErrors()) {
                res.append((!firsterr) ? " - " : "").append(subm.getName());
                firsterr = false;
            }
        }
        return res.toString();
    }

    /**
     * Returns a " - " separated list of invalid submission names
     */
    protected final String allInvalidSubmissions() {
        return invalidSubmissionNames;
    }

    public final void closeWriter() {
        try {
            if (writer != null)
                writer.close();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Exception occur in close writer", ex);
        }
        writer = null;
    }

    private void throwNotEnoughSubmissions() throws jplag.ExitException {
        StringBuilder errorStr = new StringBuilder();
        for (String str : errorVector) {
            errorStr.append(str);
            errorStr.append('\n');
        }

        throw new ExitException("Not enough valid submissions! (only " + validSubmissions() + " "
                + (validSubmissions() != 1 ? "are" : "is") + " valid):\n" + errorStr, ExitException.NOT_ENOUGH_SUBMISSIONS_ERROR);
    }

    private void throwBadBasecodeSubmission() throws jplag.ExitException {
        StringBuilder errorStr = new StringBuilder();
        for (String str : errorVector) {
            errorStr.append(str);
            errorStr.append('\n');
        }

        throw new ExitException("Bad basecode submission:\n" + errorStr);
    }

    // COMPARE

    /**
     * Now the actual comparison: All submissions are compared pairwise.
     */
    private void compare() throws jplag.ExitException {
        int size = submissions.size();

        SortedVector<AllMatches> avgmatches;
        SortedVector<AllMatches> maxmatches;
        int[] dist = new int[10];

        // Result vector
        avgmatches = new SortedVector<>(new AllMatches.AvgComparator());
        maxmatches = new SortedVector<>(new AllMatches.MaxComparator());

        long msec;

        AllBasecodeMatches bcmatch;
        Submission s1;
        Submission s2;

        options.setState();
        options.setProgress();

        if (this.options.useBasecode) {
            msec = System.currentTimeMillis();
            for (Submission submission : submissions) {
                s1 = submission;
                bcmatch = this.gSTiling.compareWithBasecode(s1, basecodeSubmission);
                htBasecodeMatches.put(s1.getName(), bcmatch);
                this.gSTiling.resetBaseSubmission(basecodeSubmission);
                options.setProgress();
            }
            long timebc = System.currentTimeMillis() - msec;
            print("\n\n", "\nTime for comparing with Basecode: " + ((timebc / 3600000 > 0) ? (timebc / 3600000) + " h " : "")
                    + ((timebc / 60000 > 0) ? ((timebc / 60000) % 60000) + MIN : "") + (timebc / 1000 % 60) + SEC
                    + "Time per basecode comparison: " + (timebc / size) + MSEC);
        }

        int i;
        int j;
        int anz = 0;
        AllMatches match;

        options.setProgress();
        msec = System.currentTimeMillis();

        for (i = 0; i < (size - 1); i++) {
            s1 = submissions.get(i);
            if (s1.getStruct() == null) {
                continue;
            }

            for (j = (i + 1); j < size; j++) {
                s2 = submissions.get(j);
                if (s2.getStruct() == null) {
                    continue;
                }

                match = this.gSTiling.compare(s1, s2);

                anz++;

                String info = "Comparing " + s1.getName() + "-" + s2.getName() + ": " + match.percent();
                LOGGER.log(Level.INFO, "{0}", info.replaceAll("[\r\n]",""));

                // histogram:
                if (options.useBasecode) {
                    match.bcmatchesA = htBasecodeMatches.get(match.subA.getName());
                    match.bcmatchesB = htBasecodeMatches.get(match.subB.getName());
                }

                registerMatch(match, dist, avgmatches, maxmatches, null, i, j);
                options.setProgress();
            }
        }
        options.setProgress();
        long time = System.currentTimeMillis() - msec;

        if (anz != 0)
            print("\n", "Total time for comparing submissions: " + ((time / 3600000 > 0) ? (time / 3600000) + " h " : "")
                    + ((time / 60000 > 0) ? ((time / 60000) % 60000) + MIN : "") + (time / 1000 % 60) + SEC + TPC
                    + (time / anz) + MSEC2);

        Cluster cluster = null;
        if (options.clustering)
            cluster = this.clusters.calculateClustering(submissions);

        writeResults(dist, avgmatches, maxmatches, null, cluster);
    }

    /**
     * Revision compare mode: Compare each submission only with its next
     * submission.
     */
    private void revisionCompare() throws jplag.ExitException {
        int size = submissions.size();

        SortedVector<AllMatches> avgmatches;
        SortedVector<AllMatches> maxmatches;
        SortedVector<AllMatches> minmatches;
        int[] dist = new int[10];

        // Result vector
        avgmatches = new SortedVector<>(new AllMatches.AvgReversedComparator());
        maxmatches = new SortedVector<>(new AllMatches.MaxReversedComparator());
        minmatches = new SortedVector<>(new AllMatches.MinReversedComparator());

        long msec;

        AllBasecodeMatches bcmatch;
        Submission s1;
        Submission s2;

        options.setState();
        options.setProgress();

        if (options.useBasecode) {
            msec = System.currentTimeMillis();
            for (Submission submission : submissions) {
                s1 = submission;
                bcmatch = gSTiling.compareWithBasecode(s1, basecodeSubmission);
                htBasecodeMatches.put(s1.getName(), bcmatch);
                gSTiling.resetBaseSubmission(basecodeSubmission);
                options.setProgress();
            }
            long timebc = System.currentTimeMillis() - msec;
            print("\n\n", "\nTime for comparing with Basecode: " + ((timebc / 3600000 > 0) ? (timebc / 3600000) + " h " : "")
                    + ((timebc / 60000 > 0) ? ((timebc / 60000) % 60000) + MIN : "") + (timebc / 1000 % 60) + SEC
                    + "Time per basecode comparison: " + (timebc / size) + MSEC);
        }

        int anz = 0;
        AllMatches match;

        options.setProgress();
        msec = System.currentTimeMillis();

        s1loop:
        for (int i = 0; i < size - 1; ) {
            s1 = submissions.get(i);
            if (s1.getStruct() == null) {
                continue;
            }

            // Find next valid submission
            int j = i;
            do {
                j++;
                if (j >= size)
                    break s1loop; // no more comparison pairs available
                s2 = submissions.get(j);
            } while (s2.getStruct() == null);

            match = this.gSTiling.compare(s1, s2);

            anz++;

            // histogram:
            if (options.useBasecode) {
                match.bcmatchesA = htBasecodeMatches.get(match.subA.getName());
                match.bcmatchesB = htBasecodeMatches.get(match.subB.getName());
            }

            registerMatch(match, dist, avgmatches, maxmatches, minmatches, i, j);
            options.setProgress();

            i = j;
        }
        options.setProgress();
        long time = System.currentTimeMillis() - msec;

        if (anz != 0)
            print("\n", "Total time for comparing submissions: " + ((time / 3600000 > 0) ? (time / 3600000) + " h " : "")
                    + ((time / 60000 > 0) ? ((time / 60000) % 60000) + MIN : "") + (time / 1000 % 60) + SEC + TPC
                    + (time / anz) + MSEC2);

        Cluster cluster = null;
        if (options.clustering)
            cluster = this.clusters.calculateClustering(submissions);

        writeResults(dist, avgmatches, maxmatches, minmatches, cluster);
    }

    private void createSubmissions() throws jplag.ExitException {
        submissions = new ArrayList<>();
        File f = FileUtils.getFile(options.rootDir);
        if (f == null || !f.isDirectory()) {
            throw new jplag.ExitException("\"" + options.rootDir + "\" is not a directory!");
        }
        String[] list;
        try {
            list = f.list();
        } catch (SecurityException e) {
            throw new jplag.ExitException("Unable to retrieve directory: " + options.rootDir + " Cause : " + e);
        }

        if (list != null) {
            Arrays.sort(list);
        }

        if (list != null) {
            for (String s : list) {
                File submDir = FileUtils.getFile(f, s);
                if (!submDir.isDirectory()) {
                    if (options.subDir != null)
                        continue;

                    boolean ok = false;
                    String name = submDir.getName();
                    for (int j = 0; j < options.suffixes.length; j++)
                        if (name.endsWith(options.suffixes[j])) {
                            ok = true;
                            break;
                        }

                    if (!ok)
                        continue;

                    submissions.add(new Submission(name, f, this, getLanguage()));
                    continue;
                }
                if (options.exp && excludeFile(submDir.toString())) { // EXPERIMENT
                    // !!
                    LOGGER.log(Level.SEVERE, "excluded: {0}", submDir.toString().replaceAll("[\r\n]",""));
                    continue;
                }

                File fileDir = ((options.subDir == null) ? // - S option
                        submDir
                        : FileUtils.getFile(submDir, options.subDir));
                if (fileDir.isDirectory()) {
                    if (options.basecode.equals(submDir.getName())) {
                        basecodeSubmission = new Submission(submDir.getName(), fileDir, options.readSubdirs, this, getLanguage());
                    } else {
                        submissions.add(new Submission(submDir.getName(), fileDir, options.readSubdirs, this, getLanguage())); // -s
                    }
                } else
                    throw new ExitException("Cannot find directory: " + fileDir);
            }
        }
    }

    private void createSubmissionsFileList() throws jplag.ExitException {
        submissions = new ArrayList<>();
        File f = null;
        if (options.rootDir != null) {
            f = FileUtils.getFile(options.rootDir);
            if (!f.isDirectory()) {
                throw new jplag.ExitException(options.rootDir + NODIR);
            }
        }
        for (String file : options.fileList) {
            submissions.add(new Submission(file, f, this, getLanguage()));
        }
    }


    /**
     * THIS IS FOR THE EMPIRICAL STUDY
     */
    private void createSubmissionsExp() throws jplag.ExitException {
        // ES IST SICHER, DASS EIN INCLUDE-FILE ANGEGEBEN WURDE!
        readIncludeFile();
        submissions = new ArrayList<>();
        File f = FileUtils.getFile(options.rootDir);
        if (f == null || !f.isDirectory()) {
            throw new jplag.ExitException(options.rootDir + NODIR);
        }
        String[] list = new String[included.size()];
        list = included.toArray(list);
        for (String s : list) {
            File submDir = FileUtils.getFile(f, s);
            if (submDir == null || !submDir.isDirectory())
                continue;
            if (options.exp && excludeFile(submDir.toString())) { // EXPERIMENT
                // !!
                LOGGER.log(Level.SEVERE, "excluded: {0}", submDir.toString().replaceAll("[\r\n]",""));

                continue;
            }
            File fileDir = ((options.subDir == null) ? // - S option
                    submDir
                    : FileUtils.getFile(submDir, options.subDir));
            if (fileDir != null && fileDir.isDirectory())
                submissions.add(new Submission(submDir.getName(), fileDir, options.readSubdirs, this, this.getLanguage())); // -s
            else if (options.subDir == null) {
                throw new ExitException(options.rootDir + NODIR);
            }
        }
    }

    /**
     * Check if a file is excluded or not
     */
    protected final boolean excludeFile(String file) {
        if (excluded == null)
            return false;
        for (String s : excluded)
            if (file.endsWith(s))
                return true;
        return false;
    }

    // EXPERIMENT !!!!! special compare routine!
    private void expCompare() {
        int size = validSubmissions();
        int[] similarity = new int[(size * size - size) / 2];

        int anzSub = submissions.size();
        int i;
        int j;
        int count = 0;
        Submission s1;
        Submission s2;
        AllMatches match;
        long msec = System.currentTimeMillis();
        for (i = 0; i < (anzSub - 1); i++) {
            s1 = submissions.get(i);
            if (s1.getStruct() == null)
                continue;
            for (j = (i + 1); j < anzSub; j++) {
                s2 = submissions.get(j);
                if (s2.getStruct() == null)
                    continue;

                match = this.gSTiling.compare(s1, s2);
                similarity[count++] = (int) match.percent();
            }
        }
        long time = System.currentTimeMillis() - msec;
        // output
        String info = options.rootDir + " ";
        LOGGER.log(Level.INFO, "{0}", info.replaceAll("[\r\n]",""));
        info = options.minTokenMatch + " ";
        LOGGER.log(Level.INFO, "{0}", info.replaceAll("[\r\n]",""));
        info = options.filtername + " ";
        LOGGER.log(Level.INFO, "{0}", info.replaceAll("[\r\n]",""));
        info = (time) + " ";
        LOGGER.log(Level.INFO, "{0}", info.replaceAll("[\r\n]",""));
        for (i = 0; i < similarity.length; i++) {
            info = similarity[i] + " ";
            LOGGER.log(Level.INFO, "{0}", info.replaceAll("[\r\n]",""));
        }
        LOGGER.log(Level.INFO, "\n");
    }

    /**
     * This is the special external comparison routine
     */
    private void externalCompare() throws jplag.ExitException {
        long size = submissions.size();
        SortedVector<AllMatches> avgmatches = new SortedVector<>(new AllMatches.AvgComparator());
        SortedVector<AllMatches> maxmatches = new SortedVector<>(new AllMatches.MaxComparator());
        int[] dist = new int[10];

        print("Comparing: " + size + " submissions\n", null);
        options.setState();
        options.setProgress();
        long totalComparisons = (size * (size - 1)) / 2;
        long count = 0;
        long comparisons = 0;
        int index;
        AllMatches match;
        Submission s1;
        Submission s2;
        long remain;
        String totalTimeStr;
        String remainTime;

        print("Checking memory size...\n", null);
        // First try to load as many submissions as possible
        index = fillMemory(0, (int) size);

        long startTime;
        long totalTime = 0;
        int startA = 0;
        int endA = index / 2;
        int startB = endA + 1;
        int endB = index;
        int i;
        int j;

        do {
            // compare A to A
            startTime = System.currentTimeMillis();
            print("Comparing block A (" + startA + "-" + endA + ") to block A\n", null);
            for (i = startA; i <= endA; i++) {
                options.setProgress();
                s1 = submissions.get(i);
                if (s1.getStruct() == null) {
                    count += (endA - i);
                    continue;
                }
                for (j = (i + 1); j <= endA; j++) {
                    s2 = submissions.get(j);
                    if (s2.getStruct() == null) {
                        count++;
                        continue;
                    }

                    match = this.gSTiling.compare(s1, s2);
                    registerMatch(match, dist, avgmatches, maxmatches, null, i, j);
                    comparisons++;
                    count++;
                }
            }
            options.setProgress();
            print("\n", null);
            totalTime += System.currentTimeMillis() - startTime;

            // Are we finished?
            if (startA == startB)
                break;

            do {
                totalTimeStr = "" + ((totalTime / 3600000 > 0) ? (totalTime / 3600000) + " h " : "")
                        + ((totalTime / 60000 > 0) ? ((totalTime / 60000) % 60) + MIN : "") + (totalTime / 1000 % 60) + " sec";
                if (comparisons != 0)
                    remain = totalTime * (totalComparisons - count) / comparisons;
                else
                    remain = 0;
                remainTime = "" + ((remain / 3600000 > 0) ? (remain / 3600000) + " h " : "")
                        + ((remain / 60000 > 0) ? ((remain / 60000) % 60) + MIN : "") + (remain / 1000 % 60) + " sec";

                print("Progress: " + (100 * count) / totalComparisons + "%\nTime used for comparisons: " + totalTimeStr
                        + "\nRemaining time (estimate): " + remainTime + "\n", null);

                // compare A to B
                startTime = System.currentTimeMillis();
                print("Comparing block A (" + startA + "-" + endA + ") to block B (" + startB + "-" + endB + ")\n", null);
                for (i = startB; i <= endB; i++) {
                    options.setProgress();
                    s1 = submissions.get(i);
                    if (s1.getStruct() == null) {
                        count += (endA - startA + 1);
                        continue;
                    }
                    for (j = startA; j <= endA; j++) {
                        s2 = submissions.get(j);
                        if (s2.getStruct() == null) {
                            count++;
                            continue;
                        }

                        match = this.gSTiling.compare(s1, s2);
                        registerMatch(match, dist, avgmatches, maxmatches, null, i, j);
                        comparisons++;
                        count++;
                    }
                    s1.setStruct(null); // remove B
                }
                options.setProgress();
                print("\n", null);
                totalTime += System.currentTimeMillis() - startTime;

                if (endB == size - 1 && comparisons != 0) {
                    totalTimeStr = "" + ((totalTime / 3600000 > 0) ? (totalTime / 3600000) + " h " : "")
                            + ((totalTime / 60000 > 0) ? ((totalTime / 60000) % 60) + MIN : "") + (totalTime / 1000 % 60) + " sec";
                    remain = totalTime * (totalComparisons - count) / comparisons;
                    remainTime = "" + ((remain / 3600000 > 0) ? (remain / 3600000) + " h " : "")
                            + ((remain / 60000 > 0) ? ((remain / 60000) % 60) + MIN : "") + (remain / 1000 % 60) + " sec";

                    print("Progress: " + (100 * count) / totalComparisons + "%\nTime used for comparisons: " + totalTimeStr
                            + "\nRemaining time (estimate): " + remainTime + "\n", null);
                    break;
                }

                // Remove B -> already done...
                Thread.yield();
                // Try to find the next B
                print("Finding next B\n", null);

                index = fillMemory(endB + 1, (int) size);

                startB = endB + 1;
                endB = index;

            } while (true);

            // Remove A
            for (i = startA; i <= endA; i++)
                submissions.get(i).setStruct(null);
            Thread.yield();
            print("Find next A.\n", null);
            // First try to load as many submissions as possible

            index = fillMemory(endA + 1, (int) size);

            if (index != size - 1) {
                startA = endA + 1;
                endA = startA + (index - startA + 1) / 2;
                startB = endA + 1;
                endB = index;
            } else {
                startA = startB; // last block
                endA = endB = index;
            }
        } while (true);

        totalTime += System.currentTimeMillis() - startTime;
        totalTimeStr = "" + ((totalTime / 3600000 > 0) ? (totalTime / 3600000) + " h " : "")
                + ((totalTime / 60000 > 0) ? ((totalTime / 60000) % 60000) + MIN : "") + (totalTime / 1000 % 60) + " sec";

        print("Total comparison time: " + totalTimeStr + "\nComparisons: " + count + "/" + comparisons + "/" + totalComparisons + "\n",
                null);

        // free remaining memory
        for (i = startA; i <= endA; i++)
            submissions.get(i).setStruct(null);
        Thread.yield();

        Cluster cluster = null;
        if (options.clustering)
            cluster = this.clusters.calculateClustering(submissions);

        writeResults(dist, avgmatches, maxmatches, null, cluster);
    }

    private int fillMemory(int from, int size) {
        Submission sub = null;
        int index = from;

        Thread.yield();
        long freeBefore = runtime.freeMemory();
        try {
            for (; index < size; index++) {
                sub = submissions.get(index);
                sub.setStruct(new Structure());
                if (!sub.getStruct().load(FileUtils.getFile("temp", sub.getDir().getName() + sub.getName())))
                    sub.setStruct(null);
            }
        } catch (java.lang.OutOfMemoryError e) {
            if (sub != null) {
                sub.setStruct(null);
            }
            print("Memory overflow after loading " + (index - from + 1) + " submissions.\n", null);
        }
        if (index >= size)
            index = size - 1;

        if (freeBefore / runtime.freeMemory() <= 2)
            return index;
        for (int i = (index - from) / 2; i > 0; i--) {
            submissions.get(index--).setStruct(null);
        }

        Thread.yield();

        // make sure we freed half of the "available" memory.
        long free;
        while (freeBefore / (free = runtime.freeMemory()) > 2) {
            submissions.get(index--).setStruct(null);
            Thread.yield();
        }
        print(free / 1024 / 1024 + "MByte freed. Current index: " + index + "\n", null);

        return index;
    }

    public final String getBasecode() {
        return this.options.basecode;
    }

    public final int getClusterType() {
        return this.options.clusterType;
    }

    public final String getCountryTag() {
        return options.getCountryTag();
    }

    /*
     * Distribution: Program given away to:
     *
     * 0: Server version
     *
     * 1: David Klausner 2: Ronald Kostoff 3: Bob Carlson 4: Neville Newman
     */

    public final Language getLanguage() {
        return this.options.language;
    }

    public final int getMinTokenMatch() {
        return this.options.minTokenMatch;
    }

    public final String getTitle() {
        return this.options.title;
    }

    public final String getOriginalDir() {
        return this.options.originalDir;
    }

    public final SimilarityMatrix getSimilarity() {
        return this.options.similarity;
    }

    public final String getSubDir() {
        return this.options.subDir;
    }

    public final String[] getSuffixes() {
        return this.options.suffixes;
    }

    public final int[] getThemewords() {
        return this.options.themewords;
    }

    public final float[] getThreshold() {
        return this.options.threshold;
    }

    public final int getErrors() {
        return errors;
    }

    private void makeTempDir() throws jplag.ExitException {
        print(null, "Creating temporary dir.\n");
        File f = new File("temp");
        if (!f.exists() && !f.mkdirs()) {
            throw new jplag.ExitException("Cannot create temporary directory!");
        }
        if (!f.isDirectory()) {
            throw new ExitException("'temp' is not a directory!");
        }
        if (!f.canWrite()) {
            throw new ExitException("Cannot write directory: 'temp'");
        }
    }

    private void myWrite(String str) {
        if (writer != null) {
            try {
                writer.write(str);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Exception occur in my writer", e);
            }
        } else
            LOGGER.log(Level.INFO, "{0}", str.replaceAll("[\r\n]",""));
    }

    // PARSE
    /*
     * Compiles all "submissions"
     */
    private void parseAll() throws jplag.ExitException {
        if (submissions == null) {
            LOGGER.log(Level.INFO, "  Nothing to parse!");
            return;
        }
        // lets go:)
        int count = 0;
        options.setState();
        options.setProgress();
        long msec = System.currentTimeMillis();
        Iterator<Submission> iter = submissions.iterator();

        if (options.externalSearch)
            makeTempDir();
        int invalid = 0;
        while (iter.hasNext()) {
            boolean ok;
            boolean removed = false;
            Submission subm = iter.next();
            print(null, "------ Parsing submission: " + subm.getName() + "\n");
            currentSubmissionName = subm.getName();
            options.setProgress();
            ok = subm.parse();
            if (!ok)
                errors++;

            if (options.exp && options.filter != null)
                subm.setStruct(options.filter.filter(subm.getStruct())); // EXPERIMENT
            count++;
            if (subm.getStruct() != null && subm.size() < options.minTokenMatch) {
                print(null, "Submission contains fewer tokens than minimum match " + "length allows!\n");
                subm.setStruct(null);
                invalid++;
                removed = true;
            }
            if (options.externalSearch && subm.getStruct() != null) {
                this.gSTiling.create_hashes(subm.getStruct(), options.minTokenMatch, false);
                subm.getStruct().save(FileUtils.getFile("temp", subm.getDir().getName() + subm.getName()));
                subm.setStruct(null);
            }
            if (!options.externalSearch && subm.getStruct() == null) {
                invalidSubmissionNames = (invalidSubmissionNames == null) ? subm.getName() : invalidSubmissionNames +
                        " - " + subm.getName();
                iter.remove();
            }
            if (ok && !removed)
                print(null, "OK\n");
            else
                print(null, "ERROR -> Submission removed\n");
        }

        options.setProgress();
        print("\n" + (count - errors - invalid) + " submissions parsed successfully!\n" + errors + " parser error"
                + (errors != 1 ? "s!\n" : "!\n"), null);
        if (invalid != 0) {
            print(null, invalid
                    + ((invalid == 1) ? " submission is not valid because it contains" : " submissions are not valid because they contain")
                    + " fewer tokens\nthan minimum match length allows.\n");
        }
        long time = System.currentTimeMillis() - msec;
        print("\n\n", "\nTotal time for parsing: " + ((time / 3600000 > 0) ? (time / 3600000) + " h " : "")
                + ((time / 60000 > 0) ? ((time / 60000) % 60000) + MIN : "") + (time / 1000 % 60) + SEC
                + "Time per parsed submission: " + (count > 0 ? (time / count) : "n/a") + MSEC);
    }

    private void parseBasecodeSubmission() throws jplag.ExitException {
        Submission subm = basecodeSubmission;
        if (subm == null) {
            options.useBasecode = false;
            return;
        }
        long msec = System.currentTimeMillis();
        print("----- Parsing basecode submission: " + subm.getName() + "\n", null);

        // lets go:
        if (options.externalSearch)
            makeTempDir();

        if (!subm.parse())
            throwBadBasecodeSubmission();

        if (options.exp && options.filter != null)
            subm.setStruct(options.filter.filter(subm.getStruct())); // EXPERIMENT

        if (subm.getStruct() != null && subm.size() < options.minTokenMatch)
            throw new ExitException("Basecode submission contains fewer tokens " + "than minimum match length allows!\n");

        if (options.useBasecode)
            gSTiling.create_hashes(subm.getStruct(), options.minTokenMatch, true);
        if (options.externalSearch && subm.getStruct() != null) {
            gSTiling.create_hashes(subm.getStruct(), options.minTokenMatch, false);
            subm.getStruct().save(FileUtils.getFile("temp", subm.getDir().getName() + subm.getName()));
            subm.setStruct(null);
        }

        print("\nBasecode submission parsed!\n", null);
        long time = System.currentTimeMillis() - msec;
        print("\n", "\nTime for parsing Basecode: " + ((time / 3600000 > 0) ? (time / 3600000) + " h " : "")
                + ((time / 60000 > 0) ? ((time / 60000) % 60000) + MIN : "") + (time / 1000 % 60) + SEC);
    }

    // Excluded files:

    /*
     * If an exclusion file is given, it is read in and all stings are saved in
     * the set "excluded".
     */
    private void readExclusionFile() {
        if (options.excludeFile == null)
            return;
        excluded = new HashSet<>();

        try {
            BufferedReader in = IOUtils.buffer(new FileReader(FileUtils.getFile(options.excludeFile)));
            String line;
            while ((line = in.readLine()) != null) {
                excluded.add(line.trim());
            }
            in.close();
        } catch (FileNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Exclusion file not found: {0}", options.excludeFile.replaceAll("[\r\n]",""));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Exception occur in read exclusion file", e);
        }
        print(null, "Excluded files:\n");
        if (options.verboseLong) {
            for (String s : excluded) {
                print(null, "  " + s + "\n");
            }
        }
    }

    /*
     * If an include file is given, read it in and store all the strings in
     * "included".
     */
    private void readIncludeFile() {
        if (options.includeFile == null)
            return;
        included = new ArrayList<>();
        try (BufferedReader in = new BufferedReader(new FileReader(FileUtils.getFile(options.includeFile)))) {
            String line;
            while ((line = in.readLine()) != null) {
                included.add(line.trim());
            }
        } catch (FileNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Include file not found: {0}", options.includeFile.replaceAll("[\r\n]",""));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Exception occur in read include file", e);
        }
        print(null, "Included dirs:\n");
        if (options.verboseLong) {
            Enumeration<String> enum1 = Collections.enumeration(included);
            while (enum1.hasMoreElements())
                print(null, "  " + enum1.nextElement() + "\n");
        }
    }

    private void registerMatch(AllMatches match, int[] dist, SortedVector<AllMatches> avgmatches, SortedVector<AllMatches> maxmatches,
                               SortedVector<AllMatches> minmatches, int a, int b) {
        float avgpercent = match.percent();
        float maxpercent = match.percentMaxAB();
        float minpercent = match.percentMinAB();

        dist[((((int) avgpercent) / 10) == 10) ? 9 : (((int) avgpercent) / 10)]++;
        if (!options.storePercent) {
            if ((avgmatches.size() < options.storeMatches || avgpercent > avgmatches.lastElement().percent()) && avgpercent > 0) {
                avgmatches.insert(match);
                if (avgmatches.size() > options.storeMatches)
                    avgmatches.removeElementAt(options.storeMatches);
            }
            if (maxmatches != null && (maxmatches.size() < options.storeMatches || maxpercent > maxmatches.lastElement().percent())
                    && maxpercent > 0) {
                maxmatches.insert(match);
                if (maxmatches.size() > options.storeMatches)
                    maxmatches.removeElementAt(options.storeMatches);
            }
            if (minmatches != null && (minmatches.size() < options.storeMatches || minpercent > minmatches.lastElement().percent())
                    && minpercent > 0) {
                minmatches.insert(match);
                if (minmatches.size() > options.storeMatches)
                    minmatches.removeElementAt(options.storeMatches);
            }
        } else { // store_percent
            if (avgpercent > options.storeMatches) {
                avgmatches.insert(match);
                if (avgmatches.size() > Options.MAX_RESULT_PAIRS)
                    avgmatches.removeElementAt(Options.MAX_RESULT_PAIRS);
            }

            if (maxmatches != null && maxpercent > options.storeMatches) {
                maxmatches.insert(match);
                if (maxmatches.size() > Options.MAX_RESULT_PAIRS)
                    maxmatches.removeElementAt(Options.MAX_RESULT_PAIRS);
            }

            if (minmatches != null && minpercent > options.storeMatches) {
                minmatches.insert(match);
                if (minmatches.size() > Options.MAX_RESULT_PAIRS)
                    minmatches.removeElementAt(Options.MAX_RESULT_PAIRS);
            }
        }
        if (options.clustering)
            options.similarity.setSimilarity(a, b, avgpercent);
    }

    private String toUTF8(String str) {
        byte[] utf8;
        utf8 = str.getBytes(StandardCharsets.UTF_8);
        return new String(utf8);
    }

    /* THE MAIN PROCEDURE */

    /**
     * **************************
     */
    public final void run() throws jplag.ExitException, IOException {
        if (options.outputFile != null) {
            try {
                writer = new FileWriter(FileUtils.getFile(options.outputFile));
                writer.write(NAME_LONG + "\n");
                writer.write(dateTimeFormat.format(new Date()) + "\n\n");
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Unable to open or write to log file: {0}", options.outputFile.replaceAll("[\r\n]",""));
                throw new ExitException("Unable to create log file!");
            }
        } else
            print(null, NAME_LONG + "\n\n");
        print(null, "Language: " + options.language.name() + "\n\n");
        if (options.originalDir == null)
            print(null, "Root-dir: " + options.rootDir + "\n"); // server
        // this file contains all files names which are excluded
        readExclusionFile();

        if (options.fileListMode) {
            createSubmissionsFileList();
        } else if (options.includeFile == null) {
            createSubmissions();
            LOGGER.log(Level.INFO, "{0} submissions", submissions.size());
        } else
            createSubmissionsExp();

        if (!options.skipParse) {
            try {
                parseAll();
                parseBasecodeSubmission();
            } catch (OutOfMemoryError e) {
                submissions = null;
                String ex = "[" + new Date() + "] OutOfMemoryError " + "during parsing of submission \"" + currentSubmissionName
                        + "\"";
                LOGGER.log(Level.SEVERE, "{0}", ex.replaceAll("[\r\n]",""));
                throw new ExitException("Out of memory during parsing of submission \"" + currentSubmissionName + "\"");
            } catch (ExitException e) {
                throw e;
            } catch (Exception e) {
                String ex = "[" + new Date() + "] Unknown exception " + "during parsing of submission \"" + currentSubmissionName
                        + "\"";
                LOGGER.log(Level.SEVERE, "{0}", ex.replaceAll("[\r\n]",""));
                LOGGER.log(Level.SEVERE, "Exception occur in run", e);
                throw new ExitException("Unknown exception during parsing of " + "submission \"" + currentSubmissionName + "\"");
            }
        } else
            print("Skipping parsing...\n", null);

        if (validSubmissions() < 2) {
            throwNotEnoughSubmissions();
        }
        errorVector = null; // errorVector is not needed anymore

        if (options.clustering) {
            clusters = new Clusters(this);
            options.similarity = new SimilarityMatrix(submissions.size());
        }

        if (options.exp) { // EXPERIMENT
            expCompare();
        } else if (options.externalSearch) {
            try {
                externalCompare();
            } catch (OutOfMemoryError e) {
                LOGGER.log(Level.SEVERE, "Exception occur in run method", e);
            }
        } else {
            if (options.compare > 0)
                specialCompare(); // compare every submission to x others
            else {
                switch (options.comparisonMode) {
                    case Options.COMPMODE_NORMAL:
                        compare();
                        break;

                    case Options.COMPMODE_REVISION:
                        revisionCompare();
                        break;

                    default:
                        throw new ExitException("Illegal comparison mode: \"" + options.comparisonMode + "\"");
                }
            }
        }
        closeWriter();

        String str = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";
        str += "<jplag_infos>\n";
        str += "<infos \n";

        String sp = "\"";
        str += " title = " + sp + toUTF8(options.getTitle()) + sp;
        str += " source = " + sp + (getOriginalDir() != null ? toUTF8(getOriginalDir()) : "") + sp;
        str += " n_of_programs = " + sp + submissions.size() + sp;
        str += " errors = " + sp + getLanguage().errorsCount() + sp;
        str += " path_to_files = " + sp + toUTF8((options.subDir != null) ? options.subDir : "") + sp;
        str += " basecode_dir = " + sp + toUTF8((options.basecode != null) ? options.basecode : "") + sp;
        str += " read_subdirs = " + sp + this.options.readSubdirs + sp;
        str += " clustertype = " + sp + this.options.getClusterTyp() + sp;
        str += " store_matches = " + sp + this.options.storeMatches + ((this.options.storePercent) ? "%" : "") + sp;
        StringBuilder suf = new StringBuilder();
        for (int s = 0; s < this.options.suffixes.length; s++)
            suf.append(",").append(this.options.suffixes[s]);
        str += " suffixes = " + sp + suf.substring(1) + sp;
        str += " language_name = " + sp + this.options.languageName + sp;
        str += " comparison_mode = " + sp + this.options.comparisonMode + sp;
        str += " country_tag = " + sp + this.options.getCountryTag() + sp;
        str += " min_token = " + sp + this.options.minTokenMatch + sp;
        str += " date = " + sp + System.currentTimeMillis() + sp;

        str += "/>\n";
        str += "</jplag_infos>";

        try (FileWriter fw = new FileWriter(FileUtils.getFile(this.options.resultDir + File.separator + "result.xml"))) {
            fw.write(str);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Unable to create result.xml");
        }
    }

    /*
     * Now the special comparison:
     */
    private void specialCompare() throws jplag.ExitException {
        File root = FileUtils.getFile(options.resultDir);
        HTMLFile f = this.report.openHTMLFile(root, "index.html");
        this.report.copyFixedFiles(root);

        this.report.writeIndexBegin(f, "Special Search Results"); // start HTML
        f.println("<P><A NAME=\"matches\"><H4>Matches:</H4><P>");

        int size = submissions.size();
        int matchIndex = 0;

        print("Comparing: ", validSubmissions() + " submissions");
        print("\n(Writing results at the same time.)\n", null);

        options.setState();
        options.setProgress();
        int i;
        int j;
        int anz = 0;
        AllMatches match;
        Submission s1;
        Submission s2;
        long msec = System.currentTimeMillis();
        for (i = 0; i < (size - 1); i++) {
            // Result vector
            SortedVector<AllMatches> matches = new SortedVector<>(new AllMatches.AvgComparator());

            s1 = submissions.get(i);
            if (s1.getStruct() == null) {
                continue;
            }
            for (j = 0; j < size; j++) {
                s2 = submissions.get(j);
                if ((i == j) || (s2.getStruct() == null)) {
                    continue;
                }

                match = this.gSTiling.compare(s1, s2);
                anz++;

                float percent = match.percent();

                if ((matches.size() < options.compare || !matches.isEmpty() || match.moreThan(matches.lastElement().percent()))
                        && match.moreThan(0)) {
                    matches.insert(match);
                    if (matches.size() > options.compare)
                        matches.removeElementAt(options.compare);
                }

                if (options.clustering)
                    options.similarity.setSimilarity(i, j, percent);

                options.setProgress();
            }

            // now output matches:
            f.println("<TABLE CELLPADDING=3 CELLSPACING=2>");
            boolean once = true;
            for (AllMatches allMatches : matches) {
                match = allMatches;
                if (once) {
                    f.println("<TR><TD BGCOLOR=" + this.report.color(match.percent(), 128, 192, 128, 192,
                            255, 255) + ">" + s1.getName()
                            + "<TD WIDTH=\"10\">-&gt;");
                    once = false;
                }

                int other = (match.subName(0).equals(s1.getName()) ? 1 : 0);
                f.println(" <TD BGCOLOR=" + this.report.color(match.percent(), 128, 192, 128, 192, 255, 255)
                        + " ALIGN=center><A HREF=\"match" + matchIndex + ".html\">" + match.subName(other) + "</A><BR><FONT COLOR=\""
                        + this.report.color(match.percent(), 0, 255, 0, 0, 0, 0) + "\"><B>(" + match.roundedPercent() + "%)</B></FONT>");
                this.report.writeMatch(root, matchIndex++, match);
            }
            f.println("</TR>");
        }
        f.println("</TABLE><P>\n");
        f.println("<!---->");
        this.report.writeIndexEnd(f);
        f.close();

        options.setProgress();
        long time = System.currentTimeMillis() - msec;
        print("\n", "Total time: " + ((time / 3600000 > 0) ? (time / 3600000) + " h " : "")
                + ((time / 60000 > 0) ? ((time / 60000) % 60000) + MIN : "") + (time / 1000 % 60) + SEC + TPC
                + (time / anz) + MSEC2);

    }

    public final boolean useClustering() {
        return this.options.clustering;
    }

    public final boolean useDebugParser() {
        return this.options.debugParser;
    }

    public final boolean useDiffReport() {
        return this.options.diffReport;
    }

    public final boolean useExternalSearch() {
        return this.options.externalSearch;
    }

    public final boolean useVerboseDetails() {
        return this.options.verboseDetails;
    }

    public final boolean useVerboseParser() {
        return this.options.verboseParser;
    }

    public final boolean useBasecode() {
        return this.options.useBasecode;
    }

    // RESULT

    /*
     * Erst wird die Existenz des Ergebnis-Verzeichnisses sichergestellt, dann
     * wird die Erstellung der Dateien durch die Klasse "Report" erledigt.
     */
    private void writeResults(int[] dist, SortedVector<AllMatches> avgmatches, SortedVector<AllMatches> maxmatches,
                              SortedVector<AllMatches> minmatches, Cluster clustering) throws jplag.ExitException {
        options.setState();
        options.setProgress();
        if (options.originalDir == null)
            print("Writing results to: " + options.resultDir + "\n", null);
        File f = FileUtils.getFile(options.resultDir);
        if (!f.exists() && !f.mkdirs()) {
            throw new jplag.ExitException("Cannot create directory!");
        }
        if (!f.isDirectory()) {
            throw new jplag.ExitException(options.resultDir + NODIR);
        }
        if (!f.canWrite()) {
            throw new jplag.ExitException("Cannot write directory: " + options.resultDir);
        }

        this.report.write(f, dist, avgmatches, maxmatches, minmatches, clustering, options);

        if (options.externalSearch)
            writeTextResult(f, avgmatches);
    }

    private void writeTextResult(File dir, SortedVector<AllMatches> matches) {
        Iterator<AllMatches> iter = matches.iterator();

        print("Writing special 'matches.txt' file\n", null);

        try {
            File f = new File(dir, "matches.txt");
            PrintWriter printWriter = new PrintWriter(new FileWriter(f));

            while (iter.hasNext()) {
                AllMatches match = iter.next();

                String file1;
                String file2;
                String tmp;
                file1 = match.subA.getName();
                file2 = match.subB.getName();

                if (file1.compareTo(file2) > 0) {
                    tmp = file2;
                    file2 = file1;
                    file1 = tmp;
                }

                printWriter.println(file1 + "\t" + file2 + "\t" + match.percent());
            }
            printWriter.close();
        } catch (IOException e) {
            print("IOException while writing file\n", null);
        }
    }
}
