/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.lxcmanager;

import com.google.common.base.Preconditions;
import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.api.lxcmanager.*;
import org.safehaus.kiskis.mgmt.api.commandrunner.CommandRunner;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.shared.protocol.settings.Common;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.safehaus.kiskis.mgmt.api.commandrunner.AgentResult;
import org.safehaus.kiskis.mgmt.api.commandrunner.Command;
import org.safehaus.kiskis.mgmt.api.monitor.Metric;
import org.safehaus.kiskis.mgmt.api.monitor.Monitor;

/**
 * This is an implementation of LxcManager
 *
 * @author dilshat
 */
public class LxcManagerImpl implements LxcManager {

    private final Pattern p = Pattern.compile("load average: (.*)");
    private final int LXC_AGENT_WAIT_TIMEOUT_SEC = 90;

    private static CommandRunner commandRunner;
    private AgentManager agentManager;
    private ExecutorService executor;
    private Monitor monitor;

    public void init() {
        Preconditions.checkNotNull(agentManager, "Agent manager is null");
        Preconditions.checkNotNull(commandRunner, "Command runner is null");
        Preconditions.checkNotNull(monitor, "Monitor is null");

        executor = Executors.newCachedThreadPool();
    }

    public void destroy() {
        commandRunner = null;
        executor.shutdown();
    }

    public void setMonitor(Monitor monitor) {
        this.monitor = monitor;
    }

    public void setCommandRunner(CommandRunner commandRunner) {
        LxcManagerImpl.commandRunner = commandRunner;
    }

    public static CommandRunner getCommandRunner() {
        return commandRunner;
    }

    public void setAgentManager(AgentManager agentManager) {
        this.agentManager = agentManager;
    }

    /**
     * Returns metrics of all physical servers connected to the management
     * server
     *
     * @return map of metrics where key is a physical agent and value is a
     * metric metric
     */
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

            Command getMetricsCommand = Commands.getMetricsCommand(agents);
            commandRunner.runCommand(getMetricsCommand);

            if (getMetricsCommand.hasCompleted()) {
                for (AgentResult result : getMetricsCommand.getResults().values()) {
                    String[] metrics = result.getStdOut().split("\n");
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
                        //get metrics from elastic search for a one week period
                        Agent agent = agentManager.getAgentByUUID(result.getAgentUUID());
                        if (agent != null) {
                            Calendar cal = Calendar.getInstance();
                            cal.add(Calendar.DATE, -7);
                            Date startDate = cal.getTime();
                            Date endDate = Calendar.getInstance().getTime();
                            Map<Metric, Double> averageMetrics = new EnumMap<Metric, Double>(Metric.class);
                            for (Metric metricKey : Metric.values()) {
                                Map<Date, Double> metricMap = monitor.getData(agent.getHostname(), metricKey, startDate, endDate);
                                if (!metricMap.isEmpty()) {
                                    double avg = 0;
                                    for (Map.Entry<Date, Double> metricEntry : metricMap.entrySet()) {
                                        avg += metricEntry.getValue();
                                    }
                                    avg /= metricMap.size();

                                    averageMetrics.put(metricKey, avg);
                                }
                            }
                            ServerMetric serverMetric = new ServerMetric(freeHddMb, freeRamMb, (int) cpuLoadPercent, numOfProc, averageMetrics);
                            serverMetrics.put(agent, serverMetric);
                        }
                    }
                }
            }

            if (!serverMetrics.isEmpty()) {
                //get number of lxcs currently present on servers
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

    /**
     * Returns number of lxc slots that each currently connected physical server
     * can host. This method uses default lxc placement strategy for
     * calculations
     *
     * @return map where key is a physical server and value is the number of lxc
     * slots
     */
    public Map<Agent, Integer> getPhysicalServersWithLxcSlots() {
        final Map<Agent, Integer> bestServers = new HashMap<Agent, Integer>();
        Map<Agent, ServerMetric> metrics = getPhysicalServerMetrics();

        LxcPlacementStrategy placementStrategy = new DefaultLxcPlacementStrategy(1);
        Map<Agent, Integer> serversSlots = placementStrategy.calculateSlots(metrics);

        if (!serversSlots.isEmpty()) {
            for (Map.Entry<Agent, Integer> serverSlots : serversSlots.entrySet()) {
                bestServers.put(serverSlots.getKey(), serverSlots.getValue());
            }
        }

        return bestServers;
    }

    /**
     * Returns information about what lxc containers each physical servers has
     * at present
     *
     * @return map where key is a hostname of physical server and value is a map
     * where key is state of lxc and value is a list of lxc hostnames
     */
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

            Command getLxcListCommand = Commands.getLxcListCommand(pAgents);
            commandRunner.runCommand(getLxcListCommand);

            if (getLxcListCommand.hasCompleted()) {
                for (AgentResult result : getLxcListCommand.getResults().values()) {
                    Agent agent = agentManager.getAgentByUUID(result.getAgentUUID());

                    String parentHostname = agent == null
                            ? String.format("Offline[%s]", result.getAgentUUID()) : agent.getHostname();
                    EnumMap<LxcState, List<String>> lxcs = new EnumMap<LxcState, List<String>>(LxcState.class);
                    String[] lxcStrs = result.getStdOut().split("\\n");
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
                            lxcs.get(currState).add(lxcStr.trim());
                        }
                    }

                    agentFamilies.put(parentHostname, lxcs);
                }
            }
        }

        return agentFamilies;
    }

    /**
     * Clones lxc on a given physical server and set its hostname
     *
     * @param physicalAgent - physical server
     * @param lxcHostname - hostname to set for a new lxc
     * @return true if all went ok, false otherwise
     */
    public boolean cloneLxcOnHost(Agent physicalAgent, String lxcHostname) {
        if (physicalAgent != null && !Util.isStringEmpty(lxcHostname)) {
            Command cloneLxcCommand = Commands.getCloneCommand(physicalAgent, lxcHostname);
            commandRunner.runCommand(cloneLxcCommand);
            return cloneLxcCommand.hasSucceeded();
        }
        return false;
    }

    /**
     * Starts lxc on a given physical server
     *
     * @param physicalAgent - physical server
     * @param lxcHostname - hostname of lxc
     * @return true if all went ok, false otherwise
     */
    public boolean startLxcOnHost(Agent physicalAgent, String lxcHostname) {
        if (physicalAgent != null && !Util.isStringEmpty(lxcHostname)) {
            Command startLxcCommand = Commands.getLxcStartCommand(physicalAgent, lxcHostname);
            commandRunner.runCommand(startLxcCommand);
            Command lxcInfoCommand = Commands.getLxcInfoCommand(physicalAgent, lxcHostname);
            commandRunner.runCommand(lxcInfoCommand);

            LxcState state = LxcState.UNKNOWN;
            if (lxcInfoCommand.hasCompleted()) {
                AgentResult result = lxcInfoCommand.getResults().entrySet().iterator().next().getValue();
                if (result.getStdOut().contains("RUNNING")) {
                    state = LxcState.RUNNING;
                }

            }

            return LxcState.RUNNING.equals(state);
        }
        return false;
    }

    /**
     * Stops lxc on a given physical server
     *
     * @param physicalAgent - physical server
     * @param lxcHostname - hostname of lxc
     * @return true if all went ok, false otherwise
     */
    public boolean stopLxcOnHost(Agent physicalAgent, String lxcHostname) {
        if (physicalAgent != null && !Util.isStringEmpty(lxcHostname)) {
            Command stopLxcCommand = Commands.getLxcStopCommand(physicalAgent, lxcHostname);
            commandRunner.runCommand(stopLxcCommand);
            Command lxcInfoCommand = Commands.getLxcInfoCommand(physicalAgent, lxcHostname);
            commandRunner.runCommand(lxcInfoCommand);

            LxcState state = LxcState.UNKNOWN;
            if (lxcInfoCommand.hasCompleted()) {
                AgentResult result = lxcInfoCommand.getResults().entrySet().iterator().next().getValue();
                if (result.getStdOut().contains("STOPPED")) {
                    state = LxcState.STOPPED;
                }

            }

            return LxcState.STOPPED.equals(state);
        }
        return false;
    }

    /**
     * Destroys lxc on a given physical server
     *
     * @param physicalAgent - physical server
     * @param lxcHostname - hostname of lxc
     * @return true if all went ok, false otherwise
     */
    public boolean destroyLxcOnHost(Agent physicalAgent, String lxcHostname) {
        if (physicalAgent != null && !Util.isStringEmpty(lxcHostname)) {
            Command destroyLxcCommand = Commands.getLxcDestroyCommand(physicalAgent, lxcHostname);
            commandRunner.runCommand(destroyLxcCommand);

            return destroyLxcCommand.hasCompleted();
        }
        return false;
    }

    /**
     * Creates specified number of lxs and starts them. Uses default placement
     * strategy for calculating location of lxcs on physical servers
     *
     * @param count
     * @return map where key is physical agent and value is a set of lxc agents
     * on it
     */
    public Map<Agent, Set<Agent>> createLxcs(int count) throws LxcCreateException {
        Map<Agent, Set<Agent>> lxcAgents = new HashMap<Agent, Set<Agent>>();

        Map<String, Map<Agent, Set<Agent>>> families = createLxcsByStrategy(new DefaultLxcPlacementStrategy(count));

        for (Map.Entry<String, Map<Agent, Set<Agent>>> family : families.entrySet()) {
            for (Map.Entry<Agent, Set<Agent>> childs : family.getValue().entrySet()) {

                Agent physicalNode = childs.getKey();
                Set<Agent> lxcNodes = childs.getValue();

                Set<Agent> lxcChilds = lxcAgents.get(physicalNode);
                if (lxcChilds == null) {
                    lxcChilds = new HashSet<Agent>();
                    lxcAgents.put(physicalNode, lxcChilds);
                }

                lxcChilds.addAll(lxcNodes);
            }
        }

        return lxcAgents;
    }

    /**
     * Clones and starts lxc on a given physical server, sets hostname of lxc
     *
     * @param physicalAgent - physical server
     * @param lxcHostname - hostname of lxc
     * @return boolean if all went ok, false otherwise
     */
    public boolean cloneNStartLxcOnHost(Agent physicalAgent, String lxcHostname) {
        if (physicalAgent != null && !Util.isStringEmpty(lxcHostname)) {
            Command cloneNStartCommand = Commands.getCloneNStartCommand(physicalAgent, lxcHostname);
            commandRunner.runCommand(cloneNStartCommand);

            LxcState state = LxcState.UNKNOWN;
            if (cloneNStartCommand.hasCompleted()) {
                AgentResult result = cloneNStartCommand.getResults().entrySet().iterator().next().getValue();
                if (result.getStdOut().contains("RUNNING")) {
                    state = LxcState.RUNNING;
                }

            }

            return LxcState.RUNNING.equals(state);
        }
        return false;
    }

    /**
     * Destroys specified lxcs
     *
     * @param lxcHostnames - hostnames of lxc to destroy
     */
    public void destroyLxcs(Set<String> lxcHostnames) throws LxcDestroyException {
        if (lxcHostnames != null && !lxcHostnames.isEmpty()) {
            CompletionService<LxcInfo> completer = new ExecutorCompletionService<LxcInfo>(executor);
            List<LxcInfo> lxcInfos = new ArrayList<LxcInfo>();
            for (String lxcHostname : lxcHostnames) {
                if (!Util.isStringEmpty(lxcHostname) && lxcHostname.matches(".+" + Common.PARENT_CHILD_LXC_SEPARATOR + ".+")) {
                    String parentHostname = lxcHostname.substring(0, lxcHostname.indexOf(Common.PARENT_CHILD_LXC_SEPARATOR));

                    Agent physicalAgent = agentManager.getAgentByHostname(parentHostname);
                    if (physicalAgent != null) {
                        LxcInfo lxcInfo = new LxcInfo(physicalAgent, lxcHostname, null);
                        lxcInfos.add(lxcInfo);
                    } else {
                        throw new LxcDestroyException(String.format("Could node determine parent host of %s container", lxcHostname));
                    }
                } else {
                    throw new LxcDestroyException(String.format("Malformed lxc hostname [%s]", lxcHostname));
                }
            }

            //launch destroyals
            for (LxcInfo lxcInfo : lxcInfos) {
                completer.submit(new LxcActor(lxcInfo, this, LxcAction.DESTROY));
            }

            //wait for completion
            try {
                for (int i = 0; i < lxcInfos.size(); i++) {
                    Future<LxcInfo> future = completer.take();
                    future.get();
                }

            } catch (InterruptedException e) {
            } catch (ExecutionException e) {
            }

            boolean result = true;
            for (LxcInfo lxcInfo : lxcInfos) {
                result &= lxcInfo.isResult();
            }

            if (!result) {
                throw new LxcDestroyException("Not all lxcs destroyed. Use LXC module to cleanup");
            }

        } else {
            throw new LxcDestroyException("Invalid set of lxc hostnames");
        }

    }

    /**
     * Creates lxcs baed on a supplied strategy.
     *
     * @param strategy
     * @return map where key is type of node and values is a map where key is a
     * physical server and value is set of lxcs on it
     * @throws LxcCreateException
     */
    public Map<String, Map<Agent, Set<Agent>>> createLxcsByStrategy(LxcPlacementStrategy strategy) throws LxcCreateException {
        if (strategy == null) {
            throw new LxcCreateException("Lxc placement strategy is null");
        }

        strategy.calculatePlacement(getPhysicalServerMetrics());
        Map<Agent, Map<String, Integer>> placementNodes = strategy.getPlacementInfoMap();

        //check placement info
        if (placementNodes.isEmpty()) {
            throw new LxcCreateException("Lxc placement strategy returned empty set");
        }

        //create lxcs here
        CompletionService<LxcInfo> completer = new ExecutorCompletionService<LxcInfo>(executor);
        Map<String, Map<Agent, Set<Agent>>> families = new HashMap<String, Map<Agent, Set<Agent>>>();
        int count = 0;
        List<LxcInfo> lxcInfos = new ArrayList<LxcInfo>();
        for (Map.Entry<Agent, Map<String, Integer>> placementEntry : placementNodes.entrySet()) {
            Agent physicalNode = placementEntry.getKey();
            for (Map.Entry<String, Integer> lxcEntry : placementEntry.getValue().entrySet()) {
                String nodeType = lxcEntry.getKey();
                Integer numOfLxcs = lxcEntry.getValue();

                //create lxc containers
                for (int i = 0; i < numOfLxcs; i++) {
                    count++;
                    StringBuilder lxcHostname = new StringBuilder(physicalNode.getHostname());
                    lxcHostname.append(Common.PARENT_CHILD_LXC_SEPARATOR);
                    lxcHostname.append(Util.generateTimeBasedUUID().toString());

                    LxcInfo lxcInfo = new LxcInfo(physicalNode, lxcHostname.toString(), nodeType);
                    lxcInfos.add(lxcInfo);
                    completer.submit(new LxcActor(lxcInfo, this, LxcAction.CREATE));

                }

            }
        }

        //wait for completion
        try {
            for (int i = 0; i < count; i++) {
                Future<LxcInfo> future = completer.take();
                future.get();
            }

        } catch (InterruptedException e) {
        } catch (ExecutionException e) {
        }

        boolean result = true;
        for (LxcInfo lxcInfo : lxcInfos) {
            result &= lxcInfo.isResult();
        }

        if (!result) {
            //cleanup lxcs
            Set<String> lxcHostnames = new HashSet<String>();
            for (LxcInfo lxcInfo : lxcInfos) {
                lxcHostnames.add(lxcInfo.getLxcHostname());
            }
            try {
                destroyLxcs(lxcHostnames);
            } catch (LxcDestroyException ex) {
                throw new LxcCreateException("Not all lxcs created successfully. Use LXC module to cleanup");
            }
            throw new LxcCreateException("Not all lxcs created successfully");
        }

        //wait for lxc agents to connect
        long waitStart = System.currentTimeMillis();
        while (!Thread.interrupted()) {
            result = true;
            for (LxcInfo lxcInfo : lxcInfos) {
                Agent lxcAgent = agentManager.getAgentByHostname(lxcInfo.getLxcHostname());
                if (lxcAgent == null) {
                    result = false;
                    break;
                } else {
                    //populate families

                    Map<Agent, Set<Agent>> family = families.get(lxcInfo.getNodeType());
                    if (family == null) {
                        family = new HashMap<Agent, Set<Agent>>();
                        families.put(lxcInfo.getNodeType(), family);
                    }
                    Set<Agent> childs = family.get(lxcInfo.getPhysicalAgent());
                    if (childs == null) {
                        childs = new HashSet<Agent>();
                        family.put(lxcInfo.getPhysicalAgent(), childs);
                    }

                    childs.add(lxcAgent);

                }

            }
            if (result) {
                break;
            } else {
                if (System.currentTimeMillis() - waitStart > LXC_AGENT_WAIT_TIMEOUT_SEC * 1000) {
                    break;
                } else {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        break;
                    }
                }
            }
        }

        if (!result) {
            //cleanup lxcs
            Set<String> lxcHostnames = new HashSet<String>();
            for (LxcInfo lxcInfo : lxcInfos) {
                lxcHostnames.add(lxcInfo.getLxcHostname());
            }
            try {
                destroyLxcs(lxcHostnames);
            } catch (LxcDestroyException ex) {
                throw new LxcCreateException("Waiting interval for lxc agents timed out. Use LXC module to cleanup");
            }
            throw new LxcCreateException("Waiting interval for lxc agents timed out");
        }

        return families;
    }

}
