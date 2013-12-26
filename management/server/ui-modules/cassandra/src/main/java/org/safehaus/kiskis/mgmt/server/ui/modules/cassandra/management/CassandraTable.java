package org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.management;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Button;
import com.vaadin.ui.Table;
import org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.CassandraModule;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.RequestType;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.TaskStatus;

import java.util.List;
import java.util.UUID;
import org.safehaus.kiskis.mgmt.shared.protocol.CassandraClusterInfo;
import org.safehaus.kiskis.mgmt.shared.protocol.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.CommandFactory;
import org.safehaus.kiskis.mgmt.shared.protocol.OutputRedirection;
import org.safehaus.kiskis.mgmt.shared.protocol.ParseResult;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.Task;

/**
 * Created with IntelliJ IDEA. User: daralbaev Date: 11/30/13 Time: 6:56 PM
 */
public class CassandraTable extends Table {

    private IndexedContainer container;
    private NodesWindow window;
    private Task task;

    public CassandraTable() {
        this.setCaption("Cassandra clusters");
        this.setContainerDataSource(getCassandraContainer());
        this.setWidth("100%");
        this.setHeight(100, Sizeable.UNITS_PERCENTAGE);
        this.setPageLength(20);
        this.setSelectable(true);
        this.setImmediate(true);
    }

    private IndexedContainer getCassandraContainer() {
        container = new IndexedContainer();
        container.addContainerProperty(CassandraClusterInfo.UUID_LABEL, UUID.class, "");
        container.addContainerProperty(CassandraClusterInfo.NAME_LABEL, String.class, "");
        container.addContainerProperty("Start", Button.class, "");
        container.addContainerProperty("Stop", Button.class, "");
        container.addContainerProperty("Destroy", Button.class, "");
        List<CassandraClusterInfo> cdList = CassandraModule.getCommandManager().getCassandraClusterData();
        for (CassandraClusterInfo cluster : cdList) {
            addOrderToContainer(container, cluster);
        }

        return container;
    }

    private void addOrderToContainer(final Container container, final CassandraClusterInfo cd) {
        final Object itemId = container.addItem();
        final Item item = container.getItem(itemId);
        item.getItemProperty(CassandraClusterInfo.UUID_LABEL).setValue(cd.getUuid());
        item.getItemProperty(CassandraClusterInfo.NAME_LABEL).setValue(cd.getName());
        Button startButton = new Button("Start");
        startButton.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                String caption = cd.getName();
                window = new NodesWindow(caption, cd.getNodes());
                window.setModal(true);
                getApplication().getMainWindow().addWindow(window);
            }
        });
        Button stopButton = new Button("Stop");
        stopButton.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                createTask();
                for (UUID uuid : cd.getNodes()) {
                    Command command = (Command) CommandFactory.createRequest(
                            RequestType.EXECUTE_REQUEST,
                            uuid,
                            CassandraModule.MODULE_NAME,
                            task.getUuid(),
                            task.getIncrementedReqSeqNumber(),
                            "/",
                            "service cassandra stop",
                            OutputRedirection.RETURN,
                            OutputRedirection.RETURN,
                            null,
                            null,
                            "root",
                            null,
                            null,
                            null);
                    CassandraModule.getCommandManager().executeCommand(command);
                }
            }
        });
        Button destroyButton = new Button("Destroy");
        destroyButton.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                createTask();
                for (UUID uuid : cd.getNodes()) {
                    Command command = (Command) CommandFactory.createRequest(
                            RequestType.EXECUTE_REQUEST,
                            uuid,
                            CassandraModule.MODULE_NAME,
                            task.getUuid(),
                            task.getIncrementedReqSeqNumber(),
                            "/",
                            "apt-get --force-yes --assume-yes purge ksks-cassandra",
                            OutputRedirection.RETURN,
                            OutputRedirection.RETURN,
                            null,
                            null,
                            "root",
                            null,
                            null,
                            null);
                    CassandraModule.getCommandManager().executeCommand(command);

                }
                if (CassandraModule.getCommandManager().deleteCassandraClusterData(cd.getUuid())) {
                    container.removeItem(itemId);
                }
            }
        });

        item.getItemProperty("Start").setValue(startButton);
        item.getItemProperty("Stop").setValue(stopButton);
        item.getItemProperty("Destroy").setValue(destroyButton);
    }

    public void refreshDatasource() {
        this.setContainerDataSource(getCassandraContainer());
    }

    public NodesWindow getNodesWindow() {
        return window;
    }

    private void createTask() {
        task = new Task();
        task.setDescription("Nodes task");
        task.setTaskStatus(TaskStatus.NEW);
    }

    public void setOutput(Response response) {
        System.out.println(response.getStdOut());
        for (ParseResult pr : CassandraModule.getCommandManager().parseTask(task, true)) {
            System.out.println(pr.getResponse().getStdOut());
        }
    }

    public Task getTask() {
        return task;
    }

}
