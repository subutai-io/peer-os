package org.safehaus.kiskis.mgmt.server.ui.modules.oozie.management;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Button;
import com.vaadin.ui.Table;
import java.util.List;
import org.safehaus.kiskis.mgmt.server.ui.modules.oozie.OozieConfig;
import org.safehaus.kiskis.mgmt.server.ui.modules.oozie.OozieDAO;
import org.safehaus.kiskis.mgmt.server.ui.modules.oozie.wizard.OozieClusterInfo;
import org.safehaus.kiskis.mgmt.server.ui.modules.oozie.wizard.exec.ServiceManager;
import org.safehaus.kiskis.mgmt.shared.protocol.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.TaskStatus;

public class OozieTable extends Table {

    private final ServiceManager manager;
    private NodesWindow nodesWindow;
    OozieCommandEnum cce;
    Button selectedStartButton;
    Button selectedStopButton;
    Item selectedItem;
    OozieConfig selectedConfig;

    public OozieTable() {
        setSizeFull();
        this.manager = new ServiceManager(this);
        this.setCaption("Oozie");
        this.setWidth("100%");
        this.setHeight(100, Sizeable.UNITS_PERCENTAGE);
        this.setPageLength(10);
        this.setSelectable(true);
        this.setImmediate(true);
    }

    private IndexedContainer getContainer() {
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(OozieClusterInfo.UUID_LABEL, String.class, "");
        container.addContainerProperty("Manage", Button.class, "");
        container.addContainerProperty("Destroy", Button.class, "");
        List<OozieConfig> cdList = OozieDAO.getClusterInfo();
        for (OozieConfig config : cdList) {
            addClusterDataToContainer(container, config);
        }
        return container;
    }

    private void addClusterDataToContainer(final Container container, final OozieConfig config) {
        final Object itemId = container.addItem();
        final Item item = container.getItem(itemId);
        item.getItemProperty(OozieClusterInfo.UUID_LABEL).setValue(config.getUuid());
        Button manageButton = new Button("Manage");
        manageButton.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                nodesWindow = new NodesWindow(config, manager);
                cce = OozieCommandEnum.MANAGE;
                getApplication().getMainWindow().addWindow(nodesWindow);

            }
        });

        Button destroyButton = new Button("Destroy");
        destroyButton.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                getWindow().showNotification("Purging: " + config.getUuid());
                cce = OozieCommandEnum.PURGE_SERVER;
                selectedItem = item;
                selectedConfig = config;
                manager.purge(config);
            }
        });

        item.getItemProperty("Manage").setValue(manageButton);
        item.getItemProperty("Destroy").setValue(destroyButton);
    }

    public void refreshDatasource() {
        this.setContainerDataSource(getContainer());
    }

    public void manageUI(Task task) {
        if (cce != null) {
            switch (cce) {
                case PURGE_SERVER: {
                    switch (task.getTaskStatus()) {
                        case SUCCESS: {
                            getWindow().showNotification("Purge success");
                            refreshDatasource();
                            break;
                        }
                        case FAIL: {
                            getWindow().showNotification("Purge failed. Please remove using Terminal");
                            break;
                        }
                    }
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

}
