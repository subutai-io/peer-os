package org.safehaus.kiskis.mgmt.impl.solr.handler;

import org.safehaus.kiskis.mgmt.api.solr.Config;
import org.safehaus.kiskis.mgmt.shared.operation.ProductOperation;
import org.safehaus.kiskis.mgmt.impl.solr.SolrImpl;
import org.safehaus.kiskis.mgmt.shared.operation.AbstractOperationHandler;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

import java.util.UUID;

/**
 * Created by dilshat on 5/7/14.
 */
public class DestroyNodeOperationHandler extends AbstractOperationHandler<SolrImpl> {
    private final ProductOperation po;
    private final String lxcHostname;

    public DestroyNodeOperationHandler(SolrImpl manager, String clusterName, String lxcHostname) {
        super(manager, clusterName);
        this.lxcHostname = lxcHostname;
        po = manager.getTracker().createProductOperation(Config.PRODUCT_KEY,
                String.format("Destroying %s in %s", lxcHostname, clusterName));
    }

    @Override
    public UUID getTrackerId() {
        return po.getId();
    }

    @Override
    public void run() {
        final Config config = manager.getCluster(clusterName);
        if (config == null) {
            po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", clusterName));
            return;
        }

        Agent agent = manager.getAgentManager().getAgentByHostname(lxcHostname);
        if (agent == null) {
            po.addLogFailed(String.format("Agent with hostname %s is not connected\nOperation aborted", lxcHostname));
            return;
        }
        if (!config.getNodes().contains(agent)) {
            po.addLogFailed(String.format("Agent with hostname %s does not belong to cluster %s", lxcHostname, clusterName));
            return;
        }

        if (config.getNodes().size() == 1) {
            po.addLogFailed("This is the last node in the cluster. Please, destroy cluster instead\nOperation aborted");
            return;
        }

        //destroy lxc
        po.addLog("Destroying lxc container...");
        Agent physicalAgent = manager.getAgentManager().getAgentByHostname(agent.getParentHostName());
        if (physicalAgent == null) {
            po.addLog(
                    String.format("Could not determine physical parent of %s. Use LXC module to cleanup, skipping...",
                            agent.getHostname())
            );
        } else {
            if (!manager.getLxcManager().destroyLxcOnHost(physicalAgent, agent.getHostname())) {
                po.addLog("Could not destroy lxc container. Use LXC module to cleanup, skipping...");
            } else {
                po.addLog("Lxc container destroyed successfully");
            }
        }
        //update db
        po.addLog("Updating db...");
        config.getNodes().remove(agent);
        if (!manager.getDbManager().saveInfo(Config.PRODUCT_KEY, config.getClusterName(), config)) {
            po.addLogFailed(String.format("Error while updating cluster info [%s] in DB. Check logs\nFailed",
                    config.getClusterName()));
        } else {
            po.addLogDone("Done");
        }
    }
}
