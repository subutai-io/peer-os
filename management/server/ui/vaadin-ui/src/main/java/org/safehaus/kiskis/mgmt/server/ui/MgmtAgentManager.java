package org.safehaus.kiskis.mgmt.server.ui;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import org.safehaus.kiskis.mgmt.server.ui.util.AppData;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.api.AgentManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ui.AgentListener;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.shared.protocol.settings.Common;

/**
 * Created with IntelliJ IDEA. User: daralbaev Date: 11/8/13 Time: 7:24 PM
 */
@SuppressWarnings("serial")
public final class MgmtAgentManager extends VerticalLayout implements
        Property.ValueChangeListener, AgentListener {

    private AgentManagerInterface agentManagerInterface;
    private Set<Agent> registeredAgents;
    private Tree tree;
    private HierarchicalContainer container;
    private static final Logger LOG = Logger.getLogger(MgmtAgentManager.class.getName());
    private volatile boolean refresh = true;
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public MgmtAgentManager(AgentManagerInterface agentManagerService) {
        this.agentManagerInterface = agentManagerService;
        setSizeFull();
        //setSpacing(true);
        setMargin(true);

        tree = new Tree("List of nodes", getNodeContainer());
        tree.setMultiSelect(true);
        tree.setImmediate(true);
        tree.addListener(this);
        addComponent(getRefreshButton());
        addComponent(tree);

        agentManagerService.addListener(this);

        executor.execute(new Runnable() {
            @Override
            public void run() {
                while (!Thread.interrupted()) {
                    try {
                        if (refresh) {
                            refresh = false;
                            refreshAgents();
                        }
                        Thread.sleep(500);
                    } catch (Exception e) {
                    }
                }
            }
        });
    }

    /*
     * Shows a notification when a selection is made.
     */
    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        if (event.getProperty().getValue() instanceof Set) {
            Tree t = (Tree) event.getProperty();

            Set<Agent> selectedList = new HashSet<Agent>();

            for (Object o : (Set<Object>) t.getValue()) {
                selectedList.add((Agent) tree.getItem(o).getItemProperty("value").getValue());
            }

            AppData.setSelectedAgentList(selectedList);
            getWindow().showNotification(
                    "Selected agents",
                    selectedList.toString(),
                    Window.Notification.TYPE_TRAY_NOTIFICATION);
        }
    }

    private Button getRefreshButton() {
        Button button = new Button("Refresh agents");
        button.setDescription("Gets LXC agents from Cassandra");
        button.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {

                container.removeAllItems();
                registeredAgents.clear();
                refreshAgents();
                AppData.setSelectedAgentList(registeredAgents);
            }
        });
        return button;
    }

    @Override
    public void agentRegistered() {
        refresh = true;
    }

    public HierarchicalContainer getNodeContainer() {

        registeredAgents = new HashSet<Agent>();
        container = new HierarchicalContainer();
        container.addContainerProperty("name", String.class, null);
        container.addContainerProperty("value", Agent.class, null);
        container.addContainerProperty("icon", ThemeResource.class,
                new ThemeResource("icons/16/document.png"));

        refreshAgents();

        return container;
    }

    private void refreshAgents() {
        try {
            //grab all agents
            Set<Agent> allFreshAgents = agentManagerInterface.getRegisteredAgents();

            // clear all agents
            Set<Agent> setToRemove = new HashSet<Agent>();
            setToRemove.addAll(registeredAgents);
            setToRemove.removeAll(allFreshAgents);
            refreshNodeContainer(setToRemove, true, null);

            //grab parents
            Set<Agent> parents = new HashSet<Agent>();
            for (Agent agent : allFreshAgents) {
                if (!agent.isIsLXC()) {
                    parents.add(agent);
                }
            }
//        setToAdd.add(null);
            refreshNodeContainer(parents, false, null);

            //grab children
            for (Agent parent : parents) {
                Set<Agent> children = new HashSet<Agent>();
                for (Agent possibleChild : allFreshAgents) {
                    if (possibleChild.isIsLXC() && possibleChild.getHostname() != null && possibleChild.getHostname().startsWith(parent.getHostname() + Common.PARENT_CHILD_LXC_SEPARATOR)) {
                        children.add(possibleChild);
                    }
                }
                refreshNodeContainer(children, false, parent);
            }

            registeredAgents.clear();
            registeredAgents.addAll(allFreshAgents);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in refreshAgents", ex);
        }
    }

    public void refreshNodeContainer(Set<Agent> agents, boolean delete, Agent parent) {
        try {
            if (delete) {
                for (Agent agent : agents) {
                    container.removeItemRecursively(agent.getHostname());
                }
            } else {
                for (Agent agent : agents) {
                    if (container.getItem(agent.getHostname()) == null) {
                        if (agent.getUuid() != null && agent.getHostname() != null) {
                            Item item = container.addItem(agent.getHostname());
                            if (item != null) {
                                item.getItemProperty("name").setValue(agent.getUuid());
                                item.getItemProperty("value").setValue(agent);
                                if (!agent.isIsLXC()) {
                                    container.setChildrenAllowed(agent.getHostname(), true);
                                } else {
                                    item.getItemProperty("icon").setValue(new ThemeResource("icons/16/folder.png"));
                                    container.setParent(agent.getHostname(), parent.getHostname());
                                    container.setChildrenAllowed(agent.getHostname(), false);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in refreshNodeContainer", ex);
        }
    }
}