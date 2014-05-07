package org.safehaus.kiskis.mgmt.impl.solr.handler;

import com.google.common.base.Strings;
import org.safehaus.kiskis.mgmt.api.commandrunner.Command;
import org.safehaus.kiskis.mgmt.api.lxcmanager.LxcCreateException;
import org.safehaus.kiskis.mgmt.api.lxcmanager.LxcDestroyException;
import org.safehaus.kiskis.mgmt.api.solr.Config;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperation;
import org.safehaus.kiskis.mgmt.impl.solr.Commands;
import org.safehaus.kiskis.mgmt.impl.solr.SolrImpl;
import org.safehaus.kiskis.mgmt.shared.protocol.AbstractOperationHandler;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Created by dilshat on 5/7/14.
 */
public class InstallOperationHandler extends AbstractOperationHandler<SolrImpl> {
    private final ProductOperation po;
    private final Config config;

    public InstallOperationHandler(SolrImpl manager, Config config) {
        super(manager, config.getClusterName());
        this.config = config;
        po = manager.getTracker().createProductOperation(Config.PRODUCT_KEY, String.format("Installing %s", Config.PRODUCT_KEY));
    }

    @Override
    public UUID getTrackerId() {
        return po.getId();
    }

    @Override
    public void run() {

        if (Strings.isNullOrEmpty(config.getClusterName()) || config.getNumberOfNodes() <= 0) {
            po.addLogFailed("Malformed configuration\nInstallation aborted");
            return;
        }

        if (manager.getCluster(config.getClusterName()) != null) {
            po.addLogFailed(String.format("Cluster with name '%s' already exists\nInstallation aborted", config.getClusterName()));
            return;
        }

        try {
            po.addLog(String.format("Creating %d lxc containers...", config.getNumberOfNodes()));
            Map<Agent, Set<Agent>> lxcAgentsMap = manager.getLxcManager().createLxcs(config.getNumberOfNodes());
            config.setNodes(new HashSet<Agent>());

            for (Map.Entry<Agent, Set<Agent>> entry : lxcAgentsMap.entrySet()) {
                config.getNodes().addAll(entry.getValue());
            }
            po.addLog("Lxc containers created successfully\nUpdating db...");
            if (manager.getDbManager().saveInfo(Config.PRODUCT_KEY, config.getClusterName(), config)) {

                po.addLog("Cluster info saved to DB\nInstalling Solr...");

                //install
                Command installCommand = Commands.getInstallCommand(config.getNodes());
                manager.getCommandRunner().runCommand(installCommand);

                if (installCommand.hasSucceeded()) {
                    po.addLogDone("Installation succeeded");
                } else {
                    po.addLogFailed(String.format("Installation failed, %s", installCommand.getAllErrors()));
                }

            } else {
                //destroy all lxcs also
                try {
                    manager.getLxcManager().destroyLxcs(lxcAgentsMap);
                } catch (LxcDestroyException ex) {
                    po.addLogFailed("Could not save cluster info to DB! Please see logs. Use LXC module to cleanup\nInstallation aborted");
                }
                po.addLogFailed("Could not save cluster info to DB! Please see logs\nInstallation aborted");
            }
        } catch (LxcCreateException ex) {
            po.addLogFailed(ex.getMessage());
        }
    }
}
