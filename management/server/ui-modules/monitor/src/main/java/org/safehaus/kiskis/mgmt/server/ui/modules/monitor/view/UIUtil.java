package org.safehaus.kiskis.mgmt.server.ui.modules.monitor.view;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;

public class UIUtil {

    public static ComboBox getComboBox(String label, String ... values) {

        ComboBox comboBox = new ComboBox(label);
        comboBox.setInputPrompt(values[0]);

        for (String value : values) {
            comboBox.addItem(value);
        }

        return comboBox;
    }

    public static Button getButton(String name, float width) {
        Button button = new Button(name);
        button.setWidth(width, Sizeable.UNITS_PIXELS);

        return button;
    }
}
