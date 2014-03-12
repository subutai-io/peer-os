package org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.install;

import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.HadoopDAO;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.common.HadoopConfig;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.common.Tasks;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.operation.InstallHadoopOperation;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.wizard.Step3;
import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.TaskStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskCallback;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.HadoopModule;

/**
 * Created with IntelliJ IDEA. User: daralbaev Date: 12/7/13 Time: 5:55 PM
 */
public class Installation {

    private Step3 panel;
    private final HadoopConfig config;
    private final AgentManager agentManager;

    public Installation() {
        config = new HadoopConfig();
        agentManager = HadoopModule.getAgentManager();
    }

    public void setPanel(Step3 step) {
        this.panel = step;
    }

    public void startInstallation() {
        final Operation installOperation = new InstallHadoopOperation(config);

        panel.addOutput(installOperation.peekNextTask(), " started.");
        HadoopModule.getTaskRunner().executeTask(installOperation.getNextTask(), new TaskCallback() {
            @Override
            public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                if (task.isCompleted()) {
                    if (task.getTaskStatus() == TaskStatus.SUCCESS) {
                        panel.addOutput(task, " succeeded.");
                        if (installOperation.hasNextTask()) {
                            return installOperation.getNextTask();
                        } else {
                            installOperation.setCompleted(true);
                            copySSH();
                        }
                    } else {
                        installOperation.setCompleted(true);
                        panel.addOutput(task, " failed.");
                        panel.addOutput(task, stdErr);
                    }
                }

                return null;
            }
        });
    }

    private void copySSH() {
        Task task = Tasks.getCopySSHKeyTask(config.getAllNodes());

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
                        panel.addOutput(task, " succeeded.");
                        pasteSSH(keys);
                    } else {
                        panel.addOutput(task, " failed.");
                        panel.addOutput(task, stdErr);
                    }
                }

                return null;
            }
        });
    }

    private void pasteSSH(HashMap<UUID, String> keys) {
        List<String> arg = new ArrayList<String>();
        for (UUID id : keys.keySet()) {
            arg.add(keys.get(id));
        }

        Task task = Tasks.getPasteSSHKeyTask(config.getAllNodes(), arg);
        HadoopModule.getTaskRunner().executeTask(task, new TaskCallback() {
            @Override
            public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                if (task.isCompleted()) {
                    if (task.getTaskStatus() == TaskStatus.SUCCESS) {
                        panel.addOutput(task, " succeeded.");
                        configSSH();
                    } else {
                        panel.addOutput(task, " failed.");
                        panel.addOutput(task, stdErr);
                    }
                }
                return null;
            }
        });
    }

    private void configSSH() {
        Task task = Tasks.getConfigSSHFolderTask(config.getAllNodes());

        HadoopModule.getTaskRunner().executeTask(task, new TaskCallback() {
            @Override
            public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                if (task.isCompleted()) {
                    if (task.getTaskStatus() == TaskStatus.SUCCESS) {
                        panel.addOutput(task, " succeeded.");
                        copyHosts();
                    } else {
                        panel.addOutput(task, " failed.");
                        panel.addOutput(task, stdErr);
                    }
                }

                return null;
            }
        });
    }

    private void copyHosts() {
        Task task = Tasks.getCopyHostsTask(config.getAllNodes());

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
                        panel.addOutput(task, " succeeded.");
                        pasteHost(hosts);
                    } else {
                        panel.addOutput(task, " failed.");
                        panel.addOutput(task, stdErr);
                    }
                }

                return null;
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
        HadoopModule.getTaskRunner().executeTask(task, new TaskCallback() {
            @Override
            public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                if (task.isCompleted()) {
                    if (task.getTaskStatus() == TaskStatus.SUCCESS) {
                        panel.addOutput(task, " succeeded.");
                        HadoopDAO.saveHadoopClusterInfo(config.getCluster());
                        panel.setCloseable();
                    } else {
                        panel.addOutput(task, " failed.");
                    }
                }

                return null;
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
