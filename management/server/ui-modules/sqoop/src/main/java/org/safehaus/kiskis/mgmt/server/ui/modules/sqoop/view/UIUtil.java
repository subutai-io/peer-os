package org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.view;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.action.ChainManager;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.common.chain.Chain;

public class UIUtil {

    static Button getButton(String name, float width, final Chain chain) {

        Button button = new Button(name);
        button.setWidth(width, Sizeable.UNITS_PIXELS);

        button.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                ChainManager.run(chain);
            }
        });

        return button;
    }

    static TextArea getTextArea(float width, float height) {

        TextArea textArea = new TextArea("Log:");
        textArea.setWidth(width, Sizeable.UNITS_PIXELS);
        textArea.setHeight(height, Sizeable.UNITS_PIXELS);
        textArea.setWordwrap(false);

        return textArea;
    }

    static Label getLabel(String text, float width, float height) {

        Label label = new Label(text);
        label.setWidth(width, Sizeable.UNITS_PIXELS);
        label.setHeight(height, Sizeable.UNITS_PIXELS);
        label.setContentMode(Label.CONTENT_XHTML);

        return label;
    }

    static AbstractTextField getTextField(String label, float width) {
        return getTextField(label, width, false);
    }

    static AbstractTextField getTextField(String label, float width, boolean isPassword) {

        AbstractTextField textField = isPassword ? new PasswordField(label) : new TextField(label);
        textField.setWidth(width, Sizeable.UNITS_PIXELS);

        return textField;
    }
}
