package org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.config;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.FieldEvents;
import com.vaadin.ui.ComboBox;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.HadoopModule;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.HadoopClusterInfo;
import org.safehaus.kiskis.mgmt.shared.protocol.api.AgentManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;

import java.util.List;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: daralbaev
 * Date: 12/22/13
 * Time: 6:29 PM
 */
public class AgentsComboBox extends ComboBox {

    private HadoopClusterInfo cluster;

    public AgentsComboBox(String clusterName) {
        this.cluster = getCommandManager().getHadoopClusterData(clusterName);

        refreshDataSource();
        setItemCaptionPropertyId("hostname");

        addListener(new FieldEvents.FocusListener(){

            @Override
            public void focus(FieldEvents.FocusEvent focusEvent) {
                refreshDataSource();
            }
        });
    }

    public void refreshDataSource() {
        this.cluster = getCommandManager().getHadoopClusterData(cluster.getClusterName());
        setContainerDataSource(getDataSourceMasters());
    }

    private BeanItemContainer getDataSourceMasters() {
        List<Agent> list = getAgentManager().getRegisteredLxcAgents();

        list.remove(getAgentManager().getAgent(cluster.getNameNode()));
        list.remove(getAgentManager().getAgent(cluster.getSecondaryNameNode()));
        list.remove(getAgentManager().getAgent(cluster.getJobTracker()));

        for (UUID uuid : cluster.getDataNodes()) {
            list.remove(getAgentManager().getAgent(uuid));
        }

        for (UUID uuid : cluster.getTaskTrackers()) {
            list.remove(getAgentManager().getAgent(uuid));
        }

        return new BeanItemContainer<Agent>(Agent.class, list);
    }

    public AgentManagerInterface getAgentManager() {
        // get bundle instance via the OSGi Framework Util class
        BundleContext ctx = FrameworkUtil.getBundle(HadoopModule.class).getBundleContext();
        if (ctx != null) {
            ServiceReference serviceReference = ctx.getServiceReference(AgentManagerInterface.class.getName());
            if (serviceReference != null) {
                return AgentManagerInterface.class.cast(ctx.getService(serviceReference));
            }
        }

        return null;
    }

    public CommandManagerInterface getCommandManager() {
        // get bundle instance via the OSGi Framework Util class
        BundleContext ctx = FrameworkUtil.getBundle(HadoopModule.class).getBundleContext();
        if (ctx != null) {
            ServiceReference serviceReference = ctx.getServiceReference(CommandManagerInterface.class.getName());
            if (serviceReference != null) {
                return CommandManagerInterface.class.cast(ctx.getService(serviceReference));
            }
        }

        return null;
    }
}
