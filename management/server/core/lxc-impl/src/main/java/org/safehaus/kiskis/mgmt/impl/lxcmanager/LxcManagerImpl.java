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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.api.lxcmanager.LxcManager;
import org.safehaus.kiskis.mgmt.api.lxcmanager.ServerMetric;
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
 *
 *
 */
public class LxcManagerImpl implements LxcManager {

    private final Pattern p = Pattern.compile("load average: (.*)");
    private final double MIN_HDD_LXC_MB = 10 * 1024;         // 10G
    private final double MIN_HDD_IN_RESERVE_MB = 50 * 1024; // 50G
    private final double MIN_RAM_LXC_MB = 1024;             // 1G
    private final double MIN_RAM_IN_RESERVE_MB = 2 * 1024;  // 2G
    private final double MIN_CPU_LXC_PERCENT = 25;          // 20%
    private final double MIN_CPU_IN_RESERVE_PERCENT = 30;   // 30%
    private final int MAX_NUMBER_OF_LXCS_PER_HOST = 10;   // 30%

    private TaskRunner taskRunner;
    private AgentManager agentManager;

    public void setTaskRunner(TaskRunner taskRunner) {
        this.taskRunner = taskRunner;
    }

    public void setAgentManager(AgentManager agentManager) {
        this.agentManager = agentManager;
    }

    public Map<Agent, ServerMetric> getPhysicalServerMetrics() {
        final Map<Agent, ServerMetric> serverMetrics = new HashMap<Agent, ServerMetric>();
        Set<Agent> agents = agentManager.getPhysicalAgents();
        //omit management server
        for (Iterator<Agent> it = agents.iterator(); it.hasNext();) {
            Agent agent = it.next();
            if (!agent.getHostname().matches("^py.*")) {
                it.remove();
            }
        }
        if (!agents.isEmpty()) {

            Task getMetricsTask = Tasks.getMetricsTask(agents);

            taskRunner.executeTask(getMetricsTask, new TaskCallback() {
                private final Map<UUID, String> stdOuts = new HashMap<UUID, String>();

                public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                    if (Util.isFinalResponse(response)) {
                        stdOuts.put(response.getUuid(), stdOut);
                    }

                    if (task.isCompleted()) {

                        for (Map.Entry<UUID, String> out : stdOuts.entrySet()) {
                            String[] metrics = out.getValue().split("\n");
                            int freeRamMb = 0;
                            int freeHddMb = 0;
                            int numOfProc = 0;
                            double loadAvg = 0;
                            double cpuLoadPercent = 100;
                            boolean serverOK = false;
                            if (metrics.length == 4) {
                                int line = 0;
                                for (String metric : metrics) {
                                    line++;
                                    if (line == 1) {
                                        //   -/+ buffers/cache:       1829       5810
                                        String[] ramMetric = metric.split("\\s+");
                                        String freeRamMbStr = ramMetric[ramMetric.length - 1];
                                        if (Util.isNumeric(freeRamMbStr)) {
                                            freeRamMb = Integer.parseInt(freeRamMbStr);
                                        } else {
                                            break;
                                        }
                                    } else if (line == 2) {
                                        //   /dev/sda1       449G  3.8G  422G   1% /
                                        String[] hddMetric = metric.split("\\s+");
                                        if (hddMetric.length == 6) {
                                            String hddMetricKbStr = hddMetric[3];
                                            if (Util.isNumeric(hddMetricKbStr)) {
                                                freeHddMb = Integer.parseInt(hddMetricKbStr) / 1024;
                                            } else {
                                                break;
                                            }
                                        } else {
                                            break;
                                        }
                                    } else if (line == 3) {
                                        //    15:10:33 up 18:51,  0 users,  load average: 0.03, 0.08, 0.06

                                        Matcher m = p.matcher(metric);
                                        if (m.find()) {
                                            String[] loads = m.group(1).split(",");
                                            if (loads.length == 3) {
                                                if (Util.isNumeric(loads[0]) && Util.isNumeric(loads[1]) && Util.isNumeric(loads[2])) {
                                                    loadAvg = (Double.parseDouble(loads[0]) + Double.parseDouble(loads[1]) + Double.parseDouble(loads[2])) / 3;
                                                } else {
                                                    break;
                                                }
                                            } else {
                                                break;
                                            }
                                        } else {
                                            break;
                                        }
                                    } else if (line == 4) {
                                        if (Util.isNumeric(metric)) {
                                            numOfProc = Integer.parseInt(metric);
                                            if (numOfProc > 0) {
                                                cpuLoadPercent = (loadAvg / numOfProc) * 100;
                                                serverOK = true;
                                            } else {
                                                break;
                                            }
                                        } else {
                                            break;
                                        }
                                    }
                                }
                            }
                            if (serverOK) {
                                ServerMetric serverMetric = new ServerMetric(freeHddMb, freeRamMb, (int) cpuLoadPercent, numOfProc);
                                Agent agent = agentManager.getAgentByUUID(out.getKey());
                                if (agent != null) {
                                    serverMetrics.put(agent, serverMetric);
                                }
                            }
                        }

                        synchronized (task) {
                            task.notifyAll();
                        }
                    }
                    return null;
                }
            });
            synchronized (getMetricsTask) {
                try {
                    getMetricsTask.wait(getMetricsTask.getAvgTimeout() * 1000 + 1000);
                } catch (InterruptedException ex) {
                }
            }

            if (!serverMetrics.isEmpty()) {
                Map<String, EnumMap<LxcState, List<String>>> lxcInfo = getLxcOnPhysicalServers();
                for (Iterator<Map.Entry<Agent, ServerMetric>> it = serverMetrics.entrySet().iterator(); it.hasNext();) {
                    Map.Entry<Agent, ServerMetric> entry = it.next();
                    EnumMap<LxcState, List<String>> info = lxcInfo.get(entry.getKey().getHostname());
                    if (info != null) {
                        int numOfExistingLxcs = (info.get(LxcState.RUNNING) != null ? info.get(LxcState.RUNNING).size() : 0)
                                + (info.get(LxcState.STOPPED) != null ? info.get(LxcState.STOPPED).size() : 0)
                                + (info.get(LxcState.FROZEN) != null ? info.get(LxcState.FROZEN).size() : 0);
                        entry.getValue().setNumOfLxcs(numOfExistingLxcs);

                    } else {
                        it.remove();
                    }
                }
            }
        }
        return serverMetrics;
    }

    public Map<Agent, Integer> getPhysicalServersWithLxcSlots() {
        final Map<Agent, Integer> bestServers = new HashMap<Agent, Integer>();
        Map<Agent, ServerMetric> metrics = getPhysicalServerMetrics();

        if (!metrics.isEmpty()) {
            for (Map.Entry<Agent, ServerMetric> entry : metrics.entrySet()) {
                ServerMetric metric = entry.getValue();
                int numOfLxcByLxcLimit = MAX_NUMBER_OF_LXCS_PER_HOST - metric.getNumOfLxcs();
                int numOfLxcByRam = (int) ((metric.getFreeRamMb() - MIN_RAM_IN_RESERVE_MB) / MIN_RAM_LXC_MB);
                int numOfLxcByHdd = (int) ((metric.getFreeHddMb() - MIN_HDD_IN_RESERVE_MB) / MIN_HDD_LXC_MB);
                int numOfLxcByCpu = (int) (((100 - metric.getCpuLoadPercent()) - (MIN_CPU_IN_RESERVE_PERCENT / metric.getNumOfProcessors())) / (MIN_CPU_LXC_PERCENT / metric.getNumOfProcessors()));
                if (numOfLxcByLxcLimit > 0 && numOfLxcByCpu > 0 && numOfLxcByHdd > 0 && numOfLxcByRam > 0) {
                    int minNumOfLxcs = Math.min(Math.min(Math.min(numOfLxcByCpu, numOfLxcByHdd), numOfLxcByRam), numOfLxcByLxcLimit);
                    bestServers.put(entry.getKey(), minNumOfLxcs);
                }
            }
        }
        return bestServers;
    }

    public Map<String, EnumMap<LxcState, List<String>>> getLxcOnPhysicalServers() {
        final Map<String, EnumMap<LxcState, List<String>>> agentFamilies = new HashMap<String, EnumMap<LxcState, List<String>>>();
        Set<Agent> pAgents = agentManager.getPhysicalAgents();
        for (Iterator<Agent> it = pAgents.iterator(); it.hasNext();) {
            Agent agent = it.next();
            if (!agent.getHostname().matches("^py.*")) {
                it.remove();
            }
        }
        if (!pAgents.isEmpty()) {

            Task getLxcListTask = Tasks.getLxcListTask(pAgents);

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
                                } else if (LxcState.FROZEN.name().equalsIgnoreCase(lxcStr)) {
                                    if (lxcs.get(LxcState.FROZEN) == null) {
                                        lxcs.put(LxcState.FROZEN, new ArrayList<String>());
                                    }
                                    currState = LxcState.FROZEN;
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

    public boolean startLxcOnHost(Agent physicalAgent, String lxcHostname) {
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

            return LxcState.RUNNING.equals(getLxcInfoTask.getData());
        }
        return false;
    }

    public boolean stopLxcOnHost(Agent physicalAgent, String lxcHostname) {
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
                            if (stdOut.indexOf("STOPPED") != -1) {
                                task.setData(LxcState.STOPPED);
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

            return LxcState.STOPPED.equals(getLxcInfoTask.getData());
        }
        return false;
    }

    public boolean destroyLxcOnHost(Agent physicalAgent, String lxcHostname) {
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
                            if (task.getTaskStatus() == TaskStatus.SUCCESS) {
                                task.setData(LxcState.STOPPED);
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

            return LxcState.STOPPED.equals(getLxcInfoTask.getData());
        }
        return false;
    }

}
