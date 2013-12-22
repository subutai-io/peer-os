package org.safehaus.kiskis.mgmt.server.ui;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.api.AgentManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ui.AgentListener;
import org.safehaus.kiskis.mgmt.shared.protocol.settings.Common;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA. User: daralbaev Date: 11/8/13 Time: 7:24 PM
 */
@SuppressWarnings("serial")

public final class MgmtAgentManager extends VerticalLayout implements
        Property.ValueChangeListener, AgentListener {

    private final AgentManagerInterface agentManagerInterface;
    private final Tree tree;
    private HierarchicalContainer container;
    private static final Logger LOG = Logger.getLogger(MgmtAgentManager.class.getName());

    public MgmtAgentManager(AgentManagerInterface agentManagerService) {

        this.agentManagerInterface = agentManagerService;
        setSizeFull();
        setMargin(true);
        tree = new Tree("List of nodes", getNodeContainer());
        tree.setMultiSelect(true);
        tree.setImmediate(true);
        tree.addListener(this);
        addComponent(getRefreshButton());
        addComponent(tree);
        agentManagerService.addListener(this);
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
                if (tree.getItem(o).getItemProperty("value").getValue() != null) {
                    selectedList.add((Agent) tree.getItem(o).getItemProperty("value").getValue());
                }
            }

            MgmtApplication.setSelectedAgents(selectedList);
            getWindow().showNotification(
                    "Selected agents",
                    selectedList.toString(),
                    Window.Notification.TYPE_TRAY_NOTIFICATION);
        }
    }

    private Button getRefreshButton() {
        Button button = new Button("Refresh");
        button.setDescription("Gets LXC agents from Cassandra");
        button.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {

                tree.setValue(null);
                container.removeAllItems();
                if (MgmtApplication.getSelectedAgents() != null) {
                    MgmtApplication.getSelectedAgents().clear();
                }
                refreshAgents(agentManagerInterface.getRegisteredAgents());
            }
        });
        return button;
    }

    @Override
    public void onAgent(List<Agent> freshAgents) {
        refreshAgents(freshAgents);
    }

    public HierarchicalContainer getNodeContainer() {
        container = new HierarchicalContainer();
        container.addContainerProperty("value", Agent.class, null);
        container.addContainerProperty("icon", Resource.class,
                new ThemeResource("icons/16/folder.png"));
        refreshAgents(agentManagerInterface.getRegisteredAgents());
        return container;
    }

    private void refreshAgents(List<Agent> allFreshAgents) {
        if (allFreshAgents != null) {
            try {

                //remember selection
                Set<String> selectedHostnames = (Set<String>) tree.getValue();

                //clear tree
                container.removeAllItems();

                //grab parents
                Set<Agent> parents = new HashSet<Agent>();
                for (Agent agent : allFreshAgents) {
                    if (!agent.isIsLXC() && agent.getUuid() != null && agent.getHostname() != null) {
                        parents.add(agent);
                    }
                }

                //find children
                Set<Agent> possibleOrpans = new HashSet<Agent>();
                Map<Agent, Set<Agent>> families = new HashMap<Agent, Set<Agent>>();
                if (!parents.isEmpty()) {
                    Set<Agent> childAgentsWithParents = new HashSet<Agent>();
                    for (Agent parent : parents) {
                        //find children
                        Set<Agent> children = new HashSet<Agent>();
                        for (Agent possibleChild : allFreshAgents) {
                            if (possibleChild.isIsLXC()
                                    && possibleChild.getUuid() != null && possibleChild.getHostname() != null) {
                                //add for further orphan children processing    
                                possibleOrpans.add(possibleChild);
                                //check if this is own child
                                if (possibleChild.getHostname().startsWith(parent.getHostname() + Common.PARENT_CHILD_LXC_SEPARATOR)) {
                                    children.add(possibleChild);
                                }
                            }
                        }
                        if (!children.isEmpty()) {
                            //add children to parent
                            childAgentsWithParents.addAll(children);
                            families.put(parent, children);
                        } else {
                            families.put(parent, null);
                        }
                    }

                    //remove all child agents having parents
                    possibleOrpans.removeAll(childAgentsWithParents);
                } else {
                    //all agents are orphans
                    for (Agent possibleChild : allFreshAgents) {
                        if (possibleChild.isIsLXC()
                                && possibleChild.getUuid() != null && possibleChild.getHostname() != null) {
                            //add for further orphan children processing 
                            possibleOrpans.add(possibleChild);
                        }
                    }
                }

                //add families to tree
                if (!families.isEmpty()) {
                    for (Map.Entry<Agent, Set<Agent>> family : families.entrySet()) {
                        Agent parentAgent = family.getKey();
                        Item parent = container.addItem(parentAgent.getHostname());
                        if (parent != null) {
                            container.setChildrenAllowed(parentAgent.getHostname(), true);
                            for (Agent childAgent : family.getValue()) {
                                Item child = container.addItem(childAgent.getHostname());
                                if (child != null) {
                                    child.getItemProperty("value").setValue(childAgent);
                                    child.getItemProperty("icon").setValue(new ThemeResource("icons/16/document.png"));
                                    container.setParent(childAgent.getHostname(), parentAgent.getHostname());
                                    container.setChildrenAllowed(childAgent.getHostname(), false);
                                }
                            }
                        }
                    }
                }

                //add orphans to tree
                if (!possibleOrpans.isEmpty()) {
                    Item parent = container.addItem(Common.UNKNOWN_LXC_PARENT_NAME);
                    if (parent != null) {
                        container.setChildrenAllowed(Common.UNKNOWN_LXC_PARENT_NAME, true);
                        for (Agent orphanAgent : possibleOrpans) {
                            Item child = container.addItem(orphanAgent.getHostname());
                            if (child != null) {
                                child.getItemProperty("value").setValue(orphanAgent);
                                child.getItemProperty("icon").setValue(new ThemeResource("icons/16/document.png"));
                                container.setParent(orphanAgent.getHostname(), Common.UNKNOWN_LXC_PARENT_NAME);
                                container.setChildrenAllowed(orphanAgent.getHostname(), false);
                            }
                        }
                    }
                }

                //return selection and deselect agents in tree that are not in allFreshAgents or have different uuids
                if (tree != null) {
                    if (selectedHostnames != null && !selectedHostnames.isEmpty()) {
                        Set<String> actualSelectedHostnames = new HashSet<String>();
                        for (String selectedHostname : selectedHostnames) {
                            for (Agent agent : allFreshAgents) {
                                if (agent.getHostname().equalsIgnoreCase(selectedHostname)) {
                                    Object value
                                            = container.getItem(selectedHostname).getItemProperty("value").getValue();
                                    if (value != null && value instanceof Agent
                                            && ((Agent) value).getUuid().compareTo(agent.getUuid()) == 0) {
                                        actualSelectedHostnames.add(selectedHostname);
                                    }
                                    break;
                                }
                            }
                        }
                        tree.setValue(actualSelectedHostnames);
                    }
                }
            } catch (Exception ex) {
                LOG.log(Level.SEVERE, "Error in refreshAgents", ex);
            }
        }
    }
}
