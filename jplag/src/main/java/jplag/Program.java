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
    public static final String name = "JPlag" + versionProps.getProperty("version", "devel");
    public static final String name_long = "JPlag (Version " + versionProps.getProperty("version", "devel") + ")";

    public DateFormat dateFormat;
    public DateFormat dateTimeFormat;

    public String currentSubmissionName = "<Unknown submission>";
    public Vector<String> errorVector = new Vector<>();

    public void addError(String errorMsg) {
        errorVector.add("[" + currentSubmissionName + "]\n" + errorMsg);
        print(errorMsg, null);
    }

    public void print(String normal, String lng) {
        if (options.verbose_parser) {
            if (lng != null)
                myWrite(lng);
            else if (normal != null)
                myWrite(normal);
        }
        if (options.verbose_quiet)
            return;
        try {
            if (normal != null) {
                System.out.print(normal);
            }

            if (lng != null) {
                if (options.verbose_long)
                    System.out.print(lng);
            }
        } catch (Throwable e) {
            System.out.println(e.getMessage());
        }
    }

    private Submission basecodeSubmission = null;

    // Used Objects of anothers jplag.Classes ,they muss be just one time
    // instantiate
    public Clusters clusters = null;

    private int errors = 0;
    private String invalidSubmissionNames = null;

    private HashSet<String> excluded = null;

    protected GSTiling gSTiling = new GSTiling(this);

    private final Hashtable<String, AllBasecodeMatches> htBasecodeMatches = new Hashtable<>(30);

    private Vector<String> included = null;

    // experiment end

    private final jplag.options.Options options;

    public Report report;

    public Messages msg;

    private final Runtime runtime = Runtime.getRuntime();

    private Vector<Submission> submissions;

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

        report = new Report(this, get_language());
    }

    /**
     * All submission with no errors are counted. (unsure if this is still
     * necessary.)
     */
    protected int validSubmissions() {
        if (submissions == null)
            return 0;
        int size = 0;
        for (int i = submissions.size() - 1; i >= 0; i--) {
            if (!submissions.elementAt(i).errors)
                size++;
        }
        return size;
    }

    /**
     * Like the validSubmissions(), but this time all the submissions are
     * returned as a string, separated by "separator".
     */
    protected String allValidSubmissions() {
        StringBuilder res = new StringBuilder();
        int size = submissions.size();
        boolean firsterr = true;
        for (int i = 0; i < size; i++) {
            Submission subm = submissions.elementAt(i);
            if (!subm.errors) {
                res.append((!firsterr) ? " - " : "").append(subm.name);
                firsterr = false;
            }
        }
        return res.toString();
    }

    /**
     * Returns a " - " separated list of invalid submission names
     */
    protected String allInvalidSubmissions() {
        return invalidSubmissionNames;
    }

    public void closeWriter() {
        try {
            if (writer != null)
                writer.close();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Exception occur", ex);
            // ex.printStackTrace();
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

        SortedVector<AllMatches> avgmatches, maxmatches;
        int[] dist = new int[10];

        // Result vector
        avgmatches = new SortedVector<>(new AllMatches.AvgComparator());
        maxmatches = new SortedVector<>(new AllMatches.MaxComparator());

        long msec;

        AllBasecodeMatches bcmatch;
        Submission s1, s2;

        options.setState(Options.COMPARING);
        options.setProgress(0);

        if (this.options.useBasecode) {
            int countBC = 0;
            // System.out.println("BC size: "+basecodeSubmission.size());
            msec = System.currentTimeMillis();
            for (int i = 0; i < (size); i++) {
                s1 = submissions.elementAt(i);
                // System.out.println("basecode recognition for: "+s1.name);
                bcmatch = this.gSTiling.compareWithBasecode(s1, basecodeSubmission);
                htBasecodeMatches.put(s1.name, bcmatch);
                this.gSTiling.resetBaseSubmission(basecodeSubmission);
                countBC++;
                options.setProgress(countBC * 100 / size);
            }
            long timebc = System.currentTimeMillis() - msec;
            print("\n\n", "\nTime for comparing with Basecode: " + ((timebc / 3600000 > 0) ? (timebc / 3600000) + " h " : "")
                    + ((timebc / 60000 > 0) ? ((timebc / 60000) % 60000) + " min " : "") + (timebc / 1000 % 60) + " sec\n"
                    + "Time per basecode comparison: " + (timebc / size) + " msec\n\n");
        }

        //		print("\nComparing:\n", validSubmissions() + " submissions");

        int totalcomps = (size - 1) * size / 2;
        int i, j, anz = 0, count = 0;
        AllMatches match;

        options.setProgress(0);
        msec = System.currentTimeMillis();

        for (i = 0; i < (size - 1); i++) {
            s1 = submissions.elementAt(i);
            if (s1.struct == null) {
                count += (size - i - 1);
                continue;
            }

            for (j = (i + 1); j < size; j++) {
                s2 = submissions.elementAt(j);
                if (s2.struct == null) {
                    count++;
                    continue;
                }

                match = this.gSTiling.compare(s1, s2);

                anz++;

                System.out.println("Comparing " + s1.name + "-" + s2.name + ": " + match.percent());

                // histogram:
                if (options.useBasecode) {
                    match.bcmatchesA = htBasecodeMatches.get(match.subA.name);
                    match.bcmatchesB = htBasecodeMatches.get(match.subB.name);
                }

                registerMatch(match, dist, avgmatches, maxmatches, null, i, j);
                count++;
                options.setProgress(count * 100 / totalcomps);
            }
        }
        options.setProgress(100);
        long time = System.currentTimeMillis() - msec;

        print("\n", "Total time for comparing submissions: " + ((time / 3600000 > 0) ? (time / 3600000) + " h " : "")
                + ((time / 60000 > 0) ? ((time / 60000) % 60000) + " min " : "") + (time / 1000 % 60) + " sec\n" + "Time per comparison: "
                + (time / anz) + " msec\n");

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

        SortedVector<AllMatches> avgmatches, maxmatches, minmatches;
        int[] dist = new int[10];

        // Result vector
        avgmatches = new SortedVector<>(new AllMatches.AvgReversedComparator());
        maxmatches = new SortedVector<>(new AllMatches.MaxReversedComparator());
        minmatches = new SortedVector<>(new AllMatches.MinReversedComparator());

        long msec;

        AllBasecodeMatches bcmatch;
        Submission s1, s2;

        options.setState(Options.COMPARING);
        options.setProgress(0);

        if (options.useBasecode) {
            msec = System.currentTimeMillis();
            for (int i = 0; i < size; i++) {
                s1 = submissions.elementAt(i);
                bcmatch = gSTiling.compareWithBasecode(s1, basecodeSubmission);
                htBasecodeMatches.put(s1.name, bcmatch);
                gSTiling.resetBaseSubmission(basecodeSubmission);
                options.setProgress((i + 1) * 100 / size);
            }
            long timebc = System.currentTimeMillis() - msec;
            print("\n\n", "\nTime for comparing with Basecode: " + ((timebc / 3600000 > 0) ? (timebc / 3600000) + " h " : "")
                    + ((timebc / 60000 > 0) ? ((timebc / 60000) % 60000) + " min " : "") + (timebc / 1000 % 60) + " sec\n"
                    + "Time per basecode comparison: " + (timebc / size) + " msec\n\n");
        }

        int totalcomps = size - 1;
        int anz = 0, count = 0;
        AllMatches match;

        options.setProgress(0);
        msec = System.currentTimeMillis();

        s1loop:
        for (int i = 0; i < size - 1; ) {
            s1 = submissions.elementAt(i);
            if (s1.struct == null) {
                count++;
                continue;
            }

            // Find next valid submission
            int j = i;
            do {
                j++;
                if (j >= size)
                    break s1loop; // no more comparison pairs available
                s2 = submissions.elementAt(j);
            } while (s2.struct == null);

            match = this.gSTiling.compare(s1, s2);

            anz++;

            /*
             * System.out.println("Comparing "+s1.name+"-"+s2.name+": "+
             * match.percent());
             */
            // histogram:
            if (options.useBasecode) {
                match.bcmatchesA = htBasecodeMatches.get(match.subA.name);
                match.bcmatchesB = htBasecodeMatches.get(match.subB.name);
            }

            registerMatch(match, dist, avgmatches, maxmatches, minmatches, i, j);
            count++;
            options.setProgress(count * 100 / totalcomps);

            i = j;
        }
        options.setProgress(100);
        long time = System.currentTimeMillis() - msec;

        print("\n", "Total time for comparing submissions: " + ((time / 3600000 > 0) ? (time / 3600000) + " h " : "")
                + ((time / 60000 > 0) ? ((time / 60000) % 60000) + " min " : "") + (time / 1000 % 60) + " sec\n" + "Time per comparison: "
                + (time / anz) + " msec\n");

        Cluster cluster = null;
        if (options.clustering)
            cluster = this.clusters.calculateClustering(submissions);

        writeResults(dist, avgmatches, maxmatches, minmatches, cluster);
    }

    private void createSubmissions() throws jplag.ExitException {
        submissions = new Vector<>();
        File f = FileUtils.getFile(options.root_dir);
        if (f == null || !f.isDirectory()) {
            throw new jplag.ExitException("\"" + options.root_dir + "\" is not a directory!");
        }
        String[] list;
        try {
            list = f.list();
        } catch (SecurityException e) {
            throw new jplag.ExitException("Unable to retrieve directory: " + options.root_dir + " Cause : " + e);
        }

        if(list != null){
            Arrays.sort(list);
        }

        if(list != null) {
            for (String s : list) {
                File subm_dir = FileUtils.getFile(f, s);
                if (!subm_dir.isDirectory()) {
                    if (options.sub_dir != null)
                        continue;

                    boolean ok = false;
                    String name = subm_dir.getName();
                    for (int j = 0; j < options.suffixes.length; j++)
                        if (name.endsWith(options.suffixes[j])) {
                            ok = true;
                            break;
                        }

                    if (!ok)
                        continue;

                    submissions.addElement(new Submission(name, f, this, get_language()));
                    continue;
                }
                if (options.exp && excludeFile(subm_dir.toString())) { // EXPERIMENT
                    // !!
                    System.err.println("excluded: " + subm_dir);
                    continue;
                }

                File file_dir = ((options.sub_dir == null) ? // - S option
                        subm_dir
                        : FileUtils.getFile(subm_dir, options.sub_dir));
                if (file_dir.isDirectory()) {
                    if (options.basecode.equals(subm_dir.getName())) {
                        basecodeSubmission = new Submission(subm_dir.getName(), file_dir, options.read_subdirs, this, get_language());
                    } else {
                        submissions.addElement(new Submission(subm_dir.getName(), file_dir, options.read_subdirs, this, get_language())); // -s
                    }
                } else
                    throw new ExitException("Cannot find directory: " + file_dir);
            }
        }
    }

	private void createSubmissionsFileList() throws jplag.ExitException {
		submissions = new Vector<>();
		File f = null;
		if (options.root_dir != null) {
			f = FileUtils.getFile(options.root_dir);
			if (!f.isDirectory()) {
				throw new jplag.ExitException(options.root_dir + " is not a directory!");
			}
		}
		for (String file : options.fileList){
			submissions.addElement(new Submission(file, f, this, get_language()));
		}
	}


    /**
     * THIS IS FOR THE EMPIRICAL STUDY
     */
    private void createSubmissionsExp() throws jplag.ExitException {
        // ES IST SICHER, DASS EIN INCLUDE-FILE ANGEGEBEN WURDE!
        readIncludeFile();
        submissions = new Vector<>();
        File f = FileUtils.getFile(options.root_dir);
        if (f == null || !f.isDirectory()) {
            throw new jplag.ExitException(options.root_dir + " is not a directory!");
        }
        String[] list = new String[included.size()];
        included.copyInto(list);
        for (String s : list) {
            File subm_dir = FileUtils.getFile(f, s);
            if (subm_dir == null || !subm_dir.isDirectory())
                continue;
            if (options.exp && excludeFile(subm_dir.toString())) { // EXPERIMENT
                // !!
                System.err.println("excluded: " + subm_dir);
                continue;
            }
            File file_dir = ((options.sub_dir == null) ? // - S option
                    subm_dir
                    : FileUtils.getFile(subm_dir, options.sub_dir));
            if (file_dir != null && file_dir.isDirectory())
                submissions.addElement(new Submission(subm_dir.getName(), file_dir, options.read_subdirs, this, this.get_language())); // -s
            else if (options.sub_dir == null) {
                throw new ExitException(options.root_dir + " is not a directory!");
            }
        }
    }

    /**
     * Check if a file is excluded or not
     */
    protected boolean excludeFile(String file) {
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
        int i, j, count = 0;
        Submission s1, s2;
        AllMatches match;
        long msec = System.currentTimeMillis();
        for (i = 0; i < (anzSub - 1); i++) {
            s1 = submissions.elementAt(i);
            if (s1.struct == null)
                continue;
            for (j = (i + 1); j < anzSub; j++) {
                s2 = submissions.elementAt(j);
                if (s2.struct == null)
                    continue;

                match = this.gSTiling.compare(s1, s2);
                similarity[count++] = (int) match.percent();
            }
        }
        long time = System.currentTimeMillis() - msec;
        // output
        System.out.print(options.root_dir + " ");
        System.out.print(options.min_token_match + " ");
        System.out.print(options.filtername + " ");
        System.out.print((time) + " ");
        for (i = 0; i < similarity.length; i++)
            System.out.print(similarity[i] + " ");
        System.out.println();
    }

    /**
     * This is the special external comparison routine
     */
    private void externalCompare() throws jplag.ExitException {
        long size = submissions.size();
        // Progress progress;
        // Result vector
        SortedVector<AllMatches> avgmatches = new SortedVector<>(new AllMatches.AvgComparator());
        SortedVector<AllMatches> maxmatches = new SortedVector<>(new AllMatches.MaxComparator());
        int[] dist = new int[10];

        print("Comparing: " + size + " submissions\n", null);
        options.setState(Options.COMPARING);
        options.setProgress(0);
        long totalComparisons = (size * (size - 1)) / 2, count = 0, comparisons = 0;
        int index;
        AllMatches match;
        Submission s1, s2;
        long remain;
        String totalTimeStr, remainTime;

        print("Checking memory size...\n", null);
        // First try to load as many submissions as possible
        index = fillMemory(0, (int)size);

        long startTime;
        long totalTime = 0;
        int startA = 0;
        int endA = index / 2;
        int startB = endA + 1;
        int endB = index;
        int i, j;
        //		long progStart;

        do {
            // compare A to A
            startTime = System.currentTimeMillis();
            print("Comparing block A (" + startA + "-" + endA + ") to block A\n", null);
            for (i = startA; i <= endA; i++) {
                //        progress.set(count - progStart);
                options.setProgress((int) (count * 100 / totalComparisons));
                s1 = submissions.elementAt(i);
                if (s1.struct == null) {
                    count += (endA - i);
                    continue;
                }
                for (j = (i + 1); j <= endA; j++) {
                    s2 = submissions.elementAt(j);
                    if (s2.struct == null) {
                        count++;
                        continue;
                    }

                    match = this.gSTiling.compare(s1, s2);
                    registerMatch(match, dist, avgmatches, maxmatches, null, i, j);
                    comparisons++;
                    count++;
                }
            }
            //      progress.set(count - progStart);
            options.setProgress((int) (count * 100 / totalComparisons));
            print("\n", null);
            totalTime += System.currentTimeMillis() - startTime;

            // Are we finished?
            if (startA == startB)
                break;

            do {
                totalTimeStr = "" + ((totalTime / 3600000 > 0) ? (totalTime / 3600000) + " h " : "")
                        + ((totalTime / 60000 > 0) ? ((totalTime / 60000) % 60) + " min " : "") + (totalTime / 1000 % 60) + " sec";
                if (comparisons != 0)
                    remain = totalTime * (totalComparisons - count) / comparisons;
                else
                    remain = 0;
                remainTime = "" + ((remain / 3600000 > 0) ? (remain / 3600000) + " h " : "")
                        + ((remain / 60000 > 0) ? ((remain / 60000) % 60) + " min " : "") + (remain / 1000 % 60) + " sec";

                print("Progress: " + (100 * count) / totalComparisons + "%\nTime used for comparisons: " + totalTimeStr
                        + "\nRemaining time (estimate): " + remainTime + "\n", null);

                // compare A to B
                startTime = System.currentTimeMillis();
                print("Comparing block A (" + startA + "-" + endA + ") to block B (" + startB + "-" + endB + ")\n", null);
                for (i = startB; i <= endB; i++) {
                    //          progress.set(count - progStart);
                    options.setProgress((int) (count * 100 / totalComparisons));
                    s1 = submissions.elementAt(i);
                    if (s1.struct == null) {
                        count += (endA - startA + 1);
                        continue;
                    }
                    for (j = startA; j <= endA; j++) {
                        s2 = submissions.elementAt(j);
                        if (s2.struct == null) {
                            count++;
                            continue;
                        }

                        match = this.gSTiling.compare(s1, s2);
                        registerMatch(match, dist, avgmatches, maxmatches, null, i, j);
                        comparisons++;
                        count++;
                    }
                    s1.struct = null; // remove B
                }
                //        progress.set(count - progStart);
                options.setProgress((int) (count * 100 / totalComparisons));
                print("\n", null);
                totalTime += System.currentTimeMillis() - startTime;

                if (endB == size - 1) {
                    totalTimeStr = "" + ((totalTime / 3600000 > 0) ? (totalTime / 3600000) + " h " : "")
                            + ((totalTime / 60000 > 0) ? ((totalTime / 60000) % 60) + " min " : "") + (totalTime / 1000 % 60) + " sec";
                    remain = totalTime * (totalComparisons - count) / comparisons;
                    remainTime = "" + ((remain / 3600000 > 0) ? (remain / 3600000) + " h " : "")
                            + ((remain / 60000 > 0) ? ((remain / 60000) % 60) + " min " : "") + (remain / 1000 % 60) + " sec";

                    print("Progress: " + (100 * count) / totalComparisons + "%\nTime used for comparisons: " + totalTimeStr
                            + "\nRemaining time (estimate): " + remainTime + "\n", null);
                    break;
                }

                // Remove B -> already done...
                runtime.runFinalization();
                runtime.gc();
                Thread.yield();
                // Try to find the next B
                print("Finding next B\n", null);

                index = fillMemory(endB + 1, (int)size);

                startB = endB + 1;
                endB = index;

            } while (true);

            // Remove A
            for (i = startA; i <= endA; i++)
                submissions.elementAt(i).struct = null;
            runtime.runFinalization();
            runtime.gc();
            Thread.yield();
            print("Find next A.\n", null);
            // First try to load as many submissions as possible

            index = fillMemory(endA + 1, (int)size);

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
                + ((totalTime / 60000 > 0) ? ((totalTime / 60000) % 60000) + " min " : "") + (totalTime / 1000 % 60) + " sec";

        print("Total comparison time: " + totalTimeStr + "\nComparisons: " + count + "/" + comparisons + "/" + totalComparisons + "\n",
                null);

        // free remaining memory
        for (i = startA; i <= endA; i++)
            submissions.elementAt(i).struct = null;
        runtime.runFinalization();
        runtime.gc();
        Thread.yield();

        // System.out.println("Matrix:\n"+ similarity);
        Cluster cluster = null;
        if (options.clustering)
            cluster = this.clusters.calculateClustering(submissions);

        writeResults(dist, avgmatches, maxmatches, null, cluster);
    }

    private int fillMemory(int from, int size) {
        Submission sub = null;
        int index = from;

        runtime.runFinalization();
        runtime.gc();
        Thread.yield();
        long freeBefore = runtime.freeMemory();
        try {
            for (; index < size; index++) {
                sub = submissions.elementAt(index);
                sub.struct = new Structure();
                if (!sub.struct.load(FileUtils.getFile("temp", sub.dir.getName() + sub.name)))
                    sub.struct = null;
            }
        } catch (java.lang.OutOfMemoryError e) {
            if(sub != null){
                sub.struct = null;
            }
            print("Memory overflow after loading " + (index - from + 1) + " submissions.\n", null);
        }
        if (index >= size)
            index = size - 1;

        if (freeBefore / runtime.freeMemory() <= 2)
            return index;
        for (int i = (index - from) / 2; i > 0; i--) {
            submissions.elementAt(index--).struct = null;
        }
        runtime.runFinalization();
        runtime.gc();
        Thread.yield();

        // make sure we freed half of the "available" memory.
        long free;
        while (freeBefore / (free = runtime.freeMemory()) > 2) {
            submissions.elementAt(index--).struct = null;
            runtime.runFinalization();
            runtime.gc();
            Thread.yield();
        }
        print(free / 1024 / 1024 + "MByte freed. Current index: " + index + "\n", null);

        return index;
    }

    public String get_basecode() {
        return this.options.basecode;
    }

    public int get_clusterType() {
        return this.options.clusterType;
    }

    public String getCountryTag() {
        return options.getCountryTag();
    }

    /*
     * Distribution: Program given away to:
     *
     * 0: Server version
     *
     * 1: David Klausner 2: Ronald Kostoff 3: Bob Carlson 4: Neville Newman
     */

    public Language get_language() {
        return this.options.language;
    }

    public int get_min_token_match() {
        return this.options.min_token_match;
    }

    public String get_title() {
        return this.options.title;
    }

    public String get_original_dir() {
        return this.options.original_dir;
    }

    public SimilarityMatrix get_similarity() {
        return this.options.similarity;
    }

    public String get_sub_dir() {
        return this.options.sub_dir;
    }

    public String[] get_suffixes() {
        return this.options.suffixes;
    }

    public int[] get_themewords() {
        return this.options.themewords;
    }

    public float[] get_threshold() {
        return this.options.threshold;
    }

    public int getErrors() {
        return errors;
    }

    private void makeTempDir() throws jplag.ExitException {
        print(null, "Creating temporary dir.\n");
        File f = new File("temp");
        if (!f.exists()) {
            if (!f.mkdirs()) {
                throw new jplag.ExitException("Cannot create temporary directory!");
            }
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
                LOGGER.log(Level.SEVERE, "Exception occur", e);
                // e.printStackTrace();
            }
        } else
            System.out.print(str);
    }

    // PARSE
    /*
     * Compiles all "submissions"
     */
    private void parseAll() throws jplag.ExitException {
        if (submissions == null) {
            System.out.println("  Nothing to parse!");
            return;
        }
        // lets go:)
        int count = 0;
        int totalcount = submissions.size();
        options.setState(Options.PARSING);
        options.setProgress(0);
        long msec = System.currentTimeMillis();
        Iterator<Submission> iter = submissions.iterator();

        if (options.externalSearch)
            makeTempDir();
        int invalid = 0;
        while (iter.hasNext()) {
            boolean ok;
            boolean removed = false;
            Submission subm = iter.next();
            print(null, "------ Parsing submission: " + subm.name + "\n");
            currentSubmissionName = subm.name;
            options.setProgress(count * 100 / totalcount);
            if (!(ok = subm.parse()))
                errors++;

            if (options.exp && options.filter != null)
                subm.struct = options.filter.filter(subm.struct); // EXPERIMENT
            count++;
            if (subm.struct != null && subm.size() < options.min_token_match) {
                print(null, "Submission contains fewer tokens than minimum match " + "length allows!\n");
                subm.struct = null;
                invalid++;
                removed = true;
            }
            if (options.externalSearch) {
                if (subm.struct != null) {
                    this.gSTiling.create_hashes(subm.struct, options.min_token_match, false);
                    subm.struct.save(FileUtils.getFile("temp", subm.dir.getName() + subm.name));
                    subm.struct = null;
                }
            }
            if (!options.externalSearch && subm.struct == null) {
                invalidSubmissionNames = (invalidSubmissionNames == null) ? subm.name : invalidSubmissionNames + " - " + subm.name;
                iter.remove();
            }
            if (ok && !removed)
                print(null, "OK\n");
            else
                print(null, "ERROR -> Submission removed\n");
        }

        options.setProgress(100);
        print("\n" + (count - errors - invalid) + " submissions parsed successfully!\n" + errors + " parser error"
                + (errors != 1 ? "s!\n" : "!\n"), null);
        if (invalid != 0) {
            print(null, invalid
                    + ((invalid == 1) ? " submission is not valid because it contains" : " submissions are not valid because they contain")
                    + " fewer tokens\nthan minimum match length allows.\n");
        }
        long time = System.currentTimeMillis() - msec;
        print("\n\n", "\nTotal time for parsing: " + ((time / 3600000 > 0) ? (time / 3600000) + " h " : "")
                + ((time / 60000 > 0) ? ((time / 60000) % 60000) + " min " : "") + (time / 1000 % 60) + " sec\n"
                + "Time per parsed submission: " + (count > 0 ? (time / count) : "n/a") + " msec\n\n");
    }

    private void parseBasecodeSubmission() throws jplag.ExitException {
        Submission subm = basecodeSubmission;
        if (subm == null) {
            options.useBasecode = false;
            return;
        }
        long msec = System.currentTimeMillis();
        print("----- Parsing basecode submission: " + subm.name + "\n", null);

        // lets go:
        if (options.externalSearch)
            makeTempDir();

        if (!subm.parse())
            throwBadBasecodeSubmission();

        if (options.exp && options.filter != null)
            subm.struct = options.filter.filter(subm.struct); // EXPERIMENT

        if (subm.struct != null && subm.size() < options.min_token_match)
            throw new ExitException("Basecode submission contains fewer tokens " + "than minimum match length allows!\n");

        if (options.useBasecode)
            gSTiling.create_hashes(subm.struct, options.min_token_match, true);
        if (options.externalSearch) {
            if (subm.struct != null) {
                gSTiling.create_hashes(subm.struct, options.min_token_match, false);
                subm.struct.save(FileUtils.getFile("temp", subm.dir.getName() + subm.name));
                subm.struct = null;
            }
        }

        print("\nBasecode submission parsed!\n", null);
        long time = System.currentTimeMillis() - msec;
        print("\n", "\nTime for parsing Basecode: " + ((time / 3600000 > 0) ? (time / 3600000) + " h " : "")
                + ((time / 60000 > 0) ? ((time / 60000) % 60000) + " min " : "") + (time / 1000 % 60) + " sec\n");
    }

    // Excluded files:

    /*
     * If an exclusion file is given, it is read in and all stings are saved in
     * the set "excluded".
     */
    private void readExclusionFile() {
        if (options.exclude_file == null)
            return;
        excluded = new HashSet<>();

        try {
            BufferedReader in = IOUtils.buffer(new FileReader(FileUtils.getFile(options.exclude_file)));
            String line;
            while ((line = in.readLine()) != null) {
                excluded.add(line.trim());
            }
            in.close();
        } catch (FileNotFoundException e) {
            System.out.println("Exclusion file not found: " + options.exclude_file);
        } catch (IOException ignored) {
        }
        print(null, "Excluded files:\n");
        if (options.verbose_long) {
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
        if (options.include_file == null)
            return;
        included = new Vector<>();
        try {
            BufferedReader in = new BufferedReader(new FileReader(FileUtils.getFile(options.include_file)));
            String line;
            while ((line = in.readLine()) != null) {
                included.addElement(line.trim());
            }
            in.close();
        } catch (FileNotFoundException e) {
            System.out.println("Include file not found: " + options.include_file);
        } catch (IOException ignored) {
        }
        print(null, "Included dirs:\n");
        if (options.verbose_long) {
            Enumeration<String> enum1 = included.elements();
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
        if (!options.store_percent) {
            if ((avgmatches.size() < options.store_matches || avgpercent > avgmatches.lastElement().percent()) && avgpercent > 0) {
                avgmatches.insert(match);
                if (avgmatches.size() > options.store_matches)
                    avgmatches.removeElementAt(options.store_matches);
            }
            if (maxmatches != null && (maxmatches.size() < options.store_matches || maxpercent > maxmatches.lastElement().percent())
                    && maxpercent > 0) {
                maxmatches.insert(match);
                if (maxmatches.size() > options.store_matches)
                    maxmatches.removeElementAt(options.store_matches);
            }
            if (minmatches != null && (minmatches.size() < options.store_matches || minpercent > minmatches.lastElement().percent())
                    && minpercent > 0) {
                minmatches.insert(match);
                if (minmatches.size() > options.store_matches)
                    minmatches.removeElementAt(options.store_matches);
            }
        } else { // store_percent
            if (avgpercent > options.store_matches) {
                avgmatches.insert(match);
                if (avgmatches.size() > Options.MAX_RESULT_PAIRS)
                    avgmatches.removeElementAt(Options.MAX_RESULT_PAIRS);
            }

            if (maxmatches != null && maxpercent > options.store_matches) {
                maxmatches.insert(match);
                if (maxmatches.size() > Options.MAX_RESULT_PAIRS)
                    maxmatches.removeElementAt(Options.MAX_RESULT_PAIRS);
            }

            if (minmatches != null && minpercent > options.store_matches) {
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
    public void run() throws jplag.ExitException {
        if (options.output_file != null) {
            try {
                writer = new FileWriter(FileUtils.getFile(options.output_file));
                writer.write(name_long + "\n");
                writer.write(dateTimeFormat.format(new Date()) + "\n\n");
            } catch (IOException ex) {
                System.out.println("Unable to open or write to log file: " + options.output_file);
                throw new ExitException("Unable to create log file!");
            }
        } else
            print(null, name_long + "\n\n");
        print(null, "Language: " + options.language.name() + "\n\n");
        if (options.original_dir == null)
            print(null, "Root-dir: " + options.root_dir + "\n"); // server
        // this file contains all files names which are excluded
        readExclusionFile();

        if (options.fileListMode) {
	        createSubmissionsFileList();
        } else if (options.include_file == null) {
            createSubmissions();
            System.out.println(submissions.size() + " submissions");
        } else
            createSubmissionsExp();

        if (!options.skipParse) {
            try {
                parseAll();
                System.gc();
                parseBasecodeSubmission();
            } catch (OutOfMemoryError e) {
                submissions = null;
                System.gc();
                System.out.println("[" + new Date() + "] OutOfMemoryError " + "during parsing of submission \"" + currentSubmissionName
                        + "\"");
                throw new ExitException("Out of memory during parsing of submission \"" + currentSubmissionName + "\"");
            } catch (ExitException e) {
                throw e;
            } catch (Throwable e) {
                System.out.println("[" + new Date() + "] Unknown exception " + "during parsing of submission \"" + currentSubmissionName
                        + "\"");
                // e.printStackTrace();
                LOGGER.log(Level.SEVERE, "Exception occur", e);
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
        System.gc();
        if (options.exp) { // EXPERIMENT
            expCompare();
        } else if (options.externalSearch) {
            try {
                externalCompare();
            } catch (OutOfMemoryError e) {
               // e.printStackTrace();
                LOGGER.log(Level.SEVERE, "Exception occur", e);
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
        str += " source = " + sp + (get_original_dir() != null ? toUTF8(get_original_dir()) : "") + sp;
        str += " n_of_programs = " + sp + submissions.size() + sp;
        str += " errors = " + sp + get_language().errorsCount() + sp;
        str += " path_to_files = " + sp + toUTF8((options.sub_dir != null) ? options.sub_dir : "") + sp;
        str += " basecode_dir = " + sp + toUTF8((options.basecode != null) ? options.basecode : "") + sp;
        str += " read_subdirs = " + sp + this.options.read_subdirs + sp;
        str += " clustertype = " + sp + this.options.getClusterTyp() + sp;
        str += " store_matches = " + sp + this.options.store_matches + ((this.options.store_percent) ? "%" : "") + sp;
        StringBuilder suf = new StringBuilder();
        for (int s = 0; s < this.options.suffixes.length; s++)
            suf.append(",").append(this.options.suffixes[s]);
        str += " suffixes = " + sp + suf.substring(1) + sp;
        str += " language_name = " + sp + this.options.languageName + sp;
        str += " comparison_mode = " + sp + this.options.comparisonMode + sp;
        str += " country_tag = " + sp + this.options.getCountryTag() + sp;
        str += " min_token = " + sp + this.options.min_token_match + sp;
        str += " date = " + sp + System.currentTimeMillis() + sp;

        str += "/>\n";
        str += "</jplag_infos>";

        try {
            FileWriter fw = new FileWriter(FileUtils.getFile(this.options.result_dir + File.separator + "result.xml"));
            fw.write(str);
            fw.close();
        } catch (IOException ex) {
            System.out.println("Unable to create result.xml");
        }
    }

    /*
     * Now the special comparison:
     */
    private void specialCompare() throws jplag.ExitException {
        File root = FileUtils.getFile(options.result_dir);
        HTMLFile f = this.report.openHTMLFile(root, "index.html");
        this.report.copyFixedFiles(root);

        this.report.writeIndexBegin(f, "Special Search Results"); // start HTML
        f.println("<P><A NAME=\"matches\"><H4>Matches:</H4><P>");

        int size = submissions.size();
        int matchIndex = 0;

        print("Comparing: ", validSubmissions() + " submissions");
        print("\n(Writing results at the same time.)\n", null);

        options.setState(Options.COMPARING);
        options.setProgress(0);
        int totalcomps = size * size;
        int i, j, anz = 0, count = 0;
        AllMatches match;
        Submission s1, s2;
        long msec = System.currentTimeMillis();
        for (i = 0; i < (size - 1); i++) {
            // Result vector
            SortedVector<AllMatches> matches = new SortedVector<>(new AllMatches.AvgComparator());

            s1 = submissions.elementAt(i);
            if (s1.struct == null) {
                count += (size - 1);
                continue;
            }
            for (j = 0; j < size; j++) {
                s2 = submissions.elementAt(j);
                if ((i == j) || (s2.struct == null)) {
                    count++;
                    continue;
                }

                match = this.gSTiling.compare(s1, s2);
                anz++;

                float percent = match.percent();

                if ((matches.size() < options.compare || matches.size() == 0 || match.moreThan(matches.lastElement().percent()))
                        && match.moreThan(0)) {
                    matches.insert(match);
                    if (matches.size() > options.compare)
                        matches.removeElementAt(options.compare);
                }

                if (options.clustering)
                    options.similarity.setSimilarity(i, j, percent);

                count++;
                options.setProgress(count * 100 / totalcomps);
            }

            // now output matches:
            f.println("<TABLE CELLPADDING=3 CELLSPACING=2>");
            boolean once = true;
            for (AllMatches allMatches : matches) {
                match = allMatches;
                if (once) {
                    f.println("<TR><TD BGCOLOR=" + this.report.color(match.percent(), 128, 192, 128, 192, 255, 255) + ">" + s1.name
                            + "<TD WIDTH=\"10\">-&gt;");
                    once = false;
                }

                int other = (match.subName(0).equals(s1.name) ? 1 : 0);
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

        options.setProgress(100);
        long time = System.currentTimeMillis() - msec;
        print("\n", "Total time: " + ((time / 3600000 > 0) ? (time / 3600000) + " h " : "")
                + ((time / 60000 > 0) ? ((time / 60000) % 60000) + " min " : "") + (time / 1000 % 60) + " sec\n" + "Time per comparison: "
                + (time / anz) + " msec\n");

    }

    public boolean use_clustering() {
        return this.options.clustering;
    }

    public boolean use_debugParser() {
        return this.options.debugParser;
    }

    public boolean use_diff_report() {
        return this.options.diff_report;
    }

    public boolean use_externalSearch() {
        return this.options.externalSearch;
    }

    public boolean use_verbose_details() {
        return this.options.verbose_details;
    }

    public boolean use_verbose_parser() {
        return this.options.verbose_parser;
    }

    public boolean useBasecode() {
        return this.options.useBasecode;
    }

    // RESULT

    /*
     * Erst wird die Existenz des Ergebnis-Verzeichnisses sichergestellt, dann
     * wird die Erstellung der Dateien durch die Klasse "Report" erledigt.
     */
    private void writeResults(int[] dist, SortedVector<AllMatches> avgmatches, SortedVector<AllMatches> maxmatches,
                              SortedVector<AllMatches> minmatches, Cluster clustering) throws jplag.ExitException {
        options.setState(Options.GENERATING_RESULT_FILES);
        options.setProgress(0);
        if (options.original_dir == null)
            print("Writing results to: " + options.result_dir + "\n", null);
        File f = FileUtils.getFile(options.result_dir);
        if (!f.exists())
            if (!f.mkdirs()) {
                throw new jplag.ExitException("Cannot create directory!");
            }
        if (!f.isDirectory()) {
            throw new jplag.ExitException(options.result_dir + " is not a directory!");
        }
        if (!f.canWrite()) {
            throw new jplag.ExitException("Cannot write directory: " + options.result_dir);
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
            PrintWriter writer = new PrintWriter(new FileWriter(f));

            while (iter.hasNext()) {
                AllMatches match = iter.next();

                String file1, file2, tmp;
                file1 = match.subA.name;
                file2 = match.subB.name;

                if (file1.compareTo(file2) > 0) {
                    tmp = file2;
                    file2 = file1;
                    file1 = tmp;
                }

                writer.println(file1 + "\t" + file2 + "\t" + match.percent());
            }
            writer.close();
        } catch (IOException e) {
            print("IOException while writing file\n", null);
        }
    }
}
