package org.safehaus.kiskis.mgmt.server.ui.modules.pig.service;

import com.vaadin.ui.TextArea;

import java.text.SimpleDateFormat;
import java.util.Date;

public class UILogger {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
    private final TextArea textArea;

    public UILogger(TextArea textArea) {
        this.textArea = textArea;
    }

    public void info(String message, Object... values) {

        String text = textArea.getValue() + "\n"
                + DATE_FORMAT.format(new Date()) + " | "
                + String.format(message, values);

        textArea.setValue(text);
    }

    public void clear() {
        textArea.setValue("");
    }
}
