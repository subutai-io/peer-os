package org.safehaus.kiskis.mgmt.server.ui.modules.monitor.view;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;

public class UIUtil {

    public static Button getButton(String name, float width/*, final Chain chain*/) {

        Button button = new Button(name);
        button.setWidth(width, Sizeable.UNITS_PIXELS);

        button.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                //chain.start(new Context());
            }
        });

        return button;
    }

    public static TextArea getTextArea(float width, float height) {

        TextArea textArea = new TextArea("Log:");
        textArea.setWidth(width, Sizeable.UNITS_PIXELS);
        textArea.setHeight(height, Sizeable.UNITS_PIXELS);
        textArea.setWordwrap(false);

        return textArea;
    }

    public static Label getLabel(String text, float width, float height) {

        Label label = new Label(text);
        label.setWidth(width, Sizeable.UNITS_PIXELS);
        label.setHeight(height, Sizeable.UNITS_PIXELS);
        label.setContentMode(Label.CONTENT_XHTML);

        return label;
    }
}
