package org.safehaus.kiskis.mgmt.impl.flume;

import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.api.dbmanager.DbManager;
import org.safehaus.kiskis.mgmt.api.flume.Config;
import org.safehaus.kiskis.mgmt.api.flume.Flume;
import org.safehaus.kiskis.mgmt.api.lxcmanager.LxcManager;
import org.safehaus.kiskis.mgmt.api.taskrunner.*;
import org.safehaus.kiskis.mgmt.api.tracker.ProductOperation;
import org.safehaus.kiskis.mgmt.api.tracker.Tracker;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.NodeState;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.ResponseType;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class FlumeImpl implements Flume {

    private TaskRunner taskRunner;
    private AgentManager agentManager;
    private Tracker tracker;
    private DbManager dbManager;
    private LxcManager lxcManager; // TODO:

    private ExecutorService executor;

    public void init() {
        executor = Executors.newCachedThreadPool();
    }

    public void destroy() {
        executor.shutdown();
    }

    public void setTaskRunner(TaskRunner taskRunner) {
        this.taskRunner = taskRunner;
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

    public void setLxcManager(LxcManager lxcManager) {
        this.lxcManager = lxcManager;
    }

    public UUID installCluster(final Config config) {
        final ProductOperation po = tracker.createProductOperation(
                Config.PRODUCT_KEY,
                String.format("Installing cluster %s", config.getClusterName()));

        executor.execute(new Runnable() {

            public void run() {
                if (getClusterConfig(config.getClusterName()) != null) {
                    po.addLogFailed(String.format(
                            "Cluster with name '%s' already exists\nInstallation aborted",
                            config.getClusterName()));
                    return;
                }

                //check if node agent is connected
                for (Iterator<Agent> it = config.getNodes().iterator(); it.hasNext(); ) {
                    Agent node = it.next();
                    if (agentManager.getAgentByHostname(node.getHostname()) != null)
                        continue;
                    po.addLog(String.format(
                            "Node %s is not connected. Omitting this node from installation",
                            node.getHostname()));
                    it.remove();
                }
                if (config.getNodes().isEmpty()) {
                    po.addLogFailed("No nodes eligible for installation. Operation aborted");
                    return;
                }

                po.addLog("Checking prerequisites...");
                //check installed ksks packages
                Task statusTask = taskRunner.executeTaskNWait(Tasks.getStatusTask(config.getNodes()));
                if (!statusTask.isCompleted()) {
                    po.addLogFailed("Failed to check presence of installed ksks packages\nInstallation aborted");
                    return;
                }

                for (Iterator<Agent> it = config.getNodes().iterator(); it.hasNext(); ) {
                    Agent node = it.next();
                    Result result = statusTask.getResults().get(node.getUuid());

                    if (result.getStdOut().contains("ksks-flume")) {
                        po.addLog(String.format(
                                "Node %s already has Flume installed. Omitting this node from installation",
                                node.getHostname()));
                        it.remove();
                    } else if (!result.getStdOut().contains("ksks-hadoop")) {
                        po.addLog(String.format(
                                "Node %s has no Hadoop installation. Omitting this node from installation",
                                node.getHostname()));
                        it.remove();
                    }
                }

                if (config.getNodes().isEmpty()) {
                    po.addLogFailed("No nodes eligible for installation. Operation aborted");
                    return;
                }

                po.addLog("Updating db...");
                if (dbManager.saveInfo(Config.PRODUCT_KEY, config.getClusterName(), config)) {
                    po.addLog("Cluster info saved to DB\nInstalling Flume...");

                    Task installTask = taskRunner.executeTaskNWait(Tasks.getInstallTask(config.getNodes()));

                    if (installTask.getTaskStatus() == TaskStatus.SUCCESS) {
                        po.addLogDone("Installation succeeded\nDone");
                    } else {
                        po.addLogFailed(String.format("Installation failed, %s",
                                installTask.getFirstError()));
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
                Config config = getClusterConfig(clusterName);
                if (config == null) {
                    po.addLogFailed(String.format(
                            "Cluster with name %s does not exist\nOperation aborted",
                            clusterName));
                    return;
                }

                po.addLog("Uninstalling Flume...");

                Task uninstallTask = taskRunner.executeTaskNWait(Tasks.getUninstallTask(config.getNodes()));

                if (uninstallTask.isCompleted()) {
                    for (Agent agent : config.getNodes()) {
                        Result result = uninstallTask.getResults().get(agent.getUuid());
                        if (result.getExitCode() != null && result.getExitCode() == 0) {
                            if (result.getStdOut().contains("ksks-flume is not installed")) {
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
                    if (dbManager.deleteInfo(Config.PRODUCT_KEY, config.getClusterName())) {
                        po.addLogDone("Cluster info deleted from DB\nDone");
                    } else {
                        po.addLogFailed("Error while deleting cluster info from DB. Check logs.\nFailed");
                    }
                } else {
                    po.addLogFailed(String.format("Uninstallation failed, %s",
                            uninstallTask.getFirstError()));
                }

            }
        });

        return po.getId();
    }

    public UUID startNode(final String clusterName, final String lxcHostname) {
        final ProductOperation po = tracker.createProductOperation(
                Config.PRODUCT_KEY,
                String.format("Starting node %s in %s", lxcHostname, clusterName));

        executor.execute(new Runnable() {

            public void run() {
                if (getClusterConfig(clusterName) == null) {
                    po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", clusterName));
                    return;
                }

                final Agent node = agentManager.getAgentByHostname(lxcHostname);
                if (node == null) {
                    po.addLogFailed(String.format("Agent with hostname %s is not connected\nOperation aborted", lxcHostname));
                    return;
                }

                po.addLog("Starting node...");
                final Task startNodeTask = Tasks.getStartTask(node);
                final CountDownLatch latch = new CountDownLatch(1);
                taskRunner.executeTask(startNodeTask, new TaskCallback() {

                    public Task onResponse(Task task, Response resp, String stdOut, String stdErr) {
                        if (resp.getStdOut() != null) po.addLog(resp.getStdOut());
                        if (resp.getStdErr() != null) po.addLog(resp.getStdErr());
                        if (resp.getType() == ResponseType.EXECUTE_TIMEOUTED) {
                            po.addLogFailed("Command execution timeout");
                            latch.countDown();
                        } else if (resp.getType() == ResponseType.EXECUTE_RESPONSE_DONE) {
                            latch.countDown();
                        }
                        return null;
                    }
                });
                try {
                    boolean b = latch.await(startNodeTask.getTotalTimeout(), TimeUnit.SECONDS);
                    if (!b) po.addLogFailed("Operation timeout");
                } catch (InterruptedException ex) {
                }

                if (startNodeTask.isCompleted()) {
                    po.addLogDone(String.format("Node on %s started", lxcHostname));
                } else {
                    po.addLogFailed(String.format("Failed to start node %s. %s",
                            lxcHostname,
                            startNodeTask.getResults().entrySet().iterator().next().getValue().getStdErr()
                    ));
                }

            }
        });

        return po.getId();
    }

    public UUID stopNode(final String clusterName, final String lxcHostname) {
        final ProductOperation po = tracker.createProductOperation(
                Config.PRODUCT_KEY,
                String.format("Stopping node %s in %s", lxcHostname, clusterName));

        executor.execute(new Runnable() {

            public void run() {
                if (getClusterConfig(clusterName) == null) {
                    po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", clusterName));
                    return;
                }

                final Agent node = agentManager.getAgentByHostname(lxcHostname);
                if (node == null) {
                    po.addLogFailed(String.format("Agent with hostname %s is not connected\nOperation aborted", lxcHostname));
                    return;
                }

                po.addLog("Stopping node...");
                final Task stopNodeTask = Tasks.getStopTask(node);
                final CountDownLatch latch = new CountDownLatch(1);
                taskRunner.executeTask(stopNodeTask, new TaskCallback() {

                    public Task onResponse(Task task, Response resp, String stdOut, String stdErr) {
                        if (resp.getStdOut() != null) po.addLog(resp.getStdOut());
                        if (resp.getStdErr() != null) po.addLog(resp.getStdErr());
                        if (resp.getType() == ResponseType.EXECUTE_TIMEOUTED) {
                            po.addLogFailed("Command execution timeout");
                            latch.countDown();
                        } else if (resp.getType() == ResponseType.EXECUTE_RESPONSE_DONE) {
                            latch.countDown();
                        }
                        return null;
                    }
                });
                try {
                    boolean b = latch.await(stopNodeTask.getTotalTimeout(), TimeUnit.SECONDS);
                    if (!b) po.addLogFailed("Operation timeout");
                } catch (InterruptedException ex) {
                }

                if (stopNodeTask.isCompleted()) {
                    po.addLogDone(String.format("Node on %s stopped", lxcHostname));
                } else {
                    po.addLogFailed(String.format("Failed to stop node %s. %s",
                            lxcHostname,
                            stopNodeTask.getResults().entrySet().iterator().next().getValue().getStdErr()
                    ));
                }

            }
        });

        return po.getId();
    }

    public UUID checkNode(final String clusterName, final String lxcHostname) {
        final ProductOperation po = tracker.createProductOperation(
                Config.PRODUCT_KEY,
                String.format("Checking node %s in %s", lxcHostname, clusterName));

        executor.execute(new Runnable() {

            public void run() {
                if (getClusterConfig(clusterName) == null) {
                    po.addLogFailed(String.format("Cluster with name %s does not exist\nOperation aborted", clusterName));
                    return;
                }

                final Agent node = agentManager.getAgentByHostname(lxcHostname);
                if (node == null) {
                    po.addLogFailed(String.format("Agent with hostname %s is not connected\nOperation aborted", lxcHostname));
                    return;
                }

                po.addLog("Checking node...");
                final Task checkNodeTask = taskRunner.executeTaskNWait(Tasks.getStatusTask(node));

                NodeState nodeState = NodeState.UNKNOWN;
                if (checkNodeTask.isCompleted()) {
                    Result result = checkNodeTask.getResults().entrySet().iterator().next().getValue();
                    if (result.getStdOut().contains("is running")) {
                        nodeState = NodeState.RUNNING;
                    } else if (result.getStdOut().contains("is not running")) {
                        nodeState = NodeState.STOPPED;
                    }
                }

                if (NodeState.UNKNOWN.equals(nodeState)) {
                    po.addLogFailed(String.format("Failed to check status of %s, %s",
                            lxcHostname,
                            checkNodeTask.getResults().entrySet().iterator().next().getValue().getStdErr()
                    ));
                } else {
                    po.addLogDone(String.format("Node %s is %s",
                            lxcHostname,
                            nodeState
                    ));
                }

            }
        });

        return po.getId();
    }

    public UUID addNode(final String clusterName, final String lxcHostname) {
        final ProductOperation po = tracker.createProductOperation(
                Config.PRODUCT_KEY,
                String.format("Adding node to %s", clusterName));

        executor.execute(new Runnable() {

            public void run() {
                Config config = getClusterConfig(clusterName);
                if (config == null) {
                    po.addLogFailed(String.format(
                            "Cluster with name %s does not exist\nOperation aborted",
                            clusterName));
                    return;
                }

                //check if node agent is connected
                Agent agent = agentManager.getAgentByHostname(lxcHostname);
                if (agent == null) {
                    po.addLogFailed(String.format(
                            "Node %s is not connected\nOperation aborted",
                            lxcHostname));
                    return;
                }

                po.addLog("Checking prerequisites...");
                Task statusTask = taskRunner.executeTaskNWait(Tasks.getStatusTask(agent));
                if (!statusTask.isCompleted()) {
                    po.addLogFailed("Failed to check presence of installed ksks packages\nInstallation aborted");
                    return;
                }

                Result result = statusTask.getResults().get(agent.getUuid());

                if (result.getStdOut().contains("ksks-flume")) {
                    po.addLogFailed(String.format("Node %s already has Flume installed\nInstallation aborted", lxcHostname));
                    return;
                } else if (!result.getStdOut().contains("ksks-hadoop")) {
                    po.addLogFailed(String.format("Node %s has no Hadoop installation\nInstallation aborted", lxcHostname));
                    return;
                }

                config.getNodes().add(agent);

                po.addLog("Updating db...");
                if (dbManager.saveInfo(Config.PRODUCT_KEY, config.getClusterName(), config)) {
                    po.addLog("Cluster info updated in DB\nInstalling Flume...");

                    Task installTask = taskRunner.executeTaskNWait(Tasks.getInstallTask(Util.wrapAgentToSet(agent)));

                    if (installTask.getTaskStatus() == TaskStatus.SUCCESS) {
                        po.addLogDone("Installation succeeded\nDone");
                    } else {
                        po.addLogFailed(String.format("Installation failed: %s",
                                installTask.getFirstError()));
                    }
                } else {
                    po.addLogFailed("Could not update cluster info in DB! Please see logs\nInstallation aborted");
                }

            }
        });

        return po.getId();
    }

    public UUID destroyNode(final String clusterName, final String lxcHostname) {
        final ProductOperation po = tracker.createProductOperation(
                Config.PRODUCT_KEY,
                String.format("Destroying %s in %s", lxcHostname, clusterName));

        executor.execute(new Runnable() {

            public void run() {
                Config config = getClusterConfig(clusterName);
                if (config == null) {
                    po.addLogFailed(String.format(
                            "Cluster with name %s does not exist\nOperation aborted",
                            clusterName));
                    return;
                }

                Agent agent = agentManager.getAgentByHostname(lxcHostname);
                if (agent == null) {
                    po.addLogFailed(String.format(
                            "Agent with hostname %s is not connected\nOperation aborted",
                            lxcHostname));
                    return;
                }

                if (config.getNodes().size() == 1) {
                    po.addLogFailed("This is the last node in the cluster. Please, destroy cluster instead\nOperation aborted");
                    return;
                }

                po.addLog("Uninstalling Flume...");
                Task uninstallTask = taskRunner.executeTaskNWait(Tasks.getUninstallTask(Util.wrapAgentToSet(agent)));

                if (uninstallTask.isCompleted()) {
                    Result result = uninstallTask.getResults().get(agent.getUuid());
                    if (result.getExitCode() != null && result.getExitCode() == 0) {
                        if (result.getStdOut().contains("ksks-flume is not installed")) {
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
                    if (dbManager.saveInfo(Config.PRODUCT_KEY, config.getClusterName(), config)) {
                        po.addLogDone("Cluster info updated in DB\nDone");
                    } else {
                        po.addLogFailed("Error while updating cluster info in DB. Check logs.\nFailed");
                    }
                } else {
                    po.addLogFailed(String.format("Uninstallation failed: %s",
                            uninstallTask.getFirstError()));
                }
            }
        });

        return po.getId();
    }

    public List<Config> getClusters() {
        return dbManager.getInfo(Config.PRODUCT_KEY, Config.class);
    }

    private Config getClusterConfig(String clusterName) {
        return dbManager.getInfo(Config.PRODUCT_KEY, clusterName, Config.class);
    }
}
