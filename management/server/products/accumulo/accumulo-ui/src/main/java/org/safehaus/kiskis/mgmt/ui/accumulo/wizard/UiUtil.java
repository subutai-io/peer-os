package org.safehaus.kiskis.mgmt.ui.accumulo.wizard;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.TwinColSelect;

/**
 * Created by dilshat on 4/28/14.
 */
public class UiUtil {

    public static ComboBox getCombo(String title) {
        ComboBox combo = new ComboBox(title);
        combo.setMultiSelect(false);
        combo.setImmediate(true);
        combo.setTextInputAllowed(false);
        combo.setRequired(true);
        combo.setNullSelectionAllowed(false);
        return combo;
    }

    public static TwinColSelect getTwinSelect(String title, String captionProperty, String leftTitle, String rightTitle, int rows) {
        TwinColSelect twinColSelect = new TwinColSelect(title);
        twinColSelect.setItemCaptionPropertyId(captionProperty);
        twinColSelect.setRows(rows);
        twinColSelect.setMultiSelect(true);
        twinColSelect.setImmediate(true);
        twinColSelect.setLeftColumnCaption(leftTitle);
        twinColSelect.setRightColumnCaption(rightTitle);
        twinColSelect.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        twinColSelect.setRequired(true);
        return twinColSelect;
    }
}
