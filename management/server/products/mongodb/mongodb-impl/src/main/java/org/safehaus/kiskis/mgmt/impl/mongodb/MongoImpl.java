/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.mongodb;

import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.api.dbmanager.DbManager;
import org.safehaus.kiskis.mgmt.api.lxcmanager.LxcCreateException;
import org.safehaus.kiskis.mgmt.api.lxcmanager.LxcDestroyException;
import org.safehaus.kiskis.mgmt.api.lxcmanager.LxcManager;
import org.safehaus.kiskis.mgmt.api.mongodb.Config;
import org.safehaus.kiskis.mgmt.api.mongodb.Mongo;
import org.safehaus.kiskis.mgmt.api.mongodb.NodeType;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperation;
import org.safehaus.kiskis.mgmt.api.tracker.Tracker;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.NodeState;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.safehaus.kiskis.mgmt.api.commandrunner.AgentResult;
import org.safehaus.kiskis.mgmt.api.commandrunner.Command;
import org.safehaus.kiskis.mgmt.api.commandrunner.CommandCallback;
import org.safehaus.kiskis.mgmt.api.commandrunner.CommandRunner;
import org.safehaus.kiskis.mgmt.api.commandrunner.CommandStatus;
import org.safehaus.kiskis.mgmt.impl.mongodb.common.CommandType;
import org.safehaus.kiskis.mgmt.impl.mongodb.common.Commands;

/**
 * @author dilshat
 */
public class MongoImpl implements Mongo {

    private static CommandRunner commandRunner;
    private static AgentManager agentManager;
    private static DbManager dbManager;
    private static LxcManager lxcManager;
    private static Tracker tracker;
    private static ExecutorService executor;

    public static Tracker getTracker() {
        return tracker;
    }

    public void setTracker(Tracker tracker) {
        MongoImpl.tracker = tracker;
    }

    public void setLxcManager(LxcManager lxcManager) {
        MongoImpl.lxcManager = lxcManager;
    }

    public void setAgentManager(AgentManager agentManager) {
        MongoImpl.agentManager = agentManager;
    }

    public void setDbManager(DbManager dbManager) {
        MongoImpl.dbManager = dbManager;
    }

    public void setCommandRunner(CommandRunner commandRunner) {
        MongoImpl.commandRunner = commandRunner;
    }

    public static CommandRunner getCommandRunner() {
        return commandRunner;
    }

    public static AgentManager getAgentManager() {
        return agentManager;
    }

    public static DbManager getDbManager() {
        return dbManager;
    }

    public static LxcManager getLxcManager() {
        return lxcManager;
    }

    public static ExecutorService getExecutor() {
        return executor;
    }

    public void init() {
        executor = Executors.newCachedThreadPool();
    }

    public void destroy() {
        MongoImpl.commandRunner = null;
        MongoImpl.agentManager = null;
        MongoImpl.dbManager = null;
        MongoImpl.lxcManager = null;
        MongoImpl.tracker = null;
        executor.shutdown();
    }

    public UUID installCluster(final Config config) {
        final ProductOperation po
                = tracker.createProductOperation(Config.PRODUCT_KEY,
                        String.format("Installing cluster %s", config.getClusterName()));

        executor.execute(new Runnable() {

            public void run() {

                if (config == null
                        || Util.isStringEmpty(config.getClusterName())
                        || Util.isStringEmpty(config.getReplicaSetName())
                        || Util.isStringEmpty(config.getDomainName())
                        || config.getNumberOfConfigServers() <= 0
                        || config.getNumberOfRouters() <= 0
                        || config.getNumberOfDataNodes() <= 0
                        || config.getCfgSrvPort() <= 0
                        || config.getDataNodePort() <= 0
                        || config.getRouterPort() <= 0) {
                    po.addLogFailed("Malformed configuration\nInstallation aborted");
                    return;
                }

                //check if mongo cluster with the same name already exists
                if (dbManager.getInfo(Config.PRODUCT_KEY, config.getClusterName(), Config.class) != null) {
                    po.addLogFailed(String.format("Cluster with name '%s' already exists\nInstallation aborted", config.getClusterName()));
                    return;
                }

                try {
                    int numberOfLxcsNeeded = config.getNumberOfConfigServers() + config.getNumberOfRouters() + config.getNumberOfDataNodes();
                    //clone lxc containers
                    po.addLog(String.format("Creating %d lxc containers...", numberOfLxcsNeeded));
                    Map<Agent, Set<Agent>> lxcAgentsMap = lxcManager.createLxcs(numberOfLxcsNeeded);

                    Set<Agent> cfgServers = new HashSet<Agent>();
                    Set<Agent> routers = new HashSet<Agent>();
                    Set<Agent> dataNodes = new HashSet<Agent>();

                    Set<Agent> availableAgents = new HashSet<Agent>();

                    for (Map.Entry<Agent, Set<Agent>> entry : lxcAgentsMap.entrySet()) {
                        availableAgents.addAll(entry.getValue());
                    }
                    for (Agent lxcAgent : availableAgents) {
                        if (cfgServers.size() < config.getNumberOfConfigServers()) {
                            cfgServers.add(lxcAgent);
                        } else if (routers.size() < config.getNumberOfRouters()) {
                            routers.add(lxcAgent);
                        } else if (dataNodes.size() < config.getNumberOfDataNodes()) {
                            dataNodes.add(lxcAgent);
                        } else {
                            break;
                        }
                    }
                    config.setConfigServers(cfgServers);
                    config.setDataNodes(dataNodes);
                    config.setRouterServers(routers);
                    po.addLog("Lxc containers created successfully\nUpdating db...");
                    if (dbManager.saveInfo(Config.PRODUCT_KEY, config.getClusterName(), config)) {
                        po.addLog("Cluster info saved to DB\nInstalling Mongo...");
                        installMongoCluster(config, po);
                    } else {
                        //destroy all lxcs also
                        Set<String> lxcHostnames = new HashSet<String>();
                        for (Agent lxcAgent : config.getConfigServers()) {
                            lxcHostnames.add(lxcAgent.getHostname());
                        }
                        for (Agent lxcAgent : config.getRouterServers()) {
                            lxcHostnames.add(lxcAgent.getHostname());
                        }
                        for (Agent lxcAgent : config.getDataNodes()) {
                            lxcHostnames.add(lxcAgent.getHostname());
                        }
                        try {
                            lxcManager.destroyLxcs(lxcHostnames);
                        } catch (LxcDestroyException ex) {
                            po.addLogFailed("Could not save cluster info to DB! Please see logs. Use LXC module to cleanup\nInstallation aborted");
                        }
                        po.addLogFailed("Could not save cluster info to DB! Please see logs\nInstallation aborted");
                    }

                } catch (LxcCreateException ex) {
                    po.addLogFailed(ex.getMessage());
                }

            }
        });

        return po.getId();
    }

    private void installMongoCluster(final Config config, final ProductOperation po) {

        List<Command> installationCommands = Commands.getInstallationCommands(config);

        boolean installationOK = true;

        for (Command command : installationCommands) {
            po.addLog(String.format("Running command: %s", command.getDescription()));
            final AtomicBoolean commandOK = new AtomicBoolean();

            if (command.getData() == CommandType.START_CONFIG_SERVERS
                    || command.getData() == CommandType.START_ROUTERS
                    || command.getData() == CommandType.START_DATA_NODES) {
                commandRunner.runCommand(command, new CommandCallback() {

                    @Override
                    public void onResponse(Response response, AgentResult agentResult, Command command) {

                        int count = 0;
                        for (AgentResult result : command.getResults().values()) {
                            if (result.getStdOut().contains("child process started successfully, parent exiting")) {
                                count++;
                            }
                        }
                        if (command.getData() == CommandType.START_CONFIG_SERVERS) {
                            if (count == config.getConfigServers().size()) {
                                commandOK.set(true);
                            }
                        } else if (command.getData() == CommandType.START_ROUTERS) {
                            if (count == config.getRouterServers().size()) {
                                commandOK.set(true);
                            }
                        } else if (command.getData() == CommandType.START_DATA_NODES) {
                            if (count == config.getDataNodes().size()) {
                                commandOK.set(true);
                            }
                        }
                        if (commandOK.get()) {
                            stop();
                        }

                    }

                });
            } else {
                commandRunner.runCommand(command);
            }

            if (command.getCommandStatus() == CommandStatus.SUCCEEDED || commandOK.get()) {
                po.addLog(String.format("Command %s succeeded", command.getDescription()));
            } else {
                po.addLog(String.format("Command %s failed: %s", command.getDescription(), command.getAllErrors()));
                installationOK = false;
                break;
            }
        }

        if (installationOK) {
            po.addLogDone("Installation succeeded");
        } else {
            po.addLogFailed("Installation failed");
        }

    }

    public UUID addNode(final String clusterName, final NodeType nodeType) {
        final ProductOperation po
                = tracker.createProductOperation(Config.PRODUCT_KEY,
                        String.format("Adding %s to %s", nodeType, clusterName));

        executor.execute(new Runnable() {

            public void run() {
                Config config = dbManager.getInfo(Config.PRODUCT_KEY, clusterName, Config.class);
                if (config == null) {
                    po.addLogFailed(String.format("Cluster with name %s does not exist", clusterName));
                    return;
                }
                if (nodeType == NodeType.DATA_NODE && config.getDataNodes().size() == 7) {
                    po.addLogFailed("Replica set cannot have more than 7 members.\nOperation aborted");
                    return;
                }
                try {

                    po.addLog("Creating lxc container");

                    Map<Agent, Set<Agent>> lxcAgentsMap = lxcManager.createLxcs(1);

                    Agent agent = lxcAgentsMap.entrySet().iterator().next().getValue().iterator().next();

                    if (nodeType == NodeType.DATA_NODE) {
                        config.getDataNodes().add(agent);
                    } else if (nodeType == NodeType.CONFIG_NODE) {
                        config.getConfigServers().add(agent);
                    } else if (nodeType == NodeType.ROUTER_NODE) {
                        config.getRouterServers().add(agent);
                    }
                    po.addLog("Lxc container created successfully\nUpdating db...");
                    if (dbManager.saveInfo(Config.PRODUCT_KEY, config.getClusterName(), config)) {
                        po.addLog("Cluster info updated in DB\nInstalling Mongo");
                        //start addition of node
                        if (nodeType == NodeType.DATA_NODE) {
                            addDataNode(po, config, agent);
                        } else if (nodeType == NodeType.ROUTER_NODE) {
                            addRouter(po, config, agent);
                        }
                    } else {
                        po.addLogFailed("Error while updating cluster info in DB. Check logs. Use LXC Module to cleanup\nFailed");
                    }
                } catch (LxcCreateException ex) {
                    po.addLogFailed(ex.getMessage());
                }

            }
        });

        return po.getId();
    }

    public UUID uninstallCluster(final String clusterName) {
        final ProductOperation po
                = tracker.createProductOperation(Config.PRODUCT_KEY,
                        String.format("Destroying cluster %s", clusterName));
        executor.execute(new Runnable() {

            public void run() {
                Config config = dbManager.getInfo(Config.PRODUCT_KEY, clusterName, Config.class);
                if (config == null) {
                    po.addLogFailed(String.format("Cluster with name %s does not exist", clusterName));
                    return;
                }

                po.addLog("Destroying lxc containers");
                Set<String> lxcHostnames = new HashSet<String>();
                for (Agent lxcAgent : config.getConfigServers()) {
                    lxcHostnames.add(lxcAgent.getHostname());
                }
                for (Agent lxcAgent : config.getRouterServers()) {
                    lxcHostnames.add(lxcAgent.getHostname());
                }
                for (Agent lxcAgent : config.getDataNodes()) {
                    lxcHostnames.add(lxcAgent.getHostname());
                }
                try {
                    lxcManager.destroyLxcs(lxcHostnames);
                    po.addLog("Lxc containers successfully destroyed");
                } catch (LxcDestroyException ex) {
                    po.addLog(String.format("%s, skipping...", ex.getMessage()));
                }
                po.addLog("Updating db...");
                if (dbManager.deleteInfo(Config.PRODUCT_KEY, config.getClusterName())) {
                    po.addLogDone("Cluster info deleted from DB\nDone");
                } else {
                    po.addLogFailed("Error while deleting cluster info from DB. Check logs\nFailed");
                }
            }
        });

        return po.getId();
    }

    public UUID destroyNode(final String clusterName, final String lxcHostname) {
        final ProductOperation po
                = tracker.createProductOperation(Config.PRODUCT_KEY,
                        String.format("Destroying %s in %s", lxcHostname, clusterName));

        //go on operation
        executor.execute(new Runnable() {

            public void run() {
                final Config config = dbManager.getInfo(Config.PRODUCT_KEY, clusterName, Config.class);
                if (config == null) {
                    po.addLogFailed(String.format("Cluster with name %s does not exist", clusterName));
                    return;
                }

                Agent agent = agentManager.getAgentByHostname(lxcHostname);
                if (agent == null) {
                    po.addLogFailed(String.format("Agent with hostname %s is not connected", lxcHostname));
                    return;
                }
                if (!config.getAllNodes().contains(agent)) {
                    po.addLogFailed(String.format("Agent with hostname %s does not belong to cluster %s", lxcHostname, clusterName));
                    return;
                }

                final NodeType nodeType = getNodeType(config, agent);
                if (nodeType == NodeType.CONFIG_NODE && config.getConfigServers().size() == 1) {
                    po.addLogFailed("This is the last configuration server in the cluster. Please, destroy cluster instead\nOperation aborted");
                    return;
                } else if (nodeType == NodeType.DATA_NODE && config.getDataNodes().size() == 1) {
                    po.addLogFailed("This is the last data node in the cluster. Please, destroy cluster instead\nOperation aborted");
                    return;
                } else if (nodeType == NodeType.ROUTER_NODE && config.getRouterServers().size() == 1) {
                    po.addLogFailed("This is the last router in the cluster. Please, destroy cluster instead\nOperation aborted");
                    return;
                }

                if (nodeType == NodeType.CONFIG_NODE) {
                    config.getConfigServers().remove(agent);
                    //restart routers
                    po.addLog("Restarting routers...");
                    Command stopRoutersCommand = Commands.getStopNodeCommand(config.getRouterServers());
                    commandRunner.runCommand(stopRoutersCommand);
                    //don't check status of this command since it always ends with execute_timeouted
                    if (stopRoutersCommand.hasCompleted()) {
                        final AtomicInteger okCount = new AtomicInteger();
                        commandRunner.runCommand(
                                Commands.getStartRouterCommand(
                                        config.getRouterPort(),
                                        config.getCfgSrvPort(),
                                        config.getDomainName(),
                                        config.getConfigServers(),
                                        config.getRouterServers()), new CommandCallback() {

                                            @Override
                                            public void onResponse(Response response, AgentResult agentResult, Command command) {
                                                okCount.set(0);
                                                for (AgentResult result : command.getResults().values()) {
                                                    if (result.getStdOut().contains("child process started successfully, parent exiting")) {
                                                        okCount.incrementAndGet();
                                                    }
                                                }
                                                if (okCount.get() == config.getRouterServers().size()) {
                                                    stop();
                                                }
                                            }

                                        });

                        if (okCount.get() != config.getRouterServers().size()) {
                            po.addLog("Not all routers restarted. Use Terminal module to restart them, skipping...");
                        }
                    } else {
                        po.addLog("Could not restart routers. Use Terminal module to restart them, skipping...");
                    }

                } else if (nodeType == NodeType.DATA_NODE) {
                    config.getDataNodes().remove(agent);
                    //unregister from primary
                    po.addLog("Unregistering this node from replica set...");
                    Command findPrimaryNodeCommand = Commands.getFindPrimaryNodeCommand(agent, config.getDataNodePort());
                    commandRunner.runCommand(findPrimaryNodeCommand);

                    if (findPrimaryNodeCommand.hasCompleted() && !findPrimaryNodeCommand.getResults().isEmpty()) {
                        Pattern p = Pattern.compile("primary\" : \"(.*)\"");
                        Matcher m = p.matcher(findPrimaryNodeCommand.getResults().get(agent.getUuid()).getStdOut());
                        Agent primaryNodeAgent = null;
                        if (m.find()) {
                            String primaryNodeHost = m.group(1);
                            if (!Util.isStringEmpty(primaryNodeHost)) {
                                String hostname = primaryNodeHost.split(":")[0].replace("." + config.getDomainName(), "");
                                primaryNodeAgent = agentManager.getAgentByHostname(hostname);
                            }
                        }
                        if (primaryNodeAgent != null) {
                            if (primaryNodeAgent != agent) {
                                Command unregisterSecondaryNodeFromPrimaryCommand
                                        = Commands.getUnregisterSecondaryNodeFromPrimaryCommand(
                                                primaryNodeAgent, config.getDataNodePort(), agent, config.getDomainName());

                                commandRunner.runCommand(unregisterSecondaryNodeFromPrimaryCommand);
                                if (!unregisterSecondaryNodeFromPrimaryCommand.hasCompleted()) {
                                    po.addLog("Could not unregister this node from replica set, skipping...");
                                }
                            }
                        } else {
                            po.addLog("Could not determine primary node for unregistering from replica set, skipping...");
                        }
                    } else {
                        po.addLog("Could not determine primary node for unregistering from replica set, skipping...");
                    }

                } else if (nodeType == NodeType.ROUTER_NODE) {
                    config.getRouterServers().remove(agent);
                }
                //destroy lxc
                po.addLog("Destroying lxc container...");
                Agent physicalAgent = agentManager.getAgentByHostname(agent.getParentHostName());
                if (physicalAgent == null) {
                    po.addLog(
                            String.format("Could not determine physical parent of %s. Use LXC module to cleanup, skipping...",
                                    agent.getHostname())
                    );
                } else {
                    if (!lxcManager.destroyLxcOnHost(physicalAgent, agent.getHostname())) {
                        po.addLog("Could not destroy lxc container. Use LXC module to cleanup, skipping...");
                    } else {
                        po.addLog("Lxc container destroyed successfully");
                    }
                }
                //update db
                po.addLog("Updating db...");
                if (!dbManager.saveInfo(Config.PRODUCT_KEY, config.getClusterName(), config)) {
                    po.addLogFailed(String.format("Error while updating cluster info [%s] in DB. Check logs\nFailed",
                            config.getClusterName()));
                } else {
                    po.addLogDone("Done");
                }
            }
        });

        return po.getId();
    }

    public List<Config> getClusters() {

        return dbManager.getInfo(Config.PRODUCT_KEY, Config.class);
    }

    public UUID startNode(final String clusterName, final String lxcHostname) {
        final ProductOperation po
                = tracker.createProductOperation(Config.PRODUCT_KEY,
                        String.format("Starting node %s in %s", lxcHostname, clusterName));

        executor.execute(new Runnable() {

            public void run() {
                Config config = dbManager.getInfo(Config.PRODUCT_KEY, clusterName, Config.class);
                if (config == null) {
                    po.addLogFailed(String.format("Cluster with name %s does not exist", clusterName));
                    return;
                }

                Agent node = agentManager.getAgentByHostname(lxcHostname);
                if (node == null) {
                    po.addLogFailed(String.format("Agent with hostname %s is not connected", lxcHostname));
                    return;
                }
                if (!config.getAllNodes().contains(node)) {
                    po.addLogFailed(String.format("Agent with hostname %s does not belong to cluster %s", lxcHostname, clusterName));
                    return;
                }

                Command startNodeCommand;
                NodeType nodeType = getNodeType(config, node);

                if (nodeType == NodeType.CONFIG_NODE) {
                    startNodeCommand = Commands.getStartConfigServerCommand(config.getCfgSrvPort(), Util.wrapAgentToSet(node));
                } else if (nodeType == NodeType.DATA_NODE) {
                    startNodeCommand = Commands.getStartDataNodeCommand(config.getDataNodePort(), Util.wrapAgentToSet(node));
                } else {
                    startNodeCommand = Commands.getStartRouterCommand(
                            config.getRouterPort(), config.getCfgSrvPort(),
                            config.getDomainName(), config.getConfigServers(),
                            Util.wrapAgentToSet(node));
                }
                po.addLog("Starting node...");
                commandRunner.runCommand(startNodeCommand, new CommandCallback() {

                    @Override
                    public void onResponse(Response response, AgentResult agentResult, Command command) {
                        if (agentResult.getStdOut().contains("child process started successfully, parent exiting")) {

                            command.setData(NodeState.RUNNING);

                            stop();
                        }
                    }

                });

                if (NodeState.RUNNING.equals(startNodeCommand.getData())) {
                    po.addLogDone(String.format("Node on %s started", lxcHostname));
                } else {
                    po.addLogFailed(String.format("Failed to start node %s. %s",
                            lxcHostname,
                            startNodeCommand.getAllErrors()
                    ));
                }

            }

        });

        return po.getId();
    }

    public UUID stopNode(final String clusterName, final String lxcHostname) {
        final ProductOperation po
                = tracker.createProductOperation(Config.PRODUCT_KEY,
                        String.format("Stopping node %s in %s", lxcHostname, clusterName));

        executor.execute(new Runnable() {

            public void run() {
                Config config = dbManager.getInfo(Config.PRODUCT_KEY, clusterName, Config.class);
                if (config == null) {
                    po.addLogFailed(String.format("Cluster with name %s does not exist", clusterName));
                    return;
                }

                Agent node = agentManager.getAgentByHostname(lxcHostname);
                if (node == null) {
                    po.addLogFailed(String.format("Agent with hostname %s is not connected", lxcHostname));
                    return;
                }
                if (!config.getAllNodes().contains(node)) {
                    po.addLogFailed(String.format("Agent with hostname %s does not belong to cluster %s", lxcHostname, clusterName));
                    return;
                }

                po.addLog("Stopping node...");
                Command stopNodeCommand = Commands.getStopNodeCommand(Util.wrapAgentToSet(node));
                commandRunner.runCommand(stopNodeCommand);

                if (stopNodeCommand.getCommandStatus() == CommandStatus.SUCCEEDED) {
                    po.addLogDone(String.format("Node on %s stopped", lxcHostname));
                } else {
                    po.addLogFailed(String.format("Failed to stop node %s. %s",
                            lxcHostname,
                            stopNodeCommand.getAllErrors()
                    ));
                }

            }
        });

        return po.getId();
    }

    public UUID checkNode(final String clusterName, final String lxcHostname) {
        final ProductOperation po
                = tracker.createProductOperation(Config.PRODUCT_KEY,
                        String.format("Checking state of %s in %s", lxcHostname, clusterName));

        executor.execute(new Runnable() {

            public void run() {
                Config config = dbManager.getInfo(Config.PRODUCT_KEY, clusterName, Config.class);
                if (config == null) {
                    po.addLogFailed(String.format("Cluster with name %s does not exist", clusterName));
                    return;
                }

                Agent node = agentManager.getAgentByHostname(lxcHostname);
                if (node == null) {
                    po.addLogFailed(String.format("Agent with hostname %s is not connected", lxcHostname));
                    return;
                }
                if (!config.getAllNodes().contains(node)) {
                    po.addLogFailed(String.format("Agent with hostname %s does not belong to cluster %s", lxcHostname, clusterName));
                    return;
                }
                po.addLog("Checking node...");
                Command checkNodeCommand = Commands.getCheckInstanceRunningCommand(node, config.getDomainName(), getNodePort(config, node));
                commandRunner.runCommand(checkNodeCommand);

                if (checkNodeCommand.hasCompleted()) {
                    AgentResult agentResult = checkNodeCommand.getResults().get(node.getUuid());
                    if (agentResult != null) {
                        if (agentResult.getStdOut().indexOf("couldn't connect to server") > -1) {
                            po.addLogDone(String.format("Node on %s is %s", lxcHostname, NodeState.STOPPED));
                        } else if (agentResult.getStdOut().indexOf("connecting to") > -1) {
                            po.addLogDone(String.format("Node on %s is %s", lxcHostname, NodeState.RUNNING));
                        } else {
                            po.addLogFailed(String.format("Node on %s is not found", lxcHostname));
                        }
                        return;
                    }
                }
                po.addLogFailed(String.format("Error checking status of node %s : %s", node.getHostname(), checkNodeCommand.getAllErrors()));

            }
        });

        return po.getId();
    }

    private NodeType getNodeType(Config config, Agent node) {
        NodeType nodeType = NodeType.DATA_NODE;

        if (config.getRouterServers().contains(node)) {
            nodeType = NodeType.ROUTER_NODE;
        } else if (config.getConfigServers().contains(node)) {
            nodeType = NodeType.CONFIG_NODE;
        }

        return nodeType;
    }

    private int getNodePort(Config config, Agent node) {

        if (config.getRouterServers().contains(node)) {
            return config.getRouterPort();
        } else if (config.getConfigServers().contains(node)) {
            return config.getCfgSrvPort();
        }

        return config.getDataNodePort();
    }

    private void addDataNode(ProductOperation po, final Config config, Agent agent) {
        List<Command> commands = Commands.getAddDataNodeCommands(config, agent);

        boolean additionOK = true;
        Command findPrimaryNodeCommand = null;

        for (Command command : commands) {
            po.addLog(String.format("Running command: %s", command.getDescription()));
            final AtomicBoolean commandOK = new AtomicBoolean();

            if (command.getData() == CommandType.FIND_PRIMARY_NODE) {
                findPrimaryNodeCommand = command;
            }

            if (command.getData() == CommandType.START_DATA_NODES) {
                commandRunner.runCommand(command, new CommandCallback() {

                    @Override
                    public void onResponse(Response response, AgentResult agentResult, Command command) {
                        if (agentResult.getStdOut().contains(
                                "child process started successfully, parent exiting")) {
                            commandOK.set(true);
                            stop();
                        }
                    }

                });
            } else {
                commandRunner.runCommand(command);
            }

            if (command.getCommandStatus() == CommandStatus.SUCCEEDED || commandOK.get()) {
                po.addLog(String.format("Command %s succeeded", command.getDescription()));
            } else {
                po.addLog(String.format("Command %s failed: %s", command.getDescription(), command.getAllErrors()));
                additionOK = false;
                break;
            }

        }

        //parse result of findPrimaryNodeCommand
        if (additionOK) {
            if (findPrimaryNodeCommand != null && !findPrimaryNodeCommand.getResults().isEmpty()) {
                Agent primaryNodeAgent = null;
                Pattern p = Pattern.compile("primary\" : \"(.*)\"");
                AgentResult result = findPrimaryNodeCommand.getResults().entrySet().iterator().next().getValue();
                Matcher m = p.matcher(result.getStdOut());
                if (m.find()) {
                    String primaryNodeHost = m.group(1);
                    if (!Util.isStringEmpty(primaryNodeHost)) {
                        String hostname = primaryNodeHost.split(":")[0].replace("." + config.getDomainName(), "");
                        primaryNodeAgent = agentManager.getAgentByHostname(hostname);
                    }
                }

                if (primaryNodeAgent != null) {
                    Command registerSecondaryNodeWithPrimaryCommand
                            = Commands.getRegisterSecondaryNodeWithPrimaryCommand(
                                    agent, config.getDataNodePort(), config.getDomainName(), primaryNodeAgent);

                    commandRunner.runCommand(registerSecondaryNodeWithPrimaryCommand);
                    if (registerSecondaryNodeWithPrimaryCommand.getCommandStatus() == CommandStatus.SUCCEEDED) {
                        po.addLogDone(String.format("Command %s succeeded\nNode addition succeeded",
                                registerSecondaryNodeWithPrimaryCommand.getDescription()));
                    } else {
                        po.addLogFailed(String.format("Command %s failed: %s\nNode addition failed",
                                registerSecondaryNodeWithPrimaryCommand.getDescription(),
                                registerSecondaryNodeWithPrimaryCommand.getAllErrors()));
                    }
                } else {
                    po.addLogFailed("Could not find primary node\nNode addition failed");
                }
            } else {
                po.addLogFailed("Could not find primary node\nNode addition failed");
            }
        } else {
            po.addLogFailed("Node addition failed");
        }

    }

    private void addRouter(ProductOperation po, final Config config, Agent agent) {
        List<Command> commands = Commands.getAddRouterCommands(config, agent);

        boolean additionOK = true;

        for (Command command : commands) {
            po.addLog(String.format("Running command: %s", command.getDescription()));
            final AtomicBoolean commandOK = new AtomicBoolean();

            if (command.getData() == CommandType.START_ROUTERS) {
                commandRunner.runCommand(command, new CommandCallback() {

                    @Override
                    public void onResponse(Response response, AgentResult agentResult, Command command) {
                        if (agentResult.getStdOut().toString().contains(
                                "child process started successfully, parent exiting")) {
                            commandOK.set(true);
                            stop();
                        }
                    }

                });
            } else {
                commandRunner.runCommand(command);
            }

            if (command.getCommandStatus() == CommandStatus.SUCCEEDED || commandOK.get()) {
                po.addLog(String.format("Command %s succeeded", command.getDescription()));
            } else {
                po.addLog(String.format("Command %s failed: %s", command.getDescription(), command.getAllErrors()));
                additionOK = false;
                break;
            }

        }

        if (additionOK) {
            po.addLogDone("Node addition succeeded");
        } else {
            po.addLogFailed("Node addition failed");
        }
    }

}
