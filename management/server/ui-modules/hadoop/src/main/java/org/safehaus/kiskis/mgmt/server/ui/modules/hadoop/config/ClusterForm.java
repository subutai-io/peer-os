package org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.config;

import com.vaadin.ui.Button;
import com.vaadin.ui.Panel;

/**
 * Created with IntelliJ IDEA.
 * User: daralbaev
 * Date: 1/11/14
 * Time: 4:22 PM
 */
public class ClusterForm extends Panel {

    private ClusterTable table;

    public ClusterForm() {

        setSizeFull();
        addComponent(getButtonRefresh());
        addComponent(getTable());
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

    private ClusterTable getTable() {
        table = new ClusterTable();
        return table;
    }
}
