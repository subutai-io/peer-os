package org.safehaus.subutai.impl.container;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.RequestBuilder;
import org.safehaus.subutai.api.container.ContainerManager;
import org.safehaus.subutai.api.lxcmanager.*;
import org.safehaus.subutai.api.manager.helper.*;
import org.safehaus.subutai.impl.strategy.PlacementStrategyFactory;
import org.safehaus.subutai.shared.protocol.Agent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContainerManagerImpl extends ContainerManagerBase {

    private static final Logger logger = LoggerFactory.getLogger(ContainerManager.class);
    private static final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
    // number sequences for template names used for new clone name generation
    private ConcurrentMap<String, AtomicInteger> sequences;

    public void init() {
        sequences = new ConcurrentHashMap<>();
    }

    public void destroy() {
        sequences.clear();
    }

    @Override
    public Set<Agent> clone(int envId, String templateName, int nodesCount, Collection<Agent> hosts, PlacementStrategyENUM... strategy) {

        // restrict metrics to provided hosts only
        Map<Agent, ServerMetric> metrics = lxcManager.getPhysicalServerMetrics();
        Iterator<Agent> it = metrics.keySet().iterator();
        while(it.hasNext()) {
            if(!hosts.contains(it.next())) it.remove();
        }

        LxcPlacementStrategy st = PlacementStrategyFactory.create(nodesCount, strategy);
        try {
            st.calculatePlacement(metrics);
        } catch(LxcCreateException ex) {
            logger.error("Failed to calculate placement", ex);
            return Collections.emptySet();
        }
        Map<Agent, Integer> slots = st.getPlacementDistribution();

        // clone specified number of instances and store their names
        List<String> cloneNames = new ArrayList<>();
        for(Map.Entry<Agent, Integer> e : slots.entrySet()) {
            Set<String> existingNames = getContainerNames(e.getKey().getHostname());
            for(int i = 0; i < e.getValue(); i++) {
                String name = nextHostName(templateName, existingNames);
                boolean b = templateManager.clone(e.getKey().getHostname(),
                        templateName, name);
                if(b) cloneNames.add(name);
            }
        }
        // get agents by names
        Set<Agent> clones = new HashSet<>();
        for(String cloneName : cloneNames) {
            Agent a = agentManager.getAgentByHostname(cloneName);
            if(a != null) clones.add(a);
        }
        try {
            saveNodeGroup(envId, templateName, clones, strategy);
        } catch(Exception ex) {
            logger.error("Failed to save nodes info", ex);
        }

        return clones;
    }

    @Override
    public boolean attachAndExecute(Agent physicalHost, String cloneName, String cmd) {
        return attachAndExecute(physicalHost, cloneName, cmd, 30, TimeUnit.SECONDS);
    }

    @Override
    public boolean attachAndExecute(Agent physicalHost, String cloneName, String cmd, long t, TimeUnit unit) {
        if(cmd == null || cmd.isEmpty()) return false;
        // synopsis:
        // lxc-attach {-n name} [-a arch] [-e] [-s namespaces] [-R] [--keep-env] [--clear-env] [-- command]
        StringBuilder sb = new StringBuilder("lxc-attach -n ");
        sb.append(cloneName).append(" -- ").append(cmd);

        int timeout = (int)unit.toSeconds(t);
        Command comm = commandRunner.createCommand(
                new RequestBuilder(sb.toString()).withTimeout(timeout),
                new HashSet<>(Arrays.asList(physicalHost)));
        commandRunner.runCommand(comm);
        return comm.hasSucceeded();
    }

    private String nextHostName(String templateName, Set<String> existingNames) {
        AtomicInteger i = sequences.putIfAbsent(templateName, new AtomicInteger());
        if(i == null) i = sequences.get(templateName);
        while(true) {
            String name = templateName + "-" + i.incrementAndGet();
            if(!existingNames.contains(name)) return name;
        }
    }

    private Set<String> getContainerNames(String hostname) {
        Map<String, EnumMap<LxcState, List<String>>> map = lxcManager.getLxcOnPhysicalServers();
        EnumMap<LxcState, List<String>> lxcs = map.get(hostname);
        if(lxcs == null) return Collections.emptySet();

        Set<String> res = new HashSet<>();
        for(List<String> ls : lxcs.values()) res.addAll(ls);
        return res;
    }

    private void saveNodeGroup(int envId, String templateName, Set<Agent> agents,
            PlacementStrategyENUM... strategy) {

        String cql = "INSERT INTO nodes(uuid, env_id, info) VALUES(?, ?, ?)";

        NodeInfo group = new NodeInfo();
        group.setEnvId(envId);
        group.setTemplateName(templateName);
        if(strategy == null || strategy.length == 0)
            strategy = new PlacementStrategyENUM[]{
                PlacementStrategyFactory.getDefaultStrategyType()
            };
        group.setStrategy(EnumSet.of(strategy[0], strategy));
        for(Agent a : agents) {
            group.setInstanceId(a.getUuid());
            dbManager.executeUpdate(cql, a.getUuid().toString(), envId, gson.toJson(group));
        }
    }

}
