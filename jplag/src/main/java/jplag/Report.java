package jplag;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

import jplag.clustering.Cluster;
import jplag.clustering.ThemeGenerator;
import jplag.options.Options;
import jplag.options.util.Messages;
import jplag.options.util.TagParser;
import org.apache.commons.io.*;

import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * This class writes all the HTML pages
 */
public class Report implements TokenConstants {
    private final Program program;
    private final Messages msg;
    private File root;
    private int[] dist;
    private Options options;
    SortedVector<AllMatches> avgmatches;
    SortedVector<AllMatches> maxmatches;
    SortedVector<AllMatches> minmatches;
    private static final String TDTR = "</TD></TR>";
    private static final String HTML = ".html\">";
    private static final String RCR = "Report.Clustering_Results";
    private static final String AAAAFF = "<TR BGCOLOR=#aaaaff VALIGN=top><TD>";
    private static final String FFFFFF = "<BODY BGCOLOR=\"#ffffff\">";
    private static final String RD = "Report.Directory";
    private static final String CODEBIG = "</CODE></BIG></BIG></TD></TR>";
    private static final String BIG = "<BIG><BIG>";
    private static final String TD = ":</TD><TD>";
    private static final String BR = ")<br>";
    private static final String RT = "Report.Treshold";
    private static final String BHTML = "</BODY>\n</HTML>";
    private static final String HTML_FILE = ".html";
    private static final String CENTER = "</CENTER></H3><HR>";
    private static final String HRCENTER = "<HR>\n<H3><CENTER>";
    private static final String PRE = "<PRE>";
    private static final String H2C = ")</H2>";
    private static final String BH2 = "<BODY>\n<H2>";
    private static final String AH3C = "</A></H3>";
    private static final String PREC = "</PRE>";
    private static final String MC = "match";
    private static final String FRAME_MC = "  <FRAME SRC=\"match";
    private static final String FONT_COLOR = "<FONT color=\"";
    private static final String AC = "\"></A>";
    private static final String FONTC = "</FONT>";
    private static final String A_NAME = "<A NAME=\"";
    private static final String BC_FONTC = "</B></FONT>";

    Map<AllMatches, Integer> matchesIndexMap = new HashMap<>();
    int curMatchIndex = 0;

    // how much did we save?
    private final Language language;

    private static final Logger LOGGER = Logger.getLogger(Report.class.getName());

    public Report(Program program, Language language) {
        this.program = program;
        this.language = language;
        this.msg = program.getMsg();
    }

    public void write(File f, int[] dist, SortedVector<AllMatches> avgmatches, SortedVector<AllMatches> maxmatches,
                      SortedVector<AllMatches> minmatches, Cluster clustering, Options options) throws jplag.ExitException {
        root = f;
        this.dist = dist;
        this.avgmatches = avgmatches;
        this.maxmatches = maxmatches;
        this.minmatches = minmatches;
        this.options = options;

        writeIndex((clustering != null));

        if (this.program.useClustering())
            writeClusters(clustering);

        copyFixedFiles(f);
        writeMatches(avgmatches);
        if (maxmatches != null)
            writeMatches(maxmatches);
        if (minmatches != null)
            writeMatches(minmatches);
    }

    // open file
    public HTMLFile openHTMLFile(File root, String name) throws ExitException {
        if (!root.exists() && !root.mkdirs()) {
            throw new jplag.ExitException("Cannot create directory!");
        }
        if (!root.isDirectory()) {
            throw new jplag.ExitException(root + " is not a directory!");
        }
        if (!root.canWrite()) {
            throw new jplag.ExitException("Cannot write directory: " + root);
        }
        // now the actual file creation:
        File f = FileUtils.getFile(root, name);
        HTMLFile res;
        try {
            res = HTMLFile.createHTMLFile(f);
        } catch (IOException e) {
            throw new jplag.ExitException("Error opening file: " + f.toString());
        }
        return res;
    }

    public void writeHTMLHeader(HTMLFile file, String title) {
        file.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
        file.println("<HTML><HEAD><TITLE>" + title + "</TITLE>");
        file.println("<META http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
        file.println("</HEAD>");
    }

    public void writeHTMLHeaderWithScript(HTMLFile file, String title) {
        file.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
        file.println("<HTML>\n<HEAD>\n <TITLE>" + title + "</TITLE>");
        file.println("<META http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
        file.println("  <script type=\"text/javascript\">\n  <!--");
        file.println("   function ZweiFrames(URL1,F1,URL2,F2)\n   {");
        file.println("    parent.frames[F1].location.href=URL1;");
        file.println("    parent.frames[F2].location.href=URL2;\n   }\n  //-->");
        file.println("  </script>\n</HEAD>");
    }

    private void writeDistribution(HTMLFile f) {
        // Die Verteilung:
        int max = 0;
        for (int i = 0; i < 10; i++)
            if (dist[i] > max)
                max = dist[i];
        f.println("<H4>" + this.msg.getString("Report.Distribution") + ":</H4>\n<CENTER>");
        f.println("<TABLE CELLPADDING=1 CELLSPACING=1>");
        for (int i = 9; i >= 0; i--) {
            f.print("<TR BGCOLOR=" + color(i * 10 + (float) 10, 128, 192, 128, 192, 255, 255) + "><TD ALIGN=center>" + (i * 10) + "% - "
                    + (i * 10 + 10) + "%" + "</TD><TD ALIGN=right>" + dist[i] + "</TD><TD>");
            //  INDEX
            int barLength = 75;
            if (max != 0) {
                for (int j = (dist[i] * barLength / max); j > 0; j--) {
                    f.print("#");
                }
            }
            if (max != 0 && (dist[i] * barLength / max == 0)) {
                if (dist[i] == 0)
                    f.print(".");
                else
                    f.print("#");
            }
            f.println(TDTR);
        }
        f.println("</TABLE></CENTER>\n<P>\n<HR>");
    }

    private int getMatchIndex(AllMatches match) {
        Integer obj = matchesIndexMap.get(match);
        if (obj == null) {
            matchesIndexMap.put(match, curMatchIndex++);
            return curMatchIndex - 1;
        } else
            return obj;
    }

    public interface MatchesHelper {
        default float getPercent(AllMatches matches) {
            return 0;
        }
    }

    private void writeLinksToMatches(HTMLFile f, SortedVector<AllMatches> matches, MatchesHelper helper, String headerStr, String csvfile) {
        //		output all the matches
        Set<AllMatches> matchesPrinted = new HashSet<>();

        f.println(headerStr + " (<a href=\"help-sim-" + program.getCountryTag() + ".html\"><small><font color=\"#000088\">"
                + msg.getString("Report.WhatIsThis") + "</font></small></a>):</H4>");
        f.println("<p><a href=\"" + csvfile + "\">download csv</a></p>");
        f.println("<TABLE CELLPADDING=3 CELLSPACING=2>");

        int anz = matches.size();
        for (int i = 0; ((i < anz) && (matchesPrinted.size() != anz)); i++) {
            AllMatches match = matches.elementAt(i);
            if (!matchesPrinted.contains(match)) {
                int a = 0;
                int b = 0;
                String nameA = match.subName(0);
                String nameB = match.subName(1);
                //				Which of both submissions is referenced more often in "matches"?
                for (int x = 0; x < anz; x++) {
                    AllMatches tmp = matches.elementAt(x);
                    if (tmp != match && !matchesPrinted.contains(tmp)) {
                        String tmpA = tmp.subName(0);
                        String tmpB = tmp.subName(1);
                        if (nameA.equals(tmpA) || nameA.equals(tmpB))
                            a += helper.getPercent(tmp);
                        if (nameB.equals(tmpA) || nameB.equals(tmpB))
                            b += helper.getPercent(tmp);
                    }
                }
                String name = (a >= b ? nameA : nameB);
                boolean header = false;

                AllMatches output;
                for (int x = 0; x < anz; x++) {
                    output = matches.elementAt(x);
                    if (!matchesPrinted.contains(output) && (output.subName(0).equals(name) || output.subName(1).equals(name))) {
                        matchesPrinted.add(output);
                        int other = (output.subName(0).equals(name) ? 1 : 0);
                        if (!header) { // only print header when necessary!
                            header = true;
                            f.print("<TR><TD BGCOLOR=" + color(helper.getPercent(match), 128, 192, 128, 192, 255, 255) + ">" + name
                                    + "</TD><TD><nobr>-&gt;</nobr>");
                        }
                        float percent = helper.getPercent(output);
                        f.print("</TD><TD BGCOLOR=" + color(percent, 128, 192, 128, 192, 255, 255) + " ALIGN=center><A HREF=\"match"
                                + getMatchIndex(output) + HTML + output.subName(other) + "</A><BR><FONT COLOR=\""
                                + color(percent, 0, 255, 0, 0, 0, 0) + "\">(" + (((int) (percent * 10)) / (float) 10) + "%)</FONT>");
                    }
                }
                if (header)
                    f.println(TDTR);
            }
        }

        f.println("</TABLE><P>\n");
        f.println("<!---->"); // important for front end
    }


    private void writeMatchesCSV(File root, String filename, SortedVector<AllMatches> matches, MatchesHelper helper) {

        // quick and very dirty csv export of results
        File f = FileUtils.getFile(root, filename);

        try (FileWriter writer = new FileWriter(String.valueOf(f.createNewFile()))) {
            //		output all the matches
            Set<AllMatches> matchesPrinted = new HashSet<>();

            int anz = matches.size();
            for (int i = 0; ((i < anz) && (matchesPrinted.size() != anz)); i++) {
                AllMatches match = matches.elementAt(i);
                if (!matchesPrinted.contains(match)) {
                    int a = 0;
                    int b = 0;
                    String nameA = match.subName(0);
                    String nameB = match.subName(1);
                    //				Which of both submissions is referenced more often in "matches"?
                    for (int x = 0; x < anz; x++) {
                        AllMatches tmp = matches.elementAt(x);
                        if (tmp != match && !matchesPrinted.contains(tmp)) {
                            String tmpA = tmp.subName(0);
                            String tmpB = tmp.subName(1);
                            if (nameA.equals(tmpA) || nameA.equals(tmpB))
                                a += helper.getPercent(tmp);
                            if (nameB.equals(tmpA) || nameB.equals(tmpB))
                                b += helper.getPercent(tmp);
                        }
                    }
                    String name = (a >= b ? nameA : nameB);
                    boolean header = false;

                    AllMatches output;
                    for (int x = 0; x < anz; x++) {
                        output = matches.elementAt(x);
                        if (!matchesPrinted.contains(output) && (output.subName(0).equals(name) || output.subName(1).equals(name))) {
                            matchesPrinted.add(output);
                            int other = (output.subName(0).equals(name) ? 1 : 0);
                            if (!header) { // only print header when necessary!
                                header = true;
                                writer.write(name + ";");
                            }
                            float percent = helper.getPercent(output);
                            writer.write(getMatchIndex(output) + ";");
                            writer.write(output.subName(other) + ";");
                            writer.write((((int) (percent * 10)) / (float) 10) + ";");

                        }
                    }
                    if (header)
                        writer.write("\n");
                }
            }

            writer.flush();

        } catch (Exception e) {
            // POC: ignore all errors
            LOGGER.log(Level.SEVERE, "Exception occur", e);
        }
    }

    private void writeIndex(boolean includeClusterLink) throws jplag.ExitException {
        HTMLFile f = openHTMLFile(root, "index.html");

        writeIndexBegin(f, msg.getString("Report.Search_Results"));

        writeDistribution(f);

        String csvfile = "matches_avg.csv";
        writeLinksToMatches(f, avgmatches, new MatchesHelper() {
            @Override
            public float getPercent(AllMatches matches) {
                return matches.percent();
            }
        }, "<H4>" + msg.getString("Report.MatchesAvg"), csvfile);

        writeMatchesCSV(root, csvfile, avgmatches,
                new MatchesHelper() {
                    @Override
                    public float getPercent(AllMatches matches) {
                        return matches.percent();
                    }
                }
        );

        if (minmatches != null) {
            csvfile = "matches_min.csv";
            writeLinksToMatches(f, minmatches, new MatchesHelper() {
                @Override
                public float getPercent(AllMatches matches) {
                    return matches.percentMinAB();
                }
            }, "<HR><H4>" + msg.getString("Report.MatchesMin"), csvfile);

            writeMatchesCSV(root, csvfile, avgmatches,
                    new MatchesHelper() {
                        @Override
                        public float getPercent(AllMatches matches) {
                            return matches.percentMinAB();
                        }
                    }
            );
        }


        if (maxmatches != null) {
            csvfile = "matches_max.csv";
            writeLinksToMatches(f, maxmatches, new MatchesHelper() {
                @Override
                public float getPercent(AllMatches matches) {
                    return matches.percentMaxAB();
                }
            }, "<HR><H4>" + msg.getString("Report.MatchesMax"), csvfile);

            writeMatchesCSV(root, csvfile, avgmatches,
                    new MatchesHelper() {
                        @Override
                        public float getPercent(AllMatches matches) {
                            return matches.percentMaxAB();
                        }
                    }
            );
        }


        if (includeClusterLink) {
            f.println("<HR><H4><A HREF=\"cluster.html\">" + msg.getString(RCR) + "</A></H4>");
        }

        writeIndexEnd(f);

        f.close();

        f.bytesWritten();

        // a few infos are saved into a textfile- used in the server environment
        if (this.program.getOriginalDir() != null) {
            f = openHTMLFile(root, "info.txt");
            f.println("directory" + "\t" + program.getOriginalDir()
                    + (program.getSubDir() != null ? File.separator + "*" + File.separator + program.getSubDir() : ""));
            f.println("language" + "\t" + language.name());
            f.println("submissions" + "\t" + program.validSubmissions());
            f.println("errors" + "\t" + program.getErrors());
            f.close();
        }

        f.bytesWritten();
    }

    public void writeIndexBegin(HTMLFile f, String title) {
        writeHTMLHeader(f, title);
        f.println("<BODY BGCOLOR=#ffffff LINK=#000088 VLINK=#000000 TEXT=#000000>");
        f.println("<TABLE ALIGN=center CELLPADDING=2 CELLSPACING=1>");
        f.println("<TR VALIGN=middle ALIGN=center BGCOLOR=#ffffff><TD>" + "<IMG SRC=\"logo.gif\" ALT=\"JPlag\" BORDER=0></TD>");
        f.println("<TD><H1><BIG>" + title + "</BIG></H1></TD></TR>");

        if (program.getTitle() != null) {
            f.println("<TR BGCOLOR=\"#aaaaff\" VALIGN=\"top\"><TD>" + BIG + msg.getString("Report.Title")
                    + ":</BIG></BIG><TD><BIG><BIG><CODE>" + program.getTitle() + CODEBIG);
            if (program.getOriginalDir() != null)
                f.println(AAAAFF + "<BIG>" + msg.getString(RD)
                        + ":</BIG></TD><TD><BIG><CODE>" + program.getOriginalDir()
                        + (program.getSubDir() == null ? "" : File.separator + "*" + File.separator + program.getSubDir())
                        + "</CODE></BIG></TD></TR>");
        } else {
            if (this.program.getOriginalDir() == null)
                f.println(AAAAFF + BIG + msg.getString(RD)
                        + ":</BIG></BIG><TD><BIG><BIG><CODE>" + msg.getString("Report.Not_available") +
                        CODEBIG);
            else
                f.println(AAAAFF + BIG + msg.getString(RD)
                        + ":</BIG></BIG></TD><TD><BIG><BIG><CODE>" + program.getOriginalDir()
                        + (program.getSubDir() == null ? "" : File.separator + "*" + File.separator + program.getSubDir())
                        + CODEBIG);
        }

        f.println(AAAAFF + msg.getString("Report.Programs") + TD);
        f.println("<CODE>" + program.allValidSubmissions() + "</CODE></TD></TR>");
        f.println(AAAAFF + msg.getString("Report.Language") + TD + this.language.name()
                + TDTR);
        f.print(AAAAFF + msg.getString("Report.Submissions") + TD
                + this.program.validSubmissions());
        if (program.getErrors() != 0) {
            if (this.program.getErrors() == 1)
                f.print(" <b>(" + msg.getString("Report.1_has_not_been_parsed_successfully") + ")</b>");
            else if (this.program.getErrors() > 1)
                f.print(" <b>("
                        + TagParser.parse(msg.getString("Report.X_have_not_been_parsed_successfully"), new String[]{program.getErrors()
                        + ""}) + ")</b>");
            f.println(TDTR);
            f.println(AAAAFF + msg.getString("Report.Invalid_submissions"));
            if (options.outputFile != null) {
                f.println(" "
                        + TagParser.parse(msg.getString("Report.see_LOGBEG_log_file_LOGEND"), new String[]{
                        "<a href=\"" + options.outputFile.substring(options.outputFile.lastIndexOf(File.separatorChar) + 1)
                                + "\">", "</a>"}));
            }
            f.println(TD);
            f.println("<CODE>" + this.program.allInvalidSubmissions() + "</CODE>");
        }
        f.println(TDTR);
        if (this.program.useBasecode()) {
            f.print(AAAAFF + msg.getString("Report.Basecode_submission") + ":</TD>" + "<TD>"
                    + this.program.getBasecode() + TDTR);
        }
        if (avgmatches != null && !avgmatches.isEmpty() || minmatches != null && !minmatches.isEmpty() || maxmatches != null
                && !maxmatches.isEmpty()) {
            f.println(AAAAFF + msg.getString("Report.Matches_displayed") + ":</TD>" + "<TD>");
            if (avgmatches != null && !avgmatches.isEmpty())
                f.println(avgmatches.size() + " (" + msg.getString(RT) + ": "
                        + avgmatches.elementAt(avgmatches.size() - 1).roundedPercent() + "%) ("
                        + msg.getString("Report.average_similarity") + BR);
            if (minmatches != null && !minmatches.isEmpty())
                f.println(minmatches.size() + " (" + msg.getString(RT) + ": "
                        + minmatches.elementAt(minmatches.size() - 1).roundedPercentMinAB() + "%) ("
                        + msg.getString("Report.minimum_similarity") + BR);
            if (maxmatches != null && !maxmatches.isEmpty())
                f.println(maxmatches.size() + " (" + msg.getString(RT) + ": "
                        + maxmatches.elementAt(maxmatches.size() - 1).roundedPercentMaxAB() + "%) ("
                        + msg.getString("Report.maximum_similarity") + BR);
            f.println(TDTR);
        }

        f.println(AAAAFF + msg.getString("Report.Date") + TD
                + program.getDateFormat().format(new Date()) + TDTR);
        f.println("<TR BGCOLOR=#aaaaff>" + "<TD><EM>" + msg.getString("Report.Minimum_Match_Length") + "</EM> ("
                + msg.getString("Report.sensitivity") + "):</TD><TD>" + program.getMinTokenMatch() + TDTR);

        f.println(AAAAFF + msg.getString("Report.Suffixes") + TD);
        for (int i = 0; i < this.program.getSuffixes().length; i++)
            f.print(this.program.getSuffixes()[i] + (i < this.program.getSuffixes().length - 1 ? ", " : "</TD></TR>\n"));
        f.println("</TABLE>\n<HR>");
    }

    public void writeIndexEnd(HTMLFile f) {
        f.println("<HR>\n<P ALIGN=right><FONT SIZE=\"1\" FACE=\"helvetica\">" + Program.NAME + "</FONT></P>");
        f.println(BHTML);
    }

    /* this function copies all submissions into the result directory */
    private int copySubmissions() throws ExitException {
        int bytes = 0;
        for (Submission sub : program.getClusters().getNeededSubmissions()) {
            int index = this.program.getClusters().getSubmissions().indexOf(sub);

            HTMLFile f = openHTMLFile(root, "submission" + index + HTML_FILE);
            writeHTMLHeader(f, sub.getName());
            f.println(FFFFFF);

            String[] files = sub.getFiles();
            String[][] text = sub.readFiles(files);

            for (int j = 0; j < files.length; j++) {
                f.println(HRCENTER + files[j] + CENTER);
                if (this.language.isPreformated())
                    f.println(PRE);
                for (int k = 0; k < text[j].length; k++) {
                    f.print(text[j][k]);
                    if (!this.language.isPreformated())
                        f.println("<BR>");
                    else
                        f.println();
                }
                if (language.isPreformated())
                    f.println(PREC);
            }

            f.println(BHTML);
            f.close();
            bytes += f.bytesWritten();
        }
        return bytes;
    }

    private void writeClusters(Cluster clustering) throws jplag.ExitException {
        int bytes = 0;

        HTMLFile f = openHTMLFile(root, "cluster.html");
        writeHTMLHeader(f, msg.getString(RCR));
        String clustertype = msg.getString("Report.Type") + ": " + program.getClusters().getType();
        f.println(BH2 + msg.getString(RCR) + " (" + clustertype + H2C);
        f.println("<H3><A HREF=\"dendro.html\">" + msg.getString("Report.Dendrogram") + AH3C);
        bytes += this.program.getClusters().makeDendrograms(root, clustering);

        if (this.program.getThreshold() != null) {
            for (int i = 0; i < this.program.getThreshold().length; i++) {
                float threshold = this.program.getThreshold()[i];
                String clustertitle = TagParser.parse(msg.getString("Report.Clusters_for_Xpercent_treshold"),
                        new String[]{threshold + ""});
                f.println("<H3><A HREF=\"cluster" + threshold + HTML + clustertitle + AH3C);
                HTMLFile f2 = openHTMLFile(root, "cluster" + threshold + HTML_FILE);
                writeHTMLHeader(f2, clustertitle);
                f2.println(BH2 + clustertitle + " (" + clustertype + H2C);
                String text = program.getClusters().printClusters(clustering, threshold, f2);
                f2.println(BHTML);
                f2.close();
                bytes += f2.bytesWritten();
                f.print(text);
            }
        } else {
            float increase = this.program.getClusters().getMaxMergeValue() / 10;
            if (increase < 5)
                increase = 5;
            for (float threshold = increase; threshold <= program.getClusters().getMaxMergeValue(); threshold += increase) {
                String clustertitle = TagParser.parse(msg.getString("Report.Clusters_for_Xpercent_treshold"),
                        new String[]{threshold + ""});
                f.println("<H3><A HREF=\"cluster" + (int) threshold + HTML + clustertitle + AH3C);
                HTMLFile f2 = openHTMLFile(root, "cluster" + (int) threshold + HTML_FILE);
                writeHTMLHeader(f2, clustertitle);
                f2.println(BH2 + clustertitle + " (" + clustertype + H2C);
                String text = program.getClusters().printClusters(clustering, (int) threshold, f2);
                f2.println(BHTML);
                f2.close();
                bytes += f2.bytesWritten();
                f.print(text);
            }
        }

        f.println(BHTML);
        f.close();

        bytes += copySubmissions();
        f.bytesWritten();

        LOGGER.log(Level.INFO, "bytes = {0}", bytes);
    }

    /*
     * Two colors, represented by Rl,Gl,Bl and Rh,Gh,Bh respectively are mixed
     * according to the percentage "percent"
     */
    public final String color(float percent, int rl, int rh, int gl, int gh, int bl, int bh) {
        int farbeR = (int) (rl + (rh - rl) * percent / 100);
        int farbeG = (int) (gl + (gh - gl) * percent / 100);
        int farbeB = (int) (bl + (bh - bl) * percent / 100);
        String helpR = (farbeR < 16 ? "0" : "") + Integer.toHexString(farbeR);
        String helpG = (farbeG < 16 ? "0" : "") + Integer.toHexString(farbeG);
        String helpB = (farbeB < 16 ? "0" : "") + Integer.toHexString(farbeB);
        return "#" + helpR + helpG + helpB;
    }

    // MATCHES
    private void writeMatches(SortedVector<AllMatches> matches) throws jplag.ExitException {
        Enumeration<AllMatches> enum1 = matches.elements();
        while (enum1.hasMoreElements()) {
            AllMatches match = enum1.nextElement();
            if (!matchesIndexMap.containsKey(match))
                continue; // match has already been written
            if (this.program.useExternalSearch()) {
                ThemeGenerator.loadStructure(match.subA);
                ThemeGenerator.loadStructure(match.subB);
            }
            writeMatch(root, getMatchIndex(match), match);
            matchesIndexMap.remove(match); // "mark" as already written
            options.setProgress();

            if (this.program.useExternalSearch()) {
                match.subA.setStruct(null);
                match.subB.setStruct(null);
            }
        }
    }

    public void writeMatch(File root, int i, AllMatches match) throws jplag.ExitException {
        this.root = root;
        int bytes;
        // match???.html
        bytes = writeFrames(i, match);
        // match???-link.html
        bytes += writeLink(i, match);
        // match???-top.html
        bytes += writeTop(i, match);
        // match???-dist.html
        if (this.program.useExternalSearch()) {
            bytes += writeDist(i, match);
        }
        // match???-?.html
        if (!this.program.useDiffReport()) {
            if (this.language.usesIndex()) {
                bytes += writeIndexedSubmission(i, match, 0);
                bytes += writeIndexedSubmission(i, match, 1);
            } else if (this.language.supportsColumns()) {
                bytes += writeImprovedSubmission(i, match, 0);
                bytes += writeImprovedSubmission(i, match, 1);
            } else {
                bytes += writeNormalSubmission(i, match, 0);
                bytes += writeNormalSubmission(i, match, 1);
            }
        } else {
            bytes += writeSubmissionDiff(i, match, 0);
            bytes += writeSubmissionDiff(i, match, 1);
        }
        LOGGER.log(Level.INFO, "bytes = {0}", bytes);
    }

    private int writeFrames(int i, AllMatches match) throws jplag.ExitException {
        HTMLFile f = openHTMLFile(root, MC + i + HTML_FILE);
        writeHTMLHeader(f,
                TagParser.parse(msg.getString("Report.Matches_for_X1_AND_X2"), new String[]{match.subName(0), match.subName(1)}));
        f.println("<FRAMESET ROWS=\"130,*\">\n <FRAMESET COLS=\"30%,70%\">");
        f.println(FRAME_MC + i + "-link.html\" NAME=\"link\" " + "FRAMEBORDER=0>");
        f.println(FRAME_MC + i + "-top.html\" NAME=\"top\" " + "FRAMEBORDER=0>");
        f.println(" </FRAMESET>");
        f.println(" <FRAMESET COLS=\"50%,50%\">");
        f.println(FRAME_MC + i + "-0.html\" NAME=\"0\">");
        f.println(FRAME_MC + i + "-1.html\" NAME=\"1\">");
        f.println(" </FRAMESET>\n</FRAMESET>\n</HTML>");
        f.close();
        return f.bytesWritten();
    }

    private int writeLink(int i, AllMatches match) throws jplag.ExitException {
        HTMLFile f = openHTMLFile(root, MC + i + "-link.html");
        writeHTMLHeader(f, msg.getString("Report.Links"));
        f.println("<BODY>\n <H3 ALIGN=\"center\">"
                + TagParser.parse(msg.getString("Report.Matches_for_X1_AND_X2"), new String[]{match.subName(0), match.subName(1)})
                + "</H3>");
        f.println(" <H1 align=\"center\">" + match.roundedPercent() + "%</H1>\n<CENTER>");
        f.println(" <A HREF=\"index.html#matches\" TARGET=\"_top\">" + msg.getString("Report.INDEX") + "</A> - ");
        f.println(" <A HREF=\"help-" + program.getCountryTag() + ".html\" TARGET=\"_top\">" + msg.getString("Report.HELP")
                + "</A></CENTER>");
        f.println(BHTML);
        f.close();
        return f.bytesWritten();
    }

    private int writeTop(int i, AllMatches match) throws jplag.ExitException {
        HTMLFile f = openHTMLFile(root, MC + i + "-top.html");
        writeHTMLHeaderWithScript(f, "Top");
        f.println(FFFFFF);

        if (this.program.useExternalSearch()) {
            f.println("<A HREF=\"match" + i + "-dist.html\" TARGET=\"_top\">" + msg.getString("Report.Distribution") + "</A><P>");
        }

        match.htmlReport(f, i, this.program);

        f.println("</BODY>\n</HTML>\n");
        f.close();
        return f.bytesWritten();
    }

    private int writeDist(int i, AllMatches match) throws jplag.ExitException {
        HTMLFile f = openHTMLFile(root, MC + i + "-dist.html");
        writeHTMLHeader(f, msg.getString("Report.Token_Distribution"));
        f.println("<BODY>");
        match.distributionReport(f, msg);
        f.println(BHTML);
        f.close();
        return f.bytesWritten();
    }

    // SUBMISSION - here it comes...
    private final String[] pics = {"forward.gif", "back.gif"};

    /*
     * i is the number of the match j == 0 if subA is considered, otherwise (j
     * must then be 1) it is subB
     */
    private int writeNormalSubmission(int i, AllMatches match, int j) throws jplag.ExitException {
        Submission sub = (j == 0 ? match.subA : match.subB);
        String[] files = match.files(j);

        String[][] text = sub.readFiles(files);

        Token[] tokens = (j == 0 ? match.subA : match.subB).getStruct().getTokens();
        Match onematch;
        String hilf;
        int h;
        for (int x = 0; x < match.size(); x++) {
            onematch = match.matches[x];

            Token start = tokens[(j == 0 ? onematch.startA : onematch.startB)];
            Token ende = tokens[((j == 0 ? onematch.startA : onematch.startB) + onematch.length - 1)];

            for (int y = 0; y < files.length; y++) {
                if (start.file.equals(files[y]) && text[y] != null) {
                    hilf = FONT_COLOR + Colors.getColor(x) + "\">" + (j == 1 ? "<div style=\"position:absolute;left:0\">" : "")
                            + "<A HREF=\"javascript:ZweiFrames('match" + i + "-" + (1 - j) + ".html#" + x + "'," + (3 - j) + ",'match" + i
                            + "-top.html#" + x + "',1)\"><IMG SRC=\"" + pics[j] + "\" ALT=\"other\" " + "BORDER=\"0\" ALIGN=\""
                            + (j == 0 ? "right" : "left") + AC + (j == 1 ? "</div>" : "") + "<B>";
                    // position the icon and the beginning of the colorblock
                    if (text[y][start.getLine() - 1].endsWith(FONTC))
                        text[y][start.getLine() - 1] += hilf;
                    else
                        text[y][start.getLine() - 1] = hilf + text[y][start.getLine() - 1];
                    // the link location is placed 3 lines before the start of a block
                    h = (Math.max(start.getLine() - 4, 0));
                    text[y][h] = A_NAME + x + AC + text[y][h];
                    // mark the end
                    if (start.getLine() != ende.getLine() && // if match is only one line
                            text[y][ende.getLine() - 1].startsWith("<FONT "))
                        text[y][ende.getLine() - 1] = BC_FONTC + text[y][ende.getLine() - 1];
                    else
                        text[y][ende.getLine() - 1] += BC_FONTC;
                }
            }
        }

        if (this.program.useBasecode() && match.bcmatchesA != null && match.bcmatchesB != null) {
            AllBasecodeMatches bcmatch = (j == 0 ? match.bcmatchesA : match.bcmatchesB);
            for (int x = 0; x < bcmatch.size(); x++) {
                onematch = bcmatch.matches[x];
                Token start = tokens[onematch.startA];
                Token ende = tokens[onematch.startA + onematch.length - 1];

                for (int y = 0; y < files.length; y++) {
                    if (start.file.equals(files[y]) && text[y] != null) {
                        hilf = ("<font color=\"#C0C0C0\"><EM>");
                        // position the icon and the beginning of the colorblock
                        if (text[y][start.getLine() - 1].endsWith("<font color=\"#000000\">"))
                            text[y][start.getLine() - 1] += hilf;
                        else
                            text[y][start.getLine() - 1] = hilf + text[y][start.getLine() - 1];

                        // mark the end
                        if (start.getLine() != ende.getLine() && // match is only one line
                                text[y][ende.getLine() - 1].startsWith("<font color=\"#C0C0C0\">"))
                            text[y][ende.getLine() - 1] = "</EM><font color=\"#000000\">" + text[y][ende.getLine() - 1];
                        else
                            text[y][ende.getLine() - 1] += "</EM><font color=\"#000000\">";
                    }
                }
            }
        }

        HTMLFile f = openHTMLFile(root, MC + i + "-" + j + HTML_FILE);
        writeHTMLHeaderWithScript(f, (j == 0 ? match.subA : match.subB).getName());
        f.println("<BODY BGCOLOR=\"#ffffff\"" + (j == 1 ? " style=\"margin-left:25\">" : ">"));

        for (int x = 0; x < text.length; x++) {
            f.println(HRCENTER + files[x] + CENTER);
            if (this.language.isPreformated())
                f.println(PRE);
            for (int y = 0; y < text[x].length; y++) {
                f.print(text[x][y]);
                if (!this.language.isPreformated())
                    f.println("<BR>");
                else
                    f.println();
            }
            if (this.language.isPreformated())
                f.println(PREC);
        }
        f.println(BHTML);
        f.close();
        return f.bytesWritten();
    }

    /*
     * i is the number of the match j == 0 if subA is considered, otherwise it
     * is subB
     *
     * This procedure uses only the getIndex() method of the token. It is meant
     * to be used with the Character front end
     */
    private int writeIndexedSubmission(int i, AllMatches match, int j) throws jplag.ExitException {
        Submission sub = (j == 0 ? match.subA : match.subB);
        String[] files = match.files(j);
        char[][] text = sub.readFilesChar(files);
        Token[] tokens = (j == 0 ? match.subA : match.subB).getStruct().getTokens();

        // get index array with matches sorted in ascending order.
        int[] perm = match.sortPermutation(j);

        // HTML intro
        HTMLFile f = openHTMLFile(root, MC + i + "-" + j + HTML_FILE);
        writeHTMLHeaderWithScript(f, (j == 0 ? match.subA : match.subB).getName());
        f.println(FFFFFF);

        int index = 0; // match index
        Match onematch = null;
        Token start = null;
        Token end = null;
        for (int fileIndex = 0; fileIndex < files.length; fileIndex++) {
            // print filename
            f.println(HRCENTER + files[fileIndex] + CENTER);
            char[] buffer = text[fileIndex];

            for (int charNr = 0; charNr < buffer.length; charNr++) {
                if (onematch == null) {
                    if (index < match.size()) {
                        onematch = match.matches[perm[index]];
                        start = tokens[(j == 0 ? onematch.startA : onematch.startB)];
                        end = tokens[((j == 0 ? onematch.startA : onematch.startB) + onematch.length - 1)];
                        index++;
                    } else {
                        start = end = null;
                    }
                }
                // begin markup
                if (start != null && start.getIndex() == charNr) {
                    f.print(A_NAME + perm[index - 1] + AC);
                    f.print(FONT_COLOR + Colors.getColor(perm[index - 1]) + "\"><B>");
                }
                // text
                if (buffer[charNr] == '<') {
                    f.print("&lt;");
                } else if (buffer[charNr] == '>') {
                    f.print("&gt;");
                } else if (buffer[charNr] == '\n') {
                    f.print("<br>\n");
                } else
                    f.print(buffer[charNr]);
                // end markup
                if (end != null && end.getIndex() == charNr) {
                    f.print(BC_FONTC);
                    onematch = null; // switch to next match
                }
            }
        }

        f.println("\n</BODY>\n</HTML>");
        f.close();
        return f.bytesWritten();
    }

    /*
     * i is the number of the match j == 0 if subA is considered, otherwise (j
     * must then be 1) it is subB
     *
     * This procedure makes use of the column and length information!
     */
    private int writeImprovedSubmission(int i, AllMatches match, int j) throws jplag.ExitException {
        Submission sub = (j == 0 ? match.subA : match.subB);
        String[] files = match.files(j);
        String[][] text = sub.readFiles(files);
        Token[] tokens = (j == 0 ? match.subA : match.subB).getStruct().getTokens();

        // Markup list:
        Comparator<MarkupText> comp = (mo1, mo2) -> {
            int col1 = mo1.column;
            int col2 = mo2.column;
            if (col1 > col2)
                return -1;
            else if (col1 < col2)
                return 1;
            else if (mo1.frontMarkup)
                return -1;
            else if (mo2.frontMarkup)
                return 1;
            else return 0;
        };
        TreeMap<MarkupText, Object> markupList = new TreeMap<>(comp);

        for (int x = 0; x < match.size(); x++) {
            Match onematch = match.matches[x];

            Token start = tokens[(j == 0 ? onematch.startA : onematch.startB)];
            Token end = tokens[((j == 0 ? onematch.startA : onematch.startB) + onematch.length - 1)];
            for (int fileIndex = 0; fileIndex < files.length; fileIndex++) {
                if (start.file.equals(files[fileIndex]) && text[fileIndex] != null) {
                    String tmp = FONT_COLOR + Colors.getColor(x) + "\">" + (j == 1 ? "<div style=\"position:absolute;left:0\">" : "")
                            + "<A HREF=\"javascript:ZweiFrames('match" + i + "-" + (1 - j) + ".html#" + x + "'," + (3 - j) + ",'match" + i
                            + "-top.html#" + x + "',1)\"><IMG SRC=\"" + pics[j] + "\" ALT=\"other\" " + "BORDER=\"0\" ALIGN=\""
                            + (j == 0 ? "right" : "left") + AC + (j == 1 ? "</div>" : "") + "<B>";
                    // position the icon and the beginning of the colorblock
                    markupList.put(new MarkupText(fileIndex, start.getLine() - 1, start.getColumn() - 1, tmp, true), null);
                    // mark the end
                    markupList
                            .put(new MarkupText(fileIndex, end.getLine() - 1, end.getColumn() + end.getLength() - 1, BC_FONTC, false),
                                    null);

                    // the link location is placed 3 lines before the start of a block
                    int linkLine = (Math.max(start.getLine() - 4, 0));
                    markupList.put(new MarkupText(fileIndex, linkLine, 0, A_NAME + x + AC, false), null);
                }
            }
        }

        if (this.program.useBasecode() && match.bcmatchesA != null && match.bcmatchesB != null) {
            AllBasecodeMatches bcmatch = (j == 0 ? match.bcmatchesA : match.bcmatchesB);
            for (int x = 0; x < bcmatch.size(); x++) {
                Match onematch = bcmatch.matches[x];
                Token start = tokens[onematch.startA];
                Token end = tokens[onematch.startA + onematch.length - 1];

                for (int fileIndex = 0; fileIndex < files.length; fileIndex++) {
                    if (start.file.equals(files[fileIndex]) && text[fileIndex] != null) {
                        String tmp = "<font color=\"#C0C0C0\"><EM>";
                        // beginning of the colorblock
                        markupList.put(new MarkupText(fileIndex, start.getLine() - 1, start.getColumn() - 1, tmp, false), null);
                        // mark the end
                        markupList.put(new MarkupText(fileIndex, end.getLine() - 1, end.getColumn() + end.getLength() - 1, "</EM></font>",
                                true), null);
                    }
                }
            }
        }

        // Apply changes:
        for (MarkupText markup : markupList.keySet()) {
            String tmp = text[markup.fileIndex][markup.lineIndex];
            // is there any &quot;, &amp;, &gt; or &lt; in the String?
            if (tmp.indexOf('&') >= 0) {
                ArrayList<String> tmpV = new ArrayList<>();
                // convert the string into a vector
                int strLength = tmp.length();
                for (int k = 0; k < strLength; k++) {
                    if (tmp.charAt(k) != '&')
                        tmpV.add(tmp.charAt(k) + "");
                    else { //put &quot;, &amp;, &gt; and &lt; into one element
                        String tmpSub = tmp.substring(k);
                        if (tmpSub.startsWith("&quot;")) {
                            tmpV.add("&quot;");
                            k = k + 5;
                        } else if (tmpSub.startsWith("&amp;")) {
                            tmpV.add("&amp;");
                            k = k + 4;
                        } else if (tmpSub.startsWith("&lt;")) {
                            tmpV.add("&lt;");
                            k = k + 3;
                        } else if (tmpSub.startsWith("&gt;")) {
                            tmpV.add("&gt;");
                            k = k + 3;
                        } else
                            tmpV.add(tmp.charAt(k) + "");
                    }
                }
                if (markup.column <= tmpV.size()) {
                    tmpV.set(markup.column, markup.text);
                } else {
                    tmpV.add(markup.text);
                }

                StringBuilder tmpVStr = new StringBuilder();
                // reconvert the Vector into a String
                for (String s : tmpV) tmpVStr.append(s);
                text[markup.fileIndex][markup.lineIndex] = tmpVStr.toString();
            } else {
                text[markup.fileIndex][markup.lineIndex] = tmp.substring(0, (Math.min(tmp.length(), markup.column)))
                        + markup.text + tmp.substring((Math.min(tmp.length(), markup.column)));
            }
        }

        HTMLFile f = openHTMLFile(root, MC + i + "-" + j + HTML_FILE);
        writeHTMLHeaderWithScript(f, (j == 0 ? match.subA : match.subB).getName());
        f.println("<BODY BGCOLOR=\"#ffffff\"" + (j == 1 ? " style=\"margin-left:25\">" : ">"));

        for (int x = 0; x < text.length; x++) {
            f.println(HRCENTER + files[x] + CENTER);
            if (this.language.isPreformated())
                f.println(PRE);
            for (int y = 0; y < text[x].length; y++) {
                f.print(text[x][y]);
                if (!this.language.isPreformated())
                    f.println("<BR>");
                else
                    f.println();
            }
            if (this.language.isPreformated())
                f.println(PRE);
        }
        f.println("\n</BODY>\n</HTML>");
        f.close();
        return f.bytesWritten();
    }

    /*
     * i is the number of the match j == 0 if subA is considered, otherwise it
     * is subB
     */
    private int writeSubmissionDiff(int i, AllMatches match, int j) throws jplag.ExitException {
        Submission sub = (j == 0 ? match.subA : match.subB);
        String[] files = match.allFiles(j);

        String[][] text = sub.readFiles(files);
        for (int x = 0; x < text.length; x++) {
            for (int line = 0; line < text[x].length; line++) {
                switch (match.diffType(files[x], line + 1, j)) {
                    case 0:
                        text[x][line] = "<FONT COLOR=\"#000000\">" + text[x][line] + FONTC;
                        break;
                    case 1:
                        text[x][line] = "<FONT COLOR=\"#0000FF\">" + text[x][line] + FONTC;
                        break;
                    case 2:
                    default:
                        text[x][line] = "<FONT COLOR=\"#FF0000\">" + text[x][line] + FONTC;
                        break;
                }
            }
        }

        HTMLFile f = openHTMLFile(root, MC + i + "-" + j + HTML_FILE);
        writeHTMLHeader(f, (j == 0 ? match.subA : match.subB).getName());
        f.println("<BODY>");

        for (int x = 0; x < text.length; x++) {
            f.println(HRCENTER + files[x] + "</CENTER></H3><HR>\n<PRE>");
            for (int y = 0; y < text[x].length; y++)
                f.println(text[x][y]);
            f.println(PREC);
        }
        f.println(BHTML);
        f.close();
        return f.bytesWritten();
    }

    /*
     * This procedure copies all the data from "data/" into the
     * result-directory.
     */
    private final String[] fileList = {"back.gif", "forward.gif", "help-en.html", "help-sim-en.html", "logo.gif", "fields.js"};

    public void copyFixedFiles(File root) {
        fileList[2] = "help-" + program.getCountryTag() + HTML_FILE;
        fileList[3] = "help-sim-" + program.getCountryTag() + HTML_FILE;
        for (int i = fileList.length - 1; i >= 0; i--) {
            java.net.URL url = Report.class.getResource("data/" + fileList[i]);
            if (url != null) {
                try (DataInputStream dis = new DataInputStream(url.openStream());
                     DataOutputStream dos = new DataOutputStream(
                             new FileOutputStream(FileUtils.getFile(root, fileList[i])))
                ) {
                    byte[] buffer = new byte[1024];
                    int count;
                    do {
                        count = dis.read(buffer);
                        if (count != -1)
                            dos.write(buffer, 0, count);
                    } while (count != -1);
                } catch (IOException | NullPointerException e) {
                    LOGGER.log(Level.SEVERE, "Exception occur", e);
                }
            }
        }
    }
}

/**
 * This class represents one markup tag that will be included in the text. It is
 * necessary to sort the objects before they are included into the text, so that
 * the original position can be found.
 */
class MarkupText {
    public int fileIndex, lineIndex, column;
    public String text;
    public boolean frontMarkup;

    public MarkupText(int fileIndex, int lineIndex, int column, String text, boolean frontMarkup) {
        this.fileIndex = fileIndex;
        this.lineIndex = lineIndex;
        this.column = column;
        this.text = text;
        this.frontMarkup = frontMarkup;
    }

    public String toString() {
        return "MarkUp - file: " + fileIndex + " line: " + lineIndex + " column: " + column + " text: " + text;
    }
}
