package org.safehaus.kiskis.mgmt.server.ui;

import com.vaadin.data.Property;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.VerticalLayout;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ui.AgentInterface;
import org.safehaus.kiskis.mgmt.server.ui.util.AppData;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.api.AgentManagerInterface;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: daralbaev
 * Date: 11/8/13
 * Time: 7:24 PM
 */
@SuppressWarnings("serial")
public class MgmtAgentManager extends VerticalLayout implements
        Property.ValueChangeListener, AgentInterface {

    private Set<Agent> agents;
    private ListSelect listSelectAgents;
    private AgentManagerInterface agentManagerService;

    public MgmtAgentManager(AgentManagerInterface agentManagerService) {
        this.agentManagerService = agentManagerService;
        agentManagerService.registerAgentInterface(this);

        setSizeFull();
        setSpacing(true);

        listSelectAgents = new ListSelect("Please select some agents");
        listSelectAgents.setRows(7);
        listSelectAgents.setNullSelectionAllowed(true);
        listSelectAgents.setMultiSelect(true);
        listSelectAgents.setImmediate(true);
        listSelectAgents.addListener(this);
        listSelectAgents.setSizeFull();
        addComponent(listSelectAgents);
    }

    /*
     * Shows a notification when a selection is made.
     */
    public void valueChange(Property.ValueChangeEvent event) {
        if (event.getProperty().getValue() instanceof Set) {
            Set<String> agents = (Set<String>) event.getProperty().getValue();

            AppData.setAgentList(agents);
            getWindow().showNotification("Selected agents: " + agents);
        }
    }

    @Override
    public void agentRegistered(Set<Agent> agents) {
        Set<Agent> agentsToRemove = new HashSet<Agent>();
        Set<Agent> agentsToAdd = new HashSet<Agent>();
        agentsToRemove.addAll(this.agents);
        agentsToAdd.addAll(agents);

        agentsToRemove.removeAll(agents);
        agentsToAdd.removeAll(this.agents);
        this.agents = agents;

        refreshAgents(agentsToRemove, agentsToAdd);
    }

    private void refreshAgents(Set<Agent> agentsToRemove, Set<Agent> agentsToAdd) {
        for(Agent a : agentsToRemove){
            listSelectAgents.removeItem(a.getUuid());
        }

        for(Agent a : agentsToAdd){
            listSelectAgents.addItem(a.getUuid());
        }
    }
}