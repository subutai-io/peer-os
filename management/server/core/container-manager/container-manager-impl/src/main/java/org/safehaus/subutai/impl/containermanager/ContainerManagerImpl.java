package org.safehaus.subutai.impl.containermanager;


import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.CommandRunner;
import org.safehaus.subutai.api.containermanager.*;
import org.safehaus.subutai.api.dbmanager.DbManager;
import org.safehaus.subutai.api.monitoring.Metric;
import org.safehaus.subutai.api.monitoring.Monitor;
import org.safehaus.subutai.api.template.manager.TemplateManager;
import org.safehaus.subutai.api.templateregistry.Template;
import org.safehaus.subutai.api.templateregistry.TemplateRegistryManager;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.PlacementStrategy;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.impl.containermanager.strategy.DefaultContainerPlacementStrategy;
import org.safehaus.subutai.impl.containermanager.strategy.PlacementStrategyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ContainerManagerImpl extends ContainerManagerBase {

    private static final Logger logger = LoggerFactory.getLogger(ContainerManagerImpl.class);
    private static final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
    private static final long WAIT_BEFORE_CHECK_STATUS_TIMEOUT_MS = 10000;
    private final Pattern loadAveragePattern = Pattern.compile("load average: (.*)");
    // number sequences for template names used for new clone name generation
    private ConcurrentMap<String, AtomicInteger> sequences;
    private ExecutorService executor;
    private Monitor monitor;

    public ContainerManagerImpl(AgentManager agentManager, CommandRunner commandRunner, Monitor monitor, TemplateManager templateManager, TemplateRegistryManager templateRegistry, DbManager dbManager) {
        this.agentManager = agentManager;
        this.commandRunner = commandRunner;
        this.monitor = monitor;
        this.templateManager = templateManager;
        this.templateRegistry = templateRegistry;
        this.dbManager = dbManager;

        Commands.init(commandRunner);
    }

    public void init() {
        Preconditions.checkNotNull(agentManager, "Agent manager is null");
        Preconditions.checkNotNull(commandRunner, "Command runner is null");
        Preconditions.checkNotNull(monitor, "Monitor is null");

        sequences = new ConcurrentHashMap<>();
        executor = Executors.newCachedThreadPool();
    }

    public void destroy() {
        sequences.clear();
        executor.shutdown();
    }

    @Override
    public Set<Agent> clone(UUID envId, String templateName, int nodesCount, Collection<Agent> hosts,
                            PlacementStrategy... strategy) throws ContainerCreateException {

        // restrict metrics to provided hosts only
        Map<Agent, ServerMetric> metrics = getPhysicalServerMetrics();
        if (hosts != null && !hosts.isEmpty()) {
            Iterator<Agent> it = metrics.keySet().iterator();
            while (it.hasNext()) {
                if (!hosts.contains(it.next())) {
                    it.remove();
                }
            }
        }

        ContainerPlacementStrategy st = PlacementStrategyFactory.create(nodesCount, strategy);

        try {
            st.calculatePlacement(metrics);
        } catch (ContainerCreateException e) {
            throw new ContainerCreateException(e.getMessage());
        }
        Map<Agent, Integer> slots = st.getPlacementDistribution();

        int totalSlots = 0;

        for (int slotCount : slots.values()) {
            totalSlots += slotCount;
        }

        if (totalSlots == 0) {
            throw new ContainerCreateException("Lxc placement strategy returned empty set");
        }

        if (totalSlots < nodesCount) {
            throw new ContainerCreateException(String.format("Only %d containers can be created", totalSlots));
        }


        // clone specified number of instances and store their names
        Map<String, Set<String>> cloneNames = new HashMap<>();

        Set<String> existingContainerNames = getContainerNames(hosts);

        List<ContainerInfo> lxcInfos = new ArrayList<>();
        for (Map.Entry<Agent, Integer> e : slots.entrySet()) {
            Set<String> hostCloneNames = new HashSet<>();
            for (int i = 0; i < e.getValue(); i++) {
                String newContainerName = nextHostName(templateName, existingContainerNames);
                hostCloneNames.add(newContainerName);
            }
            cloneNames.put(e.getKey().getHostname(), hostCloneNames);
            ContainerInfo lxcInfo = new ContainerInfo(e.getKey(), hostCloneNames);
            lxcInfos.add(lxcInfo);
        }


        if (!lxcInfos.isEmpty()) {

            CompletionService<ContainerInfo> completer = new ExecutorCompletionService<>(executor);
            //launch create commands
            for (ContainerInfo lxcInfo : lxcInfos) {
                completer.submit(new ContainerActor(lxcInfo, this, ContainerAction.CREATE, templateName));
            }

            //wait for completion
            try {
                for (ContainerInfo ignored : lxcInfos) {
                    Future<ContainerInfo> future = completer.take();
                    future.get();
                }
            } catch (InterruptedException | ExecutionException ignore) {
            }

            boolean result = true;
            for (ContainerInfo lxcInfo : lxcInfos) {
                result &= lxcInfo.isResult();
            }

            if (!result) {
                throw new ContainerCreateException(
                        String.format("Not all lxcs created. Use LXC module to cleanup %s", cloneNames));
            }
        } else {
            throw new ContainerCreateException("Empty containermanager infos provided");
        }


        boolean result = true;
        long waitStart = System.currentTimeMillis();
        Set<Agent> clones = new HashSet<>();
        while (!Thread.interrupted()) {
            result = true;
            outerloop:
            for (Set<String> names : cloneNames.values()) {
                for (String cloneName : names) {
                    Agent lxcAgent = agentManager.getAgentByHostname(cloneName);
                    if (lxcAgent == null) {
                        result = false;
                        break outerloop;
                    } else {
                        clones.add(lxcAgent);
                    }
                }
            }
            if (result) {
                break;
            } else {
                if (System.currentTimeMillis() - waitStart > Common.LXC_AGENT_WAIT_TIMEOUT_SEC * 1000) {
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

            //destroy clones

            Set<String> names = new HashSet<>();

            for (String key : cloneNames.keySet()) {
                names.addAll(cloneNames.get(key));
            }
            try {
                clonesDestroyByHostname(names);
            } catch (ContainerDestroyException ignore) {
            }

            throw new ContainerCreateException(
                    String.format("Waiting interval for lxc agents timed out. Use LXC module to cleanup nodes %s",
                            cloneNames));
        }

        try {
            if (envId != null) {
                saveNodeGroup(envId, templateName, clones, strategy);
            }
        } catch (Exception ex) {
            logger.error("Failed to save nodes info", ex);
        }

        return clones;
    }


    public Set<Agent> clone(String templateName, int nodesCount, Collection<Agent> hosts,
                            PlacementStrategy... strategy) throws ContainerCreateException {
        return clone(null, templateName, nodesCount, hosts, strategy);
    }

    public void cloneDestroy(final String hostName, final String cloneName) throws ContainerDestroyException {
        boolean result = templateManager.cloneDestroy(hostName, cloneName);
        if (!result) {
            throw new ContainerDestroyException(String.format("Error destroying containermanager %s", cloneName));
        }
    }

    /**
     * Returns information about what lxc containers each physical servers has at present
     *
     * @return map where key is a hostname of physical server and value is a map where key is state of lxc and value is
     * a list of lxc hostnames
     */
    public Map<String, EnumMap<ContainerState, List<String>>> getLxcOnPhysicalServers() {
        final Map<String, EnumMap<ContainerState, List<String>>> agentFamilies = new HashMap<>();
        Set<Agent> pAgents = agentManager.getPhysicalAgents();
        for (Iterator<Agent> it = pAgents.iterator(); it.hasNext(); ) {
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

                    String parentHostname =
                            agent == null ? String.format("Offline[%s]", result.getAgentUUID()) : agent.getHostname();
                    EnumMap<ContainerState, List<String>> lxcs = new EnumMap<>(ContainerState.class);
                    String[] lxcStrs = result.getStdOut().split("\\n");

                    for (int i = 2; i < lxcStrs.length; i++) {
                        String[] lxcProperties = lxcStrs[i].split("\\s+");
                        if (lxcProperties.length > 1) {
                            String lxcHostname = lxcProperties[0];
                            if (!(Common.BASE_CONTAINER_NAME.equalsIgnoreCase(lxcHostname)
                                    || Common.MASTER_TEMPLATE_NAME.equalsIgnoreCase(lxcHostname))) {
                                String lxcStatus = lxcProperties[1];

                                ContainerState state = ContainerState.parseState(lxcStatus);

                                if (lxcs.get(state) == null) {
                                    lxcs.put(state, new ArrayList<String>());
                                }
                                lxcs.get(state).add(lxcHostname);
                            }
                        }
                    }

                    agentFamilies.put(parentHostname, lxcs);
                }
            }
        }

        return agentFamilies;
    }

    /**
     * Returns metrics of all physical servers connected to the management server
     *
     * @return map of metrics where key is a physical agent and value is a metric
     */
    public Map<Agent, ServerMetric> getPhysicalServerMetrics() {
        final Map<Agent, ServerMetric> serverMetrics = new HashMap<>();
        Set<Agent> agents = agentManager.getPhysicalAgents();
        //omit management server
        for (Iterator<Agent> it = agents.iterator(); it.hasNext(); ) {
            Agent agent = it.next();
            if (!agent.getHostname().matches("^py.*")) {
                it.remove();
            }
        }

        if (agents.isEmpty())
            return serverMetrics;

        Command getMetricsCommand = Commands.getMetricsCommand(agents);
        commandRunner.runCommand(getMetricsCommand);

        if (getMetricsCommand.hasCompleted()) {
            for (AgentResult result : getMetricsCommand.getResults().values()) {
                String[] metrics = result.getStdOut().split("\n");
                org.safehaus.subutai.api.containermanager.ServerMetric serverMetric = gatherMetrics(metrics);
                if (serverMetric != null) {
                    Agent agent = agentManager.getAgentByUUID(result.getAgentUUID());
                    Map<Metric, Double> averageMetrics = gatherAvgMetrics(agent);
                    serverMetric.setAverageMetrics(averageMetrics);
                    serverMetrics.put(agent, serverMetric);
                }
            }
        }

        if (!serverMetrics.isEmpty()) {
            //get number of lxcs currently present on servers
            Map<String, EnumMap<ContainerState, List<String>>> lxcInfo = getLxcOnPhysicalServers();
            for (Iterator<Map.Entry<Agent, org.safehaus.subutai.api.containermanager.ServerMetric>> it = serverMetrics.entrySet().iterator();
                 it.hasNext(); ) {
                Map.Entry<Agent, org.safehaus.subutai.api.containermanager.ServerMetric> entry = it.next();
                EnumMap<ContainerState, List<String>> info = lxcInfo.get(entry.getKey().getHostname());
                if (info != null) {
                    int numOfExistingLxcs =
                            (info.get(ContainerState.RUNNING) != null ? info.get(ContainerState.RUNNING).size() : 0) + (
                                    info.get(ContainerState.STOPPED) != null ? info.get(ContainerState.STOPPED).size() : 0)
                                    + (info.get(ContainerState.FROZEN) != null ? info.get(ContainerState.FROZEN).size() :
                                    0);
                    entry.getValue().setNumOfLxcs(numOfExistingLxcs);
                } else {
                    it.remove();
                }
            }
        }
        return serverMetrics;
    }

    /**
     * Gather metrics from elastic search for a one week period
     *
     * @param agent
     * @return
     */
    private Map<Metric, Double> gatherAvgMetrics(Agent agent) {

        if (agent == null)
            return null;
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -7);
        Date startDate = cal.getTime();
        Date endDate = Calendar.getInstance().getTime();
        Map<Metric, Double> averageMetrics = new EnumMap<>(Metric.class);
        for (Metric metricKey : Metric.values()) {
            Map<Date, Double> metricMap =
                    monitor.getData(agent.getHostname(), metricKey, startDate, endDate);
            if (!metricMap.isEmpty()) {
                double avg = 0;
                for (Map.Entry<Date, Double> metricEntry : metricMap.entrySet()) {
                    avg += metricEntry.getValue();
                }
                avg /= metricMap.size();

                averageMetrics.put(metricKey, avg);
            }
        }
        return averageMetrics;
    }

    /**
     * Gather metrics from linux commands outputs.
     *
     * @param metrics
     * @return
     */
    private ServerMetric gatherMetrics(String[] metrics) {
        int freeRamMb = 0;
        int freeHddMb = 0;
        int numOfProc = 0;
        double loadAvg = 0;
        double cpuLoadPercent = 100;
        // parsing only 4 metrics
        if (metrics.length != 4)
            return null;
        boolean parseOk = true;
        for (int line = 0; parseOk && line < metrics.length; line++) {
            String metric = metrics[line];
            switch (line) {
                case 0:
                    //-/+ buffers/cache:       1829       5810
                    String[] ramMetric = metric.split("\\s+");
                    String freeRamMbStr = ramMetric[ramMetric.length - 1];
                    try {
                        freeRamMb = Integer.parseInt(freeRamMbStr);
                    } catch (Exception e) {
                        parseOk = false;
                    }
                    break;
                case 1:
                    //lxc-data       143264768 608768 142656000   1% /lxc-data
                    String[] hddMetric = metric.split("\\s+");
                    if (hddMetric.length == 6) {
                        String hddMetricKbStr = hddMetric[3];
                        try {
                            freeHddMb = Integer.parseInt(hddMetricKbStr) / 1024;
                        } catch (Exception e) {
                            parseOk = false;
                        }
                    } else {
                        parseOk = false;
                    }
                    break;
                case 2:
                    // 09:17:38 up 4 days, 23:06,  0 users,  load average: 2.18, 3.06, 2.12
                    Matcher m = loadAveragePattern.matcher(metric);
                    if (m.find()) {
                        String[] loads = m.group(1).split(",");
                        try {
                            loadAvg = (Double.parseDouble(loads[0]) + Double.parseDouble(loads[1])
                                    + Double.parseDouble(loads[2])) / 3;
                        } catch (Exception e) {
                            parseOk = false;
                        }
                    } else {
                        parseOk = false;
                    }
                    break;
                case 3:
                    try {
                        numOfProc = Integer.parseInt(metric);
                        if (numOfProc > 0) {
                            cpuLoadPercent = (loadAvg / numOfProc) * 100;
                        } else {
                            break;
                        }
                    } catch (Exception e) {
                        parseOk = false;
                    }
                    break;
            }
        }
        if (parseOk) {
            ServerMetric serverMetric =
                    new org.safehaus.subutai.api.containermanager.ServerMetric(freeHddMb, freeRamMb, (int) cpuLoadPercent, numOfProc,
                            null);
            return serverMetric;
        } else
            return null;
    }

    @Override
    public void clonesDestroy(final String hostName, final Set<String> cloneNames) throws ContainerDestroyException {
        boolean result = templateManager.cloneDestroy(hostName, cloneNames);
        if (!result) {
            throw new ContainerDestroyException(
                    String.format("Not all containers from %s are destroyed. Use LXC module to cleanup",
                            cloneNames));
        }

    }

    @Override
    public void clonesCreate(final String hostName, final String templateName, final Set<String> cloneNames)
            throws ContainerCreateException {
        boolean result = templateManager.clone(hostName, templateName, cloneNames);
        if (!result) {
            throw new ContainerCreateException(
                    String.format("Not all containers from %s : %s are created. Use LXC module to cleanup", hostName,
                            cloneNames));
        }
    }


    //    @Override
    public void clonesDestroyByHostname(final Set<String> cloneNames) throws ContainerDestroyException {
        if (cloneNames == null || cloneNames.isEmpty()) {
            throw new ContainerDestroyException("Clone names is empty or null");
        }

        Set<Agent> lxcAgents = new HashSet<>();
        for (String lxcHostname : cloneNames) {
            if (lxcHostname != null) {
                Agent lxcAgent = agentManager.getAgentByHostname(lxcHostname);
                if (lxcAgent == null) {
                    throw new ContainerDestroyException(String.format("Lxc %s is not connected", lxcHostname));
                }
                lxcAgents.add(lxcAgent);
            }
        }

        clonesDestroy(lxcAgents);
    }

    public void clonesDestroy(Set<Agent> lxcAgents) throws ContainerDestroyException {
        if (lxcAgents == null || lxcAgents.isEmpty()) {
            throw new ContainerDestroyException("LxcAgents is null or empty");
        }

        Map<Agent, Set<Agent>> families = new HashMap<>();
        for (Agent lxcAgent : lxcAgents) {
            if (lxcAgent != null) {
                Agent parentAgent = agentManager.getAgentByHostname(lxcAgent.getParentHostName());
                if (parentAgent == null) {
                    throw new ContainerDestroyException(
                            String.format("Physical parent of %s is not connected", lxcAgent.getHostname()));
                }
                Set<Agent> lxcChildren = families.get(parentAgent);
                if (lxcChildren == null) {
                    lxcChildren = new HashSet<>();
                    families.put(parentAgent, lxcChildren);
                }
                lxcChildren.add(lxcAgent);
            }
        }

        cloneDestroy(families);
    }


    private void cloneDestroy(Map<Agent, Set<Agent>> agentFamilies) throws ContainerDestroyException {
        Map<Agent, Set<String>> families = new HashMap<>();

        for (Map.Entry<Agent, Set<Agent>> entry : agentFamilies.entrySet()) {
            Agent physicalAgent = entry.getKey();
            if (physicalAgent != null) {
                Set<Agent> lxcChildren = entry.getValue();
                Set<String> lxcHostnames = families.get(physicalAgent);
                if (lxcHostnames == null) {
                    lxcHostnames = new HashSet<>();
                    families.put(physicalAgent, lxcHostnames);
                }

                for (Agent lxcAgent : lxcChildren) {
                    if (lxcAgent != null) {
                        lxcHostnames.add(lxcAgent.getHostname());
                    }
                }
            }
        }

        clonesDestroy(families);
    }


    private void clonesDestroy(Map<Agent, Set<String>> agentFamilies) throws ContainerDestroyException {
        if (agentFamilies == null || agentFamilies.isEmpty()) {
            throw new ContainerDestroyException("Agent Families is null or empty");
        }

        List<ContainerInfo> lxcInfos = new ArrayList<>();
        for (Map.Entry<Agent, Set<String>> family : agentFamilies.entrySet()) {
            Agent physicalAgent = family.getKey();
            if (physicalAgent != null) {

                ContainerInfo lxcInfo = new ContainerInfo(physicalAgent, family.getValue());
                lxcInfos.add(lxcInfo);
            }
        }

        if (!lxcInfos.isEmpty()) {

            CompletionService<ContainerInfo> completer = new ExecutorCompletionService<>(executor);
            //launch destroy commands
            for (ContainerInfo lxcInfo : lxcInfos) {
                completer.submit(new ContainerActor(lxcInfo, this, ContainerAction.DESTROY));
            }

            //wait for completion
            try {
                for (ContainerInfo ignored : lxcInfos) {
                    Future<ContainerInfo> future = completer.take();
                    future.get();
                }
            } catch (InterruptedException | ExecutionException ignore) {
            }

            boolean result = true;
            for (ContainerInfo lxcInfo : lxcInfos) {
                result &= lxcInfo.isResult();
            }

            if (!result) {
                throw new ContainerDestroyException("Not all lxcs destroyed. Use LXC module to cleanup");
            }
        } else {
            throw new ContainerDestroyException("Empty child lxcs provided");
        }
    }


    private String nextHostName(String templateName, Set<String> existingNames) {
        AtomicInteger i = sequences.putIfAbsent(templateName, new AtomicInteger());
        if (i == null) {
            i = sequences.get(templateName);
        }
        while (true) {
            String name = templateName + i.incrementAndGet();
            if (!existingNames.contains(name)) {
                return name;
            }
        }
    }


    private Set<String> getContainerNames(Collection<Agent> hostsToCheck) {
        Map<String, EnumMap<ContainerState, List<String>>> map = getLxcOnPhysicalServers();

        if (hostsToCheck != null && !hostsToCheck.isEmpty()) {
            Iterator<String> it = map.keySet().iterator();
            while (it.hasNext()) {
                String hostname = it.next();
                boolean hostIncluded = false;
                for (Agent agent : hostsToCheck) {
                    if (agent.getHostname().equalsIgnoreCase(hostname)) {
                        hostIncluded = true;
                        break;
                    }
                }
                if (!hostIncluded) {
                    it.remove();
                }
            }
        }

        Set<String> lxcHostNames = new HashSet<>();
        for (EnumMap<ContainerState, List<String>> lxcsOnOneHost : map.values()) {
            for (List<String> hosts : lxcsOnOneHost.values()) {
                lxcHostNames.addAll(hosts);
            }
        }
        return lxcHostNames;
    }


    private void saveNodeGroup(UUID envId, String templateName, Set<Agent> agents, PlacementStrategy... strategy) {

        String cql = "INSERT INTO nodes(uuid, env_id, info) VALUES(?, ?, ?)";

        NodeInfo group = new NodeInfo();
        group.setEnvId(envId);
        group.setTemplateName(templateName);
        if (strategy == null || strategy.length == 0) {
            strategy = new PlacementStrategy[]{
                    PlacementStrategyFactory.getDefaultStrategyType()
            };
        }
        group.setStrategy(EnumSet.of(strategy[0], strategy));
        Template template = templateRegistry.getTemplate(templateName);
        group.setProducts(template.getProducts());
        for (Agent a : agents) {
            group.setInstanceId(a.getUuid());
            dbManager.executeUpdate(cql, a.getUuid().toString(), envId.toString(), gson.toJson(group));
        }
    }

    /**
     * Returns number of lxc slots that each currently connected physical server can host. This method uses default lxc
     * placement strategy for calculations
     *
     * @return map where key is a physical server and value is the number of lxc slots
     */
    public Map<Agent, Integer> getPhysicalServersWithLxcSlots() {
        final Map<Agent, Integer> bestServers = new HashMap<>();
        Map<Agent, ServerMetric> metrics = getPhysicalServerMetrics();

        ContainerPlacementStrategy placementStrategy = new DefaultContainerPlacementStrategy(1);
        Map<Agent, Integer> serversSlots = placementStrategy.calculateSlots(metrics);
        // set all values to some equal amount so that RR strategy works from UI
        // TODO: remove this! This is for showing round-robin from UI
        int max = Integer.MIN_VALUE;
        for (Integer i : serversSlots.values()) {
            if (i > max) {
                max = i;
            }
        }
        max = 10;

        if (!serversSlots.isEmpty()) {
            for (Map.Entry<Agent, Integer> serverSlots : serversSlots.entrySet()) {
                bestServers.put(serverSlots.getKey(), max);
            }
        }

        return bestServers;
    }


    /**
     * Clones lxc on a given physical server and set its hostname
     *
     * @param physicalAgent - physical server
     * @param lxcHostname   - hostname to set for a new lxc
     * @return true if all went ok, false otherwise
     */
    public boolean cloneLxcOnHost(Agent physicalAgent, String lxcHostname) {
//        if ( physicalAgent != null && !Strings.isNullOrEmpty(lxcHostname) ) {
//            Command cloneLxcCommand = Commands.getCloneCommand(physicalAgent, lxcHostname);
//            commandRunner.runCommand( cloneLxcCommand );
//            return cloneLxcCommand.hasSucceeded();
//        }
        return false;
    }

    /**
     * Returns information about what lxc containers each physical servers has at present
     *
     * @return map where key is a hostname of physical server and value is a map where key is state of lxc and value is
     * a list of lxc hostnames
     */
    public Map<String, EnumMap<ContainerState, List<String>>> getContainersOnPhysicalServers() {
        final Map<String, EnumMap<ContainerState, List<String>>> agentFamilies = new HashMap<>();
        Set<Agent> pAgents = agentManager.getPhysicalAgents();
        for (Iterator<Agent> it = pAgents.iterator(); it.hasNext(); ) {
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

                    String parentHostname =
                            agent == null ? String.format("Offline[%s]", result.getAgentUUID()) : agent.getHostname();
                    EnumMap<ContainerState, List<String>> lxcs = new EnumMap<>(ContainerState.class);
                    String[] lxcStrs = result.getStdOut().split("\\n");

                    for (int i = 2; i < lxcStrs.length; i++) {
                        String[] lxcProperties = lxcStrs[i].split("\\s+");
                        if (lxcProperties.length > 1) {
                            String lxcHostname = lxcProperties[0];
                            if (!(Common.BASE_CONTAINER_NAME.equalsIgnoreCase(lxcHostname)
                                    || Common.MASTER_TEMPLATE_NAME.equalsIgnoreCase(lxcHostname))) {
                                String lxcStatus = lxcProperties[1];

                                ContainerState state = ContainerState.parseState(lxcStatus);

                                if (lxcs.get(state) == null) {
                                    lxcs.put(state, new ArrayList<String>());
                                }
                                lxcs.get(state).add(lxcHostname);
                            }
                        }
                    }
                    agentFamilies.put(parentHostname, lxcs);
                }
            }
        }

        return agentFamilies;
    }

    @Override
    public void clone(String hostName, String templateName, String cloneName) throws ContainerCreateException {
        if (!templateManager.clone(hostName, templateName, cloneName)) {
            throw new ContainerCreateException(
                    String.format("Couldn't create container %s : %s.", hostName,
                            cloneName));
        }
        else {
            long thresholdTime = System.currentTimeMillis() + Common.LXC_AGENT_WAIT_TIMEOUT_SEC*1000;
            Agent agent = agentManager.getAgentByHostname(cloneName);
            while (agent == null && thresholdTime > System.currentTimeMillis()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignore) {
                }
                agent = agentManager.getAgentByHostname(cloneName);
            }
            if (agent == null)
                throw new ContainerCreateException(
                        String.format("Couldn't create container %s : %s.", hostName,
                                cloneName));
        }
    }

    /**
     * Starts lxc on a given physical server
     *
     * @param physicalAgent - physical server
     * @param lxcHostname - hostname of lxc
     *
     * @return true if all went ok, false otherwise
     */
    public boolean startLxcOnHost( Agent physicalAgent, String lxcHostname ) {
        if ( physicalAgent != null && !Strings.isNullOrEmpty(lxcHostname) ) {
            Command startLxcCommand = Commands.getLxcStartCommand( physicalAgent, lxcHostname );
            commandRunner.runCommand( startLxcCommand );
            try {
                Thread.sleep( WAIT_BEFORE_CHECK_STATUS_TIMEOUT_MS );
            }
            catch ( InterruptedException ignore ) {
            }
            Command lxcInfoCommand = Commands.getLxcInfoCommand( physicalAgent, lxcHostname );
            commandRunner.runCommand( lxcInfoCommand );

            ContainerState state = ContainerState.UNKNOWN;
            if ( lxcInfoCommand.hasCompleted() ) {
                AgentResult result = lxcInfoCommand.getResults().entrySet().iterator().next().getValue();
                if ( result.getStdOut().contains( "RUNNING" ) ) {
                    state = ContainerState.RUNNING;
                }
            }

            return ContainerState.RUNNING.equals( state );
        }
        return false;
    }


    /**
     * Stops lxc on a given physical server
     *
     * @param physicalAgent - physical server
     * @param lxcHostname - hostname of lxc
     *
     * @return true if all went ok, false otherwise
     */
    public boolean stopLxcOnHost( Agent physicalAgent, String lxcHostname ) {
        if ( physicalAgent != null && !Strings.isNullOrEmpty( lxcHostname ) ) {
            Command stopLxcCommand = Commands.getLxcStopCommand( physicalAgent, lxcHostname );
            commandRunner.runCommand( stopLxcCommand );
            try {
                Thread.sleep( WAIT_BEFORE_CHECK_STATUS_TIMEOUT_MS );
            }
            catch ( InterruptedException ignore ) {
            }
            Command lxcInfoCommand = Commands.getLxcInfoCommand( physicalAgent, lxcHostname );
            commandRunner.runCommand( lxcInfoCommand );

            ContainerState state = ContainerState.UNKNOWN;
            if ( lxcInfoCommand.hasCompleted() ) {
                AgentResult result = lxcInfoCommand.getResults().entrySet().iterator().next().getValue();
                if ( result.getStdOut().contains( "STOPPED" ) ) {
                    state = ContainerState.STOPPED;
                }
            }

            return ContainerState.STOPPED.equals( state );
        }
        return false;
    }

    @Override
    public void destroy(String hostName, String cloneName) throws ContainerDestroyException {
        if(!templateManager.cloneDestroy(hostName, cloneName))
            throw new ContainerDestroyException(String.format("Could not destroy container %s : %s.", hostName, cloneName));
    }
}
