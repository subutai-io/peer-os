package org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.management;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.shared.protocol.api.AgentManagerInterface;

import java.util.List;
import java.util.UUID;
import org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.wizard.exec.ServiceManager;

/**
 * Created with IntelliJ IDEA. User: daralbaev Date: 12/1/13 Time: 1:38 AM
 */
public class NodesWindow extends Window {

    private final Table table;
    private IndexedContainer container;
    private final List<UUID> list;
    ServiceManager manager;

    /**
     *
     * @param caption
     * @param list
     * @param manager
     */
    public NodesWindow(String caption, List<UUID> list, ServiceManager manager) {
        this.list = list;
        this.manager = manager;
        
        setCaption(caption);
        setSizeUndefined();
        setWidth("600px");
        setHeight("500px");
        setModal(true);
        center();

        table = new Table("", getCassandraContainer());
        table.setSizeFull();
        table.setPageLength(6);
        table.setImmediate(true);

        addComponent(table);

    }

    private IndexedContainer getCassandraContainer() {
        container = new IndexedContainer();
        container.addContainerProperty("hostname", String.class, "");
        container.addContainerProperty("uuid", UUID.class, "");
        container.addContainerProperty("Start", Button.class, "");
        container.addContainerProperty("Stop", Button.class, "");
        for (UUID uuid : list) {
            addOrderToContainer(container, getAgentManager().getAgent(uuid));
        }
        return container;
    }

    private void addOrderToContainer(Container container, final Agent agent) {
        Object itemId = container.addItem();
        Item item = container.getItem(itemId);
        item.getItemProperty("hostname").setValue(agent.getHostname());
        item.getItemProperty("uuid").setValue(agent.getUuid());

        Button startButton = new Button("Start");
        startButton.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                manager.startCassandraService(agent);
            }
        });
        Button stopButton = new Button("Stop");
        stopButton.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                manager.stopCassandraService(agent);
            }
        });
        item.getItemProperty("Start").setValue(startButton);
        item.getItemProperty("Stop").setValue(stopButton);
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


    public void setOutput(Response response) {
//        System.out.println("setoutput" + response.getTaskUuid());
//        for (ParseResult pr : ServiceLocator.getService(CommandManagerInterface.class).parseTask(response.getTaskUuid(), true)) {
//            terminal.setValue(pr.getResponse().getStdOut());
//        }
        manager.onResponse(response);
    }


}
