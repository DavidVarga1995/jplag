package jplag;


import java.io.*;
import java.nio.charset.StandardCharsets;

//  FILE HANDLING
public class HTMLFile extends PrintWriter {
    private BufferedCounter bc;

    /* This static method has to be used to instanciate HTMLFile objects. */
    public static HTMLFile createHTMLFile(File f) throws IOException {

        HTMLFile htmlfile;

        try (BufferedCounter newBc = new BufferedCounter(
                new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8))) {
            htmlfile = new HTMLFile(newBc);
            htmlfile.bc = newBc;
        }
        return htmlfile;
    }

    private HTMLFile(BufferedWriter bw) {
        super(bw);
    }

    public int bytesWritten() {
        return bc.bytesWritten();
    }
}
