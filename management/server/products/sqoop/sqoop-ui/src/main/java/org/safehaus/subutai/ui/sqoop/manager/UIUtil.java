package org.safehaus.subutai.ui.sqoop.manager;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;

public class UIUtil {

    public static Button getButton(String caption, float width) {
        return getButton(caption, width, null);
    }

    public static Button getButton(String caption, float width, Button.ClickListener listener) {
        Button button = new Button(caption);
        button.setWidth(width, Sizeable.UNITS_PIXELS);
        if(listener != null) button.addListener(listener);
        return button;
    }

    public static TextArea getTextArea(String caption, float width, float height) {
        TextArea textArea = new TextArea(caption);
        textArea.setWidth(width, Sizeable.UNITS_PIXELS);
        textArea.setHeight(height, Sizeable.UNITS_PIXELS);
        textArea.setWordwrap(false);
        return textArea;
    }

    public static Label getLabel(String text, float width, int sizeableUnit) {
        Label label = new Label(text);
        label.setWidth(width, sizeableUnit);
        label.setContentMode(Label.CONTENT_XHTML);
        return label;
    }

    public static Label getLabel(String text, float width) {
        return getLabel(text, width, Sizeable.UNITS_PIXELS);
    }

    public static AbstractTextField getTextField(String label, float width) {
        return getTextField(label, width, false);
    }

    public static AbstractTextField getTextField(String label, float width, boolean isPassword) {
        AbstractTextField textField = isPassword
                ? new PasswordField(label) : new TextField(label);
        textField.setWidth(width, Sizeable.UNITS_PIXELS);
        return textField;
    }
}
