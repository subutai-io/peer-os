package org.safehaus.kiskis.mgmt.server.ui.modules.monitor.view;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.ListSelect;

public class UIUtil {

    public static ComboBox getComboBox(String label, String ... values) {

        ComboBox comboBox = new ComboBox(label);
        comboBox.setInputPrompt(values[0]);

        for (String value : values) {
            comboBox.addItem(value);
        }

        return comboBox;
    }

    public static Button getButton(String name, String width) {

        Button button = new Button(name);
        button.setWidth(width);

        return button;
    }

    public static ListSelect addListSelect(AbsoluteLayout parent, String caption, String position, String width, String height) {

        ListSelect list = new ListSelect(caption);
        list.setWidth(width);
        list.setHeight(height);
        list.setNullSelectionAllowed(false);
        list.setImmediate(true);

        parent.addComponent(list, position);

        return list;
    }
}
