package org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.component;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.api.AgentManagerInterface;

import java.util.List;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: daralbaev
 * Date: 12/1/13
 * Time: 1:38 AM
 */
public class NodesWindow extends Window {
    private Table table;
    private IndexedContainer container;
    private List<UUID> list;
    
    
    /**
     * 
     * @param caption
     * @param list 
     */
    public NodesWindow(String caption, List<UUID> list){
        this.list = list;
        setCaption(caption);
        setSizeUndefined();

        table = new Table("", getCassandraContainer());
        table.setSizeFull();

        table.setPageLength(20);
        table.setImmediate(true);

        addComponent(table);
    }

    private IndexedContainer getCassandraContainer() {
        container = new IndexedContainer();

        // Create the container properties
        container.addContainerProperty("hostname", String.class, "");
        container.addContainerProperty("uuid", UUID.class, "");
        container.addContainerProperty("Start", Button.class, "");
        container.addContainerProperty("Stop", Button.class, "");

        // Create some orders
        for (UUID uuid : list) {
            addOrderToContainer(container, getAgentManager().getAgent(uuid));
        }

        return container;
    }

    private void addOrderToContainer(Container container, Agent agent) {
        Object itemId = container.addItem();
        Item item = container.getItem(itemId);
        item.getItemProperty("hostname").setValue(agent.getHostname());
        item.getItemProperty("uuid").setValue(agent.getUuid());
        item.getItemProperty("Start").setValue(new Button("Start"));
        item.getItemProperty("Stop").setValue(new Button("Stop"));
    }

    public static AgentManagerInterface getAgentManager() {
        // get bundle instance via the OSGi Framework Util class
        BundleContext ctx = FrameworkUtil.getBundle(NodesWindow.class).getBundleContext();
        if (ctx != null) {
            ServiceReference serviceReference = ctx.getServiceReference(AgentManagerInterface.class.getName());
            if (serviceReference != null) {
                return AgentManagerInterface.class.cast(ctx.getService(serviceReference));
            }
        }

        return null;
    }
}
