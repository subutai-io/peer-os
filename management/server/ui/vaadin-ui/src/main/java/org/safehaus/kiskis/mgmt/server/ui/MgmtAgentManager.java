package org.safehaus.kiskis.mgmt.server.ui;

import com.vaadin.data.Property;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.VerticalLayout;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ui.AgentInterface;
import org.safehaus.kiskis.mgmt.server.ui.util.AppData;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.api.AgentManagerInterface;

import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: daralbaev
 * Date: 11/8/13
 * Time: 7:24 PM
 */
@SuppressWarnings("serial")
public class MgmtAgentManager extends VerticalLayout implements
        Property.ValueChangeListener, AgentInterface {

    private static final String[] cities = new String[]{"Berlin", "Brussels",
            "Helsinki", "Madrid", "Oslo", "Paris", "Stockholm"};

    private List<Agent> agentList;
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

    private void refreshAgents(){
        listSelectAgents.removeAllItems();
        for (Agent anAgentList : agentList) {
            listSelectAgents.addItem(anAgentList.getHostname());
        }
    }

    /*
     * Shows a notification when a selection is made.
     */
    public void valueChange(Property.ValueChangeEvent event) {
        Set<String> agents = (Set<String>) event.getProperty().getValue();
        AppData.setAgentList(agents);
        getWindow().showNotification("Selected cities: " + agents);
    }

    @Override
    public void agentRegistered(List<Agent> agentList) {
         this.agentList =agentList;
        refreshAgents();
    }
}