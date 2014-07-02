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
    public Set<Agent> clone(String groupName, Collection<String> hostNames,
            String templateName, int nodesCount,
            PlacementStrategyENUM... strategy) {

        LxcPlacementStrategy st = PlacementStrategyFactory.create(nodesCount, strategy);
        try {
            st.calculatePlacement(lxcManager.getPhysicalServerMetrics());
        } catch(LxcCreateException ex) {
            logger.error("Failed to calculate placement", ex);
            return Collections.emptySet();
        }
        Map<Agent, Integer> slots = st.getPlacementDistribution();

        List<String> cloneNames = new ArrayList<>();
        for(Map.Entry<Agent, Integer> e : slots.entrySet()) {
            for(int i = 0; i < e.getValue(); i++) {
                String name = nextHostName(templateName);
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
        boolean saved = saveNodeGroup(groupName, templateName, clones, strategy);
        if(!saved) logger.error("Failed to save node group info");

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

    private String nextHostName(String templateName) {
        AtomicInteger i = sequences.putIfAbsent(templateName, new AtomicInteger());
        if(i == null) i = sequences.get(templateName);
        while(true) {
            String name = templateName + "-" + i.incrementAndGet();
            Agent a = agentManager.getAgentByHostname(name);
            if(a == null) return name;
        }
    }

    private boolean saveNodeGroup(String name, String templateName, Set<Agent> agents,
            PlacementStrategyENUM... strategy) {

        String cql = "INSERT INTO node_group(name, info) VALUES(?, ?)";
        EnvironmentNodeGroup group = new EnvironmentNodeGroup();
        group.setTemplateUsed(templateName);

        Set<EnvironmentGroupInstance> instances = new HashSet<>();
        for(Agent a : agents) {
            EnvironmentGroupInstance gi = new EnvironmentGroupInstance();
            gi.setAgent(a);
            gi.setName(a.getHostname());
            gi.setPlacementStrategyENUM(strategy[0]); // TODO: first value used
            instances.add(gi);
        }
        group.setEnvironmentGroupInstanceSet(instances);

        return dbManager.executeUpdate(cql, name, gson.toJson(group));
    }

}
