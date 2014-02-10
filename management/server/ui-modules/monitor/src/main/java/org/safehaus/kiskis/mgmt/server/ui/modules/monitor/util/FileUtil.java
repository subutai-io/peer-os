package org.safehaus.kiskis.mgmt.server.ui.modules.monitor.util;

import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.logging.Logger;

public class FileUtil {

    private static final Logger log = Logger.getLogger(FileUtil.class.getName());

    static URL urlToClass;

    private FileUtil() {
        urlToClass = this.getClass().getProtectionDomain().getCodeSource().getLocation();
    }

    public static String getContent(String filePath) {

        new FileUtil();

        log.info("urlToClass: " + urlToClass);


        ClassLoader currentThreadClassLoader = Thread.currentThread().getContextClassLoader();
        //ClazzL = new URLClassLoader(new URL[]{new File("/home/grant/plugins/MenuPlugin.jar").toURL()}, currentThreadClassLoader);
        final URLClassLoader cl = new URLClassLoader(new URL[]{ urlToClass }, currentThreadClassLoader);

/*        String script = ""
                + " var s = document.createElement('script'); "
                + " s.type = 'text/javascript'; "
                + " var code = 'function hello(){console.log(123);}'; "
                + " try { "
                + " s.appendChild(document.createTextNode(code)); "
                + " document.body.appendChild(s); hello();"   // <-
                + " } catch (e) { "
                + " s.text = code; "
                + " document.body.appendChild(s); console.log(2);"
                + " } ";*/

        log.info("urlToClass: " + urlToClass);

        String script2 = "";

        try {
            InputStream is = cl.getResourceAsStream("js/text.js");
            log.info("is: " + is);
            log.info("size: " + is.available());

             script2 = streamToString(is);

            is.close();

        } catch (Exception e) {
            log.info("error: " + e);
        }

        return script2;
    }

    private static String streamToString(InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}
