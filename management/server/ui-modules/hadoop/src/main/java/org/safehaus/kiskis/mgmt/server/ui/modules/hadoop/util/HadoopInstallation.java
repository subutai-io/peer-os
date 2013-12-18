package org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.util;

import com.google.common.base.Strings;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.HadoopModule;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.wizard.Step3;
import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.TaskStatus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    private String clusterName;
    private Agent nameNode, jobTracker, sNameNode;
    private List<Agent> dataNodes, taskTrackers;
    private Integer replicationFactor;
    private List<Agent> allNodes;
    private List<Agent> allSlaveNodes;
    private List<String> keys;

    private CommandManagerInterface commandManager;

    public HadoopInstallation(CommandManagerInterface commandManagerInterface) {
        this.commandManager = commandManagerInterface;
    }

    public void installHadoop() {
        removeDuplicateAgents();

        hadoopInstallationTask = createTask("Setup Hadoop cluster");
        createInstallationRequest();
    }

    public void configureHadoop() {
        hadoopConfigureTask = createTask("Configure Hadoop cluster");
        createConfigureRequest();
    }

    public void configureSNameNode() {
        hadoopSNameNodeTask = createTask("Configure Hadoop secondary name node");
        createSNameNodeRequest();
    }

    public void setSlaveNameNode() {
        hadoopSlaveNameNode = createTask("Set Hadoop slave name nodes");
        createSetSlaveNameNodeRequest();
    }

    public void setSlaveJobTracker() {
        hadoopSlaveJobTracker = createTask("Set Hadoop slave job tracker");
        createSetSlaveJobTrackerRequest();
    }

    public void setSSH() {
        hadoopSetSSH = createTask("Set Hadoop configure SSH");
        createSSHRequest();
    }

    public void setSSHMaster() {
        hadoopSSHMaster = createTask("Set Hadoop SSH master");
        createSSHMasterRequest();
    }

    public void copySSHSlaves() {
        hadoopCopySSHSlaves = createTask("Copy Hadoop SSH key to slaves");
        createCopySSHRequest();
    }

    public void configSSHMaster() {
        hadoopConfigMasterSSH = createTask("Config SSH Masters");
        createConfigSSHMasterRequest();
    }

    public void formatMaster() {
        hadoopFormatMaster = createTask("Format name node");
        createFormatMasterRequest();
    }

    private Task createTask(String description) {
        Task clusterTask = new Task();
        clusterTask.setTaskStatus(TaskStatus.NEW);
        clusterTask.setDescription(description);
        commandManager.saveTask(clusterTask);

        return clusterTask;
    }


    private void createInstallationRequest() {
        for (Agent agent : allNodes) {
            if (agent != null) {
                createRequest(HadoopCommands.INSTALL_HADOOP, hadoopInstallationTask, agent, null, null);
            }
        }
    }

    private void createConfigureRequest() {
        for (Agent agent : allNodes) {
            if (agent != null) {
                createRequest(HadoopCommands.CONFIGURE_SLAVES, hadoopConfigureTask, agent, null, null);
            }
        }
    }

    private void createSNameNodeRequest() {
        createRequest(HadoopCommands.CLEAR_SECONDARY_NAME_NODE, hadoopSNameNodeTask, nameNode, null, null);
        createRequest(HadoopCommands.SET_SECONDARY_NAME_NODE, hadoopSNameNodeTask, nameNode, null, null);
    }

    private void createSetSlaveNameNodeRequest() {
        createRequest(HadoopCommands.CLEAR_SLAVES_NAME_NODE, hadoopSlaveNameNode, nameNode, null, null);
        for (Agent agent : allSlaveNodes) {
            if (agent != null) {
                createRequest(HadoopCommands.SET_SLAVES_NAME_NODE, hadoopSlaveNameNode, nameNode, agent, null);
            }
        }
    }

    private void createSetSlaveJobTrackerRequest() {
        createRequest(HadoopCommands.CLEAR_SLAVES_JOB_TRACKER, hadoopSlaveJobTracker, jobTracker, null, null);
        for (Agent agent : allSlaveNodes) {
            if (agent != null) {
                createRequest(HadoopCommands.SET_SLAVES_JOB_TRACKER, hadoopSlaveJobTracker, jobTracker, agent, null);
            }
        }
    }

    private void createSSHRequest() {
        for (Agent agent : allNodes) {
            if (agent != null) {
                createRequest(HadoopCommands.SET_SSH_MASTERS, hadoopSetSSH, agent, null, null);
            }
        }
    }

    private void createCopySSHRequest() {
        if (keys != null && !keys.isEmpty()) {
            for (Agent agent : allSlaveNodes) {
                if (agent != null) {
                    for (String key : keys) {
                        createRequest(HadoopCommands.COPY_SSH_SLAVES, hadoopCopySSHSlaves, agent, null, key);
                    }
                }
            }
        }
    }

    private void createConfigSSHMasterRequest() {
        createRequest(HadoopCommands.CONFIG_SSH_MASTER, hadoopConfigMasterSSH, nameNode, null, null);
        createRequest(HadoopCommands.CONFIG_SSH_MASTER, hadoopConfigMasterSSH, sNameNode, null, null);
        createRequest(HadoopCommands.CONFIG_SSH_MASTER, hadoopConfigMasterSSH, jobTracker, null, null);
    }

    private void createFormatMasterRequest() {
        createRequest(HadoopCommands.FORMAT_NAME_NODE, hadoopFormatMaster, nameNode, null, null);
    }

    private void createSSHMasterRequest() {
        createRequest(HadoopCommands.COPY_SSH_MASTERS, hadoopSSHMaster, nameNode, null, null);
        createRequest(HadoopCommands.COPY_SSH_MASTERS, hadoopSSHMaster, sNameNode, null, null);
        createRequest(HadoopCommands.COPY_SSH_MASTERS, hadoopSSHMaster, jobTracker, null, null);
    }

    private Request createRequest(final String command, Task task, Agent agent, Agent slave, String key) {
        String json = command;
        json = json.replaceAll(":taskUuid", task.getUuid().toString());
        json = json.replaceAll(":source", HadoopModule.MODULE_NAME);

        json = json.replaceAll(":uuid", agent.getUuid().toString());
        json = json.replaceAll(":requestSequenceNumber", task.getIncrementedReqSeqNumber().toString());

        json = json.replaceAll(":namenode", nameNode.getHostname());
        json = json.replaceAll(":jobtracker", jobTracker.getHostname());
        json = json.replaceAll(":replicationfactor", replicationFactor.toString());

        if (key != null) {
            json = json.replaceAll(":PUB_KEY", key);
        }

        if (slave != null) {
            json = json.replaceAll(":slave-hostname", slave.getHostname());
        }


        Request request = CommandJson.getRequest(json);
        if (commandManager != null) {
            commandManager.executeCommand(new Command(request));
        }

        return request;
    }

    public void onCommand(Response response, Step3 panel) {
        if (hadoopInstallationTask != null && response.getTaskUuid().compareTo(hadoopInstallationTask.getUuid()) == 0) {
            List<ParseResult> resultList = commandManager.parseTask(hadoopInstallationTask, true);
            hadoopInstallationTask = commandManager.getTask(hadoopInstallationTask.getUuid());

            if (resultList.size() > 0 && hadoopInstallationTask.getTaskStatus().compareTo(TaskStatus.SUCCESS) == 0) {
                panel.addOutput(hadoopInstallationTask, " successfully finished.");
//                configureHadoop();
            } else if (hadoopInstallationTask.getTaskStatus().compareTo(TaskStatus.FAIL) == 0) {
                panel.addOutput(hadoopInstallationTask, " failed.\nDetails: " + getResponseError(resultList));
            }
        } else if (hadoopConfigureTask != null && response.getTaskUuid().compareTo(hadoopConfigureTask.getUuid()) == 0) {
            List<ParseResult> resultList = commandManager.parseTask(hadoopConfigureTask, true);
            hadoopConfigureTask = commandManager.getTask(hadoopConfigureTask.getUuid());

            if (resultList.size() > 0 && hadoopConfigureTask.getTaskStatus().compareTo(TaskStatus.SUCCESS) == 0) {
                panel.addOutput(hadoopConfigureTask, " successfully finished.");
//                configureSNameNode();
            } else if (hadoopConfigureTask.getTaskStatus().compareTo(TaskStatus.FAIL) == 0) {
                panel.addOutput(hadoopConfigureTask, " failed.\nDetails: " + getResponseError(resultList));
            }
        } else if (hadoopSNameNodeTask != null && response.getTaskUuid().compareTo(hadoopSNameNodeTask.getUuid()) == 0) {
            List<ParseResult> resultList = commandManager.parseTask(hadoopSNameNodeTask, true);
            hadoopSNameNodeTask = commandManager.getTask(hadoopSNameNodeTask.getUuid());

            if (resultList.size() > 0 && hadoopSNameNodeTask.getTaskStatus().compareTo(TaskStatus.SUCCESS) == 0) {
                panel.addOutput(hadoopSNameNodeTask, " successfully finished.");
//                setSlaveNameNode();
            } else if (hadoopSNameNodeTask.getTaskStatus().compareTo(TaskStatus.FAIL) == 0) {
                panel.addOutput(hadoopSNameNodeTask, " failed.\nDetails: " + getResponseError(resultList));
            }
        } else if (hadoopSlaveNameNode != null && response.getTaskUuid().compareTo(hadoopSlaveNameNode.getUuid()) == 0) {
            List<ParseResult> resultList = commandManager.parseTask(hadoopSlaveNameNode, true);
            hadoopSlaveNameNode = commandManager.getTask(hadoopSlaveNameNode.getUuid());

            if (resultList.size() > 0 && hadoopSlaveNameNode.getTaskStatus().compareTo(TaskStatus.SUCCESS) == 0) {
                panel.addOutput(hadoopSlaveNameNode, " successfully finished.");
//                setSlaveJobTracker();
            } else if (hadoopSlaveNameNode.getTaskStatus().compareTo(TaskStatus.FAIL) == 0) {
                panel.addOutput(hadoopSlaveNameNode, " failed.\nDetails: " + getResponseError(resultList));
            }
        } else if (hadoopSlaveJobTracker != null && response.getTaskUuid().compareTo(hadoopSlaveJobTracker.getUuid()) == 0) {
            List<ParseResult> resultList = commandManager.parseTask(hadoopSlaveJobTracker, true);
            hadoopSlaveJobTracker = commandManager.getTask(hadoopSlaveJobTracker.getUuid());

            if (resultList.size() > 0 && hadoopSlaveJobTracker.getTaskStatus().compareTo(TaskStatus.SUCCESS) == 0) {
                panel.addOutput(hadoopSlaveJobTracker, " successfully finished.");
//                setSSH();
            } else if (hadoopSlaveJobTracker.getTaskStatus().compareTo(TaskStatus.FAIL) == 0) {
                panel.addOutput(hadoopSlaveJobTracker, " failed.\nDetails: " + getResponseError(resultList));
            }
        } else if (hadoopSetSSH != null && response.getTaskUuid().compareTo(hadoopSetSSH.getUuid()) == 0) {
            List<ParseResult> resultList = commandManager.parseTask(hadoopSetSSH, true);
            hadoopSetSSH = commandManager.getTask(hadoopSetSSH.getUuid());

            if (resultList.size() > 0 && hadoopSetSSH.getTaskStatus().compareTo(TaskStatus.SUCCESS) == 0) {
                panel.addOutput(hadoopSetSSH, " successfully finished.");
//                setSSHMaster();
            } else if (hadoopSetSSH.getTaskStatus().compareTo(TaskStatus.FAIL) == 0) {
                panel.addOutput(hadoopSetSSH, " failed.\nDetails: " + getResponseError(resultList));
            }
        } else if (hadoopSSHMaster != null && response.getTaskUuid().compareTo(hadoopSSHMaster.getUuid()) == 0) {
            List<ParseResult> resultList = commandManager.parseTask(hadoopSSHMaster, true);
            hadoopSSHMaster = commandManager.getTask(hadoopSSHMaster.getUuid());

            if (resultList.size() > 0 && hadoopSSHMaster.getTaskStatus().compareTo(TaskStatus.SUCCESS) == 0) {
                panel.addOutput(hadoopSSHMaster, " successfully finished.");
                keys = new ArrayList<String>();
                for (ParseResult pr : resultList) {
                    keys.add(pr.getResponse().getStdOut());
                }
            } else if (hadoopSSHMaster.getTaskStatus().compareTo(TaskStatus.FAIL) == 0) {
                panel.addOutput(hadoopSSHMaster, " failed.\nDetails: " + getResponseError(resultList));
            }
        } else if (hadoopCopySSHSlaves != null && response.getTaskUuid().compareTo(hadoopCopySSHSlaves.getUuid()) == 0) {
            List<ParseResult> resultList = commandManager.parseTask(hadoopCopySSHSlaves, true);
            hadoopCopySSHSlaves = commandManager.getTask(hadoopCopySSHSlaves.getUuid());

            if (resultList.size() > 0 && hadoopCopySSHSlaves.getTaskStatus().compareTo(TaskStatus.SUCCESS) == 0) {
                panel.addOutput(hadoopCopySSHSlaves, " successfully finished.");
//                setSSHMaster();
            } else if (hadoopCopySSHSlaves.getTaskStatus().compareTo(TaskStatus.FAIL) == 0) {
                panel.addOutput(hadoopCopySSHSlaves, " failed.\nDetails: " + getResponseError(resultList));
            }
        } else if (hadoopConfigMasterSSH != null && response.getTaskUuid().compareTo(hadoopConfigMasterSSH.getUuid()) == 0) {
            List<ParseResult> resultList = commandManager.parseTask(hadoopConfigMasterSSH, true);
            hadoopConfigMasterSSH = commandManager.getTask(hadoopConfigMasterSSH.getUuid());

            if (resultList.size() > 0 && hadoopConfigMasterSSH.getTaskStatus().compareTo(TaskStatus.SUCCESS) == 0) {
                panel.addOutput(hadoopConfigMasterSSH, " successfully finished.");
//                setSSHMaster();
            } else if (hadoopConfigMasterSSH.getTaskStatus().compareTo(TaskStatus.FAIL) == 0) {
                panel.addOutput(hadoopConfigMasterSSH, " failed.\nDetails: " + getResponseError(resultList));
            }
        } else if (hadoopFormatMaster != null && response.getTaskUuid().compareTo(hadoopFormatMaster.getUuid()) == 0) {
            List<ParseResult> resultList = commandManager.parseTask(hadoopFormatMaster, true);
            hadoopFormatMaster = commandManager.getTask(hadoopFormatMaster.getUuid());

            if (resultList.size() > 0 && hadoopFormatMaster.getTaskStatus().compareTo(TaskStatus.SUCCESS) == 0) {
                panel.addOutput(hadoopFormatMaster, " successfully finished.");
//                setSSHMaster();
            } else if (hadoopFormatMaster.getTaskStatus().compareTo(TaskStatus.FAIL) == 0) {
                panel.addOutput(hadoopFormatMaster, " failed.\nDetails: " + getResponseError(resultList));
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

    public Task getHadoopInstallationTask() {
        return hadoopInstallationTask;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public void setHadoopInstallationTask(Task hadoopInstallationTask) {
        this.hadoopInstallationTask = hadoopInstallationTask;
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

    public Integer getReplicationFactor() {
        return replicationFactor;
    }

    public void setReplicationFactor(Integer replicationFactor) {
        this.replicationFactor = replicationFactor;
    }
}
