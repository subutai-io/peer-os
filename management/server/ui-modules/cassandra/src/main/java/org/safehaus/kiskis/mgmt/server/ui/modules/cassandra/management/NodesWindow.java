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
import static org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.wizard.exec.ServiceManager.getAgentManager;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.TaskStatus;

/**
 * Created with IntelliJ IDEA. User: daralbaev Date: 12/1/13 Time: 1:38 AM
 */
public class NodesWindow extends Window {

    private final Table table;
    private IndexedContainer container;
    ServiceManager serviceManager;
    CassandraClusterInfo cci;
    CassandraCommandEnum cce;
    Item selectedItem;

    /**
     *
     * @param caption
     * @param cci
     * @param manager
     */
    public NodesWindow(String caption, final CassandraClusterInfo cci, ServiceManager manager) {
        this.cci = cci;
        this.serviceManager = manager;

        setCaption(caption);
        setSizeUndefined();
        setWidth("800px");
        setHeight("500px");
        setModal(true);
        center();
        VerticalLayout verticalLayout = new VerticalLayout();
        HorizontalLayout buttons = new HorizontalLayout();

        Button checkStatusBtn = new Button("Check status");
        checkStatusBtn.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                cce = CassandraCommandEnum.STATUS;
                serviceManager.runCommand(cci.getNodes(), cce);
            }
        });
        buttons.addComponent(checkStatusBtn);
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
//        container.addContainerProperty("uuid", UUID.class, "");
        container.addContainerProperty("Start", Button.class, "");
        container.addContainerProperty("Stop", Button.class, "");
        container.addContainerProperty("Status", Button.class, "");
        container.addContainerProperty("Seed", Button.class, "");
//        container.addContainerProperty("Destroy", Button.class, "");
        for (UUID uuid : cci.getNodes()) {
            Agent agent = getAgentManager().getAgent(uuid);
            addOrderToContainer(container, agent);
//            serviceManager.statusCassandraService(uuid);
        }
        return container;
    }

    private void addOrderToContainer(Container container, final Agent agent) {
        Object itemId = container.addItem();
        final Item item = container.getItem(itemId);
        item.getItemProperty("hostname").setValue(agent.getHostname());
//        item.getItemProperty("uuid").setValue(agent.getUuid());

        Button startButton = new Button("Start");
        startButton.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                cce = CassandraCommandEnum.START;
//                selectedStartButton = event.getButton();
                selectedItem = item;
                serviceManager.runCommand(agent.getUuid(), cce);
                table.setEnabled(false);
            }
        });
        Button stopButton = new Button("Stop");
        stopButton.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                cce = CassandraCommandEnum.STOP;
//                selectedStopButton = event.getButton();
                selectedItem = item;
                serviceManager.runCommand(agent.getUuid(), cce);
                table.setEnabled(false);
            }
        });

        Button statusButton = new Button("Status");
        statusButton.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                cce = CassandraCommandEnum.STATUS;
//                selectedStopButton = event.getButton();
                selectedItem = item;
                serviceManager.runCommand(agent.getUuid(), cce);
                table.setEnabled(false);
            }
        });

        Button setSeedsButton = new Button("Set as seed");
        if (cci.getSeeds().contains(agent.getUuid())) {
            setSeedsButton.setCaption("Remove seed");
            setSeedsButton.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    selectedItem = item;
                    cce = CassandraCommandEnum.REMOVE_SEED;
                    List<UUID> seeds = new ArrayList<UUID>(cci.getSeeds());
                    seeds.remove(agent.getUuid());
                    cci.setSeeds(seeds);

                    StringBuilder seedsSB = new StringBuilder();
                    for (UUID seed : cci.getSeeds()) {
                        Agent agent = getAgentManager().getAgent(seed);
                        seedsSB.append(agent.getListIP().get(0)).append(",");
                    }

                    serviceManager.updateSeeds(cci.getNodes(), seedsSB.substring(0, seedsSB.length() - 1));
                    if (ServiceLocator.getService(CommandManagerInterface.class).saveCassandraClusterData(cci)) {
                        System.out.println("updated");
                    }
                }
            });
        } else {
            setSeedsButton.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    selectedItem = item;
                    cce = CassandraCommandEnum.SET_SEED;
                    List<UUID> seeds = new ArrayList<UUID>(cci.getSeeds());
                    seeds.add(agent.getUuid());
                    cci.setSeeds(seeds);
                    StringBuilder seedsSB = new StringBuilder();
                    for (UUID seed : cci.getSeeds()) {
                        Agent agent = getAgentManager().getAgent(seed);
                        seedsSB.append(agent.getListIP().get(0)).append(",");
                    }

                    serviceManager.updateSeeds(cci.getNodes(), seedsSB.substring(0, seedsSB.length() - 1));
                    if (ServiceLocator.getService(CommandManagerInterface.class).saveCassandraClusterData(cci)) {
                        System.out.println("updated");
                    }
                }
            });
        }

//        Button destroyButton = new Button("Destroy");
//        destroyButton.addListener(new Button.ClickListener() {
//
//            @Override
//            public void buttonClick(Button.ClickEvent event) {
////                serviceManager.uninstallCassandraService(agent);
//
////                cci.getNodes().remove(agent.getUuid());
//                serviceManager.runCommand(agent.getUuid(), CassandraCommandEnum.PURGE);
//                if (ServiceLocator.getService(CommandManagerInterface.class).saveCassandraClusterData(cci)) {
//                    System.out.println("updated");
//                }
//            }
//        });
        item.getItemProperty("Start").setValue(startButton);
        item.getItemProperty("Stop").setValue(stopButton);
        item.getItemProperty("Status").setValue(statusButton);
        item.getItemProperty("Seed").setValue(setSeedsButton);
//        item.getItemProperty("Destroy").setValue(destroyButton);
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

    public void updateUI(TaskStatus ts) {
        if (cce != null) {
            switch (cce) {
                case START: {
                    switchState(false);
                    break;
                }
                case STOP: {
                    switchState(true);
                    break;
                }
                case STATUS: {
                    switch (ts) {
                        case SUCCESS: {
                            switchState(false);
                            break;
                        }
                        case FAIL: {
                            switchState(true);
                            break;
                        }
                    }
//                    Button statusBtn = (Button) selectedItem.getItemProperty("Status").getValue();
//                    statusBtn.setCaption("Running");
                    break;
                }
                case SET_SEED: {
                    Button seed = (Button) selectedItem.getItemProperty("Seed").getValue();
                    seed.setCaption("Remove seed");
                    break;
                }
                case REMOVE_SEED: {
                    Button seed = (Button) selectedItem.getItemProperty("Seed").getValue();
                    seed.setCaption("Set as seed");
                    break;
                }
            }
        }
        table.setEnabled(true);
    }

    private void switchState(Boolean state) {
        Button start = (Button) selectedItem.getItemProperty("Start").getValue();
        start.setEnabled(state);
        Button stop = (Button) selectedItem.getItemProperty("Stop").getValue();
        stop.setEnabled(!state);
    }
}
