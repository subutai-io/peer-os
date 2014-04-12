package org.safehaus.kiskis.mgmt.ui.hadoop.manager;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Label;
import com.vaadin.ui.TreeTable;
import org.safehaus.kiskis.mgmt.api.hadoop.Config;
import org.safehaus.kiskis.mgmt.ui.hadoop.HadoopUI;

import java.util.List;

/**
 * Created by daralbaev on 12.04.14.
 */
public class HadoopTable extends TreeTable {

    protected static final String CLUSTER_NAME_PROPERTY = "Cluster Name";
    protected static final String DOMAIN_NAME_PROPERTY = "Domain Name";
    protected static final String NAMENODE_PROPERTY = "NameNode/DataNodes";
    protected static final String SECONDARY_NAMENODE_PROPERTY = "Secondary NameNode";
    protected static final String JOBTRACKER_PROPERTY = "JobTracker/TaskTrackers";
    protected static final String REPLICATION_PROPERTY = "Replication Factor";

    private Label indicator;

    public HadoopTable(String caption, Label indicator) {
        this.indicator = indicator;
        setCaption(caption);

        this.setWidth("100%");
        this.setHeight(100, Sizeable.UNITS_PERCENTAGE);

        this.setPageLength(10);
        this.setSelectable(true);
        this.setImmediate(true);

        addContainerProperty(CLUSTER_NAME_PROPERTY, String.class, null);
        addContainerProperty(DOMAIN_NAME_PROPERTY, String.class, null);
        addContainerProperty(NAMENODE_PROPERTY, Label.class, null);
        addContainerProperty(SECONDARY_NAMENODE_PROPERTY, Label.class, null);
        addContainerProperty(JOBTRACKER_PROPERTY, Label.class, null);
        addContainerProperty(REPLICATION_PROPERTY, Integer.class, null);

        refreshDataSource();
    }

    public void refreshDataSource() {
        indicator.setVisible(true);
        removeAllItems();

        final Object parentId = addItem(new Object[]{"All clusters", null, null, null, null, null});
        setCollapsed(parentId, false);

        List<Config> list = HadoopUI.getHadoopManager().getClusters();
        for (Config cluster : list) {
            Object rowId = addItem(new Object[]{cluster.getClusterName(), cluster.getDomainName(), null, null, null, null});
            setParent(parentId, rowId);
            setCollapsed(rowId, false);
        }
    }
}
