/*
 * Created on 10.02.2005
 */
package jplag.options.util;

import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Author Emeric Kwemou, Moritz Kroll
 */
public class ZipUtil {

  private static final Logger LOGGER = Logger.getLogger(ZipUtil.class.getName());

    /**
     *
     * @param file file
     * @param dest
     *          Where the zipped file will be stored
     * @return zipped file
     */
    public static File zip(File file, String dest) {
    File zippedFile = FileUtils.getFile(dest + "/" + file.getName() + ".zip");
    try {
      FileOutputStream ops = new FileOutputStream(zippedFile);
      ZipOutputStream zos = new ZipOutputStream(ops);
      zip(file, zos, "");
      zos.close();
    } catch (IOException fnfex) {
      // fnfex.printStackTrace();
        LOGGER.log(Level.SEVERE, "Exception occur", fnfex);
    }// ioex.printStackTrace();

        return zippedFile;
  }

  public static void zip(File file, ZipOutputStream zos, String prefix)
  {
    File[] entries = file.listFiles();
    if (entries != null) {
      for (File entry : entries) {
          if (entry.isDirectory()) {
              // generate directory entry
              ZipEntry zi = new ZipEntry(prefix + entry.getName() + "/");
              try {
                  zos.putNextEntry(zi);
                  zos.closeEntry();
              } catch (IOException ioex) {
                  // ioex.printStackTrace();
                  LOGGER.log(Level.SEVERE, "Exception occur", ioex);
              }
              zip(entry, zos, prefix + entry.getName() + "/");
          } else {
              try {
                  FileInputStream fis = new FileInputStream(entry);
                  ZipEntry zi = new ZipEntry(prefix + entry.getName());
                  zos.putNextEntry(zi);
                  copystream(fis, zos);
                  zos.closeEntry();
              } catch (IOException ex) {
                  // ex.printStackTrace();
                  LOGGER.log(Level.SEVERE, "Exception occur", ex);
              }// ioex.printStackTrace();

          }
      }
  }
  }

  public static int copystream(InputStream in, OutputStream out)
      throws IOException {

    byte[] buffer = new byte[1024];
    int len;
    int totalsize = 0;

    while ((len = in.read(buffer)) >= 0)
    {
        out.write(buffer, 0, len);
        totalsize += len;
    }
    totalsize += len;

    in.close();
    // out.close();
    return totalsize;
  }

	/**
	 * @param container
	 *            all extracted file will be stored in container, which will be
	 *            created in the destination
	 * @param file file
	 * @param destination
	 *            where the file will be stored
	 * @return total size of all unzipped files
	 *
	 */
	public static int unzip(File file, String destination, String container) {
		int totalsize = 0;

        int THRESHOLD_ENTRIES = 10000;
        int THRESHOLD_SIZE = 1000000000; // 1 GB
        double THRESHOLD_RATIO = 10;
        int totalSizeArchive = 0;
        int totalEntryArchive = 0;

        File result = FileUtils.getFile(destination + File.separator + container);
		if(result.mkdir())
		destination = destination + File.separator + container + File.separator;
		try {
            ZipFile zipFile = new ZipFile(file);
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry ze = entries.nextElement();
                File ze_f = FileUtils.getFile(destination, ze.getName());
                String canonicalDestinationPath = ze_f.getCanonicalPath();

                if (!canonicalDestinationPath.startsWith(destination)) {
                    throw new IOException("Entry is outside of the target directory");
                }
				if (ze.isDirectory())
					if(!ze_f.mkdir())
                        break;
				else {
					// make sure directories exist in case the client
					// didn't provide directory entries!

                    InputStream in = null;
                    BufferedOutputStream bos = null;

					if((FileUtils.getFile(FileUtils.getFile(destination, ze.getName()).getParent())).mkdirs()){
                        bos = new BufferedOutputStream(new FileOutputStream(FileUtils.getFile(
                                destination, ze.getName())));
                        in = zipFile.getInputStream(ze);
                        totalsize += copystream(in, bos);
                        bos.close();
                    }

                        totalEntryArchive ++;

                        int nBytes;
                        byte[] buffer = new byte[2048];
                        int totalSizeEntry = 0;

                        if(in != null) {
                            while ((nBytes = in.read(buffer)) > 0) { // Compliant
                                bos.write(buffer, 0, nBytes);
                                totalSizeEntry += nBytes;
                                totalSizeArchive += nBytes;

                                double compressionRatio = (double) totalSizeEntry / ze.getCompressedSize();
                                if (compressionRatio > THRESHOLD_RATIO) {
                                    // ratio between compressed and uncompressed data is highly suspicious, looks like a Zip Bomb Attack
                                    break;
                                }
                            }
                        }

                        if(totalSizeArchive > THRESHOLD_SIZE) {
                            // the uncompressed data size is too much for the application resource capacity
                            break;
                        }

                        if(totalEntryArchive > THRESHOLD_ENTRIES) {
                            // too much entries in this archive, can lead to inodes exhaustion of the system
                            break;
                        }

				}
			}
			zipFile.close();
		} catch (IOException ioex) {
			// ioex.printStackTrace();
            LOGGER.log(Level.SEVERE, "Exception occur", ioex);
		}
		return totalsize;
	}
}
