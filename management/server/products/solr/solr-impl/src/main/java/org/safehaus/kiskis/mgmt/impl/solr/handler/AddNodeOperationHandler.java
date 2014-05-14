package org.safehaus.kiskis.mgmt.impl.solr.handler;

import org.safehaus.kiskis.mgmt.api.commandrunner.Command;
import org.safehaus.kiskis.mgmt.api.lxcmanager.LxcCreateException;
import org.safehaus.kiskis.mgmt.api.solr.Config;
import org.safehaus.kiskis.mgmt.shared.protocol.operation.ProductOperation;
import org.safehaus.kiskis.mgmt.impl.solr.Commands;
import org.safehaus.kiskis.mgmt.impl.solr.SolrImpl;
import org.safehaus.kiskis.mgmt.shared.protocol.operation.AbstractOperationHandler;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Created by dilshat on 5/7/14.
 */
public class AddNodeOperationHandler extends AbstractOperationHandler<SolrImpl> {
    private final ProductOperation po;

    public AddNodeOperationHandler(SolrImpl manager, String clusterName) {
        super(manager, clusterName);
        po = manager.getTracker().createProductOperation(Config.PRODUCT_KEY,
                String.format("Adding node to %s", clusterName));
    }

    @Override
    public UUID getTrackerId() {
        return po.getId();
    }

    @Override
    public void run() {
        Config config = manager.getCluster(clusterName);
        if (config == null) {
            po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", clusterName));
            return;
        }

        try {

            po.addLog("Creating lxc container...");

            Map<Agent, Set<Agent>> lxcAgentsMap = manager.getLxcManager().createLxcs(1);

            Agent lxcAgent = lxcAgentsMap.entrySet().iterator().next().getValue().iterator().next();

            config.getNodes().add(lxcAgent);
            po.addLog("Lxc container created successfully\nUpdating db...");
            if (manager.getDbManager().saveInfo(Config.PRODUCT_KEY, clusterName, config)) {
                po.addLog("Cluster info updated in DB\nInstalling Solr...");

                Command installCommand = Commands.getInstallCommand(Util.wrapAgentToSet(lxcAgent));
                manager.getCommandRunner().runCommand(installCommand);

                if (installCommand.hasSucceeded()) {
                    po.addLogDone("Installation succeeded\nDone");

                } else {
                    po.addLogFailed(String.format("Installation failed, %s",
                            installCommand.getAllErrors()));
                }
            } else {
                po.addLogFailed("Error while updating cluster info in DB. Check logs. Use LXC Module to cleanup\nFailed");
            }

        } catch (LxcCreateException ex) {
            po.addLogFailed(ex.getMessage());
        }
    }
}
