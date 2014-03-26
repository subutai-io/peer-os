package org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.operation;

import org.safehaus.kiskis.mgmt.api.taskrunner.Task;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskCallback;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskStatus;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.HadoopClusterInfo;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.HadoopDAO;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.HadoopModule;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.common.Tasks;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.config.datanode.DataNodesWindow;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;

import java.util.*;

/**
 * Created by daralbaev on 23.03.14.
 */
public class DataNodeConfiguration {
    public static void removeNode(final DataNodesWindow form, final String clusterName, final Agent agent, final boolean restart) {
        form.setLoading(true);
        final HadoopClusterInfo cluster = HadoopDAO.getHadoopClusterInfo(clusterName);

        Task task = Tasks.getRemoveNodeCommand(cluster, agent);

        HadoopModule.getTaskRunner().executeTask(task, new TaskCallback() {
            @Override
            public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                if (task.isCompleted()) {
                    cluster.getDataNodes().remove(agent);

                    HadoopDAO.deleteHadoopClusterInfo(cluster.getClusterName());
                    HadoopDAO.saveHadoopClusterInfo(cluster);

                    if (restart) {
                        form.restartCluster();
                    } else {
                        form.getStatus();
                    }
                    //uninstallDeb(agent);
                }

                return null;
            }
        });
    }

    public static void uninstallDeb(final Agent agent) {

        Task task = Tasks.getUninstallTask(agent);

        HadoopModule.getTaskRunner().executeTask(task, new TaskCallback() {
            @Override
            public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                return null;
            }
        });
    }

    public static void addNode(final DataNodesWindow form, final String clusterName, final Agent agent) {
        form.setLoading(true);

        final HadoopClusterInfo cluster = HadoopDAO.getHadoopClusterInfo(clusterName);
        cluster.getDataNodes().add(agent);

        Task task = Tasks.getInstallTask(Arrays.asList(agent));

        HadoopModule.getTaskRunner().executeTask(task, new TaskCallback() {
            @Override
            public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                if (task.isCompleted()) {
                    if (task.getTaskStatus() == TaskStatus.SUCCESS) {
                        setMasters(form, cluster, agent);
                    } else {
                        System.err.println(stdErr);
                    }
                }

                return null;
            }
        });
    }

    public static void setMasters(final DataNodesWindow form, final HadoopClusterInfo cluster, final Agent agent) {

        Task task = Tasks.getSetMastersTask(Arrays.asList(agent), cluster.getNameNode(), cluster.getJobTracker(),
                cluster.getReplicationFactor());

        HadoopModule.getTaskRunner().executeTask(task, new TaskCallback() {
            @Override
            public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                if (task.isCompleted()) {
                    if (task.getTaskStatus() == TaskStatus.SUCCESS) {
                        registerNode(form, cluster, agent);
                    } else {
                        System.err.println(stdErr);
                    }
                }

                return null;
            }
        });
    }

    public static void registerNode(final DataNodesWindow form, final HadoopClusterInfo cluster, final Agent agent) {

        Task task = Tasks.getSetDataNodesTask(cluster.getNameNode(), agent);

        HadoopModule.getTaskRunner().executeTask(task, new TaskCallback() {
            @Override
            public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                if (task.isCompleted()) {
                    if (task.getTaskStatus() == TaskStatus.SUCCESS) {
                        setSSH(form, cluster);
                    } else {
                        System.err.println(stdErr);
                    }
                }

                return null;
            }
        });
    }

    private static void setSSH(final DataNodesWindow form, final HadoopClusterInfo cluster) {
        Task task = Tasks.getSetSSHTask(cluster.getAllAgents());

        HadoopModule.getTaskRunner().executeTask(task, new TaskCallback() {
            @Override
            public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                if (task.isCompleted()) {
                    if (task.getTaskStatus() == TaskStatus.SUCCESS) {
                        copySSH(form, cluster);
                    } else {
                        System.err.println(stdErr);
                    }
                }

                return null;
            }
        });
    }

    private static void copySSH(final DataNodesWindow form, final HadoopClusterInfo cluster) {
        Task task = Tasks.getCopySSHKeyTask(cluster.getAllAgents());

        HadoopModule.getTaskRunner().executeTask(task, new TaskCallback() {
            HashMap<UUID, String> keys = new HashMap<UUID, String>();

            @Override
            public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                StringBuilder value = new StringBuilder();
                if (keys.containsKey(response.getUuid())) {
                    value.append(keys.get(response.getUuid()));
                }
                if (!Util.isStringEmpty(response.getStdOut())) {
                    value.append(response.getStdOut());
                }
                keys.put(response.getUuid(), value.toString());

                if (task.isCompleted()) {
                    if (task.getTaskStatus() == TaskStatus.SUCCESS) {
                        pasteSSH(form, keys, cluster);
                    } else {
                        System.err.println(stdErr);
                    }
                }

                return null;
            }
        });
    }

    private static void pasteSSH(final DataNodesWindow form, HashMap<UUID, String> keys, final HadoopClusterInfo cluster) {
        List<String> arg = new ArrayList<String>();
        for (UUID id : keys.keySet()) {
            arg.add(keys.get(id));
        }

        Task task = Tasks.getPasteSSHKeyTask(cluster.getAllAgents(), arg);
        HadoopModule.getTaskRunner().executeTask(task, new TaskCallback() {
            @Override
            public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                if (task.isCompleted()) {
                    if (task.getTaskStatus() == TaskStatus.SUCCESS) {
                        configSSH(form, cluster);
                    } else {
                        System.err.println(stdErr);
                    }
                }
                return null;
            }
        });
    }

    private static void configSSH(final DataNodesWindow form, final HadoopClusterInfo cluster) {
        Task task = Tasks.getConfigSSHFolderTask(cluster.getAllAgents());

        HadoopModule.getTaskRunner().executeTask(task, new TaskCallback() {
            @Override
            public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                if (task.isCompleted()) {
                    if (task.getTaskStatus() == TaskStatus.SUCCESS) {
                        copyHosts(form, cluster);
                    } else {
                        System.err.println(stdErr);
                    }
                }

                return null;
            }
        });
    }

    private static void copyHosts(final DataNodesWindow form, final HadoopClusterInfo cluster) {
        Task task = Tasks.getCopyHostsTask(cluster.getAllAgents());

        HadoopModule.getTaskRunner().executeTask(task, new TaskCallback() {
            HashMap<UUID, String> hosts = new HashMap<UUID, String>();

            @Override
            public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                StringBuilder value = new StringBuilder();
                if (hosts.containsKey(response.getUuid())) {
                    value.append(hosts.get(response.getUuid()));
                }
                if (!Util.isStringEmpty(response.getStdOut())) {
                    value.append(response.getStdOut());
                }
                hosts.put(response.getUuid(), value.toString());

                if (task.isCompleted()) {
                    if (task.getTaskStatus() == TaskStatus.SUCCESS) {
                        pasteHost(form, hosts, cluster);
                    } else {
                        System.err.println(stdErr);
                    }
                }

                return null;
            }
        });
    }

    private static void pasteHost(final DataNodesWindow form, HashMap<UUID, String> hosts, final HadoopClusterInfo cluster) {
        for (UUID id : hosts.keySet()) {
            Agent agent = HadoopModule.getAgentManager().getAgentByUUID(id);
            String host = editHosts(hosts.get(id), agent, cluster);
            hosts.put(id, host);
        }

        Task task = Tasks.getPasteHostsTask(hosts);
        HadoopModule.getTaskRunner().executeTask(task, new TaskCallback() {
            @Override
            public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                if (task.isCompleted()) {
                    if (task.getTaskStatus() == TaskStatus.SUCCESS) {
                        HadoopDAO.deleteHadoopClusterInfo(cluster.getClusterName());
                        HadoopDAO.saveHadoopClusterInfo(cluster);

                        form.getStatus();
                    } else {
                        System.err.println(stdErr);
                    }
                }

                return null;
            }
        });
    }

    private static String editHosts(String input, Agent localAgent, final HadoopClusterInfo cluster) {
        StringBuilder result = new StringBuilder();

        String[] hosts = input.split("\n");
        for (String host : hosts) {
            host = host.trim();
            boolean isContains = false;
            for (Agent agent : cluster.getAllAgents()) {
                if (host.contains(agent.getHostname())
                        || host.contains("localhost")
                        || host.contains(localAgent.getHostname())
                        || host.contains(localAgent.getListIP().get(0))) {
                    isContains = true;
                }
            }

            if (!isContains) {
                result.append(host);
                result.append("\n");
            }
        }

        for (Agent agent : cluster.getAllAgents()) {
            result.append(agent.getListIP().get(0));
            result.append("\t");
            result.append(agent.getHostname());
            result.append(".");
            result.append(cluster.getIpMask());
            result.append("\t");
            result.append(agent.getHostname());
            result.append("\n");
        }

        result.append("127.0.0.1\tlocalhost");

        return result.toString();
    }

}
