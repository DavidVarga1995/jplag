package jplagUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PropertiesLoader {

	private static final Logger LOGGER = Logger.getLogger(PropertiesLoader.class.getName());

	public static Properties loadProps(String resourceName) {
		Properties props = new Properties();
		try (InputStream in = PropertiesLoader.class.getClassLoader()
				.getResourceAsStream(resourceName)) {
			props.load(in);
			assert in != null;
		} catch (IOException e) {
			// e.printStackTrace();
			LOGGER.log(Level.SEVERE, "Exception occur", e);
		}
		return props;
	}
}
