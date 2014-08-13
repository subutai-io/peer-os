package org.safehaus.subutai.impl.oozie;


import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.CommandRunner;
import org.safehaus.subutai.api.dbmanager.DbManager;
import org.safehaus.subutai.api.oozie.Oozie;
import org.safehaus.subutai.api.oozie.OozieConfig;
import org.safehaus.subutai.api.tracker.Tracker;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.Util;
import org.safehaus.subutai.shared.protocol.settings.Common;

public class OozieImpl implements Oozie {

    public AgentManager agentManager;
    private DbManager dbManager;
    private Tracker tracker;
    private ExecutorService executor;
    private CommandRunner commandRunner;


    public void init() {
        Commands.init(commandRunner);
        executor = Executors.newCachedThreadPool();
    }

    public void destroy() {
        executor.shutdown();
    }

    public void setDbManager(DbManager dbManager) {
        this.dbManager = dbManager;
    }

    public void setTracker(Tracker tracker) {
        this.tracker = tracker;
    }

    public void setAgentManager(AgentManager agentManager) {
        this.agentManager = agentManager;
    }

    public void setCommandRunner(CommandRunner commandRunner) {
        this.commandRunner = commandRunner;
    }

    public UUID installCluster(final OozieConfig config) {
        final ProductOperation po = tracker.createProductOperation(OozieConfig.PRODUCT_KEY, "Installing Oozie");

        executor.execute(new Runnable() {

            public void run() {
                if (dbManager.getInfo(config.PRODUCT_KEY, config.getClusterName(), OozieConfig.class) != null) {
                    po.addLogFailed(String.format("Cluster with name '%s' already exists\nInstallation aborted", config.getClusterName()));
                    return;
                }

                if (dbManager.saveInfo(config.PRODUCT_KEY, config.getClusterName(), config)) {
                    po.addLog("Cluster info saved to DB");

//                    Set<Agent> allNodes = new HashSet<Agent>();
//                    allNodes.add(config.getServer());
//                    allNodes.addAll(config.getClients());

                    // Installing Oozie server
                    po.addLog("Installing Oozie server...");
                    Set<Agent> servers = new HashSet<Agent>();
                    Agent serverAgent = agentManager.getAgentByHostname( config.getServer() );
                    servers.add(serverAgent);
                    Command installServerCommand = Commands.getInstallServerCommand(servers);
                    commandRunner.runCommand(installServerCommand);

                    if (installServerCommand.hasSucceeded()) {
                        po.addLog("Install server successful.");
                    } else {
                        po.addLogFailed(String.format("Installation failed, %s", installServerCommand.getAllErrors()));
                        return;
                    }

                    // Installing Oozie client
                    po.addLog("Installing Oozie clients...");
                    Set<Agent> clientAgents = new HashSet<Agent>();
                    for(String clientAgent : config.getClients()) {
                        Agent client = agentManager.getAgentByHostname( clientAgent );
                        clientAgents.add( client );
                    }
                    Command installClientsCommand = Commands.getInstallClientCommand( clientAgents );
                    commandRunner.runCommand(installClientsCommand);

                    if (installClientsCommand.hasSucceeded()) {
                        po.addLog("Install clients successful.");
                    } else {
                        po.addLogFailed(String.format("Installation failed, %s", installClientsCommand.getAllErrors()));
                        return;
                    }

                    po.addLog("Configuring root hosts...");
                    Agent server = agentManager.getAgentByHostname( config.getServer() );
                    Set<Agent> hadoopNodes = new HashSet<Agent>();
                    for(String hadoopNode : config.getHadoopNodes()) {
                        Agent hadoopNodeAgent = agentManager.getAgentByHostname( hadoopNode );
                        hadoopNodes.add( hadoopNodeAgent );
                    }
                    Command configureRootHostsCommand = Commands.getConfigureRootHostsCommand(hadoopNodes,
                            Util.getAgentIpByMask(server, Common.IP_MASK));
                    commandRunner.runCommand(configureRootHostsCommand);

                    if (configureRootHostsCommand.hasSucceeded()) {
                        po.addLog("Configuring root hosts successful.");
                    } else {
                        po.addLogFailed(String.format("Configuration failed, %s", configureRootHostsCommand.getAllErrors()));
                        return;
                    }

                    po.addLog("Configuring root groups...");
                    Command configureRootGroupsCommand = Commands.getConfigureRootGroupsCommand(hadoopNodes);
                    commandRunner.runCommand(configureRootGroupsCommand);

                    if (configureRootGroupsCommand.hasSucceeded()) {
                        po.addLog("Configuring root groups successful.");
                    } else {
                        po.addLogFailed(String.format("Configuring failed, %s", configureRootGroupsCommand.getAllErrors()));
                        return;
                    }
                    po.addLogDone("Oozie installation succeeded");


                } else {
                    po.addLogFailed("Could not save cluster info to DB! Please see logs\nInstallation aborted");
                }

            }
        });

        return po.getId();
    }

    public UUID uninstallCluster(final String clusterName) {
        final ProductOperation po
                = tracker.createProductOperation(OozieConfig.PRODUCT_KEY,
                String.format("Destroying cluster %s", clusterName));
        executor.execute(new Runnable() {

            public void run() {
                OozieConfig config = dbManager.getInfo(OozieConfig.PRODUCT_KEY, clusterName, OozieConfig.class);
                if (config == null) {
                    po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", clusterName));
                    return;
                }

                Set<Agent> servers = new HashSet<Agent>();
                Agent serverAgent = agentManager.getAgentByHostname( config.getServer() );
                servers.add(serverAgent);

                Command uninstallServerCommand = Commands.getUninstallServerCommand(servers);
                commandRunner.runCommand(uninstallServerCommand);

                if (uninstallServerCommand.hasSucceeded()) {
                    po.addLog("Uninstall server succeeded");
                } else {
                    po.addLogFailed(String.format("Uninstall server failed, %s", uninstallServerCommand.getAllErrors()));
                    return;
                }

                Set<Agent> clientAgents = new HashSet<Agent>();
                for(String clientHostname : config.getClients()) {
                    Agent clientAgent = agentManager.getAgentByHostname( clientHostname );
                    clientAgents.add( clientAgent );
                }
                Command uninstallClientsCommand = Commands.getUninstallClientsCommand(clientAgents);
                commandRunner.runCommand(uninstallClientsCommand);

                if (uninstallClientsCommand.hasSucceeded()) {
                    po.addLog("Uninstall clients succeeded");
                } else {
                    po.addLogFailed(String.format("Uninstall clients failed, %s", uninstallClientsCommand.getAllErrors()));
                    return;
                }

                po.addLog("Updating db...");
                if (dbManager.deleteInfo(OozieConfig.PRODUCT_KEY, config.getClusterName())) {
                    po.addLog("Cluster info deleted from DB");
                } else {
                    po.addLogFailed("Error while deleting cluster info from DB. Check logs.\nFailed");
                }

                po.addLogDone("Oozie cluster deleted");

            }
        });

        return po.getId();
    }


    public List<OozieConfig> getClusters() {

        return dbManager.getInfo(OozieConfig.PRODUCT_KEY, OozieConfig.class);

    }

    @Override
    public OozieConfig getCluster(String clusterName) {
        return dbManager.getInfo(OozieConfig.PRODUCT_KEY, clusterName, OozieConfig.class);
    }

    @Override
    public UUID startServer(final OozieConfig config) {
        final ProductOperation po
                = tracker.createProductOperation(config.PRODUCT_KEY,
                String.format("Starting cluster %s", config.getClusterName()));
        final String clusterName = config.getClusterName();
        executor.execute(new Runnable() {

            public void run() {
                OozieConfig config = dbManager.getInfo(OozieConfig.PRODUCT_KEY, clusterName, OozieConfig.class);
                if (config == null) {
                    po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", config.getClusterName()));
                    return;
                }
                Set<Agent> servers = new HashSet<Agent>();
                Agent serverAgent = agentManager.getAgentByHostname( config.getServer() );
                servers.add(serverAgent);
                Command startServiceCommand = Commands.getStartServerCommand(servers);
                commandRunner.runCommand(startServiceCommand);

                if (startServiceCommand.hasSucceeded()) {
                    po.addLogDone("Start succeeded");
                } else {
                    po.addLogFailed(String.format("Start failed, %s", startServiceCommand.getAllErrors()));
                }

            }
        });

        return po.getId();
    }

    @Override
    public UUID stopServer(final OozieConfig config) {
        final ProductOperation po
                = tracker.createProductOperation(config.PRODUCT_KEY,
                String.format("Stopping cluster %s", config.getClusterName()));
        final String clusterName = config.getClusterName();
        executor.execute(new Runnable() {

            public void run() {
                OozieConfig config = dbManager.getInfo(OozieConfig.PRODUCT_KEY, clusterName, OozieConfig.class);
                if (config == null) {
                    po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", config.getClusterName()));
                    return;
                }
                Set<Agent> servers = new HashSet<Agent>();
                Agent serverAgent = agentManager.getAgentByHostname( config.getServer() );
                servers.add(serverAgent);
                Command stopServiceCommand = Commands.getStopServerCommand(servers);
                commandRunner.runCommand(stopServiceCommand);

                if (stopServiceCommand.hasSucceeded()) {
                    po.addLogDone("Stop succeeded");
                } else {
                    po.addLogFailed(String.format("Stop failed, %s", stopServiceCommand.getAllErrors()));
                }

            }
        });

        return po.getId();
    }

    @Override
    public UUID checkServerStatus(final OozieConfig config) {
        final ProductOperation po
                = tracker.createProductOperation(config.PRODUCT_KEY,
                String.format("Checking status of cluster %s", config.getClusterName()));
        final String clusterName = config.getClusterName();
        executor.execute(new Runnable() {

            public void run() {
                OozieConfig config = dbManager.getInfo(OozieConfig.PRODUCT_KEY, clusterName, OozieConfig.class);
                if (config == null) {
                    po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", config.getClusterName()));
                    return;
                }
                Set<Agent> servers = new HashSet<Agent>();
                Agent serverAgent = agentManager.getAgentByHostname( config.getServer() );
                servers.add(serverAgent);
                Command statusServiceCommand = Commands.getStatusServerCommand(servers);
                commandRunner.runCommand(statusServiceCommand);

                if (statusServiceCommand.hasSucceeded()) {
                    po.addLogDone("Server is running.");
                } else {
                    po.addLogFailed(String.format("Status failed, %s", statusServiceCommand.getAllErrors()));
                }

            }
        });

        return po.getId();
    }


}
