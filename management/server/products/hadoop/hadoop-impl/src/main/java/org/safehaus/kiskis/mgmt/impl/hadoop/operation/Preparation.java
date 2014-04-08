package org.safehaus.kiskis.mgmt.impl.hadoop.operation;

import com.google.common.base.Strings;
import org.safehaus.kiskis.mgmt.api.hadoop.Config;
import org.safehaus.kiskis.mgmt.api.lxcmanager.LxcCreateException;
import org.safehaus.kiskis.mgmt.api.lxcmanager.LxcDestroyException;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperation;
import org.safehaus.kiskis.mgmt.impl.hadoop.HadoopImpl;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

import java.util.*;

/**
 * Created by daralbaev on 08.04.14.
 */
public class Preparation {
    private HadoopImpl parent;
    private Config config;

    public Preparation(HadoopImpl parent, Config config) {
        this.parent = parent;
        this.config = config;
    }

    public UUID execute() {
        final ProductOperation po = parent.getTracker().createProductOperation(Config.PRODUCT_KEY, "Creating LXC for Hadoop");

        parent.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                if (config == null || Strings.isNullOrEmpty(config.getClusterName()) || Strings.isNullOrEmpty(config.getDomainName())) {
                    po.addLogFailed("Malformed configuration\nLXC creation aborted");
                    return;
                }

                if (parent.getDbManager().getInfo(Config.PRODUCT_KEY, config.getClusterName(), Config.class) != null) {
                    po.addLogFailed(String.format("Cluster with name '%s' already exists\nLXC creation aborted", config.getClusterName()));
                    return;
                }

                try {
                    po.addLog(String.format("Creating %d lxc containers...", config.getNodeSize()));
                    Map<Agent, Set<Agent>> lxcAgentsMap = parent.getLxcManager().createLxcs(config.getNodeSize());
                    config.setNodes(new HashSet<Agent>());

                    for (Map.Entry<Agent, Set<Agent>> entry : lxcAgentsMap.entrySet()) {
                        config.getNodes().addAll(entry.getValue());
                    }
                    po.addLog("Lxc containers created successfully\nConfiguring network...");

                    if (parent.getNetworkManager().configHostsOnAgents(new ArrayList<Agent>(config.getNodes()), config.getDomainName()) &&
                            parent.getNetworkManager().configSshOnAgents(new ArrayList<Agent>(config.getNodes()))) {
                        po.addLog("Cluster network configured");

                        if (parent.getDbManager().saveInfo(Config.PRODUCT_KEY, config.getClusterName(), config)) {
                            po.addLog("Cluster info saved to DB");
                        } else {
                            destroyLXC(po, "Could not save cluster info to DB! Please see logs\nLXC creation aborted");
                        }
                    } else {
                        destroyLXC(po, "Could not configure network! Please see logs\nLXC creation aborted");
                    }
                } catch (LxcCreateException ex) {
                    po.addLogFailed(ex.getMessage());
                }
            }
        });

        return po.getId();
    }

    private void destroyLXC(ProductOperation po, String log) {
        //destroy all lxcs also
        Set<String> lxcHostnames = new HashSet<String>();
        for (Agent lxcAgent : config.getNodes()) {
            lxcHostnames.add(lxcAgent.getHostname());
        }
        try {
            parent.getLxcManager().destroyLxcs(lxcHostnames);
            if (parent.getDbManager().deleteInfo(Config.PRODUCT_KEY, config.getClusterName())) {
                po.addLogDone("Cluster info deleted from DB\nDone");
            } else {
                po.addLogFailed("Error while deleting cluster info from DB. Check logs.\nFailed");
            }
        } catch (LxcDestroyException ex) {
            po.addLogFailed(log + "\nUse LXC module to cleanup");
        }
        po.addLogFailed(log);
    }
}
