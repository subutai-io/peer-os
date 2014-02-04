package org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.view;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Button;
import com.vaadin.ui.Layout;
import com.vaadin.ui.TextArea;
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



}
