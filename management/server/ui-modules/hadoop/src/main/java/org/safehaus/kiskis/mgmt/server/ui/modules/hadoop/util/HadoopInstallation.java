package org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.util;

import com.google.common.base.Strings;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.HadoopModule;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.wizard.Step3;
import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.TaskStatus;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: daralbaev
 * Date: 12/7/13
 * Time: 5:55 PM
 */
public class HadoopInstallation {
    private Task hadoopInstallationTask;
    private Task hadoopConfigureTask;
    private Task hadoopSNameNodeTask;
    private Task hadoopSlaveNameNode;
    private Task hadoopSlaveJobTracker;
    private Task hadoopSetSSH;
    private Task hadoopSSHMaster;
    private Task hadoopCopySSHSlaves;
    private Task hadoopConfigMasterSSH;
    private Task hadoopFormatMaster;

    private HadoopClusterInfo cluster;
    private String clusterName;
    private Agent nameNode, jobTracker, sNameNode;
    private List<Agent> dataNodes, taskTrackers;
    private Integer replicationFactor;
    private List<Agent> allNodes;
    private List<Agent> allSlaveNodes;
    private List<String> keys;

    private CommandManagerInterface commandManager;
    private Step3 panel;

    public HadoopInstallation(CommandManagerInterface commandManagerInterface) {
        this.commandManager = commandManagerInterface;

        hadoopInstallationTask = null;
        hadoopConfigureTask = null;
        hadoopSNameNodeTask = null;
        hadoopSlaveNameNode = null;
        hadoopSlaveJobTracker = null;
        hadoopSetSSH = null;
        hadoopSSHMaster = null;
        hadoopCopySSHSlaves = null;
        hadoopConfigMasterSSH = null;
        hadoopFormatMaster = null;
    }

    public void setPanel(Step3 step) {
        this.panel = step;
    }

    public void installHadoop() {
        setCluster();
        if (hadoopInstallationTask == null) {
            hadoopInstallationTask = RequestUtil.createTask(commandManager, "Setup Hadoop cluster");
            panel.addOutput(hadoopInstallationTask, " started...");

            for (Agent agent : allNodes) {
                if (agent != null) {
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put(":source", HadoopModule.MODULE_NAME);
                    map.put(":uuid", agent.getUuid().toString());

                    RequestUtil.createRequest(commandManager, HadoopCommands.INSTALL_DEB, hadoopInstallationTask, map);
                }
            }
        }
    }

    public void configureHadoop() {
        if (hadoopConfigureTask == null) {
            hadoopConfigureTask = RequestUtil.createTask(commandManager, "Configure Hadoop cluster");
            panel.addOutput(hadoopConfigureTask, " started...");

            for (Agent agent : allNodes) {
                if (agent != null) {
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put(":source", HadoopModule.MODULE_NAME);
                    map.put(":uuid", agent.getUuid().toString());

                    map.put(":namenode", nameNode.getListIP().get(0));
                    map.put(":jobtracker", jobTracker.getListIP().get(0));
                    map.put(":replicationfactor", replicationFactor.toString());

                    RequestUtil.createRequest(commandManager, HadoopCommands.SET_MASTERS, hadoopConfigureTask, map);
                }
            }
        }
    }

    public void configureSNameNode() {
        if (hadoopSNameNodeTask == null) {
            hadoopSNameNodeTask = RequestUtil.createTask(commandManager, "Configure Hadoop secondary name node");
            panel.addOutput(hadoopSNameNodeTask, " started...");

            HashMap<String, String> map = new HashMap<String, String>();
            map.put(":source", HadoopModule.MODULE_NAME);
            map.put(":uuid", nameNode.getUuid().toString());

            RequestUtil.createRequest(commandManager, HadoopCommands.CLEAR_MASTER, hadoopSNameNodeTask, map);

            map = new HashMap<String, String>();
            map.put(":source", HadoopModule.MODULE_NAME);
            map.put(":uuid", nameNode.getUuid().toString());

            map.put(":secondarynamenode", sNameNode.getListIP().get(0));

            RequestUtil.createRequest(commandManager, HadoopCommands.SET_SECONDARY_NAME_NODE, hadoopSNameNodeTask, map);
        }
    }

    public void setSlaveNameNode() {
        if (hadoopSlaveNameNode == null) {
            hadoopSlaveNameNode = RequestUtil.createTask(commandManager, "Set Hadoop slave name nodes");
            panel.addOutput(hadoopSlaveNameNode, " started...");

            HashMap<String, String> map = new HashMap<String, String>();
            map.put(":source", HadoopModule.MODULE_NAME);
            map.put(":uuid", nameNode.getUuid().toString());

            RequestUtil.createRequest(commandManager, HadoopCommands.CLEAR_SLAVES, hadoopSlaveNameNode, map);

            for (Agent agent : dataNodes) {
                if (agent != null) {
                    map = new HashMap<String, String>();
                    map.put(":source", HadoopModule.MODULE_NAME);
                    map.put(":uuid", nameNode.getUuid().toString());

                    map.put(":slave-hostname", agent.getListIP().get(0));

                    RequestUtil.createRequest(commandManager, HadoopCommands.SET_SLAVES, hadoopSlaveNameNode, map);
                }
            }
        }

    }

    public void setSlaveJobTracker() {
        if (hadoopSlaveJobTracker == null) {
            hadoopSlaveJobTracker = RequestUtil.createTask(commandManager, "Set Hadoop slave job tracker");
            panel.addOutput(hadoopSlaveJobTracker, " started...");

            HashMap<String, String> map = new HashMap<String, String>();
            map.put(":source", HadoopModule.MODULE_NAME);
            map.put(":uuid", jobTracker.getUuid().toString());

            RequestUtil.createRequest(commandManager, HadoopCommands.CLEAR_SLAVES, hadoopSlaveJobTracker, map);

            for (Agent agent : taskTrackers) {
                if (agent != null) {

                    map = new HashMap<String, String>();
                    map.put(":source", HadoopModule.MODULE_NAME);
                    map.put(":uuid", jobTracker.getUuid().toString());

                    map.put(":slave-hostname", agent.getListIP().get(0));

                    RequestUtil.createRequest(commandManager, HadoopCommands.SET_SLAVES, hadoopSlaveJobTracker, map);
                }
            }
        }

    }

    public void setSSH() {
        if (hadoopSetSSH == null) {
            hadoopSetSSH = RequestUtil.createTask(commandManager, "Set Hadoop configure SSH");
            panel.addOutput(hadoopSetSSH, " started...");

            HashMap<String, String> map = new HashMap<String, String>();
            map.put(":source", HadoopModule.MODULE_NAME);
            map.put(":uuid", nameNode.getUuid().toString());
            RequestUtil.createRequest(commandManager, HadoopCommands.SET_MASTER_KEY, hadoopSetSSH, map);

            if (!nameNode.equals(sNameNode)) {
                map = new HashMap<String, String>();
                map.put(":source", HadoopModule.MODULE_NAME);
                map.put(":uuid", sNameNode.getUuid().toString());
                RequestUtil.createRequest(commandManager, HadoopCommands.SET_MASTER_KEY, hadoopSetSSH, map);
            }

            if (!jobTracker.equals(nameNode) && !jobTracker.equals(sNameNode)) {
                map = new HashMap<String, String>();
                map.put(":source", HadoopModule.MODULE_NAME);
                map.put(":uuid", jobTracker.getUuid().toString());
                RequestUtil.createRequest(commandManager, HadoopCommands.SET_MASTER_KEY, hadoopSetSSH, map);
            }
        }

    }

    public void setSSHMaster() {
        if (hadoopSSHMaster == null) {
            hadoopSSHMaster = RequestUtil.createTask(commandManager, "Set Hadoop SSH master");
            panel.addOutput(hadoopSSHMaster, " started...");

            HashMap<String, String> map = new HashMap<String, String>();
            map.put(":source", HadoopModule.MODULE_NAME);
            map.put(":uuid", nameNode.getUuid().toString());
            RequestUtil.createRequest(commandManager, HadoopCommands.COPY_MASTER_KEY, hadoopSSHMaster, map);

            if (!nameNode.equals(sNameNode)) {
                map = new HashMap<String, String>();
                map.put(":source", HadoopModule.MODULE_NAME);
                map.put(":uuid", sNameNode.getUuid().toString());
                RequestUtil.createRequest(commandManager, HadoopCommands.COPY_MASTER_KEY, hadoopSSHMaster, map);
            }

            if (!jobTracker.equals(nameNode) && !jobTracker.equals(sNameNode)) {
                map = new HashMap<String, String>();
                map.put(":source", HadoopModule.MODULE_NAME);
                map.put(":uuid", jobTracker.getUuid().toString());
                RequestUtil.createRequest(commandManager, HadoopCommands.COPY_MASTER_KEY, hadoopSSHMaster, map);
            }
        }

    }

    public void copySSHSlaves() {
        if (hadoopCopySSHSlaves == null) {
            hadoopCopySSHSlaves = RequestUtil.createTask(commandManager, "Copy Hadoop SSH key to slaves");
            panel.addOutput(hadoopCopySSHSlaves, " started...");

            if (keys != null && !keys.isEmpty()) {
                for (Agent agent : allNodes) {
                    if (agent != null) {
                        for (String key : keys) {
                            HashMap<String, String> map = new HashMap<String, String>();
                            map.put(":source", HadoopModule.MODULE_NAME);
                            map.put(":uuid", agent.getUuid().toString());

                            map.put(":PUB_KEY", key);

                            RequestUtil.createRequest(commandManager, HadoopCommands.PASTE_MASTER_KEY, hadoopCopySSHSlaves, map);
                        }
                    }
                }
            }
        }

    }

    public void configSSHMaster() {
        if (hadoopConfigMasterSSH == null) {
            hadoopConfigMasterSSH = RequestUtil.createTask(commandManager, "Config SSH Masters");
            panel.addOutput(hadoopConfigMasterSSH, " started...");

            HashMap<String, String> map = new HashMap<String, String>();
            map.put(":source", HadoopModule.MODULE_NAME);
            map.put(":uuid", nameNode.getUuid().toString());
            RequestUtil.createRequest(commandManager, HadoopCommands.SET_MASTER_CONFIG, hadoopConfigMasterSSH, map);

            if (!nameNode.equals(sNameNode)) {
                map = new HashMap<String, String>();
                map.put(":source", HadoopModule.MODULE_NAME);
                map.put(":uuid", sNameNode.getUuid().toString());
                RequestUtil.createRequest(commandManager, HadoopCommands.SET_MASTER_CONFIG, hadoopConfigMasterSSH, map);
            }

            if (!jobTracker.equals(nameNode) && !jobTracker.equals(sNameNode)) {
                map = new HashMap<String, String>();
                map.put(":source", HadoopModule.MODULE_NAME);
                map.put(":uuid", jobTracker.getUuid().toString());
                RequestUtil.createRequest(commandManager, HadoopCommands.SET_MASTER_CONFIG, hadoopConfigMasterSSH, map);
            }
        }

    }

    public void formatMaster() {
        if (hadoopFormatMaster == null) {
            hadoopFormatMaster = RequestUtil.createTask(commandManager, "Format name node");
            panel.addOutput(hadoopFormatMaster, " started...");

            HashMap<String, String> map = new HashMap<String, String>();
            map.put(":source", HadoopModule.MODULE_NAME);
            map.put(":uuid", nameNode.getUuid().toString());
            RequestUtil.createRequest(commandManager, HadoopCommands.FORMAT_NAME_NODE, hadoopFormatMaster, map);

            System.out.println(cluster);
            commandManager.saveHadoopClusterData(cluster);
        }

    }

    public void onCommand(Response response, Step3 panel) {
        List<ParseResult> list = commandManager.parseTask(response.getTaskUuid(), true);
        Task task = commandManager.getTask(response.getTaskUuid());
        int count = commandManager.getResponseCount(task.getUuid());
        if (!list.isEmpty() && panel != null && list.size() == count) {
            if (task.getTaskStatus().compareTo(TaskStatus.SUCCESS) == 0) {
                if (task.equals(hadoopInstallationTask)) {
                    configureHadoop();
                } else if (task.equals(hadoopConfigureTask)) {
                    configureSNameNode();
                } else if (task.equals(hadoopSNameNodeTask)) {
                    setSlaveNameNode();
                } else if (task.equals(hadoopSlaveNameNode)) {
                    setSlaveJobTracker();
                } else if (task.equals(hadoopSlaveJobTracker)) {
                    setSSH();
                } else if (task.equals(hadoopSetSSH)) {
                    setSSHMaster();
                } else if (task.equals(hadoopSSHMaster)) {
                    keys = new ArrayList<String>();
                    for (ParseResult pr : list) {
                        keys.add(pr.getResponse().getStdOut().trim());
                    }
                    copySSHSlaves();
                } else if (task.equals(hadoopCopySSHSlaves)) {
                    configSSHMaster();
                } else if (task.equals(hadoopConfigMasterSSH)) {
                    formatMaster();
                } else if (task.equals(hadoopFormatMaster)) {
                    panel.setCloseable();
                }
            } else if (task.getTaskStatus().compareTo(TaskStatus.FAIL) == 0) {
                panel.addOutput(task, " failed.\n" +
                        "Details: " + getResponseError(list));
                panel.setCloseable();
            }
        }
    }

    private void removeDuplicateAgents() {
        Set<Agent> allAgents = new HashSet<Agent>();
        if (dataNodes != null) {
            allAgents.addAll(dataNodes);
        }
        if (taskTrackers != null) {
            allAgents.addAll(taskTrackers);
        }

        this.allSlaveNodes = new ArrayList<Agent>();
        this.allSlaveNodes.addAll(allAgents);

        if (nameNode != null) {
            allAgents.add(nameNode);
        }
        if (jobTracker != null) {
            allAgents.add(jobTracker);
        }
        if (sNameNode != null) {
            allAgents.add(sNameNode);
        }

        this.allNodes = new ArrayList<Agent>();
        this.allNodes.addAll(allAgents);
    }

    private String getResponseError(List<ParseResult> list) {
        StringBuilder stringBuilder = new StringBuilder();
        for (ParseResult pr : list) {
            if (!Strings.isNullOrEmpty(pr.getResponse().getStdErr())) {
                stringBuilder.append(pr.getRequest());
                stringBuilder.append("\n");
                stringBuilder.append(pr.getResponse());
                stringBuilder.append("\n");
            }
        }

        return stringBuilder.toString();
    }

    private void setCluster() {
        removeDuplicateAgents();
        cluster = new HadoopClusterInfo();

        cluster.setClusterName(clusterName);
        cluster.setReplicationFactor(replicationFactor);

        cluster.setNameNode(nameNode.getUuid());
        cluster.setSecondaryNameNode(sNameNode.getUuid());
        cluster.setJobTracker(jobTracker.getUuid());

        List<UUID> list = new ArrayList<UUID>();
        for (Agent agent : dataNodes) {
            list.add(agent.getUuid());
        }
        cluster.setDataNodes(list);

        list = new ArrayList<UUID>();
        for (Agent agent : taskTrackers) {
            list.add(agent.getUuid());
        }
        cluster.setTaskTrackers(list);
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public Agent getNameNode() {
        return nameNode;
    }

    public void setNameNode(Agent nameNode) {
        this.nameNode = nameNode;
    }

    public Agent getJobTracker() {
        return jobTracker;
    }

    public void setJobTracker(Agent jobTracker) {
        this.jobTracker = jobTracker;
    }

    public Agent getsNameNode() {
        return sNameNode;
    }

    public void setsNameNode(Agent sNameNode) {
        this.sNameNode = sNameNode;
    }

    public List<Agent> getDataNodes() {
        return dataNodes;
    }

    public void setDataNodes(List<Agent> dataNodes) {
        this.dataNodes = dataNodes;
    }

    public List<Agent> getTaskTrackers() {
        return taskTrackers;
    }

    public void setTaskTrackers(List<Agent> taskTrackers) {
        this.taskTrackers = taskTrackers;
    }

    public void setReplicationFactor(Integer replicationFactor) {
        this.replicationFactor = replicationFactor;
    }
}
