package org.safehaus.kiskis.mgmt.impl.flume;

import java.util.*;
import java.util.concurrent.*;
import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.api.commandrunner.*;
import org.safehaus.kiskis.mgmt.api.dbmanager.DbManager;
import org.safehaus.kiskis.mgmt.api.flume.Config;
import org.safehaus.kiskis.mgmt.api.flume.Flume;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperation;
import org.safehaus.kiskis.mgmt.api.tracker.Tracker;
import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.NodeState;

public class FlumeImpl implements Flume {

    private CommandRunner commandRunner;
    private AgentManager agentManager;
    private Tracker tracker;
    private DbManager dbManager;

    private ExecutorService executor;

    public void init() {
        executor = Executors.newCachedThreadPool();
    }

    public void destroy() {
        executor.shutdown();
    }

    public void setCommandRunner(CommandRunner commandRunner) {
        this.commandRunner = commandRunner;
    }

    public void setAgentManager(AgentManager agentManager) {
        this.agentManager = agentManager;
    }

    public void setTracker(Tracker tracker) {
        this.tracker = tracker;
    }

    public void setDbManager(DbManager dbManager) {
        this.dbManager = dbManager;
    }

    public UUID installCluster(final Config config) {
        final ProductOperation po = tracker.createProductOperation(
                Config.PRODUCT_KEY,
                String.format("Installing %s", Config.PRODUCT_KEY));

        executor.execute(new Runnable() {

            public void run() {
                if(config == null) {
                    po.addLogFailed("Malformed configuration\nInstallation aborted");
                    return;
                }
                if(getCluster(config.getClusterName()) != null) {
                    po.addLogFailed(String.format(
                            "Cluster with name '%s' already exists\nInstallation aborted",
                            config.getClusterName()));
                    return;
                }

                //check if node agent is connected
                for(Iterator<Agent> it = config.getNodes().iterator(); it.hasNext();) {
                    Agent node = it.next();
                    if(agentManager.getAgentByHostname(node.getHostname()) != null)
                        continue;
                    po.addLog(String.format(
                            "Node %s is not connected. Omitting this node from installation",
                            node.getHostname()));
                    it.remove();
                }
                if(config.getNodes().isEmpty()) {
                    po.addLogFailed("No nodes eligible for installation. Operation aborted");
                    return;
                }

                po.addLog("Checking prerequisites...");
                //check installed ksks packages
                Command cmd = commandRunner.createCommand(
                        new RequestBuilder(Commands.make(CommandType.STATUS)),
                        config.getNodes());
                commandRunner.runCommand(cmd);
                if(!cmd.hasSucceeded()) {
                    po.addLogFailed("Failed to check presence of installed ksks packages\nInstallation aborted");
                    return;
                }

                for(Iterator<Agent> it = config.getNodes().iterator(); it.hasNext();) {
                    Agent node = it.next();
                    AgentResult result = cmd.getResults().get(node.getUuid());

                    if(result.getStdOut().contains("ksks-flume")) {
                        po.addLog(String.format(
                                "Node %s already has Flume installed. Omitting this node from installation",
                                node.getHostname()));
                        it.remove();
                    } else if(!result.getStdOut().contains("ksks-hadoop")) {
                        po.addLog(String.format(
                                "Node %s has no Hadoop installation. Omitting this node from installation",
                                node.getHostname()));
                        it.remove();
                    }
                }

                if(config.getNodes().isEmpty()) {
                    po.addLogFailed("No nodes eligible for installation. Operation aborted");
                    return;
                }

                po.addLog("Updating db...");
                if(dbManager.saveInfo(Config.PRODUCT_KEY, config.getClusterName(), config)) {
                    po.addLog("Cluster info saved to DB\nInstalling Flume...");

                    String s = Commands.make(CommandType.INSTALL);
                    cmd = commandRunner.createCommand(
                            new RequestBuilder(s).withTimeout(90),
                            config.getNodes());
                    commandRunner.runCommand(cmd);

                    if(cmd.hasSucceeded()) {
                        po.addLogDone("Installation succeeded\nDone");
                    } else {
                        po.addLogFailed(String.format("Installation failed, %s",
                                cmd.getAllErrors()));
                    }
                } else {
                    po.addLogFailed("Could not save cluster info to DB! Please see logs\nInstallation aborted");
                }
            }
        });

        return po.getId();
    }

    public UUID uninstallCluster(final String clusterName) {
        final ProductOperation po = tracker.createProductOperation(
                Config.PRODUCT_KEY,
                String.format("Destroying cluster %s", clusterName));

        executor.execute(new Runnable() {

            public void run() {
                Config config = getCluster(clusterName);
                if(config == null) {
                    po.addLogFailed(String.format(
                            "Cluster with name %s does not exist\nOperation aborted",
                            clusterName));
                    return;
                }

                po.addLog("Uninstalling Flume...");

                Command cmd = commandRunner.createCommand(
                        new RequestBuilder(Commands.make(CommandType.UNINSTALL)),
                        config.getNodes());
                commandRunner.runCommand(cmd);

                if(cmd.hasCompleted()) {
                    for(Agent agent : config.getNodes()) {
                        AgentResult result = cmd.getResults().get(agent.getUuid());
                        if(isZero(result.getExitCode())) {
                            if(result.getStdOut().contains("ksks-flume is not installed")) {
                                po.addLog(String.format(
                                        "Flume is not installed, so not removed on node %s",
                                        agent.getHostname()));
                            } else {
                                po.addLog(String.format("Flume is removed from node %s",
                                        agent.getHostname()));
                            }
                        } else {
                            po.addLog(String.format("Error on node %s: %s",
                                    agent.getHostname(), result.getStdErr()));
                        }
                    }

                    po.addLog("Updating db...");
                    if(dbManager.deleteInfo(Config.PRODUCT_KEY, config.getClusterName())) {
                        po.addLogDone("Cluster info deleted from DB\nDone");
                    } else {
                        po.addLogFailed("Error while deleting cluster info from DB. Check logs.\nFailed");
                    }
                } else {
                    po.addLogFailed(String.format("Uninstallation failed, %s",
                            cmd.getAllErrors()));
                }

            }
        });

        return po.getId();
    }

    public UUID startNode(final String clusterName, final String hostname) {
        final ProductOperation po = tracker.createProductOperation(
                Config.PRODUCT_KEY,
                String.format("Starting node %s in %s", hostname, clusterName));

        executor.execute(new Runnable() {

            public void run() {
                if(getCluster(clusterName) == null) {
                    po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", clusterName));
                    return;
                }

                final Agent node = agentManager.getAgentByHostname(hostname);
                if(node == null) {
                    po.addLogFailed(String.format("Agent with hostname %s is not connected\nOperation aborted", hostname));
                    return;
                }

                po.addLog("Starting node...");
                Command cmd = commandRunner.createCommand(
                        new RequestBuilder(Commands.make(CommandType.START)),
                        new HashSet<Agent>(Arrays.asList(node)));
                commandRunner.runCommand(cmd);

                if(cmd.hasSucceeded()) {
                    po.addLogDone(String.format("Node on %s started", hostname));
                } else {
                    AgentResult res = cmd.getResults().get(node.getUuid());
                    if(res.getStdOut().contains("agent running"))
                        po.addLogDone("Flume already started on " + hostname);
                    else {
                        po.addLog(res.getStdOut());
                        po.addLog(res.getStdErr());
                        po.addLogFailed("Failed to start node " + hostname);
                    }
                }

            }
        });

        return po.getId();
    }

    public UUID stopNode(final String clusterName, final String hostname) {
        final ProductOperation po = tracker.createProductOperation(
                Config.PRODUCT_KEY,
                String.format("Stopping node %s in %s", hostname, clusterName));

        executor.execute(new Runnable() {

            public void run() {
                if(getCluster(clusterName) == null) {
                    po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", clusterName));
                    return;
                }

                final Agent node = agentManager.getAgentByHostname(hostname);
                if(node == null) {
                    po.addLogFailed(String.format("Agent with hostname %s is not connected\nOperation aborted", hostname));
                    return;
                }

                po.addLog("Stopping node...");
                Command cmd = commandRunner.createCommand(
                        new RequestBuilder(Commands.make(CommandType.STOP)),
                        new HashSet<Agent>(Arrays.asList(node)));
                commandRunner.runCommand(cmd);

                if(cmd.hasSucceeded()) {
                    po.addLogDone(String.format("Node on %s stopped", hostname));
                } else {
                    po.addLogFailed(String.format("Failed to stop node %s. %s",
                            hostname, cmd.getAllErrors()));
                }

            }
        });

        return po.getId();
    }

    public UUID checkNode(final String clusterName, final String hostname) {
        final ProductOperation po = tracker.createProductOperation(
                Config.PRODUCT_KEY,
                String.format("Checking node %s in %s", hostname, clusterName));

        executor.execute(new Runnable() {

            public void run() {
                if(getCluster(clusterName) == null) {
                    po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", clusterName));
                    return;
                }

                final Agent node = agentManager.getAgentByHostname(hostname);
                if(node == null) {
                    po.addLogFailed(String.format("Agent with hostname %s is not connected\nOperation aborted", hostname));
                    return;
                }

                po.addLog("Checking node...");
                Command cmd = commandRunner.createCommand(
                        new RequestBuilder(Commands.make(CommandType.STATUS)),
                        new HashSet<Agent>(Arrays.asList(node)));
                commandRunner.runCommand(cmd);

                NodeState nodeState = NodeState.UNKNOWN;
                if(cmd.hasSucceeded()) {
                    AgentResult result = cmd.getResults().get(node.getUuid());
                    if(result.getStdOut().contains("is running")) {
                        nodeState = NodeState.RUNNING;
                    } else if(result.getStdOut().contains("is not running")) {
                        nodeState = NodeState.STOPPED;
                    }
                }

                if(NodeState.UNKNOWN.equals(nodeState)) {
                    po.addLogFailed(String.format("Failed to check status of %s, %s",
                            hostname, cmd.getAllErrors()));
                } else {
                    po.addLogDone(String.format("Node %s is %s", hostname,
                            nodeState));
                }

            }
        });

        return po.getId();
    }

    public UUID addNode(final String clusterName, final String hostname) {
        final ProductOperation po = tracker.createProductOperation(
                Config.PRODUCT_KEY,
                String.format("Adding node to %s", clusterName));

        executor.execute(new Runnable() {

            public void run() {
                Config config = getCluster(clusterName);
                if(config == null) {
                    po.addLogFailed(String.format(
                            "Cluster with name %s does not exist\nOperation aborted",
                            clusterName));
                    return;
                }

                //check if node agent is connected
                Agent agent = agentManager.getAgentByHostname(hostname);
                if(agent == null) {
                    po.addLogFailed(String.format(
                            "Node %s is not connected\nOperation aborted",
                            hostname));
                    return;
                }
                Set<Agent> set = new HashSet<Agent>(Arrays.asList(agent));

                po.addLog("Checking prerequisites...");
                Command cmd = commandRunner.createCommand(
                        new RequestBuilder(Commands.make(CommandType.STATUS)), set);
                commandRunner.runCommand(cmd);
                if(!cmd.hasSucceeded()) {
                    po.addLogFailed("Failed to check installed packages\nInstallation aborted");
                    return;
                }

                AgentResult result = cmd.getResults().get(agent.getUuid());

                if(result.getStdOut().contains("ksks-flume")) {
                    po.addLogFailed(String.format("Node %s already has Flume installed\nInstallation aborted", hostname));
                    return;
                } else if(!result.getStdOut().contains("ksks-hadoop")) {
                    po.addLogFailed(String.format("Node %s has no Hadoop installation\nInstallation aborted", hostname));
                    return;
                }

                config.getNodes().add(agent);

                po.addLog("Updating db...");
                if(dbManager.saveInfo(Config.PRODUCT_KEY, config.getClusterName(), config)) {
                    po.addLog("Cluster info updated in DB\nInstalling Flume...");

                    cmd = commandRunner.createCommand(
                            new RequestBuilder(Commands.make(CommandType.INSTALL)),
                            set);
                    commandRunner.runCommand(cmd);

                    if(cmd.hasSucceeded()) {
                        po.addLogDone("Installation succeeded\nDone");
                    } else {
                        po.addLogFailed(String.format("Installation failed: %s",
                                cmd.getAllErrors()));
                    }
                } else {
                    po.addLogFailed("Could not update cluster info in DB! Please see logs\nInstallation aborted");
                }

            }
        });

        return po.getId();
    }

    public UUID destroyNode(final String clusterName, final String hostname) {
        final ProductOperation po = tracker.createProductOperation(
                Config.PRODUCT_KEY,
                String.format("Destroying %s in %s", hostname, clusterName));

        executor.execute(new Runnable() {

            public void run() {
                Config config = getCluster(clusterName);
                if(config == null) {
                    po.addLogFailed(String.format(
                            "Cluster with name %s does not exist\nOperation aborted",
                            clusterName));
                    return;
                }

                Agent agent = agentManager.getAgentByHostname(hostname);
                if(agent == null) {
                    po.addLogFailed(String.format(
                            "Agent with hostname %s is not connected\nOperation aborted",
                            hostname));
                    return;
                }

                if(config.getNodes().size() == 1) {
                    po.addLogFailed("This is the last node in the cluster. Please, destroy cluster instead\nOperation aborted");
                    return;
                }

                po.addLog("Uninstalling Flume...");
                Set<Agent> set = new HashSet<Agent>(Arrays.asList(agent));
                Command cmd = commandRunner.createCommand(
                        new RequestBuilder(Commands.make(CommandType.UNINSTALL)),
                        set);
                commandRunner.runCommand(cmd);

                if(cmd.hasCompleted()) {
                    AgentResult result = cmd.getResults().get(agent.getUuid());
                    if(isZero(result.getExitCode())) {
                        if(result.getStdOut().contains("ksks-flume is not installed")) {
                            po.addLog(String.format(
                                    "Flume is not installed, so not removed on node %s",
                                    agent.getHostname()));
                        } else {
                            po.addLog(String.format("Flume is removed from node %s",
                                    agent.getHostname()));
                        }
                    } else {
                        po.addLog(String.format("Error on node %s: %s",
                                agent.getHostname(), result.getStdErr()));
                    }

                    config.getNodes().remove(agent);

                    po.addLog("Updating db...");
                    if(dbManager.saveInfo(Config.PRODUCT_KEY, config.getClusterName(), config)) {
                        po.addLogDone("Cluster info updated in DB\nDone");
                    } else {
                        po.addLogFailed("Error while updating cluster info in DB. Check logs.\nFailed");
                    }
                } else {
                    po.addLogFailed(String.format("Uninstallation failed: %s",
                            cmd.getAllErrors()));
                }
            }
        });

        return po.getId();
    }

    public List<Config> getClusters() {
        return dbManager.getInfo(Config.PRODUCT_KEY, Config.class);
    }

    @Override
    public Config getCluster(String clusterName) {
        return dbManager.getInfo(Config.PRODUCT_KEY, clusterName, Config.class);
    }

    private boolean isZero(Integer i) {
        return i != null && i.intValue() == 0;
    }
}
