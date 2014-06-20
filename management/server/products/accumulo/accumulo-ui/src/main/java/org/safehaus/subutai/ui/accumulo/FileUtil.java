package org.safehaus.subutai.ui.accumulo;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.logging.Logger;

public class FileUtil {

	private static final Logger log = Logger.getLogger(FileUtil.class.getName());

	private static URLClassLoader classLoader;

	public static InputStream readFile(String filePath) throws IOException {

		InputStream resource = getClassLoader().getResourceAsStream(filePath);

		return resource;
	}

	private static URLClassLoader getClassLoader() {

		if (classLoader != null) {
			return classLoader;
		}

		// Needed an instance to get URL, i.e. the static way doesn't work: FileUtil.class.getClass().
		URL url = new FileUtil().getClass().getProtectionDomain().getCodeSource().getLocation();
		classLoader = new URLClassLoader(new URL[] {url}, Thread.currentThread().getContextClassLoader());

		return classLoader;
	}
}
