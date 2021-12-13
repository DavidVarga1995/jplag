package jplag;

import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.logging.Level;

/*
 * Everything about a single submission is stored in this object. (directory,
 * files, ...)
 */
public class Submission implements Comparable<Submission> {

    private static final Logger LOGGER = Logger.getLogger(Submission.class.getName());

    private final String name;

    public final String getName() {
        return name;
    }

    private final Program program;

    private final boolean readSubDirs;

    private final Language language;

    private final File dir;

    public final File getDir() {
        return dir;
    }

    private String[] files = null;

    public final String[] getFiles() {
        return files;
    }

    private Structure struct;

    public final Structure getStruct() {
        return struct;
    }

    public final void setStruct(Structure struct) {
        this.struct = struct;
    }

    private int structSize = 0;

    boolean exactMatch = false; // used for fallback

    private boolean errors = false;

    public final boolean getErrors() {
        return !errors;
    }

    private final DecimalFormat format = new DecimalFormat("0000");

    public Submission(String name, File dir, boolean readSubDirs, Program p, Language language) {
        this.program = p;
        this.language = language;
        this.dir = dir;
        this.name = name;
        this.readSubDirs = readSubDirs;
        try {
            lookupDir(dir, "");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception occur in Submission", e);
        }
        if (program.useVerboseDetails()) {
            program.print("Files in submission '" + name + "':\n", null);
            for (String file : files) program.print("  " + file + '\n', null);
        }
    }

    public Submission(String name, File dir, Program p, Language language) {
        this.language = language;
        this.program = p;
        this.dir = dir;
        this.name = name;
        this.readSubDirs = false;

        files = new String[1];
        files[0] = name;

        if (program.useVerboseDetails()) {
            program.print("Files in submission '" + name + "':\n", null);
            for (String file : files) program.print("  " + file + '\n', null);
        }
    }

    // recursively read in all the files
    private void lookupDir(File dir, String subDir) {
        File aktDir = FileUtils.getFile(dir, subDir);
        if (!aktDir.isDirectory())
            return;
        if (readSubDirs) {
            String[] dirs = aktDir.list();
            if (!subDir.equals("") && dirs != null)
                for (String s : dirs) lookupDir(dir, subDir + File.separator + s);
            else if (dirs != null)
                for (String s : dirs) lookupDir(dir, s);
        }
        String[] newFiles = aktDir.list((dir1, nameLookupDir) -> {
            if (!FileUtils.getFile(dir1, nameLookupDir).isFile())
                return false;
            if (program.excludeFile(nameLookupDir))
                return false;
            String[] suffies = program.getSuffixes();
            for (String suffy : suffies)
                if (exactMatch) {
                    if (nameLookupDir.equals(suffy))
                        return true;
                } else {
                    if (nameLookupDir.endsWith(suffy))
                        return true;
                }
            return false;
        });
        if (files != null) {
            String[] oldFiles = files;
            if (newFiles != null)
                files = new String[oldFiles.length + newFiles.length];
            if (!Objects.equals(subDir, "") && newFiles != null)
                for (int i = 0; i < newFiles.length; i++)
                    files[i] = subDir + File.separator + newFiles[i];
            else if (newFiles != null)
                System.arraycopy(newFiles, 0, files, 0, newFiles.length);

            if (newFiles != null)
                System.arraycopy(oldFiles, 0, files, newFiles.length, oldFiles.length);
        } else {
            if (!Objects.equals(subDir, "") && newFiles != null) {
                files = new String[newFiles.length];
                for (int i = 0; i < newFiles.length; i++)
                    files[i] = subDir + File.separator + newFiles[i];
            } else
                files = newFiles;
        }
    }

    /* parse all the files... */
    public final boolean parse() {
        if (!program.useVerboseParser() && (files == null || files.length == 0)) {
            program.print("ERROR: nothing to parse for submission \"" + name + "\"\n", null);
            return false;
        }

        struct = this.language.parse(dir, files);
        if (!language.errors()) {
            if (struct.size() < 3) {
                program.print("Submission \"" + name + "\" is too short!\n", null);
                struct = null;
                errors = true; // invalidate submission
                return false;
            }
            return true;
        }

        struct = null;
        errors = true; // invalidate submission
        if (program.useDebugParser())
            copySubmission();
        return false;
    }

    /*
     * This method is used to copy files that can not be parsed to a special
     * folder: jplag/errors/java old_java scheme cpp /001/(...files...)
     * /002/(...files...)
     */
    private void copySubmission() {
        File errorDir = null;
        try {
            URL url = Submission.class.getResource("errors");
            if (url != null)
                errorDir = new File(url.getFile());
        } catch (NullPointerException e) {
            return;
        }
        boolean successful = false;
        if (errorDir != null) {
            errorDir = FileUtils.getFile(errorDir, this.language.getShortName());
            if (!errorDir.exists())
                successful = errorDir.mkdir();
            int i = 0;
            File destDir = null;

            if (successful) {
                while ((destDir = FileUtils.getFile(errorDir, format.format(i))).exists()) {
                    i++;
                }
            }
            if (destDir != null) {
                successful = destDir.mkdir();

            }
            if (successful) {
                for (i = 0; i < files.length; i++)
                    copyFile(FileUtils.getFile(dir, files[i]), FileUtils.getFile(destDir, files[i]));
            }
        }
    }

    /* Physical copy. :-) */
    private void copyFile(File in, File out) {
        byte[] buffer = new byte[10000];
        try (FileInputStream dis = new FileInputStream(in);
             FileOutputStream dos = new FileOutputStream(out)){
            int count;
            do {
                count = dis.read(buffer);
                if (count != -1)
                    dos.write(buffer, 0, count);
            } while (count != -1);
        } catch (IOException e) {
            program.print("Error copying file: " + e + "\n", null);
        }
    }

    public final int size() {
        if (struct != null) {
            structSize = struct.size();
            return structSize;
        }
        return structSize;
    }

    /*
     * Used by the "Report" class. All source files are returned as an array of
     * an array of strings.
     */
    public final String[][] readFiles(String[] files) throws jplag.ExitException {
        String[][] result = new String[files.length][];
        String help;

        ArrayList<String> text = new ArrayList<>();
        for (int i = 0; i < files.length; i++) {
            text.clear();
            try (FileInputStream fileInputStream = new FileInputStream(FileUtils.getFile(dir, files[i]));
                 InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
                    BufferedReader in = new BufferedReader(inputStreamReader)){
                /* file encoding = "UTF-8" */

                while ((help = in.readLine()) != null) {
                    help = help.replace("&", "&amp;");
                    help = help.replace("<", "&lt;");
                    help = help.replace(">", "&gt;");
                    help = help.replace("\"", "&quot;");
                    text.add(help);
                }
            } catch (FileNotFoundException e) {
                String error = "File not found: " + ((FileUtils.getFile(dir, files[i])).toString());
                LOGGER.log(Level.SEVERE, "{0}", error.replaceAll("[\r\n]",""));
            } catch (IOException e) {
                throw new jplag.ExitException("I/O exception!");
            }
            result[i] = new String[text.size()];
            result[i] = text.toArray(result[i]);
        }
        return result;
    }

    /*
     * Used by the "Report" class. All source files are returned as an array of
     * an array of chars.
     */
    public final char[][] readFilesChar(String[] files) throws jplag.ExitException {
        char[][] result = new char[files.length][];

        for (int i = 0; i < files.length; i++) {

            File file = FileUtils.getFile(dir, files[i]);
            try (FileReader fis = new FileReader(file)){

                int size = (int) file.length();
                char[] buffer = new char[size];

                if (size != fis.read(buffer)) {
                    String info = "Not right size read from the file, " + "but I will still continue...";
                    LOGGER.log(Level.INFO, "{0}", info);
                }

                result[i] = buffer;
            } catch (FileNotFoundException e) {
                String er = "File not found: " + ((FileUtils.getFile(dir, files[i])).toString());
                LOGGER.log(Level.SEVERE, "{0}", er.replaceAll("[\r\n]",""));
            } catch (IOException e) {
                throw new jplag.ExitException("I/O exception reading file \"" + (FileUtils.getFile(dir, files[i])).toString() + "\"!", e);
            }
        }
        return result;
    }

    @Override
    public final int compareTo(Submission other) {
        return name.compareTo(other.name);
    }

    @Override
    public final boolean equals(Object obj){
        return false;
    }

    @Override
    public final int hashCode() {
        return 0;
    }

    public final String toString() {
        return name;
    }
}
