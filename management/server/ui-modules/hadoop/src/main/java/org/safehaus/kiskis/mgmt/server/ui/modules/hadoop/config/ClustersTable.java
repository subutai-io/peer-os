package org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.config;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Table;
import java.util.HashMap;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.HadoopClusterInfo;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.HadoopDAO;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA. User: daralbaev Date: 11/30/13 Time: 6:56 PM
 */
public class ClustersTable extends Table {
    
    Map<String, HadoopClusterInfo> hadoops = new HashMap<String, HadoopClusterInfo>();

    public ClustersTable() {
        this.setCaption(" Hadoop Clusters");
        this.setContainerDataSource(getContainer());

        this.setWidth("100%");
        this.setHeight(100, Sizeable.UNITS_PERCENTAGE);

        this.setPageLength(10);
        this.setSelectable(true);
        this.setImmediate(true);

    }

    private IndexedContainer getContainer() {
        IndexedContainer container = new IndexedContainer();

        // Create the container properties
        container.addContainerProperty(HadoopClusterInfo.CLUSTER_NAME_LABEL, String.class, "");
        container.addContainerProperty(HadoopClusterInfo.UUID_LABEL, UUID.class, "");

        // Create some orders
        List<HadoopClusterInfo> cdList = HadoopDAO.getHadoopClusterInfo();
        for (HadoopClusterInfo cluster : cdList) {
            addOrderToContainer(container, cluster);
        }

        return container;
    }

    private void addOrderToContainer(Container container, HadoopClusterInfo cluster) {
        Object itemId = container.addItem();
        Item item = container.getItem(itemId);

        item.getItemProperty(HadoopClusterInfo.CLUSTER_NAME_LABEL).setValue(cluster.getClusterName());
        item.getItemProperty(HadoopClusterInfo.UUID_LABEL).setValue(cluster.getUuid());
        hadoops.put(cluster.getClusterName(), cluster);
    }

    public void refreshDataSource() {
        this.setContainerDataSource(getContainer());
    }
    
    public HadoopClusterInfo getHCI(String name){
        return hadoops.get(name);
    }

}
