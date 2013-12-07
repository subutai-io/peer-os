package org.safehaus.kiskis.mgmt.server.ui.modules.lxc.forms;

import com.vaadin.ui.Button;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import org.safehaus.kiskis.mgmt.server.ui.util.AppData;
import org.safehaus.kiskis.mgmt.shared.protocol.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: daralbaev
 * Date: 12/1/13
 * Time: 5:56 PM
 */
@SuppressWarnings("serial")
public class LxcManageForm extends VerticalLayout {

    private Agent physicalAgent;
    private LxcTable table;

    public LxcManageForm() {
        setSpacing(true);

        Panel panel = new Panel("Manage LXC containers");
        panel.addComponent(getRefreshButton());
        panel.addComponent(getLxcTable());

        addComponent(panel);
    }

    private LxcTable getLxcTable() {
        table = new LxcTable();

        return table;
    }

    private Button getRefreshButton() {
        Button buttonRefresh = new Button("Refresh LXC containers");
        buttonRefresh.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                List<Agent> agents = AppData.getSelectedAgentList();
                if (agents != null && agents.size() > 0) {
                    Set<Agent> physicalAgents = new HashSet<Agent>();
                    for (Agent agent : agents) {
                        if (!agent.isIsLXC()) {
                            physicalAgent = agent;
                            physicalAgents.add(agent);
                        }
                    }

                    if (physicalAgents.size() != 1) {
                        getWindow().showNotification("Select only one physical agent");
                    } else {
                        table.setAgent(physicalAgent);

                    }
                }
            }
        });

        return buttonRefresh;
    }

    public void outputResponse(Response response) {
        table.outputResponse(response);
    }
}
