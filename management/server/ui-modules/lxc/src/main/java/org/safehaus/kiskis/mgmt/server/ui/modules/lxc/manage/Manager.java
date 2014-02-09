package org.safehaus.kiskis.mgmt.server.ui.modules.lxc.manage;

import com.vaadin.data.Item;
import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.VerticalLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.server.ui.ConfirmationDialogCallback;
import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;
import org.safehaus.kiskis.mgmt.server.ui.modules.lxc.common.Buttons;
import org.safehaus.kiskis.mgmt.server.ui.modules.lxc.common.LxcState;
import org.safehaus.kiskis.mgmt.server.ui.modules.lxc.common.TaskType;
import org.safehaus.kiskis.mgmt.server.ui.modules.lxc.common.Tasks;
import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.shared.protocol.api.AgentManager;
import org.safehaus.kiskis.mgmt.shared.protocol.api.AsyncTaskRunner;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ChainedTaskCallback;
import org.safehaus.kiskis.mgmt.shared.protocol.settings.Common;

@SuppressWarnings("serial")
public class Manager extends VerticalLayout {

    private static final Logger LOG = Logger.getLogger(Manager.class.getName());

    private final AsyncTaskRunner taskRunner;
    private final Label indicator;
    private final Button infoBtn;
    private final Button startAllBtn;
    private final Button stopAllBtn;
    private final Button destroyAllBtn;
    private final TreeTable lxcTable;
    private final Map<UUID, StringBuilder> lxcMap = new HashMap<UUID, StringBuilder>();
    private final AgentManager agentManager;
    private final static String physicalHostLabel = "Physical Host";
    private Set<Agent> physicalAgents;
    private volatile boolean isDestroyAllButtonClicked = false;
    private volatile int taskCount;

    public Manager(AsyncTaskRunner taskRunner) {

        setSpacing(true);
        setMargin(true);

        this.taskRunner = taskRunner;
        this.agentManager = ServiceLocator.getService(AgentManager.class);

        lxcTable = createTableTemplate("Lxc containers", 500);

        infoBtn = new Button(Buttons.INFO.getButtonLabel());
        infoBtn.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                physicalAgents = Util.filterPhysicalAgents(MgmtApplication.getSelectedAgents());

                if (physicalAgents.isEmpty()) {
                    getWindow().showNotification("Select at least one physical agent");
                } else {
                    //do the magic
                    sendGetLxcListCmd(physicalAgents);
                }
            }
        });

        stopAllBtn = new Button(Buttons.STOP_ALL.getButtonLabel());
        stopAllBtn.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                for (Iterator it = lxcTable.getItemIds().iterator(); it.hasNext();) {
                    Item row = lxcTable.getItem(it.next());
                    Button stopBtn = (Button) (row.getItemProperty(Buttons.STOP.getButtonLabel()).getValue());
                    if (stopBtn != null) {
                        stopBtn.click();
                    }
                }
            }
        });
        startAllBtn = new Button(Buttons.START_ALL.getButtonLabel());
        startAllBtn.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                for (Iterator it = lxcTable.getItemIds().iterator(); it.hasNext();) {
                    Item row = lxcTable.getItem(it.next());
                    Button startBtn = (Button) (row.getItemProperty(Buttons.START.getButtonLabel()).getValue());
                    if (startBtn != null) {
                        startBtn.click();
                    }
                }
            }
        });
        destroyAllBtn = new Button(Buttons.DESTROY_ALL.getButtonLabel());
        destroyAllBtn.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                MgmtApplication.showConfirmationDialog(
                        "Lxc destruction confirmation",
                        "Do you want to destroy all lxc nodes?",
                        "Yes", "No", new ConfirmationDialogCallback() {

                            @Override
                            public void response(boolean ok) {
                                if (ok) {
                                    isDestroyAllButtonClicked = true;
                                    for (Iterator it = lxcTable.getItemIds().iterator(); it.hasNext();) {
                                        Item row = lxcTable.getItem(it.next());
                                        Button destroyBtn = (Button) (row.getItemProperty(Buttons.DESTROY.getButtonLabel()).getValue());
                                        if (destroyBtn != null && row.getItemProperty(physicalHostLabel).getValue() == null) {
                                            destroyBtn.click();
                                        }
                                    }
                                    isDestroyAllButtonClicked = false;
                                }
                            }
                        });
            }
        });

        indicator = MgmtApplication.createImage("indicator.gif", 50, 11);
        indicator.setVisible(false);

        GridLayout grid = new GridLayout(5, 1);
        grid.setSpacing(true);

        grid.addComponent(infoBtn);
        grid.addComponent(startAllBtn);
        grid.addComponent(stopAllBtn);
        grid.addComponent(destroyAllBtn);
        grid.addComponent(indicator);
        grid.setComponentAlignment(indicator, Alignment.MIDDLE_CENTER);
        addComponent(grid);

        addComponent(lxcTable);

    }

    private TreeTable createTableTemplate(String caption, int size) {
        TreeTable table = new TreeTable(caption);
        table.addContainerProperty(physicalHostLabel, String.class, null);
        table.addContainerProperty("Lxc Host", String.class, null);
        table.addContainerProperty(Buttons.START.getButtonLabel(), Button.class, null);
        table.addContainerProperty(Buttons.STOP.getButtonLabel(), Button.class, null);
        table.addContainerProperty(Buttons.DESTROY.getButtonLabel(), Button.class, null);
        table.addContainerProperty("Status", Embedded.class, null);
        table.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        table.setHeight(size, Sizeable.UNITS_PIXELS);
        table.setPageLength(10);
        table.setSelectable(false);
        table.setImmediate(true);
        return table;
    }

    public void sendGetLxcListCmd(Set<Agent> physicalAgents) {
        lxcMap.clear();
        lxcTable.setEnabled(false);
        Task getLxcListTask = Tasks.getLxcListTask(physicalAgents);
        executeTask(getLxcListTask, new ChainedTaskCallback() {

            @Override
            public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                if (task.getData() == TaskType.GET_LXC_LIST) {
                    if (lxcMap.get(response.getUuid()) == null) {
                        lxcMap.put(response.getUuid(), new StringBuilder());
                    }
                    if (!Util.isStringEmpty(response.getStdOut())) {
                        lxcMap.get(response.getUuid()).append(response.getStdOut());
                    }

                    if (task.isCompleted()) {
                        Map<String, EnumMap<LxcState, List<String>>> agentFamilies = new HashMap<String, EnumMap<LxcState, List<String>>>();
                        for (Map.Entry<UUID, StringBuilder> parentEntry : lxcMap.entrySet()) {
                            Agent agent = agentManager.getAgentByUUID(parentEntry.getKey());
                            String parentHostname = agent == null
                                    ? String.format("Offline[%s]", parentEntry.getKey()) : agent.getHostname();
                            EnumMap<LxcState, List<String>> lxcs = new EnumMap<LxcState, List<String>>(LxcState.class);
                            String[] lxcStrs = parentEntry.getValue().toString().split("\\n");
                            LxcState currState = null;
                            for (String lxcStr : lxcStrs) {
                                if (LxcState.RUNNING.name().equalsIgnoreCase(lxcStr)) {
                                    if (lxcs.get(LxcState.RUNNING) == null) {
                                        lxcs.put(LxcState.RUNNING, new ArrayList<String>());
                                    }
                                    currState = LxcState.RUNNING;
                                } else if (LxcState.STOPPED.name().equalsIgnoreCase(lxcStr)) {
                                    if (lxcs.get(LxcState.STOPPED) == null) {
                                        lxcs.put(LxcState.STOPPED, new ArrayList<String>());
                                    }
                                    currState = LxcState.STOPPED;
                                } else if (currState != null
                                        && !Util.isStringEmpty(lxcStr) && lxcStr.contains(Common.PARENT_CHILD_LXC_SEPARATOR)) {
                                    lxcs.get(currState).add(lxcStr);
                                }
                            }
                            agentFamilies.put(parentHostname, lxcs);
                        }

                        populateTable(agentFamilies);
                        clearEmptyParents();
                        lxcTable.setEnabled(true);
                    }
                }

                return null;
            }
        });
    }

    private void clearEmptyParents() {
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

    private void populateTable(Map<String, EnumMap<LxcState, List<String>>> agentFamilies) {
        lxcTable.removeAllItems();

        for (Map.Entry<String, EnumMap<LxcState, List<String>>> agentFamily : agentFamilies.entrySet()) {
            final String parentHostname = agentFamily.getKey();
            final Button startAllChildrenBtn = new Button(Buttons.START.getButtonLabel());
            final Button stopAllChildrenBtn = new Button(Buttons.STOP.getButtonLabel());
            final Button destroyAllChildrenBtn = new Button(Buttons.DESTROY.getButtonLabel());
            final Object parentId = lxcTable.addItem(new Object[]{parentHostname, null, startAllChildrenBtn, stopAllChildrenBtn, destroyAllChildrenBtn, null}, parentHostname);
            lxcTable.setCollapsed(parentHostname, false);

            startAllChildrenBtn.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    Collection col = lxcTable.getChildren(parentId);
                    if (col != null) {
                        for (Iterator it = col.iterator(); it.hasNext();) {
                            Item row = lxcTable.getItem(it.next());
                            Button startBtn = (Button) (row.getItemProperty(Buttons.START.getButtonLabel()).getValue());
                            if (startBtn != null) {
                                startBtn.click();
                            }
                        }
                    }
                }
            });

            stopAllChildrenBtn.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    Collection col = lxcTable.getChildren(parentId);
                    if (col != null) {
                        for (Iterator it = col.iterator(); it.hasNext();) {
                            Item row = lxcTable.getItem(it.next());
                            Button stopBtn = (Button) (row.getItemProperty(Buttons.STOP.getButtonLabel()).getValue());
                            if (stopBtn != null) {
                                stopBtn.click();
                            }
                        }
                    }
                }
            });

            destroyAllChildrenBtn.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {

                    MgmtApplication.showConfirmationDialog(
                            "Lxc destruction confirmation",
                            "Do you want to destroy all lxc nodes on this physical node?",
                            "Yes", "No", new ConfirmationDialogCallback() {

                                @Override
                                public void response(boolean ok) {
                                    if (ok) {
                                        Collection col = lxcTable.getChildren(parentId);
                                        if (col != null) {
                                            isDestroyAllButtonClicked = true;
                                            for (Iterator it = col.iterator(); it.hasNext();) {
                                                Item row = lxcTable.getItem(it.next());
                                                Button destroyBtn = (Button) (row.getItemProperty(Buttons.DESTROY.getButtonLabel()).getValue());
                                                if (destroyBtn != null) {
                                                    destroyBtn.click();
                                                }
                                            }
                                            isDestroyAllButtonClicked = false;
                                        }
                                    }
                                }
                            });
                }
            });

            for (Map.Entry<LxcState, List<String>> lxcs : agentFamily.getValue().entrySet()) {

                for (final String lxcHostname : lxcs.getValue()) {
                    final Button startBtn = new Button(Buttons.START.getButtonLabel());
                    final Button stopBtn = new Button(Buttons.STOP.getButtonLabel());
                    final Button destroyBtn = new Button(Buttons.DESTROY.getButtonLabel());
                    final Embedded progressIcon = new Embedded("", new ThemeResource("../base/common/img/loading-indicator.gif"));
                    progressIcon.setVisible(false);

                    if (lxcs.getKey() == LxcState.RUNNING) {
                        startBtn.setEnabled(false);
                    } else if (lxcs.getKey() == LxcState.STOPPED) {
                        stopBtn.setEnabled(false);
                    }
                    final Object rowId = lxcTable.addItem(new Object[]{
                        null,
                        lxcHostname,
                        startBtn,
                        stopBtn,
                        destroyBtn,
                        progressIcon
                    },
                            lxcHostname);

                    lxcTable.setParent(lxcHostname, parentHostname);
                    lxcTable.setChildrenAllowed(lxcHostname, false);

                    startBtn.addListener(new Button.ClickListener() {

                        @Override
                        public void buttonClick(Button.ClickEvent event) {

                            final Agent physicalAgent = agentManager.getAgentByHostname(parentHostname);
                            if (physicalAgent != null) {
                                Task startLxcTask = Tasks.getLxcStartTask(physicalAgent, lxcHostname);
                                startBtn.setEnabled(false);
                                destroyBtn.setEnabled(false);
                                progressIcon.setVisible(true);
                                executeTask(startLxcTask, new ChainedTaskCallback() {

                                    @Override
                                    public Task onResponse(Task task, Response response, String stdOut, String stdErr) {

                                        if (task.getData() == TaskType.START_LXC) {
                                            //send lxc-info cmd
                                            if (task.isCompleted()) {
                                                return Tasks.getLxcInfoWithWaitTask(physicalAgent, lxcHostname);
                                            }
                                        } else if (task.getData() == TaskType.GET_LXC_INFO) {
                                            if (task.isCompleted()) {
                                                if (stdOut.indexOf("RUNNING") != -1) {
                                                    stopBtn.setEnabled(true);
                                                } else {
                                                    startBtn.setEnabled(true);
                                                }
                                                destroyBtn.setEnabled(true);
                                                progressIcon.setVisible(false);
                                            }
                                        }

                                        return null;
                                    }
                                });
                            }
                        }
                    });
                    stopBtn.addListener(new Button.ClickListener() {

                        @Override
                        public void buttonClick(Button.ClickEvent event) {

                            final Agent physicalAgent = agentManager.getAgentByHostname(parentHostname);
                            if (physicalAgent != null) {
                                Task stopLxcTask = Tasks.getLxcStopTask(physicalAgent, lxcHostname);
                                stopBtn.setEnabled(false);
                                destroyBtn.setEnabled(false);
                                progressIcon.setVisible(true);
                                executeTask(stopLxcTask, new ChainedTaskCallback() {

                                    @Override
                                    public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                                        if (task.getData() == TaskType.STOP_LXC) {
                                            //send lxc-info cmd
                                            if (task.isCompleted()) {
                                                return Tasks.getLxcInfoTask(physicalAgent, lxcHostname);
                                            }
                                        } else if (task.getData() == TaskType.GET_LXC_INFO) {
                                            if (task.isCompleted()) {
                                                if (stdOut.indexOf("RUNNING") != -1) {
                                                    stopBtn.setEnabled(true);
                                                } else {
                                                    startBtn.setEnabled(true);
                                                }
                                                destroyBtn.setEnabled(true);
                                                progressIcon.setVisible(false);
                                            }
                                        }

                                        return null;
                                    }
                                });
                            }
                        }
                    });
                    destroyBtn.addListener(new Button.ClickListener() {

                        @Override
                        public void buttonClick(Button.ClickEvent event) {
                            if (!isDestroyAllButtonClicked) {
                                MgmtApplication.showConfirmationDialog(
                                        "Lxc destruction confirmation",
                                        "Do you want to destroy this lxc node?",
                                        "Yes", "No", new ConfirmationDialogCallback() {

                                            @Override
                                            public void response(boolean ok) {
                                                if (ok) {
                                                    final Agent physicalAgent = agentManager.getAgentByHostname(parentHostname);
                                                    if (physicalAgent != null) {
                                                        Task destroyLxcTask = Tasks.getLxcDestroyTask(physicalAgent, lxcHostname);
                                                        startBtn.setEnabled(false);
                                                        stopBtn.setEnabled(false);
                                                        destroyBtn.setEnabled(false);
                                                        progressIcon.setVisible(true);
                                                        executeTask(destroyLxcTask, new ChainedTaskCallback() {

                                                            @Override
                                                            public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                                                                if (task.getData() == TaskType.DESTROY_LXC) {
                                                                    //send lxc-info cmd
                                                                    if (task.isCompleted()) {
                                                                        return Tasks.getLxcInfoTask(physicalAgent, lxcHostname);
                                                                    }
                                                                } else if (task.getData() == TaskType.GET_LXC_INFO) {
                                                                    if (task.isCompleted()) {
                                                                        if (stdOut.indexOf("RUNNING") != -1) {
                                                                            stopBtn.setEnabled(true);
                                                                            destroyBtn.setEnabled(true);
                                                                            progressIcon.setVisible(false);
                                                                        } else {
                                                                            //remove row
                                                                            lxcTable.removeItem(rowId);
                                                                            clearEmptyParents();
                                                                        }
                                                                    }
                                                                }
                                                                return null;
                                                            }
                                                        });
                                                    }
                                                }
                                            }
                                        });
                            } else {

                                final Agent physicalAgent = agentManager.getAgentByHostname(parentHostname);
                                if (physicalAgent != null) {
                                    Task destroyLxcTask = Tasks.getLxcDestroyTask(physicalAgent, lxcHostname);
                                    startBtn.setEnabled(false);
                                    stopBtn.setEnabled(false);
                                    destroyBtn.setEnabled(false);
                                    progressIcon.setVisible(true);
                                    executeTask(destroyLxcTask, new ChainedTaskCallback() {

                                        @Override
                                        public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                                            if (task.getData() == TaskType.DESTROY_LXC) {
                                                //send lxc-info cmd
                                                if (task.isCompleted()) {
                                                    return Tasks.getLxcInfoTask(physicalAgent, lxcHostname);
                                                }
                                            } else if (task.getData() == TaskType.GET_LXC_INFO) {
                                                if (task.isCompleted()) {
                                                    if (stdOut.indexOf("RUNNING") != -1) {
                                                        stopBtn.setEnabled(true);
                                                        destroyBtn.setEnabled(true);
                                                        progressIcon.setVisible(false);
                                                    } else {
                                                        //remove row
                                                        lxcTable.removeItem(rowId);
                                                        clearEmptyParents();
                                                    }
                                                }
                                            }

                                            return null;
                                        }
                                    });
                                }
                            }

                        }
                    });

                }
            }
        }

    }

    private void executeTask(Task task, final ChainedTaskCallback callback) {
        indicator.setVisible(true);
        taskCount++;
        taskRunner.executeTask(task, new ChainedTaskCallback() {

            @Override
            public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                Task nextTask = callback.onResponse(task, response, stdOut, stdErr);
                if (task.isCompleted() && nextTask == null) {
                    taskCount--;
                    if (taskCount == 0) {
                        indicator.setVisible(false);
                    }
                }

                return nextTask;
            }
        });
    }

}
