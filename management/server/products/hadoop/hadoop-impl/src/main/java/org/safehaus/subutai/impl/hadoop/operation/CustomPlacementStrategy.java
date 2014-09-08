package org.safehaus.subutai.impl.hadoop.operation;

import org.safehaus.subutai.core.container.api.lxcmanager.LxcCreateException;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcManager;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcPlacementStrategy;
import org.safehaus.subutai.core.container.api.lxcmanager.ServerMetric;
import org.safehaus.subutai.common.protocol.Agent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

class CustomPlacementStrategy extends LxcPlacementStrategy {

	public static final String MASTER_NODE_TYPE = "master";
	public static final String SLAVE_NODE_TYPE = "slave";
	private static final Logger logger = LoggerFactory.getLogger(CustomPlacementStrategy.class);
	private final Map<String, Integer> nodesCount;
	private float hddPerNodeMb;
	private float hddReservedMb;
	private float ramPerNodeMb;
	private float ramReservedMb;
	private float cpuPerNodePercentage;
	private float cpuReservedPercentage;


	public CustomPlacementStrategy(int masterNodes, int slaveNodes) {
		this.nodesCount = new HashMap<>();
		this.nodesCount.put(MASTER_NODE_TYPE, masterNodes);
		this.nodesCount.put(SLAVE_NODE_TYPE, slaveNodes);
	}


	private static Map<String, Set<Agent>> getFromExistingAgents(LxcManager lxcManager) throws LxcCreateException {

		Set<Agent> agents = lxcManager.getAgentManager().getAgents();

		if (agents.isEmpty() || agents.size() < 5) {
			throw new LxcCreateException("Not enough agents");
		}

		HashSet<Agent> masterNodeAgents = new HashSet<>();
		HashSet<Agent> slaveNodeAgents = new HashSet<>();
		int i = 1;

		for (Agent agent : agents) {
			if (i <= 3) {
				masterNodeAgents.add(agent);
			} else {
				slaveNodeAgents.add(agent);
			}

			i++;
		}

		Map<String, Set<Agent>> agentMap = new HashMap<>();
		agentMap.put(MASTER_NODE_TYPE, masterNodeAgents);
		agentMap.put(SLAVE_NODE_TYPE, slaveNodeAgents);

		return agentMap;
	}


	public static Map<String, Set<Agent>> getNodes(LxcManager lxcManager, int masterNodes, int slaveNodes)
			throws LxcCreateException {

		boolean useRoundRobin = true;
		if (useRoundRobin) return createByRoundRobin(lxcManager, masterNodes, slaveNodes);

		LxcPlacementStrategy strategy = new CustomPlacementStrategy(masterNodes, slaveNodes);
		Map<String, Map<Agent, Set<Agent>>> nodes = lxcManager.createLxcsByStrategy(strategy);

		// Collect nodes by types regardless of parent nodes
		Map<String, Set<Agent>> res = new HashMap<>();

		for (String type : new String[] {MASTER_NODE_TYPE, SLAVE_NODE_TYPE}) {
			Map<Agent, Set<Agent>> map = nodes.get(type);
			if (map == null) {
				throw new LxcCreateException("No nodes for type " + type);
			}

			Set<Agent> all = new HashSet<>();
			for (Set<Agent> children : map.values()) {
				all.addAll(children);
			}

			Set<Agent> set = res.get(type);
			if (set != null)
				set.addAll(all);
			else
				res.put(type, all);
		}

		return res;
	}

	private static Map<String, Set<Agent>> createByRoundRobin(LxcManager lxcManager, int masterNodes, int slaveNodes) throws LxcCreateException {
		Map<String, Set<Agent>> res = new HashMap<>();
		Map<Agent, Set<Agent>> allNodes = lxcManager.createLxcs(masterNodes + slaveNodes);
		Set<Agent> all = new HashSet<>();
		for (Set<Agent> s : allNodes.values()) all.addAll(s);

		// collect master nodes from different physical servers
		Set<Agent> masters = new HashSet<>();
		for (int i = 0; i < masterNodes; i++) {
			Iterator<Agent> it = all.iterator();
			if (it.hasNext()) {
				masters.add(it.next());
				it.remove();
			}
		}
		res.put(MASTER_NODE_TYPE, masters);

		Set<Agent> slaves = new HashSet<>();
		for (int i = 0; i < slaveNodes; i++) {
			Iterator<Agent> it = all.iterator();
			if (it.hasNext()) {
				slaves.add(it.next());
				it.remove();
			}
		}
		res.put(SLAVE_NODE_TYPE, slaves);

		return res;
	}

	@Override
	public Map<Agent, Integer> calculateSlots(Map<Agent, ServerMetric> metrics) {
		try {
			return calculateSlotsInternal(metrics);
		} catch (LxcCreateException ex) {
			return Collections.emptyMap();
		}
	}

	Map<Agent, Integer> calculateSlotsInternal(Map<Agent, ServerMetric> metrics) throws LxcCreateException {
		if (metrics == null || metrics.isEmpty()) return Collections.emptyMap();

		logger.info(" ----- Calculating slots for each server -----");
		Map<Agent, Integer> slots = new HashMap<>();
		for (Map.Entry<Agent, ServerMetric> e : metrics.entrySet()) {
			ServerMetric m = e.getValue();
			int min = Integer.MAX_VALUE;
			logger.info("{}", m);

			int n = Math.round((m.getFreeRamMb() - ramReservedMb) / ramPerNodeMb);
			if ((min = Math.min(n, min)) <= 0)
				throw new LxcCreateException("Placement strategy returned empty due to RAM resources");

			n = Math.round((m.getFreeHddMb() - hddReservedMb) / hddPerNodeMb);
			if ((min = Math.min(n, min)) <= 0)
				throw new LxcCreateException("Placement strategy returned empty due to HDD resources");

			// TODO: check cpu load when cpu load determination is reimplemented
			slots.put(e.getKey(), min);
		}
		return slots;
	}

	@Override
	public void calculatePlacement(Map<Agent, ServerMetric> serverMetrics) throws LxcCreateException {
		for (String type : new String[] {MASTER_NODE_TYPE, SLAVE_NODE_TYPE}) {

			setCriteria(type);
			Map<Agent, Integer> serverSlots = calculateSlotsInternal(serverMetrics);
			if (serverSlots == null || serverSlots.isEmpty()) return;

			int available = 0;
			for (Integer i : serverSlots.values()) available += i.intValue();
			if (available < nodesCount.get(type))
				throw new LxcCreateException(String.format(
						"Placement strategy returned only %d container(s)",
						available));

			calculatePlacement(type, serverSlots);
		}
	}

	public void setCriteria(String type) {
		switch (type) {
			case MASTER_NODE_TYPE:
				hddPerNodeMb = GB2MB(2);
				hddReservedMb = GB2MB(4);
				ramPerNodeMb = GB2MB(1);
				ramReservedMb = GB2MB(1);
				cpuPerNodePercentage = 10;
				cpuReservedPercentage = 20;
				break;
			case SLAVE_NODE_TYPE:
				hddPerNodeMb = GB2MB(2);
				hddReservedMb = GB2MB(4);
				ramPerNodeMb = GB2MB(1);
				ramReservedMb = GB2MB(1);
				cpuPerNodePercentage = 5;
				cpuReservedPercentage = 10;
				break;
			default:
				throw new AssertionError("Invalid node type");

		}
	}

	private void calculatePlacement(String type, Map<Agent, Integer> serverSlots) throws LxcCreateException {
		for (int i = 0; i < nodesCount.get(type); i++) {
			Agent physicalNode = findBestServer(serverSlots);
			if (physicalNode == null) break;

			Integer slotsCount = serverSlots.get(physicalNode);
			serverSlots.put(physicalNode, slotsCount - 1);

			Map<String, Integer> info = getPlacementInfoMap().get(physicalNode);
			int cnt = 1;
			if (info != null && info.get(type) != null)
				cnt = info.get(type).intValue() + 1;
			addPlacementInfo(physicalNode, type, cnt);
		}
	}

	private int GB2MB(float gb) {
		return Math.round(gb * 1024);
	}

	private Agent findBestServer(Map<Agent, Integer> map) {
		int max = 0;
		Agent best = null;
		for (Map.Entry<Agent, Integer> e : map.entrySet()) {
			if (e.getValue().intValue() > max) {
				best = e.getKey();
				max = e.getValue().intValue();
			}
		}
		return best;
	}

}
