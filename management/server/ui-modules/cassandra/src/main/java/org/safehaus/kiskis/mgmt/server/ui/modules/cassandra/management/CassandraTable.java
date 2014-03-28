package org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.management;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Button;
import com.vaadin.ui.Table;

import java.util.List;
import org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.dao.CassandraDAO;
import org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.wizard.exec.ServiceManager;
import org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.dao.CassandraClusterInfo;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskStatus;

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
    CassandraClusterInfo selectedCci;

    public CassandraTable() {
        this.manager = new ServiceManager(this);
        this.setCaption("Cassandra clusters");
        this.setWidth("100%");
        this.setHeight(100, Sizeable.UNITS_PERCENTAGE);
        this.setPageLength(10);
        this.setSelectable(true);
        this.setImmediate(true);
    }

    private IndexedContainer getCassandraContainer() {
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(CassandraClusterInfo.DOMAINNAME_LABEL, String.class, "");
        container.addContainerProperty(CassandraClusterInfo.NAME_LABEL, String.class, "");
        container.addContainerProperty("Start", Button.class, "");
        container.addContainerProperty("Stop", Button.class, "");
//        container.addContainerProperty("Status", Button.class, "");
        container.addContainerProperty("Manage", Button.class, "");
        container.addContainerProperty("Destroy", Button.class, "");
        List<CassandraClusterInfo> cdList = CassandraDAO.getCassandraClusterInfo();
        for (CassandraClusterInfo cluster : cdList) {
            addClusterDataToContainer(container, cluster);
        }
        return container;
    }

    private void addClusterDataToContainer(final Container container, final CassandraClusterInfo cci) {
        final Object itemId = container.addItem();
        final Item item = container.getItem(itemId);
        item.getItemProperty(CassandraClusterInfo.DOMAINNAME_LABEL).setValue(cci.getDomainName());
        item.getItemProperty(CassandraClusterInfo.NAME_LABEL).setValue(cci.getName());

        Button startButton = new Button("Start");
        startButton.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                getWindow().showNotification("Starting cassandra cluster: " + cci.getName());
                cce = CassandraCommandEnum.START;
                selectedItem = item;
                manager.runCommand(cci.getNodes(), cce);
            }
        });

        Button stopButton = new Button("Stop");
        stopButton.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                getWindow().showNotification("Stopping cassandra cluster: " + cci.getName());
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
                CassandraClusterInfo info = CassandraDAO.getCassandraClusterInfoByUUID(cci.getUuid());
                nodesWindow = new NodesWindow(info, manager);
                getApplication().getMainWindow().addWindow(nodesWindow);

            }
        });

        Button destroyButton = new Button("Destroy");
        destroyButton.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                selectedCci = cci;
                getWindow().showNotification("Destroying cassandra cluster: " + cci.getName());
                cce = CassandraCommandEnum.PURGE;
                manager.runCommand(cci.getNodes(), cce);

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

//    public void onResponse(Response response) {
//        if (manager.getCurrentTask() != null && response.getTaskUuid() != null
//                && manager.getCurrentTask().getUuid().compareTo(response.getTaskUuid()) == 0) {
//            List<ParseResult> list = RequestUtil.parseTask(response.getTaskUuid(), true);
//            Task task = RequestUtil.getTask(response.getTaskUuid());
//            if (!list.isEmpty()) {
//                if (task.getTaskStatus() == TaskStatus.SUCCESS) {
//                    manager.moveToNextTask();
//                    if (manager.getCurrentTask() != null) {
//                        for (Command command : manager.getCurrentTask().getCommands()) {
//                            manager.executeCommand(command);
//                        }
//                    } else {
//                        if (nodesWindow != null && nodesWindow.isVisible()) {
//                            nodesWindow.updateUI(task);
//                        }
//                        manageUI(task.getTaskStatus());
//                    }
//                } else if (task.getTaskStatus() == TaskStatus.FAIL) {
//                    if (nodesWindow != null && nodesWindow.isVisible()) {
//                        nodesWindow.updateUI(task);
//                    }
//                }
//            }
//        }
//    }
    public void manageUI(TaskStatus ts) {
        if (cce != null) {
            switch (cce) {
                case START: {

                    switch (ts) {
                        case SUCCESS: {
                            getWindow().showNotification("Start success");
                            switchState(false);
                            break;
                        }
                        case FAIL: {
                            getWindow().showNotification("Start failed. Please use Terminal to check the problem");
                            break;
                        }
                    }
                    break;

                }
                case STOP: {

                    switch (ts) {
                        case SUCCESS: {
                            getWindow().showNotification("Stop success");
                            switchState(true);
                            break;
                        }
                        case FAIL: {
                            getWindow().showNotification("Stop failed. Please use Terminal to check the problem");
                            break;
                        }
                    }
                    break;
                }
                case PURGE: {
                    switch (ts) {
                        case SUCCESS: {
                            getWindow().showNotification("Purge success");
                            if (CassandraDAO
                                    .deleteCassandraClusterInfo(selectedCci.getUuid())) {
//                    container.removeItem(itemId);
                                refreshDatasource();
                            }
                            break;
                        }
                        case FAIL: {
                            getWindow().showNotification("Purge failed. Please remove using Terminal");
                            break;
                        }
                    }
                    break;
                }
            }
        }
        cce = null;
    }
}
