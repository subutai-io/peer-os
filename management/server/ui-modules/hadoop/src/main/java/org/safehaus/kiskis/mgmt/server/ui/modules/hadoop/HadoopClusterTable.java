package org.safehaus.kiskis.mgmt.server.ui.modules.hadoop;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Table;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.safehaus.kiskis.mgmt.shared.protocol.HadoopClusterInfo;
import org.safehaus.kiskis.mgmt.shared.protocol.ParseResult;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: daralbaev
 * Date: 11/30/13
 * Time: 6:56 PM
 */
public class HadoopClusterTable extends Table {
    private IndexedContainer container;

    public HadoopClusterTable() {
        this.setCaption(" Hadoop Clusters");
        this.setContainerDataSource(getContainer());

        this.setWidth("100%");
        this.setHeight(100, Sizeable.UNITS_PERCENTAGE);

        this.setPageLength(10);
        this.setSelectable(true);
        this.setImmediate(true);
    }

    private IndexedContainer getContainer() {
        container = new IndexedContainer();

        // Create the container properties
        container.addContainerProperty("name", String.class, "");
        container.addContainerProperty("Name Node", String.class, "");
        container.addContainerProperty("Secondary Name Node", String.class, "");
        container.addContainerProperty("Job Tracker", String.class, "");
        container.addContainerProperty("Data Nodes", Integer.class, "");
        container.addContainerProperty("Task Trackers", Integer.class, "");

        // Create some orders
        List<HadoopClusterInfo> cdList = getCommandManager().getHadoopClusterData();
        for (HadoopClusterInfo cluster : cdList) {
            addOrderToContainer(container, cluster);
        }

        return container;
    }

    private void addOrderToContainer(Container container, HadoopClusterInfo cluster) {
        Object itemId = container.addItem();
        Item item = container.getItem(itemId);

        item.getItemProperty("name").setValue(cluster.getClusterName());
//        item.getItemProperty("Name Node").setValue(getCommandManager().);
    }

    private void refreshDataSource(ParseResult parseResult) {
//        this.setCaption(agent.getHostname() + " LXC containers");
//
//        String[] lxcs = parseResult.getResponse().getStdOut().split("\\n");
//        ArrayList<String> startedLXC = new ArrayList<String>();
//        ArrayList<String> stoppedLXC = new ArrayList<String>();
//        ArrayList<String> frozenLXC = new ArrayList<String>();
//
//        ArrayList<String> temp = null;
//        for(String s : lxcs){
//            if(s.trim().contains("RUNNING")){
//                temp = startedLXC;
//            } else if(s.trim().contains("STOPPED")){
//                temp = stoppedLXC;
//            }  else if(s.trim().contains("FROZEN")){
//                temp = frozenLXC;
//            } else {
//                if (!Strings.isNullOrEmpty(s.trim()) && temp != null && !s.trim().equals("base-container")) {
//                    temp.add(s.trim());
//                }
//            }
//
//        }
//
//        this.setContainerDataSource(getContainer(startedLXC, stoppedLXC));
    }

    public CommandManagerInterface getCommandManager() {
        // get bundle instance via the OSGi Framework Util class
        BundleContext ctx = FrameworkUtil.getBundle(HadoopModule.class).getBundleContext();
        if (ctx != null) {
            ServiceReference serviceReference = ctx.getServiceReference(CommandManagerInterface.class.getName());
            if (serviceReference != null) {
                return CommandManagerInterface.class.cast(ctx.getService(serviceReference));
            }
        }

        return null;
    }
}
