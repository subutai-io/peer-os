package org.safehaus.subutai.ui.accumulo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Scanner;
import java.util.logging.Logger;

public class FileUtil {

	private static final Logger log = Logger.getLogger(FileUtil.class.getName());

	private static URLClassLoader classLoader;

    /*public static String getContent(String filePath) {
        String content = "";

        try {
            content = readFile(filePath);
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error while reading file: " + e);
        }

        return content;
    }*/

	public static void readFile(String filePath) {

		try {
			String currentPath = System.getProperty("user.dir") + "/res";
			InputStream inputStream = getClassLoader().getResourceAsStream(filePath);

			File folder = new File(currentPath);
			if (!folder.exists()) {
				folder.mkdir();
			}

			OutputStream outputStream = new FileOutputStream(new File(currentPath + "accumulo.png"));
			int read;
			byte[] bytes = new byte[1024];

			while ((read = inputStream.read(bytes)) != -1) {
				outputStream.write(bytes, 0, read);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
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
