package org.safehaus.kiskis.mgmt.server.ui.modules.hbase.management;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Button;
import com.vaadin.ui.Table;

import java.util.List;

import org.safehaus.kiskis.mgmt.server.ui.modules.hbase.HBaseDAO;
import org.safehaus.kiskis.mgmt.server.ui.modules.hbase.HBaseConfig;
import org.safehaus.kiskis.mgmt.server.ui.modules.hbase.wizard.exec.ServiceManager;
import org.safehaus.kiskis.mgmt.server.ui.modules.hbase.wizard.HBaseClusterInfo;
import org.safehaus.kiskis.mgmt.api.taskrunner.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;

public class HBaseTable extends Table {

    private final ServiceManager manager;
    private NodesWindow nodesWindow;
    HBaseCommandEnum cce;
    Button selectedStartButton;
    Button selectedStopButton;
    Item selectedItem;
    HBaseConfig selectedConfig;

    public HBaseTable() {
        setSizeFull();
        this.manager = new ServiceManager(this);
        this.setCaption("HBase clusters");
        this.setWidth("100%");
        this.setHeight(100, Sizeable.UNITS_PERCENTAGE);
        this.setPageLength(10);
        this.setSelectable(true);
        this.setImmediate(true);
    }

    private IndexedContainer getContainer() {
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(HBaseClusterInfo.UUID_LABEL, String.class, "");
//        container.addContainerProperty(HBaseClusterInfo.DOMAINNAME_LABEL, String.class, "");
        container.addContainerProperty("Start", Button.class, "");
        container.addContainerProperty("Stop", Button.class, "");
        container.addContainerProperty("Status", Button.class, "");
        container.addContainerProperty("Details", Button.class, "");
        container.addContainerProperty("Destroy", Button.class, "");
        List<HBaseConfig> cdList = HBaseDAO.getClusterInfo();
        for (HBaseConfig config : cdList) {
            addClusterDataToContainer(container, config);
        }
        return container;
    }

    private void addClusterDataToContainer(final Container container, final HBaseConfig config) {
        final Object itemId = container.addItem();
        final Item item = container.getItem(itemId);
        item.getItemProperty(HBaseClusterInfo.UUID_LABEL).setValue(config.getUuid());
//        item.getItemProperty(HBaseClusterInfo.DOMAINNAME_LABEL).setValue(config.get);

        Button startButton = new Button("Start");
        startButton.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                getWindow().showNotification("Starting cluster: " + config.getUuid());
                cce = HBaseCommandEnum.START;
                selectedItem = item;
                manager.runCommand(config.getMaster(), cce);
            }
        });

        Button stopButton = new Button("Stop");
        stopButton.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                getWindow().showNotification("Stopping cluster: " + config.getUuid());
                cce = HBaseCommandEnum.STOP;
                selectedItem = item;
                manager.runCommand(config.getMaster(), cce);

            }
        });

        Button statusButton = new Button("Status");
        statusButton.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                getWindow().showNotification("Checking status: " + config.getUuid());
                cce = HBaseCommandEnum.STATUS;
                selectedItem = item;
                manager.runCommand(config.getMaster(), cce);

            }
        });


        Button manageButton = new Button("Details");
        manageButton.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                cce = HBaseCommandEnum.MANAGE;
                nodesWindow = new NodesWindow(config, manager);
                getApplication().getMainWindow().addWindow(nodesWindow);

            }
        });

        Button destroyButton = new Button("Destroy");
        destroyButton.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                getWindow().showNotification("Purging cluster: " + config.getUuid());
                cce = HBaseCommandEnum.PURGE;
                selectedItem = item;
                selectedConfig = config;
                manager.runCommand(config.getAgents(), cce);
            }
        });

        item.getItemProperty("Start").setValue(startButton);
        item.getItemProperty("Stop").setValue(stopButton);
        item.getItemProperty("Status").setValue(statusButton);
        item.getItemProperty("Details").setValue(manageButton);
        item.getItemProperty("Destroy").setValue(destroyButton);
    }

    public void refreshDatasource() {

        this.setContainerDataSource(getContainer());
    }

    private void switchState(Boolean state) {
        Button start = (Button) selectedItem.getItemProperty("Start").getValue();
        start.setEnabled(state);
        Button stop = (Button) selectedItem.getItemProperty("Stop").getValue();
        stop.setEnabled(!state);
    }

    public void manageUI(Task task, Response response, String stdOut, String stdErr) {
        if (cce != null) {
            switch (cce) {
                case START: {
                    getWindow().showNotification("Start success");
                    switchState(false);
                    /*switch (task.getTaskStatus()) {
                        case SUCCESS: {
                            break;
                        }
                        case FAIL: {
                            getWindow().showNotification("Start failed. Please use Terminal to check the problem");
                            break;
                        }
                    }*/
                    break;

                }
                case STOP: {
                    getWindow().showNotification("Stop success");
                    switchState(true);
                    /*switch (task.getTaskStatus()) {
                        case SUCCESS: {
                            break;
                        }
                        case FAIL: {
                            getWindow().showNotification("Stop failed. Please use Terminal to check the problem");
                            break;
                        }
                    }*/
                    break;
                }
                case STATUS: {
                    switch (task.getTaskStatus()) {
                        case SUCCESS: {
                            if (stdOut.toLowerCase().contains("is running")) {
                                getWindow().showNotification("Cluster is running");
                                switchState(false);
                            } else {
                                getWindow().showNotification("Cluster is not running");
                                switchState(true);
                            }
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
                    getWindow().showNotification("Purge success");
                    HBaseDAO
                            .deleteClusterInfo(selectedConfig.getUuid());
                    /*switch (task.getTaskStatus()) {
                        case SUCCESS: {
                            if (HBaseDAO
                                    .deleteClusterInfo(selectedConfig.getUuid())) {
                            }
                            break;
                        }
                        case FAIL: {
                            getWindow().showNotification("Purge failed. Please remove using Terminal");
                            break;
                        }
                    }*/
                    refreshDatasource();
                    break;
                }
                case MANAGE: {
                    if (nodesWindow.isVisible()) {
                        nodesWindow.updateUI(task);
                    }
                    break;
                }
            }
        }
    }

    public NodesWindow getNodesWindow() {
        return nodesWindow;
    }

}
