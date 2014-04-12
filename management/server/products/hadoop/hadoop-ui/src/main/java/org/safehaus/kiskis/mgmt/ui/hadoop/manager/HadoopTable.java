package org.safehaus.kiskis.mgmt.ui.hadoop.manager;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Label;
import com.vaadin.ui.TreeTable;

/**
 * Created by daralbaev on 12.04.14.
 */
public class HadoopTable extends TreeTable {
    public HadoopTable(String caption) {
        setCaption(caption);

        this.setWidth("100%");
        this.setHeight(100, Sizeable.UNITS_PERCENTAGE);

        this.setPageLength(10);
        this.setSelectable(true);
        this.setImmediate(true);

        addContainerProperty("Cluster Name", Label.class, null);
        addContainerProperty("NameNode/DataNodes", Label.class, null);
        addContainerProperty("Secondary NameNode", Label.class, null);
        addContainerProperty("JobTracker/TaskTrackers", Label.class, null);
        addContainerProperty("Replication Factor", Integer.class, null);
    }

}
