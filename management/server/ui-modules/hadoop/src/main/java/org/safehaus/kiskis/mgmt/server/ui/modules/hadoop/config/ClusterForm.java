package org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.config;

import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;

/**
 * Created with IntelliJ IDEA.
 * User: daralbaev
 * Date: 1/11/14
 * Time: 4:22 PM
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
        indicator = MgmtApplication.createImage("indicator.gif", 50, 11);
        indicator.setVisible(false);

        return indicator;
    }

    public void setVisible(boolean isVisible) {
        indicator.setVisible(isVisible);
    }
}
