package org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.view;

import com.vaadin.ui.TextArea;

import java.text.SimpleDateFormat;
import java.util.Date;

public class UILogger {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
    private static TextArea textArea;

    public static void init(TextArea textArea) {
        UILogger.textArea = textArea;
    }

    public static void info(String message, Object... values) {

        String text = textArea.getValue() + "\n"
                + DATE_FORMAT.format(new Date()) + " | "
                + String.format(message, values);

        textArea.setValue(text);
    }

    public static void clear() {
        textArea.setValue("");
    }
}
