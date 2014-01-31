package org.safehaus.kiskis.mgmt.server.ui.modules.lxc.clone;

import com.vaadin.data.Item;
import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;
import org.safehaus.kiskis.mgmt.shared.protocol.*;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.server.ui.modules.lxc.common.Commands;
import org.safehaus.kiskis.mgmt.server.ui.modules.lxc.common.Tasks;
import org.safehaus.kiskis.mgmt.server.ui.modules.lxc.dao.LxcCloneInfo;
import org.safehaus.kiskis.mgmt.server.ui.modules.lxc.dao.LxcCloneStatus;
import org.safehaus.kiskis.mgmt.server.ui.modules.lxc.dao.LxcDao;
import org.safehaus.kiskis.mgmt.shared.protocol.api.AsyncTaskRunner;
import org.safehaus.kiskis.mgmt.shared.protocol.api.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.api.TaskCallback;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.ResponseType;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.TaskStatus;

@SuppressWarnings("serial")
public class Cloner extends VerticalLayout implements TaskCallback {
    
    private static final Logger LOG = Logger.getLogger(Cloner.class.getName());
    
    private final Button cloneBtn;
    private final TextField textFieldLxcName;
    private final Slider slider;
    private final Label indicator;
    private final TreeTable lxcTable;
    private final Table tasksTable;
    private final TaskRunner taskRunner;
    private final Map<String, String> requestToLxcMatchMap = new HashMap<String, String>();
    private final int timeout;
    private final String physicalHostLabel = "Physical Host";
    private final String statusLabel = "Status";
    private final String okIconSource = "icons/16/ok.png";
    private final String errorIconSource = "icons/16/cancel.png";
    private final String loadIconSource = "../base/common/img/loading-indicator.gif";
    private Thread operationTimeoutThread;
    private Thread taskPollerThread;
    private final AsyncTaskRunner asyncTaskRunner;
    
    public Cloner(TaskRunner taskRunner) {
        setSpacing(true);
        setMargin(true);
        
        asyncTaskRunner = ServiceLocator.getService(AsyncTaskRunner.class);
        
        this.taskRunner = taskRunner;
        timeout = Commands.getCloneCommand().getRequest().getTimeout();
        
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
                startCloneTask(false);
            }
        });
        Button asyncCloneBtn = new Button("Clone in background");
        asyncCloneBtn.addListener(new Button.ClickListener() {
            
            @Override
            public void buttonClick(Button.ClickEvent event) {
                startCloneTask(true);
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
        
        Button refreshTasksBtn = new Button("Refresh Background Tasks");
        refreshTasksBtn.addListener(new Button.ClickListener() {
            
            @Override
            public void buttonClick(Button.ClickEvent event) {
                populateTasksTable();
            }
        });
        
        indicator = MgmtApplication.createImage("indicator.gif", 50, 11);
        indicator.setVisible(false);
        
        GridLayout topContent = new GridLayout(9, 1);
        topContent.setSpacing(true);
        
        topContent.addComponent(new Label("Product name"));
        topContent.addComponent(textFieldLxcName);
        topContent.addComponent(new Label("Lxc count"));
        topContent.addComponent(slider);
        topContent.addComponent(cloneBtn);
        topContent.addComponent(asyncCloneBtn);
        topContent.addComponent(refreshTasksBtn);
        topContent.addComponent(clearBtn);
        topContent.addComponent(indicator);
        topContent.setComponentAlignment(indicator, Alignment.MIDDLE_CENTER);
        addComponent(topContent);
        
        GridLayout bottomContent = new GridLayout(2, 1);
        bottomContent.setSizeFull();
        lxcTable = createLxcTable("Lxc containers", 500);
        bottomContent.addComponent(lxcTable);
        
        tasksTable = createTasksTable("Background Clone Tasks", 500);
        bottomContent.addComponent(tasksTable);
        
        addComponent(bottomContent);
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
    
    private void populateTasksTable() {
        List<LxcCloneInfo> cloneInfos = LxcDao.getLxcCloneInfos();
        tasksTable.removeAllItems();
        if (!cloneInfos.isEmpty()) {
            for (LxcCloneInfo cloneInfo : cloneInfos) {
                Button checkBtn = new Button("Check");
                Embedded statusIcon;
                if (cloneInfo.getCloneStatus() == LxcCloneStatus.FAILED) {
                    statusIcon = new Embedded("", new ThemeResource(errorIconSource));
                } else if (cloneInfo.getCloneStatus() == LxcCloneStatus.SUCCEEDED) {
                    statusIcon = new Embedded("", new ThemeResource(okIconSource));
                } else {
                    statusIcon = new Embedded("", new ThemeResource(loadIconSource));
                }
                tasksTable.addItem(new Object[]{cloneInfo.getPhysicalHosts(), checkBtn, statusIcon}, cloneInfo.getTaskUUID());
            }
        }
    }
    
    private void startCloneTask(boolean runInBackground) {
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
                if (!runInBackground) {
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
                    runTimeoutThread();
                    taskRunner.runTask(task, this);
                } else {
                    //run task in background
                    List<String> physicalHosts = new ArrayList<String>();
                    for (Agent agent : physicalAgents) {
                        physicalHosts.add(agent.getHostname());
                    }
                    final LxcCloneInfo cloneInfo = new LxcCloneInfo(
                            task.getUuid(), physicalHosts, new Date(), LxcCloneStatus.NEW);
                    if (LxcDao.saveLxcCloneInfo(cloneInfo)) {
                        asyncTaskRunner.executeTask(task, new TaskCallback() {
                            
                            @Override
                            public void onResponse(Task task, Response response) {
                                if (task.isCompleted()) {
                                    //one could save task status to db here
                                    //code here must not reference any UI specific objects

                                    cloneInfo.setCloneStatus(
                                            task.getTaskStatus() == TaskStatus.SUCCESS
                                            ? LxcCloneStatus.SUCCEEDED : LxcCloneStatus.FAILED);
                                    if (LxcDao.saveLxcCloneInfo(cloneInfo)) {
                                        System.out.println("Background cloning is done");
                                    }
                                }
                            }
                        });
                        runTaskPollerThread(task);
                        show("Clone task is submitted for execution.<br/>Please, check later the status of nodes");
                    } else {
                        show("Error saving background task to DB");
                    }
                }
            }
        } else {
            show("Select at least one physical agent");
        }
    }
    
    private void show(String msg) {
        getWindow().showNotification(msg);
    }

//    private void show(String msg, int delayMs) {
//        Window.Notification notification = new Window.Notification(msg);
//        notification.setDelayMsec(delayMs);
//        getWindow().showNotification(notification);
//    }
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
    
    private void runTaskPollerThread(final Task task) {
        try {
            if (taskPollerThread != null && taskPollerThread.isAlive()) {
                taskPollerThread.interrupt();
            }
            taskPollerThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (!Thread.interrupted()) {
                            populateTasksTable();
                            if (task.isCompleted()) {
                                return;
                            }
                            Thread.sleep(500);
                        }
                        
                    } catch (InterruptedException ex) {
                    }
                }
            });
            taskPollerThread.start();
            
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error in runPollerThread", e);
        }
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
        if (task.isCompleted() && taskRunner.getRemainingTaskCount() == 0) {
            indicator.setVisible(false);
            requestToLxcMatchMap.clear();
        }
    }
}
