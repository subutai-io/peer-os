package org.safehaus.kiskis.mgmt.server.ui.modules.oozie.management;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.shared.protocol.api.AgentManager;

import org.safehaus.kiskis.mgmt.server.ui.modules.oozie.OozieConfig;
import static org.safehaus.kiskis.mgmt.server.ui.modules.oozie.management.OozieCommandEnum.START;
import static org.safehaus.kiskis.mgmt.server.ui.modules.oozie.management.OozieCommandEnum.STATUS;
import static org.safehaus.kiskis.mgmt.server.ui.modules.oozie.management.OozieCommandEnum.STOP;
import org.safehaus.kiskis.mgmt.server.ui.modules.oozie.wizard.exec.ServiceManager;
import static org.safehaus.kiskis.mgmt.shared.protocol.enums.TaskStatus.FAIL;
import static org.safehaus.kiskis.mgmt.shared.protocol.enums.TaskStatus.SUCCESS;

/**
 * Created with IntelliJ IDEA. User: daralbaev Date: 12/1/13 Time: 1:38 AM
 */
public class NodesWindow extends Window {

    private final Table table;
    private IndexedContainer container;
    ServiceManager serviceManager;
    OozieConfig config;
    OozieCommandEnum cce;
    Item selectedItem;

    /**
     *
     * @param config
     * @param manager
     */
    public NodesWindow(OozieConfig config, ServiceManager manager) {
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
//        container.addContainerProperty("uuid", UUID.class, "");
        container.addContainerProperty("Start", Button.class, "");
        container.addContainerProperty("Stop", Button.class, "");
        container.addContainerProperty("Status", Button.class, "");
//        container.addContainerProperty("Destroy", Button.class, "");
        for (Agent agent : config.getAgents()) {
            addOrderToContainer(container, agent);
        }
        return container;
    }

    private void addOrderToContainer(Container container, final Agent agent) {
        Object itemId = container.addItem();
        final Item item = container.getItem(itemId);
        item.getItemProperty("Hostname").setValue(agent.getHostname());
//        item.getItemProperty("uuid").setValue(agent.getUuid());

        Button startButton = new Button("Start");
        startButton.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                getWindow().showNotification("Starting instance: " + agent.getHostname());
                cce = OozieCommandEnum.START;
                selectedItem = item;
                table.setEnabled(false);
                serviceManager.runCommand(agent, cce);
            }
        });
        Button stopButton = new Button("Stop");
        stopButton.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                getWindow().showNotification("Stopping instance: " + agent.getHostname());
                cce = OozieCommandEnum.STOP;
                selectedItem = item;
                table.setEnabled(false);
                serviceManager.runCommand(agent, cce);
            }
        });

        Button statusButton = new Button("Status");
        statusButton.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                getWindow().showNotification("Checking the status: " + agent.getHostname());
                cce = OozieCommandEnum.STATUS;
                selectedItem = item;
                table.setEnabled(false);
                serviceManager.runCommand(agent, cce);
            }
        });

        item.getItemProperty("Start").setValue(startButton);
        item.getItemProperty("Stop").setValue(stopButton);
        item.getItemProperty("Status").setValue(statusButton);
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

    public void updateUI(Task ts) {
        if (cce != null) {
            switch (cce) {
                case START: {
                    switch (ts.getTaskStatus()) {
                        case SUCCESS: {
                            switchState(false);
                            break;
                        }
                        case FAIL: {
                            getWindow().showNotification("Start failed. Please use Terminal to check the problem");
                            break;
                        }
                    }

                }
                case STOP: {
                    switch (ts.getTaskStatus()) {
                        case SUCCESS: {
                            switchState(true);
                            break;
                        }
                        case FAIL: {
                            getWindow().showNotification("Stop failed. Please use Terminal to check the problem");
                            break;
                        }
                    }
                }
                case STATUS: {
                    switch (ts.getTaskStatus()) {
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
