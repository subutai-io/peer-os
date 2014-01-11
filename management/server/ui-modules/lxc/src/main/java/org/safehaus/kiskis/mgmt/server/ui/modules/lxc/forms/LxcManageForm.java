package org.safehaus.kiskis.mgmt.server.ui.modules.lxc.forms;

import com.google.common.base.Strings;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;
import org.safehaus.kiskis.mgmt.server.ui.modules.lxc.LxcModule;
import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.TaskStatus;

import java.util.*;

//import org.safehaus.kiskis.mgmt.server.ui.install.AppData;

/**
 * Created with IntelliJ IDEA. User: daralbaev Date: 12/1/13 Time: 5:56 PM
 */
@SuppressWarnings("serial")
public class LxcManageForm extends VerticalLayout {

    private Agent physicalAgent;
    private LxcTable table;

    private Task listTask, startTask, stopTask, destroyTask;

    private Button startAllButton, stopAllButton, destroyAllButton;
    private List<String> startedLXC, stoppedLXC;

    public LxcManageForm() {
        setSpacing(true);

        HorizontalLayout hLayout = new HorizontalLayout();
        hLayout.setSpacing(true);

        hLayout.addComponent(getRefreshButton());
        hLayout.addComponent(getStartAllButton());
        hLayout.addComponent(getStopAllButton());
        hLayout.addComponent(getDestroyAllButton());

        Panel panel = new Panel("Manage LXC containers");
        panel.addComponent(hLayout);
        panel.addComponent(getLxcTable());

        addComponent(panel);
    }

    private LxcTable getLxcTable() {
        table = new LxcTable(this);

        return table;
    }

    private Button getRefreshButton() {
        Button buttonRefresh = new Button("Refresh LXC containers");
        buttonRefresh.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (getPhysicalAgent() != null) {
                    refreshTable();
                }
            }
        });

        return buttonRefresh;
    }

    private Button getStartAllButton() {
        startAllButton = new Button("Start all LXC");
        startAllButton.setEnabled(false);

        startAllButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (getPhysicalAgent() != null) {
                    startAllTask();
                }
            }
        });

        return startAllButton;
    }

    private Button getStopAllButton() {
        stopAllButton = new Button("Stop all LXC");
        stopAllButton.setEnabled(false);

        stopAllButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (getPhysicalAgent() != null) {
                    stopAllTask();
                }
            }
        });

        return stopAllButton;
    }

    private Button getDestroyAllButton() {
        destroyAllButton = new Button("Destroy all LXC");
        destroyAllButton.setEnabled(false);

        destroyAllButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (getPhysicalAgent() != null) {
                    destroyAllTask();
                }
            }
        });

        return destroyAllButton;
    }

    public void refreshTable() {
        listTask = RequestUtil.createTask(getCommandManager(), "List lxc container");

        HashMap<String, String> map = new HashMap<String, String>();
        map.put(":source", LxcModule.MODULE_NAME);
        map.put(":uuid", getPhysicalAgent().getUuid().toString());

        RequestUtil.createRequest(getCommandManager(), LxcTable.LIST_LXC, listTask, map);
    }

    private void startAllTask() {
        if (!stoppedLXC.isEmpty()) {
            startTask = RequestUtil.createTask(getCommandManager(), "Start all lxc containers");

            for (String lxc : stoppedLXC) {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put(":source", LxcModule.MODULE_NAME);
                map.put(":uuid", getPhysicalAgent().getUuid().toString());
                map.put(":lxc-host-name", lxc);

                RequestUtil.createRequest(getCommandManager(), LxcTable.START_LXC, startTask, map);
            }
        }
    }

    private void stopAllTask() {
        if (!startedLXC.isEmpty()) {
            stopTask = RequestUtil.createTask(getCommandManager(), "Stop all lxc containers");

            for (String lxc : startedLXC) {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put(":source", LxcModule.MODULE_NAME);
                map.put(":uuid", getPhysicalAgent().getUuid().toString());
                map.put(":lxc-host-name", lxc);

                RequestUtil.createRequest(getCommandManager(), LxcTable.STOP_LXC, stopTask, map);
            }
        }
    }

    private void destroyAllTask() {
        destroyTask = RequestUtil.createTask(getCommandManager(), "Destroy all lxc containers");

        if (!startedLXC.isEmpty()) {
            for (String lxc : startedLXC) {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put(":source", LxcModule.MODULE_NAME);
                map.put(":uuid", getPhysicalAgent().getUuid().toString());
                map.put(":lxc-host-name", lxc);

                RequestUtil.createRequest(getCommandManager(), LxcTable.STOP_LXC, destroyTask, map);
            }
        }

        List<String> allList = new ArrayList<String>();
        allList.addAll(startedLXC);
        allList.addAll(stoppedLXC);

        if (!allList.isEmpty()) {
            for (String lxc : allList) {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put(":source", LxcModule.MODULE_NAME);
                map.put(":uuid", getPhysicalAgent().getUuid().toString());
                map.put(":lxc-host-name", lxc);

                RequestUtil.createRequest(getCommandManager(), LxcTable.DESTROY_LXC, destroyTask, map);
            }
        }


    }

    public void outputResponse(Response response) {
        List<ParseResult> output = getCommandManager().parseTask(response.getTaskUuid(), true);
        Task task = getCommandManager().getTask(response.getTaskUuid());

        if (listTask != null && task.equals(listTask)) {
            if (!output.isEmpty() && task.getTaskStatus().compareTo(TaskStatus.SUCCESS) == 0) {
                for (ParseResult pr : output) {
                    parseResult(pr);
                }
                listTask = null;

                getWindow().showNotification(task.getDescription() + " finished.");
            }
        } else if (startTask != null && task.equals(startTask)) {
            if (!output.isEmpty() && task.getTaskStatus().compareTo(TaskStatus.SUCCESS) == 0) {
                refreshTable();
                startTask = null;

                getWindow().showNotification(task.getDescription() + " finished.");
            }
        } else if (stopTask != null && task.equals(stopTask)) {
            if (!output.isEmpty() && task.getTaskStatus().compareTo(TaskStatus.SUCCESS) == 0) {
                refreshTable();
                stopTask = null;

                getWindow().showNotification(task.getDescription() + " finished.");
            }
        } else if (destroyTask != null && task.equals(destroyTask)) {
            if (!output.isEmpty()) {
                refreshTable();
                destroyTask = null;

                getWindow().showNotification(task.getDescription() + " finished.");
            }
        } else {
            table.outputResponse(response);
        }
    }

    private void parseResult(ParseResult parseResult) {
        String[] lxcs = parseResult.getResponse().getStdOut().split("\\n");
        startedLXC = new ArrayList<String>();
        stoppedLXC = new ArrayList<String>();
        ArrayList<String> frozenLXC = new ArrayList<String>();

        ArrayList<String> temp = null;
        for (String s : lxcs) {
            if (s.trim().contains("RUNNING")) {
                temp = (ArrayList<String>) startedLXC;
            } else if (s.trim().contains("STOPPED")) {
                temp = (ArrayList<String>) stoppedLXC;
            } else if (s.trim().contains("FROZEN")) {
                temp = frozenLXC;
            } else {
                if (!Strings.isNullOrEmpty(s.trim()) && temp != null && !s.trim().equals("base-container")) {
                    temp.add(s.trim());
                }
            }
        }

        if (!stoppedLXC.isEmpty()) {
            startAllButton.setEnabled(true);
        } else {
            startAllButton.setEnabled(false);
        }

        if (!startedLXC.isEmpty()) {
            stopAllButton.setEnabled(true);
        } else {
            stopAllButton.setEnabled(false);
        }

        if (!startedLXC.isEmpty() || !stoppedLXC.isEmpty()) {
            destroyAllButton.setEnabled(true);
        } else {
            destroyAllButton.setEnabled(false);
        }

        table.setAgent(physicalAgent, startedLXC, stoppedLXC);
    }

    private Agent getPhysicalAgent() {

        Set<Agent> agents = MgmtApplication.getSelectedAgents();
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
                physicalAgent = null;
            }
        }

        return physicalAgent;
    }

    public CommandManagerInterface getCommandManager() {
        // get bundle instance via the OSGi Framework Util class
        BundleContext ctx = FrameworkUtil.getBundle(LxcModule.class).getBundleContext();
        if (ctx != null) {
            ServiceReference serviceReference = ctx.getServiceReference(CommandManagerInterface.class.getName());
            if (serviceReference != null) {
                return CommandManagerInterface.class.cast(ctx.getService(serviceReference));
            }
        }

        return null;
    }
}