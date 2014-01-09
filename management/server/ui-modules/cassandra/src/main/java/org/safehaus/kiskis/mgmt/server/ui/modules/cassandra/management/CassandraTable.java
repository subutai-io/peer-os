package org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.management;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Button;
import com.vaadin.ui.Table;

import java.util.List;
import org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.wizard.exec.ServiceManager;
import org.safehaus.kiskis.mgmt.shared.protocol.CassandraClusterInfo;
import org.safehaus.kiskis.mgmt.shared.protocol.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.ParseResult;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.ServiceLocator;
import org.safehaus.kiskis.mgmt.shared.protocol.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.TaskStatus;

/**
 * Created with IntelliJ IDEA. User: daralbaev Date: 11/30/13 Time: 6:56 PM
 */
public class CassandraTable extends Table {

//    private IndexedContainer container;
    private final ServiceManager manager;
    private NodesWindow nodesWindow;
    CassandraCommandEnum cce;
    Button selectedStartButton;
    Button selectedStopButton;
    Item selectedItem;

    public CassandraTable() {
        this.manager = new ServiceManager();
        this.setCaption("Cassandra clusters");
        this.setWidth("100%");
        this.setHeight(100, Sizeable.UNITS_PERCENTAGE);
        this.setPageLength(10);
        this.setSelectable(true);
        this.setImmediate(true);
    }

    private IndexedContainer getCassandraContainer() {
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(CassandraClusterInfo.UUID_LABEL, String.class, "");
        container.addContainerProperty(CassandraClusterInfo.NAME_LABEL, String.class, "");
        container.addContainerProperty("Start", Button.class, "");
        container.addContainerProperty("Stop", Button.class, "");
//        container.addContainerProperty("Status", Button.class, "");
        container.addContainerProperty("Manage", Button.class, "");
        container.addContainerProperty("Destroy", Button.class, "");
        List<CassandraClusterInfo> cdList = ServiceLocator.getService(CommandManagerInterface.class).getCassandraClusterData();
        for (CassandraClusterInfo cluster : cdList) {
            addClusterDataToContainer(container, cluster);
        }
        return container;
    }

    private void addClusterDataToContainer(final Container container, final CassandraClusterInfo cci) {
        final Object itemId = container.addItem();
        final Item item = container.getItem(itemId);
        item.getItemProperty(CassandraClusterInfo.UUID_LABEL).setValue(cci.getDomainName());
        item.getItemProperty(CassandraClusterInfo.NAME_LABEL).setValue(cci.getName());

        Button startButton = new Button("Start");
        startButton.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                cce = CassandraCommandEnum.START;
                selectedItem = item;
                manager.runCommand(cci.getNodes(), cce);
            }
        });

        Button stopButton = new Button("Stop");
        stopButton.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                cce = CassandraCommandEnum.STOP;
                selectedItem = item;
                manager.runCommand(cci.getNodes(), cce);

            }
        });

//        Button statusButton = new Button("Status");
//        statusButton.addListener(new Button.ClickListener() {
//
//            @Override
//            public void buttonClick(Button.ClickEvent event) {
//                cce = CassandraCommandEnum.STATUS;
//                manager.runCommand(cci.getNodes(), cce);
//            }
//        });
        Button manageButton = new Button("Manage");
        manageButton.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                CassandraClusterInfo info = ServiceLocator.getService(CommandManagerInterface.class).getCassandraClusterDataByUUID(cci.getUuid());
                nodesWindow = new NodesWindow(cci.getName(), info, manager);
                getApplication().getMainWindow().addWindow(nodesWindow);

            }
        });

        Button destroyButton = new Button("Destroy");
        destroyButton.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                manager.runCommand(cci.getNodes(), CassandraCommandEnum.PURGE);
                if (ServiceLocator.getService(CommandManagerInterface.class).deleteCassandraClusterData(cci.getUuid())) {
//                    container.removeItem(itemId);
                    refreshDatasource();
                }
            }
        });

        item.getItemProperty("Start").setValue(startButton);
        item.getItemProperty("Stop").setValue(stopButton);
//        item.getItemProperty("Status").setValue(statusButton);
        item.getItemProperty("Manage").setValue(manageButton);
        item.getItemProperty("Destroy").setValue(destroyButton);
    }

    public void refreshDatasource() {
        this.setContainerDataSource(getCassandraContainer());
    }

    public NodesWindow getNodesWindow() {
        return nodesWindow;
    }

    private void switchState(Boolean state) {
        Button start = (Button) selectedItem.getItemProperty("Start").getValue();
        start.setEnabled(state);
        Button stop = (Button) selectedItem.getItemProperty("Stop").getValue();
        stop.setEnabled(!state);
    }

    public void onResponse(Response response) {
        if (manager.getCurrentTask() != null && response.getTaskUuid() != null
                && manager.getCurrentTask().getUuid().compareTo(response.getTaskUuid()) == 0) {
            List<ParseResult> list = ServiceLocator.getService(CommandManagerInterface.class).parseTask(response.getTaskUuid(), true);
            Task task = ServiceLocator.getService(CommandManagerInterface.class).getTask(response.getTaskUuid());
            if (!list.isEmpty()) {
                if (task.getTaskStatus() == TaskStatus.SUCCESS) {
                    if (nodesWindow != null && nodesWindow.isVisible()) {
                        nodesWindow.updateUI(task);
                    }
                    manageUI();
                    manager.moveToNextTask();
                    if (manager.getCurrentTask() != null) {
                        for (Command command : manager.getCurrentTask().getCommands()) {
                            manager.executeCommand(command);
                        }
                    } else {
                    }
                } else if (task.getTaskStatus() == TaskStatus.FAIL) {
                    if (nodesWindow != null && nodesWindow.isVisible()) {
                        nodesWindow.updateUI(task);
                    }
                }
            }
        }
    }

    private void manageUI() {
        if (cce != null) {
            switch (cce) {
                case START: {
                    switchState(false);
                    break;
                }
                case STOP: {
                    switchState(true);
                    break;
                }
            }
        }
    }
}
