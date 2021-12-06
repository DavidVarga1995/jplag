package jplagUtils;

import java.awt.Desktop;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Logger;
import java.util.logging.Level;

public class DesktopUtils {

	private static final Logger LOGGER = Logger.getLogger(DesktopUtils.class.getName());

	public static boolean isBrowseSupported() {
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
		if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
			return true;
		} else {
			return false;
		}
	}

	public static void openWebpage(URI uri) {
		if (isBrowseSupported()) {
			Desktop desktop = Desktop.getDesktop();
			try {
				desktop.browse(uri);
			} catch (Exception e) {
				// e.printStackTrace();
				LOGGER.log(Level.SEVERE, "Exception occur", e);
			}
		} else {
			System.out.println("Desktop does not support opening of a browser :/ open " + uri + " yourself");
		}
	}

	public static void openWebpage(URL url) {
		if (isBrowseSupported()) {
			try {
				openWebpage(url.toURI());
			} catch (URISyntaxException e) {
				// e.printStackTrace();
				LOGGER.log(Level.SEVERE, "Exception occur", e);
			}
		} else {
			System.out.println("Desktop does not support opening of a browser :/ open " + url + " yourself");
		}
	}

	public static void openWebpage(String url) throws MalformedURLException {
		if (isBrowseSupported()) {
			openWebpage(new URL((!url.startsWith("https://") && !url.startsWith("http://") ? "http://" : "") + url));
		} else {
			System.out.println("Desktop does not support opening of a browser :/ open " + url + " yourself");
		}
	}
}
