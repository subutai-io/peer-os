package org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.management;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Button;
import com.vaadin.ui.Table;
import org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.CassandraModule;

import java.util.List;
import java.util.UUID;
import org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.wizard.exec.ServiceManager;
import org.safehaus.kiskis.mgmt.shared.protocol.CassandraClusterInfo;

/**
 * Created with IntelliJ IDEA. User: daralbaev Date: 11/30/13 Time: 6:56 PM
 */
public class CassandraTable extends Table {

//    private IndexedContainer container;
    ServiceManager manager;

    public CassandraTable(ServiceManager manager) {
        this.manager = manager;
        this.setCaption("Cassandra clusters");
        this.setContainerDataSource(getCassandraContainer());
        this.setWidth("100%");
        this.setHeight(100, Sizeable.UNITS_PERCENTAGE);
        this.setPageLength(10);
        this.setSelectable(true);
        this.setImmediate(true);
    }

    private IndexedContainer getCassandraContainer() {
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(CassandraClusterInfo.UUID_LABEL, UUID.class, "");
        container.addContainerProperty(CassandraClusterInfo.NAME_LABEL, String.class, "");
        container.addContainerProperty("Start", Button.class, "");
        container.addContainerProperty("Stop", Button.class, "");
        container.addContainerProperty("Status", Button.class, "");
        container.addContainerProperty("Destroy", Button.class, "");
        List<CassandraClusterInfo> cdList = CassandraModule.getCommandManager().getCassandraClusterData();
        for (CassandraClusterInfo cluster : cdList) {
            addClusterDataToContainer(container, cluster);
        }
        return container;
    }

    private void addClusterDataToContainer(final Container container, final CassandraClusterInfo cd) {
        final Object itemId = container.addItem();
        final Item item = container.getItem(itemId);
        item.getItemProperty(CassandraClusterInfo.UUID_LABEL).setValue(cd.getUuid());
        item.getItemProperty(CassandraClusterInfo.NAME_LABEL).setValue(cd.getName());

        Button startButton = new Button("Start");
        startButton.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                manager.startCassandraServices(cd.getNodes());
            }
        });

        Button stopButton = new Button("Stop");
        stopButton.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                manager.stopCassandraServices(cd.getNodes());
            }
        });

        Button statusButton = new Button("Status");
        statusButton.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                manager.statusCassandraServices(cd.getNodes());
            }
        });

        Button destroyButton = new Button("Destroy");
        destroyButton.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                manager.purgeCassandraServices(cd.getNodes());
                if (CassandraModule.getCommandManager().deleteCassandraClusterData(cd.getUuid())) {
//                    container.removeItem(itemId);
                    refreshDatasource();
                }
            }
        });

        item.getItemProperty("Start").setValue(startButton);
        item.getItemProperty("Stop").setValue(stopButton);
        item.getItemProperty("Status").setValue(statusButton);
        item.getItemProperty("Destroy").setValue(destroyButton);
    }

    public void refreshDatasource() {
        this.setContainerDataSource(getCassandraContainer());
    }

}
