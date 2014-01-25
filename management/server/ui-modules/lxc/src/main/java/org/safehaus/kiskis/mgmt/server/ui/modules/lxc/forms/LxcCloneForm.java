package org.safehaus.kiskis.mgmt.server.ui.modules.lxc.forms;

import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.*;
import java.util.ArrayList;
import java.util.HashMap;
import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;
import org.safehaus.kiskis.mgmt.shared.protocol.*;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.safehaus.kiskis.mgmt.server.ui.modules.lxc.common.Commands;
import org.safehaus.kiskis.mgmt.shared.protocol.api.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.api.TaskCallback;
import org.safehaus.kiskis.mgmt.shared.protocol.settings.Common;

@SuppressWarnings("serial")
public class LxcCloneForm extends VerticalLayout implements Button.ClickListener, TaskCallback {

    private final Button cloneBtn;
    private final TextField textFieldLxcName;
    private final Slider slider;
    private final Label indicator;
    private final Table lxcTable;
    private final TaskRunner taskRunner;

    public LxcCloneForm(TaskRunner taskRunner) {
        this.taskRunner = taskRunner;
        setSpacing(true);
        Panel panel = new Panel("Clone LXC template");
        textFieldLxcName = new TextField();
        slider = new Slider();
        slider.setMin(1);
        slider.setMax(20);
        slider.setWidth(150, Sizeable.UNITS_PIXELS);
        slider.setImmediate(true);
        cloneBtn = new Button("Clone");
        cloneBtn.addListener(this);

        indicator = MgmtApplication.createImage("indicator.gif", 50, 11);
        indicator.setVisible(false);

        GridLayout grid = new GridLayout(6, 1);
        grid.setSpacing(true);
//        grid.setWidth("100%");

        grid.addComponent(new Label("Product name"));
        grid.addComponent(textFieldLxcName);
        grid.addComponent(new Label("Lxc count"));
        grid.addComponent(slider);
        grid.addComponent(cloneBtn);
        grid.addComponent(indicator);
        panel.addComponent(grid);

        lxcTable = createTableTemplate("Lxc containers", 500);
        panel.addComponent(lxcTable);

        addComponent(panel);
    }

    private Table createTableTemplate(String caption, int size) {
        Table table = new Table(caption);
        table.addContainerProperty("Physical Host", String.class, null);
        table.addContainerProperty("Lxc Host", String.class, null);
        table.addContainerProperty("Check", Button.class, null);
        table.addContainerProperty("Start", Button.class, null);
        table.addContainerProperty("Stop", Button.class, null);
        table.addContainerProperty("Destroy", Button.class, null);
        table.addContainerProperty("Status", Embedded.class, null);
        table.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        table.setHeight(size, Sizeable.UNITS_PIXELS);
        table.setPageLength(10);
        table.setSelectable(false);
        table.setImmediate(true);
        return table;
    }

    private void populateTable(Map<Agent, List<String>> agents) {

        lxcTable.removeAllItems();

        for (final Agent agent : agents.keySet()) {
            for (String lxc : agents.get(agent)) {
                final Button checkBtn = new Button("Check");
                final Button startBtn = new Button("Start");
                final Button stopBtn = new Button("Stop");
                final Button destroyBtn = new Button("Destroy");
                final Embedded progressIcon = new Embedded("", new ThemeResource("../base/common/img/loading-indicator.gif"));
                stopBtn.setEnabled(false);
                startBtn.setEnabled(false);
                progressIcon.setVisible(false);

                final Object rowId = lxcTable.addItem(new Object[]{
                    agent.getHostname(),
                    lxc,
                    checkBtn,
                    startBtn,
                    stopBtn,
                    destroyBtn,
                    progressIcon},
                        null);

                checkBtn.addListener(new Button.ClickListener() {

                    @Override
                    public void buttonClick(Button.ClickEvent event) {
                    }
                });

                startBtn.addListener(new Button.ClickListener() {

                    @Override
                    public void buttonClick(Button.ClickEvent event) {
                    }
                });

                stopBtn.addListener(new Button.ClickListener() {

                    @Override
                    public void buttonClick(Button.ClickEvent event) {
                    }
                });

                destroyBtn.addListener(new Button.ClickListener() {

                    @Override
                    public void buttonClick(Button.ClickEvent event) {
                    }
                });
            }
        }
    }

    @Override
    public void buttonClick(Button.ClickEvent clickEvent) {
        Set<Agent> agents = MgmtApplication.getSelectedAgents();
        if (agents.size() > 0) {
            Set<Agent> physicalAgents = new HashSet<Agent>();
            //filter physical agents
            for (Agent agent : agents) {
                if (!agent.isIsLXC()) {
                    physicalAgents.add(agent);
                }
            }

            if (physicalAgents.isEmpty()) {
                getWindow().showNotification("Select at least one physical agent");
            } else if (Util.isStringEmpty(textFieldLxcName.getValue().toString())) {
                getWindow().showNotification("Enter product name");
            } else {
                //do the magic
                lxcTable.removeAllItems();
                String productName = textFieldLxcName.getValue().toString().trim();
                Task task = new Task("Clone lxc containers for " + productName);
                Map<Agent, List<String>> agentFamilies = new HashMap<Agent, List<String>>();
                for (Agent physAgent : physicalAgents) {
                    StringBuilder lxcHost = new StringBuilder(physAgent.getHostname());
                    lxcHost.append(Common.PARENT_CHILD_LXC_SEPARATOR).append(productName);
                    List<String> lxcNames = new ArrayList<String>();
                    for (int i = 1; i <= (Double) slider.getValue(); i++) {
                        Command cmd = Commands.getCloneCommand();
                        cmd.getRequest().setUuid(physAgent.getUuid());
                        String lxcHostFull = lxcHost.toString() + i;
                        cmd.getRequest().getArgs().set(cmd.getRequest().getArgs().size() - 1, lxcHostFull);
                        task.addCommand(cmd);
                        lxcNames.add(lxcHostFull);
                    }
                    agentFamilies.put(physAgent, lxcNames);
                }
                populateTable(agentFamilies);
                indicator.setVisible(true);
//                taskRunner.runTask(task, this);
            }
        } else {
            getWindow().showNotification("Select at least one physical agent");
        }
    }

    @Override
    public void onResponse(Task task, Response response) {
        if (task.isCompleted()) {
            indicator.setVisible(false);
        }
    }
}
