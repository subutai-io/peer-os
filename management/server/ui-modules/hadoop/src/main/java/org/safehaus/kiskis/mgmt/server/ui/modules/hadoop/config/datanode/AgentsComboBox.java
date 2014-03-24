package org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.config.datanode;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.FieldEvents;
import com.vaadin.ui.ComboBox;
import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.HadoopClusterInfo;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.HadoopDAO;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.HadoopModule;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

import java.util.Set;

/**
 * Created with IntelliJ IDEA. User: daralbaev Date: 12/22/13 Time: 6:29 PM
 */
public final class AgentsComboBox extends ComboBox {

    private HadoopClusterInfo cluster;
    private final String clusterName;
    private final AgentManager agentManager;

    public AgentsComboBox(String clusterName) {
        this.clusterName = clusterName;
        agentManager = HadoopModule.getAgentManager();

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
        Set<Agent> list = agentManager.getLxcAgents();

        if (cluster != null) {
            list.remove(cluster.getNameNode());
            list.remove(cluster.getSecondaryNameNode());
            list.remove(cluster.getJobTracker());

            for (Agent agent : cluster.getDataNodes()) {
                list.remove(agent);
            }

            for (Agent agent : cluster.getTaskTrackers()) {
                list.remove(agent);
            }
        }

        return new BeanItemContainer<Agent>(Agent.class, list);
    }
}
