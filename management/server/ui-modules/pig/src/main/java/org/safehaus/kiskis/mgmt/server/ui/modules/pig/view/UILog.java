package org.safehaus.kiskis.mgmt.server.ui.modules.pig.view;

import com.vaadin.ui.TextArea;

import java.text.SimpleDateFormat;
import java.util.Date;

public class UILog {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
    private final TextArea textArea;

    public UILog(TextArea textArea) {
        this.textArea = textArea;
    }

    public void log(String message, Object ... values) {

        String s = String.format(message, values);
        s = String.format("%s | %s", DATE_FORMAT.format(new Date()), s);

        textArea.setValue(textArea.getValue() + "\n" + s);
    }

    public void clear() {
        textArea.setValue("");
    }
}
