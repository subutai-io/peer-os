/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.lxcmanager;

import org.safehaus.kiskis.mgmt.api.lxcmanager.LxcState;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.api.lxcmanager.LxcManager;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskCallback;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskRunner;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.TaskStatus;
import org.safehaus.kiskis.mgmt.shared.protocol.settings.Common;

/**
 *
 * @author dilshat
 */
public class LxcManagerImpl implements LxcManager {

    private TaskRunner taskRunner;
    private AgentManager agentManager;

    public void setTaskRunner(TaskRunner taskRunner) {
        this.taskRunner = taskRunner;
    }

    public void setAgentManager(AgentManager agentManager) {
        this.agentManager = agentManager;
    }

    public Map<Agent, Integer> getBestHostServers() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public Map<String, EnumMap<LxcState, List<String>>> getLxcOnPhysicalServers() {
        final Map<String, EnumMap<LxcState, List<String>>> agentFamilies = new HashMap<String, EnumMap<LxcState, List<String>>>();
        Set<Agent> pAgents = agentManager.getPhysicalAgents();
        if (!pAgents.isEmpty()) {

            Task getLxcListTask = Tasks.getLxcListTask(agentManager.getPhysicalAgents());

            taskRunner.executeTask(getLxcListTask, new TaskCallback() {
                private final Map<UUID, String> lxcMap = new HashMap<UUID, String>();

                @Override
                public Task onResponse(Task task, Response response, String stdOut, String stdErr) {

                    if (Util.isFinalResponse(response)) {
                        lxcMap.put(response.getUuid(), stdOut);
                    }

                    if (task.isCompleted()) {
                        for (Map.Entry<UUID, String> parentEntry : lxcMap.entrySet()) {
                            Agent agent = agentManager.getAgentByUUID(parentEntry.getKey());
                            String parentHostname = agent == null
                                    ? String.format("Offline[%s]", parentEntry.getKey()) : agent.getHostname();
                            EnumMap<LxcState, List<String>> lxcs = new EnumMap<LxcState, List<String>>(LxcState.class);
                            String[] lxcStrs = parentEntry.getValue().split("\\n");
                            LxcState currState = null;
                            for (String lxcStr : lxcStrs) {
                                if (LxcState.RUNNING.name().equalsIgnoreCase(lxcStr)) {
                                    if (lxcs.get(LxcState.RUNNING) == null) {
                                        lxcs.put(LxcState.RUNNING, new ArrayList<String>());
                                    }
                                    currState = LxcState.RUNNING;
                                } else if (LxcState.STOPPED.name().equalsIgnoreCase(lxcStr)) {
                                    if (lxcs.get(LxcState.STOPPED) == null) {
                                        lxcs.put(LxcState.STOPPED, new ArrayList<String>());
                                    }
                                    currState = LxcState.STOPPED;
                                } else if (currState != null
                                        && !Util.isStringEmpty(lxcStr) && lxcStr.contains(Common.PARENT_CHILD_LXC_SEPARATOR)) {
                                    lxcs.get(currState).add(lxcStr);
                                }
                            }
                            agentFamilies.put(parentHostname, lxcs);
                        }
                        synchronized (task) {
                            task.notifyAll();
                        }
                    }

                    return null;
                }
            });

            synchronized (getLxcListTask) {
                try {
                    getLxcListTask.wait(getLxcListTask.getAvgTimeout() * 1000 + 1000);
                } catch (InterruptedException ex) {
                }
            }
        }

        return agentFamilies;
    }

    public boolean cloneLxcOnHost(Agent physicalAgent, String lxcHostname) {
        if (physicalAgent != null && !Util.isStringEmpty(lxcHostname)) {
            Task cloneTask = Tasks.getCloneSingleLxcTask(physicalAgent, lxcHostname);
            taskRunner.executeTask(cloneTask, new TaskCallback() {

                public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                    if (task.isCompleted()) {
                        synchronized (task) {
                            task.notifyAll();
                        }
                    }

                    return null;
                }
            });

            synchronized (cloneTask) {
                try {
                    cloneTask.wait(cloneTask.getAvgTimeout() * 1000 + 1000);
                } catch (InterruptedException ex) {
                }
            }

            return cloneTask.getTaskStatus() == TaskStatus.SUCCESS;
        }
        return false;
    }

    public LxcState startLxcOnHost(Agent physicalAgent, String lxcHostname) {
        if (physicalAgent != null && !Util.isStringEmpty(lxcHostname)) {
            Task startLxcTask = Tasks.getLxcStartTask(physicalAgent, lxcHostname);
            final Task getLxcInfoTask = Tasks.getLxcInfoWithWaitTask(physicalAgent, lxcHostname);
            taskRunner.executeTask(startLxcTask, new TaskCallback() {

                public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                    if (task.isCompleted()) {
                        if (task.getData() == TaskType.START_LXC) {
                            //send lxc-info cmd
                            return getLxcInfoTask;
                        } else if (task.getData() == TaskType.GET_LXC_INFO) {
                            if (stdOut.indexOf("RUNNING") != -1) {
                                task.setData(LxcState.RUNNING);
                            }
                            synchronized (task) {
                                task.notifyAll();
                            }
                        }
                    }

                    return null;
                }
            });
            synchronized (getLxcInfoTask) {
                try {
                    getLxcInfoTask.wait((startLxcTask.getAvgTimeout() + getLxcInfoTask.getAvgTimeout()) * 1000 + 1000);
                } catch (InterruptedException ex) {
                }
            }

            return LxcState.RUNNING.equals(getLxcInfoTask.getData()) ? LxcState.RUNNING : LxcState.STOPPED;
        }
        return LxcState.STOPPED;
    }

    public LxcState stopLxcOnHost(Agent physicalAgent, String lxcHostname) {
        if (physicalAgent != null && !Util.isStringEmpty(lxcHostname)) {
            Task stopLxcTask = Tasks.getLxcStopTask(physicalAgent, lxcHostname);
            final Task getLxcInfoTask = Tasks.getLxcInfoWithWaitTask(physicalAgent, lxcHostname);
            taskRunner.executeTask(stopLxcTask, new TaskCallback() {

                public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                    if (task.isCompleted()) {
                        if (task.getData() == TaskType.STOP_LXC) {
                            //send lxc-info cmd
                            return getLxcInfoTask;
                        } else if (task.getData() == TaskType.GET_LXC_INFO) {
                            if (stdOut.indexOf("RUNNING") != -1) {
                                task.setData(LxcState.RUNNING);
                            }
                            synchronized (task) {
                                task.notifyAll();
                            }
                        }
                    }

                    return null;
                }
            });
            synchronized (getLxcInfoTask) {
                try {
                    getLxcInfoTask.wait((stopLxcTask.getAvgTimeout() + getLxcInfoTask.getAvgTimeout()) * 1000 + 1000);
                } catch (InterruptedException ex) {
                }
            }

            return LxcState.RUNNING.equals(getLxcInfoTask.getData()) ? LxcState.RUNNING : LxcState.STOPPED;
        }
        return LxcState.STOPPED;
    }

    public LxcState destroyLxcOnHost(Agent physicalAgent, String lxcHostname) {
        if (physicalAgent != null && !Util.isStringEmpty(lxcHostname)) {
            Task destroyLxcTask = Tasks.getLxcDestroyTask(physicalAgent, lxcHostname);
            final Task getLxcInfoTask = Tasks.getLxcInfoWithWaitTask(physicalAgent, lxcHostname);
            taskRunner.executeTask(destroyLxcTask, new TaskCallback() {

                public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                    if (task.isCompleted()) {
                        if (task.getData() == TaskType.DESTROY_LXC) {
                            //send lxc-info cmd
                            return getLxcInfoTask;
                        } else if (task.getData() == TaskType.GET_LXC_INFO) {
                            if (stdOut.indexOf("RUNNING") != -1) {
                                task.setData(LxcState.RUNNING);
                            }
                            synchronized (task) {
                                task.notifyAll();
                            }
                        }
                    }

                    return null;
                }
            });
            synchronized (getLxcInfoTask) {
                try {
                    getLxcInfoTask.wait((destroyLxcTask.getAvgTimeout() + getLxcInfoTask.getAvgTimeout()) * 1000 + 1000);
                } catch (InterruptedException ex) {
                }
            }

            return LxcState.RUNNING.equals(getLxcInfoTask.getData()) ? LxcState.RUNNING : LxcState.STOPPED;
        }
        return LxcState.STOPPED;
    }

}
