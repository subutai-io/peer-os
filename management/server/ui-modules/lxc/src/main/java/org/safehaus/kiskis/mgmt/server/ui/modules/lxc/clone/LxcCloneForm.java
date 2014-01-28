package org.safehaus.kiskis.mgmt.server.ui.modules.lxc.clone;

import com.vaadin.data.Item;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.server.ui.modules.lxc.common.Commands;
import org.safehaus.kiskis.mgmt.server.ui.modules.lxc.common.Tasks;
import org.safehaus.kiskis.mgmt.shared.protocol.api.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.api.TaskCallback;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.ResponseType;

@SuppressWarnings("serial")
public class LxcCloneForm extends VerticalLayout implements Button.ClickListener, TaskCallback {

    private static final Logger LOG = Logger.getLogger(LxcCloneForm.class.getName());

    private final Button cloneBtn;
    private final TextField textFieldLxcName;
    private final Slider slider;
    private final Label indicator;
    private final TreeTable lxcTable;
    private final TaskRunner taskRunner;
    private final Map<Integer, String> requestToLxcMatchMap = new HashMap<Integer, String>();
    private final int timeout;
    private Thread operationTimeoutThread;

    public LxcCloneForm(TaskRunner taskRunner) {
        setSpacing(true);
        setMargin(true);

        this.taskRunner = taskRunner;
        timeout = Commands.getCloneCommand().getRequest().getTimeout();

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

        grid.addComponent(new Label("Product name"));
        grid.addComponent(textFieldLxcName);
        grid.addComponent(new Label("Lxc count"));
        grid.addComponent(slider);
        grid.addComponent(cloneBtn);
        grid.addComponent(indicator);
        grid.setComponentAlignment(indicator, Alignment.MIDDLE_CENTER);
        addComponent(grid);

        lxcTable = createTableTemplate("Lxc containers", 500);
        addComponent(lxcTable);
    }

    private TreeTable createTableTemplate(String caption, int size) {
        TreeTable table = new TreeTable(caption);
        table.addContainerProperty("Physical Host", String.class, null);
        table.addContainerProperty("Lxc Host", String.class, null);
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
            lxcTable.addItem(new Object[]{agent.getHostname(), null, null}, agent.getHostname());
            lxcTable.setCollapsed(agent.getHostname(), false);
            for (String lxc : agents.get(agent)) {
                Embedded progressIcon = new Embedded("", new ThemeResource("../base/common/img/loading-indicator.gif"));

                lxcTable.addItem(new Object[]{
                    null,
                    lxc,
                    progressIcon},
                        lxc);

                lxcTable.setParent(lxc, agent.getHostname());
                lxcTable.setChildrenAllowed(lxc, false);
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
                requestToLxcMatchMap.clear();
                String productName = textFieldLxcName.getValue().toString().trim();
                Task task = Tasks.getCloneTask(physicalAgents, productName, (Double) slider.getValue());
                Map<Agent, List<String>> agentFamilies = new HashMap<Agent, List<String>>();
                for (Agent physAgent : physicalAgents) {
                    List<String> lxcNames = new ArrayList<String>();
                    for (Command cmd : task.getCommands()) {
                        if (cmd.getRequest().getUuid().compareTo(physAgent.getUuid()) == 0) {
                            String lxcHostname
                                    = cmd.getRequest().getArgs().get(cmd.getRequest().getArgs().size() - 1);
                            requestToLxcMatchMap.put(cmd.getRequest().getRequestSequenceNumber(),
                                    lxcHostname);

                            lxcNames.add(lxcHostname);
                        }
                    }
                    agentFamilies.put(physAgent, lxcNames);
                }
                populateTable(agentFamilies);
                indicator.setVisible(true);
                cloneBtn.setEnabled(false);
                runTimeoutThread();
                taskRunner.runTask(task, this);
            }
        } else {
            getWindow().showNotification("Select at least one physical agent");
        }
    }

    private void runTimeoutThread() {
        try {
            if (operationTimeoutThread != null && operationTimeoutThread.isAlive()) {
                operationTimeoutThread.interrupt();
            }
            operationTimeoutThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        //wait for timeout + 5 sec just in case
                        Thread.sleep(timeout * 1000 + 5000);

                        cloneBtn.setEnabled(true);
                        indicator.setVisible(false);
                    } catch (InterruptedException ex) {
                    }
                }
            });
            operationTimeoutThread.start();

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error in runTimeoutThread", e);
        }
    }

    @Override
    public void onResponse(Task task, Response response) {
        if (Util.isFinalResponse(response)) {
            String lxcHost = requestToLxcMatchMap.get(response.getRequestSequenceNumber());
            if (lxcHost != null) {
                Item row = lxcTable.getItem(lxcHost);
                if (row != null) {
                    if (response.getType() == ResponseType.EXECUTE_RESPONSE_DONE && response.getExitCode() == 0) {
                        row.getItemProperty("Status").setValue(new Embedded("", new ThemeResource("icons/16/ok.png")));
                    } else {
                        row.getItemProperty("Status").setValue(new Embedded("", new ThemeResource("icons/16/cancel.png")));
                    }
                }
            }
            requestToLxcMatchMap.remove(response.getRequestSequenceNumber());
        }
        if (task.isCompleted()) {
            indicator.setVisible(false);
            cloneBtn.setEnabled(true);
        }
    }
}
