package org.safehaus.subutai.core.monitor.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

class FileUtil {

	private static final Logger log = Logger.getLogger(FileUtil.class.getName());

	private static URLClassLoader classLoader;

	static String getContent(String filePath) {
		String content = "";

		try {
			content = readFile(filePath);
		} catch (Exception e) {
			log.log(Level.SEVERE, "Error while reading file: " + e);
		}

		return content;
	}

	private static String readFile(String filePath) throws IOException {

		InputStream is = getClassLoader().getResourceAsStream(filePath);
		String s = streamToString(is);
		is.close();

		return s;
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

	private static String streamToString(InputStream is) {
		Scanner scanner = new Scanner(is).useDelimiter("\\A");
		return scanner.hasNext() ? scanner.next() : "";
	}
}
