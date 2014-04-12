package org.safehaus.kiskis.mgmt.ui.hadoop.manager;

import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;

/**
 * Created by daralbaev on 12.04.14.
 */
public class Manager extends Panel {
    private Label indicator;
    private HadoopTable table;

    public Manager() {
        setSizeFull();

        addComponent(getIndicator());
        addComponent(getHadoopTable());
    }

    private Label getIndicator() {
        indicator = new Label();
        indicator.setIcon(new ThemeResource("icons/indicator.gif"));
        indicator.setContentMode(Label.CONTENT_XHTML);
        indicator.setHeight(11, Sizeable.UNITS_PIXELS);
        indicator.setWidth(50, Sizeable.UNITS_PIXELS);
        indicator.setVisible(false);

        return indicator;
    }

    private HadoopTable getHadoopTable() {
        if (table == null) {
            table = new HadoopTable("Hadoop Clusters");
        }

        return table;
    }
}
