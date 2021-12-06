/*
 * Created on 10.02.2005
 */
package jplag.options.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import jplag.Program;
import org.apache.commons.io.*;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Author Emeric Kwemou, Moritz Kroll
 */
public class ZipUtil {
  /**
   * 
   * @param file
   * @param dest
   *          Where the zipped file will be stored
   * @return zipped file
   */

  private static final Logger LOGGER = Logger.getLogger(ZipUtil.class.getName());

    public static File zip(File file, String dest) {
    File zippedFile = FileUtils.getFile(dest + "/" + file.getName() + ".zip");
    try {
      FileOutputStream ops = new FileOutputStream(zippedFile);
      ZipOutputStream zos = new ZipOutputStream(ops);
      zip(file, zos, "");
      zos.close();
    } catch (FileNotFoundException fnfex) {
      // fnfex.printStackTrace();
        LOGGER.log(Level.SEVERE, "Exception occur", fnfex);
    } catch (IOException ioex) {
      // ioex.printStackTrace();
        LOGGER.log(Level.SEVERE, "Exception occur", ioex);
    }
    return zippedFile;
  }

  public static ZipOutputStream zip(File file, ZipOutputStream zos, String prefix)
  {
    File[] entries = file.listFiles();
    for (int i = 0; i < entries.length; i++) {
      if (entries[i].isDirectory())
      {
      	// generate directory entry
      	ZipEntry zi=new ZipEntry(prefix + entries[i].getName()+"/");
      	try	{
      	  zos.putNextEntry(zi);
      	  zos.closeEntry();
		}
      	catch (IOException ioex)
		{
      	  // ioex.printStackTrace();
            LOGGER.log(Level.SEVERE, "Exception occur", ioex);
		}
  		zip(entries[i], zos, prefix + entries[i].getName() + "/");
      }
      else
      {
        try {
          FileInputStream fis = new FileInputStream(entries[i]);
          ZipEntry zi = new ZipEntry(prefix + entries[i].getName());
          zos.putNextEntry(zi);
          copystream(fis, zos);
          zos.closeEntry();
        }
        catch (FileNotFoundException ex) {
          // ex.printStackTrace();
            LOGGER.log(Level.SEVERE, "Exception occur", ex);
        }
        catch (IOException ioex) {
          // ioex.printStackTrace();
            LOGGER.log(Level.SEVERE, "Exception occur", ioex);
        }
      }
    }
    return zos;
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
	 * @param file
	 * @param destination
	 *            where the file will be stored
	 * @return total size of all unzipped files
	 * 
	 */
	public static int unzip(File file, String destination, String container) {
		int totalsize = 0;
		File result = FileUtils.getFile(destination + File.separator + container);
		result.mkdir();
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
					ze_f.mkdir();
				else {
					// make sure directories exist in case the client
					// didn't provide directory entries!

					File f = FileUtils.getFile(destination, ze.getName());
					(FileUtils.getFile(f.getParent())).mkdirs();

					FileOutputStream fos = new FileOutputStream(f);
					BufferedOutputStream bos = new BufferedOutputStream(fos);
					InputStream in = zipFile.getInputStream(ze);
					totalsize += copystream(in, bos);
					bos.close();
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
