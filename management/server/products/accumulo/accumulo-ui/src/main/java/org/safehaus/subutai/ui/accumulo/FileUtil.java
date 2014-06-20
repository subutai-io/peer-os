package org.safehaus.subutai.ui.accumulo;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Scanner;
import java.util.logging.Logger;

public class FileUtil {

	private static final Logger log = Logger.getLogger(FileUtil.class.getName());

	private static URLClassLoader classLoader;

	public static String getContent(String filePath) {
		String content = "";

		/*try {
//			content = readFile(filePath);
		} catch (Exception e) {
			log.log(Level.SEVERE, "Error while reading file: " + e);
		}*/

		return content;
	}

	public static File readFile(String filePath) throws IOException {

		InputStream is = getClassLoader().getResourceAsStream(filePath);
		File image = File.createTempFile("logo", ".png");
		convertStreamToFile(is, image);

		return image;
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

	public static void convertStreamToFile(InputStream is, File file) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		BufferedWriter fileWriter = new BufferedWriter(new FileWriter(file));
		String line = null;
		while ((line = reader.readLine()) != null) {
			fileWriter.write(line + "\n");
		}
		fileWriter.flush();
		fileWriter.close();
		is.close();
	}

	private static String streamToString(InputStream is) {
		Scanner scanner = new Scanner(is).useDelimiter("\\A");
		return scanner.hasNext() ? scanner.next() : "";
	}
}
