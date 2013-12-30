package org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.management;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import java.util.ArrayList;
import java.util.List;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.shared.protocol.api.AgentManagerInterface;

import java.util.UUID;
import org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.wizard.exec.ServiceManager;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;

/**
 * Created with IntelliJ IDEA. User: daralbaev Date: 12/1/13 Time: 1:38 AM
 */
public class NodesWindow extends Window {

    private final Table table;
    private IndexedContainer container;
//    private final List<UUID> nodes;
    ServiceManager manager;
    CassandraClusterInfo cci;

    /**
     *
     * @param caption
     * @param cci
     * @param manager
     */
    public NodesWindow(String caption, CassandraClusterInfo cci, ServiceManager manager) {
        this.cci = cci;
        this.manager = manager;

        setCaption(caption);
        setSizeUndefined();
        setWidth("800px");
        setHeight("500px");
        setModal(true);
        center();
        VerticalLayout verticalLayout = new VerticalLayout();
        HorizontalLayout buttons = new HorizontalLayout();

        Button addNewNode = new Button("Add new seed");
        addNewNode.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {

            }
        });
        buttons.addComponent(addNewNode);

        table = new Table("", getCassandraContainer());
        table.setSizeFull();
        table.setPageLength(6);
        table.setImmediate(true);

        verticalLayout.addComponent(buttons);
        verticalLayout.addComponent(table);
        addComponent(verticalLayout);

    }

    private IndexedContainer getCassandraContainer() {
        container = new IndexedContainer();
        container.addContainerProperty("hostname", String.class, "");
        container.addContainerProperty("uuid", UUID.class, "");
        container.addContainerProperty("Start", Button.class, "");
        container.addContainerProperty("Stop", Button.class, "");
        container.addContainerProperty("Seed", Button.class, "");
        container.addContainerProperty("Destroy", Button.class, "");
        for (UUID uuid : cci.getNodes()) {
            Agent agent = getAgentManager().getAgent(uuid);
            addOrderToContainer(container, agent);
            manager.statusCassandraService(uuid);
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

        Button setSeedsButton = new Button("Set as seed");
        if (cci.getSeeds().contains(agent.getUuid())) {
            setSeedsButton.setCaption("Remove seed");
            setSeedsButton.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                   List<UUID> seeds = new ArrayList<UUID>(cci.getSeeds());
                    seeds.remove(agent.getUuid());
                    cci.setSeeds(seeds);
                    manager.updateSeeds(cci);
                    if (ServiceLocator.getService(CommandManagerInterface.class).saveCassandraClusterData(cci)) {
                        System.out.println("updated");
                    }
                }
            });
        } else {
            setSeedsButton.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    List<UUID> seeds = new ArrayList<UUID>(cci.getSeeds());
                    seeds.add(agent.getUuid());
                    cci.setSeeds(seeds);
                    manager.updateSeeds(cci);
                    if (ServiceLocator.getService(CommandManagerInterface.class).saveCassandraClusterData(cci)) {
                        System.out.println("updated");
                    }
                }
            });
        }

        Button destroyButton = new Button("Destroy");
        destroyButton.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                manager.uninstallCassandraService(agent);
                cci.getNodes().remove(agent.getUuid());
                if (ServiceLocator.getService(CommandManagerInterface.class).saveCassandraClusterData(cci)) {
                    System.out.println("updated");
                }
            }
        });
        item.getItemProperty("Start").setValue(startButton);
        item.getItemProperty("Stop").setValue(stopButton);
        item.getItemProperty("Seed").setValue(setSeedsButton);
        item.getItemProperty("Destroy").setValue(destroyButton);
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

//    public void setOutput(Response response) {
//        System.out.println("setoutput" + response.getTaskUuid());
//        for (ParseResult pr : ServiceLocator.getService(CommandManagerInterface.class).parseTask(response.getTaskUuid(), true)) {
//            terminal.setValue(pr.getResponse().getStdOut());
//        }
//        manager.onResponse(response);
//    }
}
