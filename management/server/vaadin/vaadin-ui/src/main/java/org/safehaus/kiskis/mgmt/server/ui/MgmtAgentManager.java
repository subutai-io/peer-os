package org.safehaus.kiskis.mgmt.server.ui;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.*;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.api.agentmanager.AgentListener;
import org.safehaus.kiskis.mgmt.shared.protocol.settings.Common;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.shared.protocol.Disposable;

/**
 * @author dilshat
 */
@SuppressWarnings("serial")

public final class MgmtAgentManager extends ConcurrentComponent
        implements AgentListener, Disposable {

    private final AgentManager agentManager;
    private final Tree tree;
    private HierarchicalContainer container;
    private static final Logger LOG = Logger.getLogger(MgmtAgentManager.class.getName());
    private Set<Agent> currentAgents = new HashSet<Agent>();
    private Set<Agent> selectedAgents = new HashSet<Agent>();

    public Set<Agent> getSelectedAgents() {
        return Collections.unmodifiableSet(selectedAgents);
    }

    public MgmtAgentManager(AgentManager agentManager, final boolean global) {
        this.agentManager = agentManager;

        setSizeFull();
        setMargin(true);

        tree = new Tree("List of nodes");
        tree.setContainerDataSource(getNodeContainer());
        tree.setItemIconPropertyId("icon");
        tree.setItemDescriptionGenerator(new AbstractSelect.ItemDescriptionGenerator() {

            @Override
            public String generateDescription(Component source, Object itemId,
                    Object propertyId) {
                String description = "";

                Item item = tree.getItem(itemId);
                if (item != null) {
                    Agent agent = (Agent) item.getItemProperty("value").getValue();
                    if (agent != null) {
                        description = "Hostname: " + agent.getHostname() + "<br>"
                                + "Is LXC: " + agent.isIsLXC() + "<br>"
                                + "UUID: " + agent.getUuid() + "<br>"
                                + "IP: " + agent.getListIP();
                    }
                }

                return description;
            }
        });
        tree.setMultiSelect(true);
        tree.setImmediate(true);
        tree.addListener(new Property.ValueChangeListener() {

            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                if (event.getProperty().getValue() instanceof Set) {
                    Tree t = (Tree) event.getProperty();

                    Set<Agent> selectedList = new HashSet<Agent>();

                    for (Object o : (Iterable<?>) t.getValue()) {
                        if (tree.getItem(o).getItemProperty("value").getValue() != null) {
                            Agent agent = (Agent) tree.getItem(o).getItemProperty("value").getValue();
                            selectedList.add(agent);
                        }
                    }

                    if (global) {
                        MgmtApplication.setSelectedAgents(selectedList);
                    } else {
                        selectedAgents = selectedList;
                    }

                }
            }
        });
        addComponent(tree);

        agentManager.addListener(this);
    }

    @Override
    public synchronized void setParent(Component parent) {
        super.setParent(parent);
    }

    @Override
    public synchronized Component getParent() {
        return super.getParent();
    }

    @Override
    public void onAgent(final Set<Agent> freshAgents) {
        executeUpdate(new Runnable() {
            @Override
            public void run() {
                refreshAgents(freshAgents);
            }
        });
    }

    public HierarchicalContainer getNodeContainer() {
        container = new HierarchicalContainer();
        container.addContainerProperty("value", Agent.class, null);
        container.addContainerProperty("icon", Resource.class,
                new ThemeResource("icons/16/folder.png"));
        refreshAgents(agentManager.getAgents());
        return container;
    }

    private void refreshAgents(Set<Agent> allFreshAgents) {
        if (allFreshAgents != null && tree != null) {
            try {

                currentAgents.removeAll(allFreshAgents);

                if (!currentAgents.isEmpty()) {
                    for (Agent missingAgent : currentAgents) {
                        container.removeItemRecursively(missingAgent.getUuid());
                    }
                }

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

                        Item parent = container.getItem(parentAgent.getUuid());
                        //agent is not yet in the tree
                        if (parent == null) {
                            parent = container.addItem(parentAgent.getUuid());
                        }
                        if (parent != null) {
                            tree.setItemCaption(parentAgent.getUuid(), parentAgent.getHostname());
                            parent.getItemProperty("value").setValue(parentAgent);
                            if (family.getValue() != null) {
                                container.setChildrenAllowed(parentAgent.getUuid(), true);
                                for (Agent childAgent : family.getValue()) {
                                    Item child = container.getItem(childAgent.getUuid());
                                    //child is not yet in the tree
                                    if (child == null) {
                                        child = container.addItem(childAgent.getUuid());
                                    }
                                    if (child != null) {
                                        tree.setItemCaption(childAgent.getUuid(), childAgent.getHostname());
                                        child.getItemProperty("value").setValue(childAgent);
                                        child.getItemProperty("icon").setValue(new ThemeResource("icons/16/document.png"));
                                        container.setParent(childAgent.getUuid(), parentAgent.getUuid());
                                        container.setChildrenAllowed(childAgent.getUuid(), false);
                                    }
                                }
                            } else {
                                container.setChildrenAllowed(parentAgent.getUuid(), false);
                            }
                        }
                    }
                }

                //add orphans to tree
                if (!possibleOrpans.isEmpty()) {
                    Item parent = container.getItem(Common.UNKNOWN_LXC_PARENT_NAME);
                    if (parent == null) {
                        parent = container.addItem(Common.UNKNOWN_LXC_PARENT_NAME);
                    }
                    if (parent != null) {
                        container.setChildrenAllowed(Common.UNKNOWN_LXC_PARENT_NAME, true);
                        for (Agent orphanAgent : possibleOrpans) {
                            Item child = container.getItem(orphanAgent.getUuid());
                            //orphan is not yet in the tree
                            if (child == null) {
                                child = container.addItem(orphanAgent.getUuid());
                            }
                            if (child != null) {
                                tree.setItemCaption(orphanAgent.getUuid(), orphanAgent.getHostname());
                                child.getItemProperty("value").setValue(orphanAgent);
                                child.getItemProperty("icon").setValue(new ThemeResource("icons/16/document.png"));
                                container.setParent(orphanAgent.getUuid(), Common.UNKNOWN_LXC_PARENT_NAME);
                                container.setChildrenAllowed(orphanAgent.getUuid(), false);
                            }
                        }
                    }
                } else {
                    container.removeItemRecursively(Common.UNKNOWN_LXC_PARENT_NAME);
                }

                currentAgents = allFreshAgents;
                container.sort(new Object[]{"value"}, new boolean[]{true});

            } catch (Property.ReadOnlyException ex) {
                LOG.log(Level.SEVERE, "Error in refreshAgents", ex);
            } catch (Property.ConversionException ex) {
                LOG.log(Level.SEVERE, "Error in refreshAgents", ex);
            }
        }
    }

    public void dispose() {
        agentManager.removeListener(this);
    }
}
