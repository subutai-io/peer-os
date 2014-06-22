package org.safehaus.subutai.shared.protocol;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.logging.Logger;

public class FileUtil {

	private static final Logger log = Logger.getLogger(FileUtil.class.getName());

	private static URLClassLoader classLoader;

	public static File getFile(String fileName, Object object) {
		String currentPath = System.getProperty("user.dir") + "/res/" + fileName;
		File file = new File(currentPath);
		if (!file.exists()) {
			writeFile(fileName, object);
			file = new File(currentPath);
		}
		return file;
	}

	private static void writeFile(String fileName, Object object) {

		try {
			String currentPath = System.getProperty("user.dir") + "/res";
			InputStream inputStream = getClassLoader(object).getResourceAsStream("img/" + fileName);

			File folder = new File(currentPath);
			if (!folder.exists()) {
				folder.mkdir();
			}

			OutputStream outputStream = new FileOutputStream(new File(currentPath + "/" + fileName));
			int read;
			byte[] bytes = new byte[1024];

			while ((read = inputStream.read(bytes)) != -1) {
				outputStream.write(bytes, 0, read);
			}

			inputStream.close();
			outputStream.close();
		} catch (Exception ex) {
			System.out.println();
			System.out.println(fileName);
			ex.printStackTrace();
		}
	}

	private static URLClassLoader getClassLoader(Object object) {

		if (classLoader != null) {
			return classLoader;
		}

		// Needed an instance to get URL, i.e. the static way doesn't work: FileUtil.class.getClass().
		URL url = object.getClass().getProtectionDomain().getCodeSource().getLocation();
		classLoader = new URLClassLoader(new URL[] {url}, Thread.currentThread().getContextClassLoader());

		return classLoader;
	}
}
