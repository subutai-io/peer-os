package org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.install;

import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.HadoopDAO;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.common.HadoopConfig;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.common.Tasks;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.operation.InstallHadoopOperation;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.wizard.Step3;
import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.shared.protocol.api.AgentManager;
import org.safehaus.kiskis.mgmt.shared.protocol.api.TaskCallback;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.TaskStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA. User: daralbaev Date: 12/7/13 Time: 5:55 PM
 */
public class Installation {

    private Step3 panel;
    private TaskRunner taskRunner;
    private HadoopConfig config;
    private final AgentManager agentManager;

    public Installation(TaskRunner taskRunner) {
        this.taskRunner = taskRunner;

        config = new HadoopConfig();
        agentManager = ServiceLocator.getService(AgentManager.class);
    }

    public void setPanel(Step3 step) {
        this.panel = step;
    }

    public void startInstallation() {
        final Operation installOperation = new InstallHadoopOperation(config);

        panel.addOutput(installOperation.peekNextTask(), " started.");
        taskRunner.runTask(installOperation.getNextTask(), new TaskCallback() {
            @Override
            public void onResponse(Task task, Response response) {
                if (task.isCompleted()) {
                    if (task.getTaskStatus() == TaskStatus.SUCCESS) {
                        panel.addOutput(task, " succeeded.");
                        if (installOperation.hasNextTask()) {
                            taskRunner.runTask(installOperation.getNextTask(), this);
                        } else {
                            installOperation.setCompleted(true);
                            copySSH();
                        }
                    } else {
                        installOperation.setCompleted(true);
                        panel.addOutput(task, " failed.");
                    }
                }
            }
        });
    }

    private void copySSH() {
        Task task = Tasks.getCopySSHKeyTask(config.getAllNodes());

        taskRunner.runTask(task, new TaskCallback() {
            HashMap<UUID, String> keys = new HashMap<UUID, String>();

            @Override
            public void onResponse(Task task, Response response) {
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
                        panel.addOutput(task, " succeeded.");
                        pasteSSH(keys);
                    } else {
                        panel.addOutput(task, " failed.");
                    }
                }
            }
        });
    }

    private void pasteSSH(HashMap<UUID, String> keys) {
        List<String> arg = new ArrayList<String>();
        for (UUID id : keys.keySet()) {
            arg.add(keys.get(id));
        }

        Task task = Tasks.getPasteSSHKeyTask(config.getAllNodes(), arg);
        taskRunner.runTask(task, new TaskCallback() {
            @Override
            public void onResponse(Task task, Response response) {
                if (task.isCompleted()) {
                    if (task.getTaskStatus() == TaskStatus.SUCCESS) {
                        panel.addOutput(task, " succeeded.");
                        configSSH();
                    } else {
                        panel.addOutput(task, " failed.");
                    }
                }
            }
        });
    }

    private void configSSH() {
        Task task = Tasks.getConfigSSHFolderTask(config.getAllNodes());

        taskRunner.runTask(task, new TaskCallback() {
            @Override
            public void onResponse(Task task, Response response) {
                if (task.isCompleted()) {
                    if (task.getTaskStatus() == TaskStatus.SUCCESS) {
                        panel.addOutput(task, " succeeded.");
                        copyHosts();
                    } else {
                        panel.addOutput(task, " failed.");
                    }
                }
            }
        });
    }

    private void copyHosts() {
        Task task = Tasks.getCopyHostsTask(config.getAllNodes());

        taskRunner.runTask(task, new TaskCallback() {

            HashMap<UUID, String> hosts = new HashMap<UUID, String>();

            @Override
            public void onResponse(Task task, Response response) {
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
                        panel.addOutput(task, " succeeded.");
                        pasteHost(hosts);
                    } else {
                        panel.addOutput(task, " failed.");
                    }
                }
            }
        });
    }

    private void pasteHost(HashMap<UUID, String> hosts) {
        for (UUID id : hosts.keySet()) {
            Agent agent = agentManager.getAgentByUUID(id);
            String host = editHosts(hosts.get(id), agent);
            hosts.put(id, host);
        }

        Task task = Tasks.getPasteHostsTask(hosts);
        taskRunner.runTask(task, new TaskCallback() {
            @Override
            public void onResponse(Task task, Response response) {
                if (task.isCompleted()) {
                    if (task.getTaskStatus() == TaskStatus.SUCCESS) {
                        panel.addOutput(task, " succeeded.");
                        HadoopDAO.saveHadoopClusterInfo(config.getCluster());
                        panel.setCloseable();
                    } else {
                        panel.addOutput(task, " failed.");
                    }
                }
            }
        });
    }

    private String editHosts(String input, Agent localAgent) {
        StringBuilder result = new StringBuilder();

        String[] hosts = input.split("\n");
        for (String host : hosts) {
            host = host.trim();
            boolean isContains = false;
            for (Agent agent : config.getAllNodes()) {
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

        for (Agent agent : config.getAllNodes()) {
            result.append(agent.getListIP().get(0));
            result.append("\t");
            result.append(agent.getHostname());
            result.append(".");
            result.append(config.getDomainName());
            result.append("\t");
            result.append(agent.getHostname());
            result.append("\n");
        }

        result.append("127.0.0.1\tlocalhost");

        return result.toString();
    }

    public HadoopConfig getConfig() {
        return config;
    }
}
