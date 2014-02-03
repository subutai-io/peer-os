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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.server.ui.modules.lxc.common.Tasks;
import org.safehaus.kiskis.mgmt.shared.protocol.api.AsyncTaskRunner;
import org.safehaus.kiskis.mgmt.shared.protocol.api.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.api.TaskCallback;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.ResponseType;

@SuppressWarnings("serial")
public class Cloner extends VerticalLayout implements TaskCallback {

    private static final Logger LOG = Logger.getLogger(Cloner.class.getName());

    private final Button cloneBtn;
    private final TextField textFieldLxcName;
    private final Slider slider;
    private final Label indicator;
    private final TreeTable lxcTable;
    private final AsyncTaskRunner taskRunner;
    private final Map<String, String> requestToLxcMatchMap = new HashMap<String, String>();
    private final String physicalHostLabel = "Physical Host";
    private final String statusLabel = "Status";
    private final String okIconSource = "icons/16/ok.png";
    private final String errorIconSource = "icons/16/cancel.png";
    private final String loadIconSource = "../base/common/img/loading-indicator.gif";
    private volatile int taskCount;

    public Cloner(AsyncTaskRunner taskRunner) {
        setSpacing(true);
        setMargin(true);

        this.taskRunner = taskRunner;

        textFieldLxcName = new TextField();
        slider = new Slider();
        slider.setMin(1);
        slider.setMax(20);
        slider.setWidth(150, Sizeable.UNITS_PIXELS);
        slider.setImmediate(true);
        cloneBtn = new Button("Clone");
        cloneBtn.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                startCloneTask();
            }
        });

        Button clearBtn = new Button("Clear");
        clearBtn.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                //clear completed
                for (Iterator it = lxcTable.getItemIds().iterator(); it.hasNext();) {
                    Object rowId = it.next();
                    Item row = lxcTable.getItem(rowId);
                    if (row != null) {
                        Embedded statusIcon = (Embedded) (row.getItemProperty(statusLabel).getValue());
                        if (statusIcon != null
                                && (okIconSource.equals(((ThemeResource) statusIcon.getSource()).getResourceId())
                                || errorIconSource.equals(((ThemeResource) statusIcon.getSource()).getResourceId()))) {
                            lxcTable.removeItem(rowId);
                        }
                    }
                }
                //clear empty parents
                for (Iterator it = lxcTable.getItemIds().iterator(); it.hasNext();) {
                    Object rowId = it.next();
                    Item row = lxcTable.getItem(rowId);
                    if (row != null && row.getItemProperty(physicalHostLabel).getValue() != null
                            && (lxcTable.getChildren(rowId) == null || lxcTable.getChildren(rowId).isEmpty())) {
                        lxcTable.removeItem(rowId);
                    }
                }
            }
        });

        indicator = MgmtApplication.createImage("indicator.gif", 50, 11);
        indicator.setVisible(false);

        GridLayout topContent = new GridLayout(7, 1);
        topContent.setSpacing(true);

        topContent.addComponent(new Label("Product name"));
        topContent.addComponent(textFieldLxcName);
        topContent.addComponent(new Label("Lxc count"));
        topContent.addComponent(slider);
        topContent.addComponent(cloneBtn);
        topContent.addComponent(clearBtn);
        topContent.addComponent(indicator);
        topContent.setComponentAlignment(indicator, Alignment.MIDDLE_CENTER);
        addComponent(topContent);

        lxcTable = createLxcTable("Lxc containers", 500);
        addComponent(lxcTable);
    }

    private TreeTable createLxcTable(String caption, int size) {
        TreeTable table = new TreeTable(caption);
        table.addContainerProperty(physicalHostLabel, String.class, null);
        table.addContainerProperty("Lxc Host", String.class, null);
        table.addContainerProperty(statusLabel, Embedded.class, null);
        table.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        table.setHeight(size, Sizeable.UNITS_PIXELS);
        table.setPageLength(10);
        table.setSelectable(false);
        table.setImmediate(true);
        return table;
    }

    private Table createTasksTable(String caption, int size) {
        Table table = new Table(caption);
        table.addContainerProperty("Physical Hosts", String.class, null);
        table.addContainerProperty("Check status", Button.class, null);
        table.addContainerProperty(statusLabel, Embedded.class, null);
        table.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        table.setHeight(size, Sizeable.UNITS_PIXELS);
        table.setPageLength(10);
        table.setSelectable(false);
        table.setImmediate(true);
        return table;
    }

    private void populateLxcTable(Map<Agent, List<String>> agents) {

        for (final Agent agent : agents.keySet()) {
            if (lxcTable.getItem(agent.getHostname()) == null) {
                lxcTable.addItem(new Object[]{agent.getHostname(), null, null}, agent.getHostname());
            }
            lxcTable.setCollapsed(agent.getHostname(), false);
            for (String lxc : agents.get(agent)) {
                Embedded progressIcon = new Embedded("", new ThemeResource(loadIconSource));

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

    private void startCloneTask() {
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
                show("Select at least one physical agent");
            } else if (Util.isStringEmpty(textFieldLxcName.getValue().toString())) {
                show("Enter product name");
            } else {
                //do the magic
                String productName = textFieldLxcName.getValue().toString().trim();
                Task task = Tasks.getCloneTask(physicalAgents, productName, (Double) slider.getValue());
                Map<Agent, List<String>> agentFamilies = new HashMap<Agent, List<String>>();
                for (Agent physAgent : physicalAgents) {
                    List<String> lxcNames = new ArrayList<String>();
                    for (Command cmd : task.getCommands()) {
                        if (cmd.getRequest().getUuid().compareTo(physAgent.getUuid()) == 0) {
                            String lxcHostname
                                    = cmd.getRequest().getArgs().get(cmd.getRequest().getArgs().size() - 1);
                            requestToLxcMatchMap.put(task.getUuid() + "-" + cmd.getRequest().getRequestSequenceNumber(),
                                    lxcHostname);

                            lxcNames.add(lxcHostname);
                        }
                    }
                    agentFamilies.put(physAgent, lxcNames);
                }
                populateLxcTable(agentFamilies);
                indicator.setVisible(true);
                taskCount++;
                taskRunner.executeTask(task, this);
            }
        } else {
            show("Select at least one physical agent");
        }
    }

    private void show(String msg) {
        getWindow().showNotification(msg);
    }

    @Override
    public void onResponse(Task task, Response response) {
        if (Util.isFinalResponse(response)) {
            String lxcHost = requestToLxcMatchMap.get(task.getUuid() + "-" + response.getRequestSequenceNumber());
            if (lxcHost != null) {
                Item row = lxcTable.getItem(lxcHost);
                if (row != null) {
                    if (response.getType() == ResponseType.EXECUTE_RESPONSE_DONE && response.getExitCode() == 0) {
                        row.getItemProperty("Status").setValue(new Embedded("", new ThemeResource(okIconSource)));
                    } else {
                        row.getItemProperty("Status").setValue(new Embedded("", new ThemeResource(errorIconSource)));
                    }
                }
            }
            requestToLxcMatchMap.remove(task.getUuid() + "-" + response.getRequestSequenceNumber());
        }
        if (task.isCompleted()) {
            taskCount--;
            if (taskCount == 0) {
                indicator.setVisible(false);
                requestToLxcMatchMap.clear();
            }
        }
    }
}
