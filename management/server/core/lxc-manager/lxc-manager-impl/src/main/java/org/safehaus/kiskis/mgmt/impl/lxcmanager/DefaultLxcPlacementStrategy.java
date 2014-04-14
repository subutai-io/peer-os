/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.lxcmanager;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.safehaus.kiskis.mgmt.api.lxcmanager.LxcCreateException;
import org.safehaus.kiskis.mgmt.api.lxcmanager.LxcPlacementStrategy;
import org.safehaus.kiskis.mgmt.api.lxcmanager.ServerMetric;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;

/**
 * @author dilshat
 */
public class DefaultLxcPlacementStrategy extends LxcPlacementStrategy {

    private final Pattern p = Pattern.compile("load average: (.*)");
    private final double MIN_HDD_LXC_MB = 15 * 1024;         // 15G
    private final double MIN_HDD_IN_RESERVE_MB = 100 * 1024; // 100G
    private final double MIN_RAM_LXC_MB = 1 * 1024;          // 1G
    private final double MIN_RAM_IN_RESERVE_MB = 2 * 1024;   // 2G
    private final double MIN_CPU_LXC_PERCENT = 15;           // 15%
    private final double MIN_CPU_IN_RESERVE_PERCENT = 30;    // 30%
    //    private final int MAX_NUMBER_OF_LXCS_PER_HOST = 5;       // 5
    private final int numOfNodes;
    private final String defaultNodeType = "default";

    public DefaultLxcPlacementStrategy(int numOfNodes) {
        this.numOfNodes = numOfNodes;
    }

    @Override
    public void calculatePlacement(Map<Agent, ServerMetric> serverMetrics) throws LxcCreateException {
        //implement default simple lxc placement strategy here

        if (serverMetrics != null && !serverMetrics.isEmpty()) {
            Map<Agent, Integer> bestServers = new HashMap<Agent, Integer>();
            for (Map.Entry<Agent, ServerMetric> entry : serverMetrics.entrySet()) {
                ServerMetric metric = entry.getValue();
//                int numOfLxcByLxcLimit = MAX_NUMBER_OF_LXCS_PER_HOST - metric.getNumOfLxcs();
                int numOfLxcByRam = (int) ((metric.getFreeRamMb() - MIN_RAM_IN_RESERVE_MB) / MIN_RAM_LXC_MB);
                int numOfLxcByHdd = (int) ((metric.getFreeHddMb() - MIN_HDD_IN_RESERVE_MB) / MIN_HDD_LXC_MB);
                int numOfLxcByCpu = (int) (((100 - metric.getCpuLoadPercent()) - (MIN_CPU_IN_RESERVE_PERCENT / metric.getNumOfProcessors())) / (MIN_CPU_LXC_PERCENT / metric.getNumOfProcessors()));
                if (numOfLxcByCpu > 0 && numOfLxcByHdd > 0 && numOfLxcByRam > 0) {
                    int minNumOfLxcs = Math.min(Math.min(numOfLxcByCpu, numOfLxcByHdd), numOfLxcByRam);
                    bestServers.put(entry.getKey(), minNumOfLxcs);
                }
//                if (numOfLxcByLxcLimit > 0 && numOfLxcByCpu > 0 && numOfLxcByHdd > 0 && numOfLxcByRam > 0) {
//                    int minNumOfLxcs = Math.min(Math.min(Math.min(numOfLxcByCpu, numOfLxcByHdd), numOfLxcByRam), numOfLxcByLxcLimit);
//                    bestServers.put(entry.getKey(), minNumOfLxcs);
//                }
            }

            if (!bestServers.isEmpty()) {
                int numOfAvailableLxcSlots = 0;
                for (Map.Entry<Agent, Integer> srv : bestServers.entrySet()) {
                    numOfAvailableLxcSlots += srv.getValue();
                }

                if (numOfAvailableLxcSlots >= numOfNodes) {

                    for (int i = 0; i < numOfNodes; i++) {
                        Map<Agent, Integer> sortedBestServers = Util.sortMapByValueDesc(bestServers);

                        Map.Entry<Agent, Integer> entry = sortedBestServers.entrySet().iterator().next();
                        Agent physicalNode = entry.getKey();
                        Integer numOfLxcSlots = entry.getValue();
                        bestServers.put(physicalNode, numOfLxcSlots - 1);

                        Map<String, Integer> info = getPlacementInfoMap().get(physicalNode);

                        if (info == null) {

                            addPlacementInfo(physicalNode, defaultNodeType, 1);
                        } else {
                            addPlacementInfo(physicalNode, defaultNodeType, info.get(defaultNodeType) + 1);
                        }

                    }

                }

            }
        }

    }

}
