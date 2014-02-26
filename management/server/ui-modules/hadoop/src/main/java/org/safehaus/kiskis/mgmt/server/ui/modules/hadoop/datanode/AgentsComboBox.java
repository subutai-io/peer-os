package org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.datanode;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.FieldEvents;
import com.vaadin.ui.ComboBox;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.HadoopModule;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.HadoopClusterInfo;
import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManager;

import java.util.Set;
import java.util.UUID;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.HadoopDAO;

/**
 * Created with IntelliJ IDEA. User: daralbaev Date: 12/22/13 Time: 6:29 PM
 */
public class AgentsComboBox extends ComboBox {

    private HadoopClusterInfo cluster;
    private String clusterName;

    public AgentsComboBox(String clusterName) {
        this.clusterName = clusterName;

        refreshDataSource();
        setItemCaptionPropertyId("hostname");

        addListener(new FieldEvents.FocusListener() {

            @Override
            public void focus(FieldEvents.FocusEvent focusEvent) {
                refreshDataSource();
            }
        });
    }

    public void refreshDataSource() {
        this.cluster = HadoopDAO.getHadoopClusterInfo(clusterName);
        setContainerDataSource(getDataSourceMasters());
    }

    private BeanItemContainer getDataSourceMasters() {
        Set<Agent> list = getAgentManager().getLxcAgents();

        if (cluster != null) {
            list.remove(getAgentManager().getAgentByUUID(cluster.getNameNode()));
            list.remove(getAgentManager().getAgentByUUID(cluster.getSecondaryNameNode()));
            list.remove(getAgentManager().getAgentByUUID(cluster.getJobTracker()));

            for (UUID uuid : cluster.getDataNodes()) {
                list.remove(getAgentManager().getAgentByUUID(uuid));
            }

            for (UUID uuid : cluster.getTaskTrackers()) {
                list.remove(getAgentManager().getAgentByUUID(uuid));
            }
        }

        return new BeanItemContainer<Agent>(Agent.class, list);
    }

    public AgentManager getAgentManager() {
        // get bundle instance via the OSGi Framework Util class
        BundleContext ctx = FrameworkUtil.getBundle(HadoopModule.class).getBundleContext();
        if (ctx != null) {
            ServiceReference serviceReference = ctx.getServiceReference(AgentManager.class.getName());
            if (serviceReference != null) {
                return AgentManager.class.cast(ctx.getService(serviceReference));
            }
        }

        return null;
    }

    public CommandManager getCommandManager() {
        // get bundle instance via the OSGi Framework Util class
        BundleContext ctx = FrameworkUtil.getBundle(HadoopModule.class).getBundleContext();
        if (ctx != null) {
            ServiceReference serviceReference = ctx.getServiceReference(CommandManager.class.getName());
            if (serviceReference != null) {
                return CommandManager.class.cast(ctx.getService(serviceReference));
            }
        }

        return null;
    }
}
