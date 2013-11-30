package org.safehaus.kiskis.mgmt.server.ui.modules.cassandra;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.wizzard.CassandraWizard;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;
import org.safehaus.kiskis.mgmt.server.ui.services.ModuleService;
import org.safehaus.kiskis.mgmt.shared.protocol.ClusterData;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ui.CommandListener;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class CassandraModule implements Module {

    public static final String MODULE_NAME = "CassandraModule";
    private BundleContext context;

    private static ModuleComponent component;

    public static class ModuleComponent extends CustomComponent implements
            CommandListener {

        private final Button buttonInstallWizard;
        private final Button getClusters;
        private CassandraWizard subwindow;
        private Table table;

        public ModuleComponent(final CommandManagerInterface commandManagerInterface) {

            VerticalLayout verticalLayout = new VerticalLayout();
            verticalLayout.setSpacing(true);

            // Create table
            table = getTable();

            buttonInstallWizard = new Button("CassandraModule Installation Wizard");
            buttonInstallWizard.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    subwindow = new CassandraWizard();
                    getApplication().getMainWindow().addWindow(subwindow);
                }
            });
            verticalLayout.addComponent(buttonInstallWizard);

            getClusters = new Button("Get Cassandra clusters");
            getClusters.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    table.setContainerDataSource(getCassandraContainer());
                }
            });

            verticalLayout.addComponent(getClusters);
            verticalLayout.addComponent(table);

            setCompositionRoot(verticalLayout);
        }

        @Override
        public void outputCommand(Response response) {
            try {
                if (response != null && subwindow != null && subwindow.isVisible()) {
                    subwindow.setOutput(response);
                }
            } catch (Exception ex) {
                System.out.println("outputCommand event Exception");
                ex.printStackTrace();
            }

        }

        @Override
        public String getName() {
            return CassandraModule.MODULE_NAME;
        }

        private Table getTable() {
            final Table table = new Table("", getCassandraContainer());
            table.setColumnExpandRatio(ClusterData.NAME_LABEL, 1);
            table.setWidth("100%");
            table.setPageLength(6);
            table.setFooterVisible(true);
            table.setSelectable(true);
            table.setImmediate(true);
            table.addListener(new Table.ValueChangeListener() {
                public void valueChange(Property.ValueChangeEvent event) {
                    Set<?> value = (Set<?>) event.getProperty().getValue();
                    if (value != null && value.size() > 0) {
                        getWindow().showNotification("Selected: " + value);
                    }
                }
            });

            return table;
        }

        private IndexedContainer getCassandraContainer() {
            IndexedContainer container = new IndexedContainer();

            // Create the container properties
            container.addContainerProperty(ClusterData.UUID_LABEL, UUID.class, "");
            container.addContainerProperty(ClusterData.NAME_LABEL, String.class, "");
            container.addContainerProperty(ClusterData.NODES_LABEL, Integer.class, 0);
            container.addContainerProperty(ClusterData.SEEDS_LABEL, Integer.class, 0);
            container.addContainerProperty(ClusterData.DATADIR_LABEL, String.class, "");
            container.addContainerProperty(ClusterData.COMMITLOGDIR_LABEL, String.class, "");
            container.addContainerProperty(ClusterData.SAVEDCACHEDIR_LOG, String.class, "");
            container.addContainerProperty("Start/Stop", Button.class, "");

            // Create some orders
            List<ClusterData> cdList = getCommandManager().getClusterData();
            for (ClusterData cluster : cdList) {
                addOrderToContainer(container, cluster);
            }

            return container;
        }

        private void addOrderToContainer(Container container, ClusterData cd) {
            Object itemId = container.addItem();
            Item item = container.getItem(itemId);
            item.getItemProperty(ClusterData.UUID_LABEL).setValue(cd.getUuid());
            item.getItemProperty(ClusterData.NAME_LABEL).setValue(cd.getName());
            item.getItemProperty(ClusterData.NODES_LABEL).setValue(cd.getNodes() == null ? 0 : cd.getNodes().size());
            item.getItemProperty(ClusterData.SEEDS_LABEL).setValue(cd.getSeeds() == null ? 0 : cd.getSeeds().size());
            item.getItemProperty(ClusterData.DATADIR_LABEL).setValue(cd.getDataDir());
            item.getItemProperty(ClusterData.COMMITLOGDIR_LABEL).setValue(cd.getCommitLogDir());
            item.getItemProperty(ClusterData.SAVEDCACHEDIR_LOG).setValue(cd.getSavedCacheDir());
            item.getItemProperty("Start/Stop").setValue(new Button("Start or Stop"));
        }
    }

    @Override
    public String getName() {
        return CassandraModule.MODULE_NAME;
    }

    @Override
    public Component createComponent() {
        CommandManagerInterface commandManagerInterface = getCommandManager();
        component = new ModuleComponent(commandManagerInterface);
        commandManagerInterface.addListener(component);

        return component;
    }

    public void setModuleService(ModuleService service) {
        System.out.println("CassandraModule: registering with ModuleService");
        service.registerModule(this);
    }

    public void unsetModuleService(ModuleService service) {
        if (getCommandManager() != null) {
            getCommandManager().removeListener(component);
            service.unregisterModule(this);
        }
    }

    public void setContext(BundleContext context) {
        this.context = context;
    }

    public static CommandManagerInterface getCommandManager() {
        BundleContext ctx = FrameworkUtil.getBundle(CassandraModule.class).getBundleContext();
        if (ctx != null) {
            ServiceReference serviceReference = ctx.getServiceReference(CommandManagerInterface.class.getName());
            if (serviceReference != null) {
                return CommandManagerInterface.class.cast(ctx.getService(serviceReference));
            }
        }

        return null;
    }
}
