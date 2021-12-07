package jplag;

import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Objects;

/*
 * Everything about a single submission is stored in this object. (directory,
 * files, ...)
 */
public class Submission implements Comparable<Submission> {
    public String name;

    private final Program program;

    private final boolean readSubDirs;

    private final Language language;

    public File dir;

    public String[] files = null; // = new String[0];

    public Structure struct;

    public int structSize = 0;

    // public long structMem;
    boolean exact_match = false; // used for fallback

    public boolean errors = false;

    public DecimalFormat format = new DecimalFormat("0000");

    public Submission(String name, File dir, boolean readSubDirs, Program p, Language language) {
        this.program = p;
        this.language = language;
        this.dir = dir;
        this.name = name;
        this.readSubDirs = readSubDirs;
        try {
            lookupDir(dir, "");
        } catch (Throwable ignored) {
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
        String[] newFiles = aktDir.list((dir1, name) -> {
            if (!FileUtils.getFile(dir1, name).isFile())
                return false;
            if (program.excludeFile(name))
                return false;
            String[] suffies = program.getSuffixes();
            for (String suffy : suffies)
                if (exact_match) {
                    if (name.equals(suffy))
                        return true;
                } else {
                    if (name.endsWith(suffy))
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
    public boolean parse() throws jplag.ExitException {
        if (!program.useVerboseParser()) {
            if (files == null || files.length == 0) {
                program.print("ERROR: nothing to parse for submission \"" + name + "\"\n", null);
                return false;
            }
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
        try {
            FileInputStream dis = new FileInputStream(in);
            FileOutputStream dos = new FileOutputStream(out);
            int count;
            do {
                count = dis.read(buffer);
                if (count != -1)
                    dos.write(buffer, 0, count);
            } while (count != -1);
            dis.close();
            dos.close();
        } catch (IOException e) {
            program.print("Error copying file: " + e + "\n", null);
        }
    }

    public int size() {
        if (struct != null)
            return structSize = struct.size();
        return structSize;
    }

    /*
     * Used by the "Report" class. All source files are returned as an array of
     * an array of strings.
     */
    public String[][] readFiles(String[] files) throws jplag.ExitException {
        String[][] result = new String[files.length][];
        String help;

        ArrayList<String> text = new ArrayList<>();
        for (int i = 0; i < files.length; i++) {
            text.clear();
            try {
                /* file encoding = "UTF-8" */
                FileInputStream fileInputStream = new FileInputStream(FileUtils.getFile(dir, files[i]));
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
                BufferedReader in = new BufferedReader(inputStreamReader);
                while ((help = in.readLine()) != null) {
                    help = help.replaceAll("&", "&amp;");
                    help = help.replaceAll("<", "&lt;");
                    help = help.replaceAll(">", "&gt;");
                    help = help.replaceAll("\"", "&quot;");
                    text.add(help);
                }
                in.close();
                inputStreamReader.close();
                fileInputStream.close();
            } catch (FileNotFoundException e) {
                System.out.println("File not found: " + ((FileUtils.getFile(dir, files[i])).toString()));
            } catch (IOException e) {
                throw new jplag.ExitException("I/O exception!");
            }
            result[i] = new String[text.size()];
            result[i] = text.toArray(String[]::new);
        }
        return result;
    }

    /*
     * Used by the "Report" class. All source files are returned as an array of
     * an array of chars.
     */
    public char[][] readFilesChar(String[] files) throws jplag.ExitException {
        char[][] result = new char[files.length][];

        for (int i = 0; i < files.length; i++) {
            try {
                File file = FileUtils.getFile(dir, files[i]);
                int size = (int) file.length();
                char[] buffer = new char[size];

                FileReader fis = new FileReader(file);

                if (size != fis.read(buffer)) {
                    System.out.println("Not right size read from the file, " + "but I will still continue...");
                }

                result[i] = buffer;
                fis.close();
            } catch (FileNotFoundException e) {
                // TODO: Should an ExitException be thrown here?
                System.out.println("File not found: " + ((FileUtils.getFile(dir, files[i])).toString()));
            } catch (IOException e) {
                throw new jplag.ExitException("I/O exception reading file \"" + (FileUtils.getFile(dir, files[i])).toString() + "\"!", e);
            }
        }
        return result;
    }

    public int compareTo(Submission other) {
        return name.compareTo(other.name);
    }

    public String toString() {
        return name;
    }
}
