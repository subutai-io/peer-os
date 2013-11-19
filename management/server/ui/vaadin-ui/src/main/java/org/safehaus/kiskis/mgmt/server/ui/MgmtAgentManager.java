package org.safehaus.kiskis.mgmt.server.ui;

import com.vaadin.data.Property;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.VerticalLayout;
import org.safehaus.kiskis.mgmt.server.ui.util.AppData;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.api.AgentManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ui.AgentListener;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created with IntelliJ IDEA.
 * User: daralbaev
 * Date: 11/8/13
 * Time: 7:24 PM
 */
@SuppressWarnings("serial")
public class MgmtAgentManager extends VerticalLayout implements
        Property.ValueChangeListener, AgentListener {

    private Set<Agent> registeredAgents;
    private ListSelect listSelectAgents;
    private int id;
    private AgentManagerInterface agentManagerService;

    public MgmtAgentManager(AgentManagerInterface agentManagerService) {


        setSizeFull();
        setSpacing(true);

        listSelectAgents = new ListSelect("Please select some Registered Agents");
        listSelectAgents.setRows(7);
        listSelectAgents.setNullSelectionAllowed(true);
        listSelectAgents.setMultiSelect(true);
        listSelectAgents.setImmediate(true);
        listSelectAgents.addListener(this);
        listSelectAgents.setSizeFull();

        addComponent(listSelectAgents);

        id = ThreadLocalRandom.current().nextInt(1, 1000000);
        agentManagerService.addListener(this);


        this.registeredAgents = new HashSet<Agent>();
        this.registeredAgents.addAll(agentManagerService.getRegisteredAgents());
        refreshAgents(this.registeredAgents, false);
    }

    /*
     * Shows a notification when a selection is made.
     */
    public void valueChange(Property.ValueChangeEvent event) {
        if (event.getProperty().getValue() instanceof Set) {
            Set<String> agents = (Set<String>) event.getProperty().getValue();

            AppData.setAgentList(agents);
            getWindow().showNotification(agents.toString());
        }
    }

    @Override
    public void agentRegistered(Set<Agent> agents) {

        try {
            Set<Agent> setToRemove = new HashSet<Agent>();
            Set<Agent> setToAdd = new HashSet<Agent>();
            setToRemove.addAll(registeredAgents);
            setToRemove.removeAll(agents);
            refreshAgents(setToRemove, true);

            setToAdd.addAll(agents);
            setToAdd.removeAll(registeredAgents);
            refreshAgents(setToAdd, false);

            registeredAgents.clear();
            registeredAgents.addAll(agents);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            return;
        }
    }

    @Override
    public int getId() {
        return this.id;
    }

    private void refreshAgents(Set<Agent> agentsToAdd, boolean isRemove) {
        if (agentsToAdd != null) {
            for (Agent a : agentsToAdd) {
                if (isRemove) {
                    listSelectAgents.removeItem(a.getUuid());
                } else {
                    listSelectAgents.addItem(a.getUuid());
                }
            }
        }
    }
}