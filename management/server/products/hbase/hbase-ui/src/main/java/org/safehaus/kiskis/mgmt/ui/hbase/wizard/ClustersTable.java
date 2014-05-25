package org.safehaus.kiskis.mgmt.ui.hbase.wizard;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Table;
import org.safehaus.subutai.api.hadoop.Config;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA. User: daralbaev Date: 11/30/13 Time: 6:56 PM
 */
public class ClustersTable extends Table {

    Map<String, Config> hadoops = new HashMap<String, Config>();

    public ClustersTable() {
        this.setCaption("Hadoop Clusters");
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
        container.addContainerProperty("Cluster", String.class, "");

        // Create some orders
//        List<HadoopClusterInfo> cdList = HadoopDAO.getHadoopClusterInfo();
//        for (HadoopClusterInfo cluster : cdList) {
//            addOrderToContainer(container, cluster);
//        }

        return container;
    }

    private void addOrderToContainer(Container container, Config cluster) {
        Object itemId = container.addItem();
        Item item = container.getItem(itemId);

        item.getItemProperty("Cluster").setValue(cluster.getClusterName());
        hadoops.put(cluster.getClusterName(), cluster);
    }

    public void refreshDataSource() {
        this.setContainerDataSource(getContainer());
    }

    public Config getHCI(String name) {
        return hadoops.get(name);
    }

}
