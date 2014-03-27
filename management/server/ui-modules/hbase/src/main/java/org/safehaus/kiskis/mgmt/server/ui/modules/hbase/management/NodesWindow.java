package org.safehaus.kiskis.mgmt.server.ui.modules.hbase.management;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.api.taskrunner.Task;
import org.safehaus.kiskis.mgmt.server.ui.modules.hbase.HBaseConfig;
import org.safehaus.kiskis.mgmt.server.ui.modules.hbase.wizard.exec.ServiceManager;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

/**
 * Created with IntelliJ IDEA. User: daralbaev Date: 12/1/13 Time: 1:38 AM
 */
public class NodesWindow extends Window {

    private final Table table;
    private IndexedContainer container;
    ServiceManager serviceManager;
    HBaseConfig config;
    HBaseCommandEnum cce;
    Item selectedItem;

    /**
     *
     * @param config
     * @param manager
     */
    public NodesWindow(HBaseConfig config, ServiceManager manager) {
        this.config = config;
        this.serviceManager = manager;

        setCaption("Cluster: " + config.getUuid());
        setSizeUndefined();
        setWidth("800px");
        setHeight("500px");
        setModal(true);
        center();
        VerticalLayout verticalLayout = new VerticalLayout();
        HorizontalLayout buttons = new HorizontalLayout();

        table = new Table("", getCassandraContainer());
        table.setSizeFull();
        table.setPageLength(6);
        table.setImmediate(true);
        verticalLayout.addComponent(buttons);
        verticalLayout.addComponent(table);
        addComponent(verticalLayout);

    }

    @Override
    public void addListener(CloseListener listener) {
        getWindow().getParent().removeWindow(this);
    }

    private IndexedContainer getCassandraContainer() {
        container = new IndexedContainer();
        container.addContainerProperty("Hostname", String.class, "");
        container.addContainerProperty("Type", String.class, "");
//        container.addContainerProperty("uuid", UUID.class, "");
//        container.addContainerProperty("Start", Button.class, "");
//        container.addContainerProperty("Stop", Button.class, "");
//        container.addContainerProperty("Status", Button.class, "");
//        container.addContainerProperty("Destroy", Button.class, "");
        for (Agent agent : config.getBackupMasters()) {
            addOrderToContainer(container, agent, "Backup masters");
        }
        for (Agent agent : config.getMaster()) {
            addOrderToContainer(container, agent, "Master");
        }
        for (Agent agent : config.getQuorum()) {
            addOrderToContainer(container, agent, "Quorum");
        }
        for (Agent agent : config.getRegion()) {
            addOrderToContainer(container, agent, "Region");
        }
        return container;
    }

    private void addOrderToContainer(Container container, final Agent agent, String type) {
        Object itemId = container.addItem();
        final Item item = container.getItem(itemId);
        item.getItemProperty("Hostname").setValue(agent.getHostname());
        item.getItemProperty("Type").setValue(type);
//        item.getItemProperty("uuid").setValue(agent.getUuid());

//        Button startButton = new Button("Start");
//        startButton.addListener(new Button.ClickListener() {
//
//            @Override
//            public void buttonClick(Button.ClickEvent event) {
//                getWindow().showNotification("Starting instance: " + agent.getHostname());
//                cce = HBaseCommandEnum.START;
//                selectedItem = item;
//                table.setEnabled(false);
//                serviceManager.runCommand(agent, cce);
//            }
//        });
//        Button stopButton = new Button("Stop");
//        stopButton.addListener(new Button.ClickListener() {
//
//            @Override
//            public void buttonClick(Button.ClickEvent event) {
//                getWindow().showNotification("Stopping instance: " + agent.getHostname());
//                cce = HBaseCommandEnum.STOP;
//                selectedItem = item;
//                table.setEnabled(false);
//                serviceManager.runCommand(agent, cce);
//            }
//        });
//
//        Button statusButton = new Button("Status");
//        statusButton.addListener(new Button.ClickListener() {
//
//            @Override
//            public void buttonClick(Button.ClickEvent event) {
//                getWindow().showNotification("Checking the status: " + agent.getHostname());
//                cce = HBaseCommandEnum.STATUS;
//                selectedItem = item;
//                table.setEnabled(false);
//                serviceManager.runCommand(agent, cce);
//            }
//        });
//
//        item.getItemProperty("Start").setValue(startButton);
//        item.getItemProperty("Stop").setValue(stopButton);
//        item.getItemProperty("Status").setValue(statusButton);
//        item.getItemProperty("Destroy").setValue(destroyButton);
    }

    public static AgentManager getAgentManager() {
        // get bundle instance via the OSGi Framework Util class
        BundleContext ctx = FrameworkUtil.getBundle(NodesWindow.class).getBundleContext();
        if (ctx != null) {
            ServiceReference serviceReference = ctx.getServiceReference(AgentManager.class.getName());
            if (serviceReference != null) {
                return AgentManager.class.cast(ctx.getService(serviceReference));
            }
        }

        return null;
    }

    public void updateUI(Task task) {
        if (cce != null) {
            switch (cce) {
                case START: {
                    switch (task.getTaskStatus()) {
                        case SUCCESS: {
                            switchState(false);
                            getWindow().showNotification("HBase started.");
                            break;
                        }
                        case FAIL: {
                            getWindow().showNotification("Start failed. Please use Terminal to check the problem");
                            break;
                        }
                    }
                    break;
                }
                case STOP: {
                    switch (task.getTaskStatus()) {
                        case SUCCESS: {
                            getWindow().showNotification("HBase stopped.");
                            switchState(true);
                            break;
                        }
                        case FAIL: {
                            getWindow().showNotification("Stop failed. Please use Terminal to check the problem");
                            break;
                        }
                    }
                    break;
                }
                case STATUS: {
                    switch (task.getTaskStatus()) {
                        case SUCCESS: {
                            switchState(false);
                            break;
                        }
                        case FAIL: {
                            switchState(true);
                            break;
                        }
                    }
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
