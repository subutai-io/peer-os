package org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.config;

import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;

/**
 * Created with IntelliJ IDEA. User: daralbaev Date: 1/11/14 Time: 4:22 PM
 */
public class ClusterForm extends Panel {

    private ClusterTable table;
    private Label indicator;

    public ClusterForm() {

        setSizeFull();
        addComponent(getButtonRefresh());
        addComponent(getIndicator());
        addComponent(getTable(this));
    }

    private Button getButtonRefresh() {
        Button button = new Button("Refresh Hadoop Cluster Table");
        button.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                table.refreshDataSource();
            }
        });

        return button;
    }

    private ClusterTable getTable(ClusterForm form) {
        table = new ClusterTable(form);
        return table;
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

    public void refreshDataSource(boolean isVisible) {
        indicator.setVisible(isVisible);
        if (!isVisible) {
            table.refreshDataSource();
        }
    }
}
