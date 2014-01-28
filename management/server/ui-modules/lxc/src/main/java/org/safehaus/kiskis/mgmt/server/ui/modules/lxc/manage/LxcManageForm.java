package org.safehaus.kiskis.mgmt.server.ui.modules.lxc.manage;

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
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;
import org.safehaus.kiskis.mgmt.server.ui.modules.lxc.common.Commands;
import org.safehaus.kiskis.mgmt.server.ui.modules.lxc.common.LxcState;
import org.safehaus.kiskis.mgmt.server.ui.modules.lxc.common.TaskType;
import org.safehaus.kiskis.mgmt.server.ui.modules.lxc.common.Tasks;
import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.shared.protocol.api.AgentManager;
import org.safehaus.kiskis.mgmt.shared.protocol.api.TaskCallback;
import org.safehaus.kiskis.mgmt.shared.protocol.settings.Common;

@SuppressWarnings("serial")
public class LxcManageForm extends VerticalLayout implements Button.ClickListener, TaskCallback {

    private static final Logger LOG = Logger.getLogger(LxcManageForm.class.getName());

    private final TaskRunner taskRunner;
    private final Label indicator;
    private final Button getLxcsBtn;
    private final TreeTable lxcTable;
    private final int timeout;
    private final Map<UUID, StringBuilder> lxcMap = new HashMap<UUID, StringBuilder>();
    private final AgentManager agentManager;
    private Thread operationTimeoutThread;
    private Set<Agent> physicalAgents;

    public LxcManageForm(TaskRunner taskRunner) {

        setSpacing(true);
        setMargin(true);

        this.taskRunner = taskRunner;
        this.agentManager = ServiceLocator.getService(AgentManager.class);
        timeout = Commands.getLxcListCommand().getRequest().getTimeout();

        getLxcsBtn = new Button("Get LXCs");
        getLxcsBtn.addListener(this);

        indicator = MgmtApplication.createImage("indicator.gif", 50, 11);
        indicator.setVisible(false);

        GridLayout grid = new GridLayout(6, 1);
        grid.setSpacing(true);

        grid.addComponent(getLxcsBtn);
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

    @Override
    public void buttonClick(Button.ClickEvent event) {
        Set<Agent> agents = MgmtApplication.getSelectedAgents();
        if (agents.size() > 0) {
            physicalAgents = new HashSet<Agent>();
            //filter physical agents
            for (Agent agent : agents) {
                if (!agent.isIsLXC()) {
                    physicalAgents.add(agent);
                }
            }

            if (physicalAgents.isEmpty()) {
                getWindow().showNotification("Select at least one physical agent");
            } else {
                //do the magic
                sendGetLxcListCmd(physicalAgents);
            }
        } else {
            getWindow().showNotification("Select at least one physical agent");
        }
    }

    private void sendGetLxcListCmd(Set<Agent> physicalAgents) {
        lxcMap.clear();
        lxcTable.setEnabled(false);
        Task getLxcListTask = Tasks.getLxcListTask(physicalAgents);
        taskRunner.runTask(getLxcListTask, this);
        runTimeoutThread();
        showProgress();
    }

    private void showProgress() {
        indicator.setVisible(true);
        getLxcsBtn.setEnabled(false);
    }

    private void hideProgress() {
        indicator.setVisible(false);
        getLxcsBtn.setEnabled(true);
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

                        hideProgress();
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
        try {
            if (task.getData() == TaskType.GET_LXC_LIST) {
                processLxcListCmd(task, response);
            }

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error in onResponse", e);
        }
    }

    private void processLxcListCmd(Task task, Response response) {
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
            lxcTable.setEnabled(true);
            hideProgress();
        }
    }

    private void populateTable(Map<String, EnumMap<LxcState, List<String>>> agentFamilies) {
        lxcTable.removeAllItems();

        for (Map.Entry<String, EnumMap<LxcState, List<String>>> agentFamily : agentFamilies.entrySet()) {
            final String parentHostname = agentFamily.getKey();
            lxcTable.addItem(new Object[]{parentHostname, null, null, null, null, null}, parentHostname);
            lxcTable.setCollapsed(parentHostname, false);

            for (Map.Entry<LxcState, List<String>> lxcs : agentFamily.getValue().entrySet()) {

                for (final String lxcHostname : lxcs.getValue()) {
                    final Button startBtn = new Button("Start");
                    final Button stopBtn = new Button("Stop");
                    final Button destroyBtn = new Button("Destroy");
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
                                showProgress();
                                taskRunner.runTask(startLxcTask, new TaskCallback() {
                                    StringBuilder output = new StringBuilder();

                                    @Override
                                    public void onResponse(Task task, Response response) {

                                        if (task.getData() == TaskType.START_LXC) {
                                            //send lxc-info cmd
                                            if (task.isCompleted()) {
                                                Task lxcInfoTask = Tasks.getLxcInfoWithWaitTask(physicalAgent, lxcHostname);
                                                taskRunner.runTask(lxcInfoTask, this);
                                            }
                                        } else if (task.getData() == TaskType.GET_LXC_INFO) {
                                            if (!Util.isStringEmpty(response.getStdOut())) {
                                                output.append(response.getStdOut());
                                            }
                                            if (task.isCompleted()) {
                                                if (output.indexOf("RUNNING") != -1) {
                                                    stopBtn.setEnabled(true);
                                                } else {
                                                    startBtn.setEnabled(true);
                                                }
                                                destroyBtn.setEnabled(true);
                                                progressIcon.setVisible(false);
                                                hideProgress();
                                            }
                                        }
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
                                showProgress();
                                taskRunner.runTask(stopLxcTask, new TaskCallback() {
                                    StringBuilder output = new StringBuilder();

                                    @Override
                                    public void onResponse(Task task, Response response) {
                                        if (task.getData() == TaskType.STOP_LXC) {
                                            //send lxc-info cmd
                                            if (task.isCompleted()) {
                                                Task lxcInfoTask = Tasks.getLxcInfoTask(physicalAgent, lxcHostname);
                                                taskRunner.runTask(lxcInfoTask, this);
                                            }
                                        } else if (task.getData() == TaskType.GET_LXC_INFO) {
                                            if (!Util.isStringEmpty(response.getStdOut())) {
                                                output.append(response.getStdOut());
                                            }
                                            if (task.isCompleted()) {
                                                if (output.indexOf("RUNNING") != -1) {
                                                    stopBtn.setEnabled(true);
                                                } else {
                                                    startBtn.setEnabled(true);
                                                }
                                                destroyBtn.setEnabled(true);
                                                progressIcon.setVisible(false);
                                                hideProgress();
                                            }
                                        }
                                    }
                                });
                            }
                        }
                    });
                    destroyBtn.addListener(new Button.ClickListener() {

                        @Override
                        public void buttonClick(Button.ClickEvent event) {

                            final Agent physicalAgent = agentManager.getAgentByHostname(parentHostname);
                            if (physicalAgent != null) {
                                Task destroyLxcTask = Tasks.getLxcDestroyTask(physicalAgent, lxcHostname);
                                startBtn.setEnabled(false);
                                stopBtn.setEnabled(false);
                                destroyBtn.setEnabled(false);
                                progressIcon.setVisible(true);
                                showProgress();
                                taskRunner.runTask(destroyLxcTask, new TaskCallback() {
                                    StringBuilder output = new StringBuilder();

                                    @Override
                                    public void onResponse(Task task, Response response) {
                                        if (task.getData() == TaskType.DESTROY_LXC) {
                                            //send lxc-info cmd
                                            if (task.isCompleted()) {
                                                Task lxcInfoTask = Tasks.getLxcInfoTask(physicalAgent, lxcHostname);
                                                taskRunner.runTask(lxcInfoTask, this);
                                            }
                                        } else if (task.getData() == TaskType.GET_LXC_INFO) {
                                            if (!Util.isStringEmpty(response.getStdOut())) {
                                                output.append(response.getStdOut());
                                            }
                                            if (task.isCompleted()) {
                                                if (output.indexOf("RUNNING") != -1) {
                                                    stopBtn.setEnabled(true);
                                                    destroyBtn.setEnabled(true);
                                                    progressIcon.setVisible(false);
                                                } else {
                                                    //remove row
                                                    lxcTable.removeItem(rowId);
                                                }
                                                hideProgress();
                                            }
                                        }
                                    }
                                });
                            }
                        }
                    });

                }
            }
        }

    }

}
