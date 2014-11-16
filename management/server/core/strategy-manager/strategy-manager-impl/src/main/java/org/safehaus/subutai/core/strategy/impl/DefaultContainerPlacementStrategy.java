package org.safehaus.subutai.core.strategy.impl;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.core.strategy.api.AbstractContainerPlacementStrategy;
import org.safehaus.subutai.common.protocol.Criteria;
import org.safehaus.subutai.core.strategy.api.ServerMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This is a default container placement strategy. According to metrics and limits calculates number of containers that
 * each connected physical host can accommodate
 */
public class DefaultContainerPlacementStrategy extends AbstractContainerPlacementStrategy
{
    public static final String DEFAULT_NODE_TYPE = "default";
    private static final Logger LOG = LoggerFactory.getLogger( DefaultContainerPlacementStrategy.class );
    private static final double MIN_HDD_LXC_MB = 5 * 1024;
    private static final double MIN_HDD_IN_RESERVE_MB = 20 * 1024;
    private static final double MIN_RAM_LXC_MB = 512;          // 1G
    private static final double MIN_RAM_IN_RESERVE_MB = 1024;   // 1G
    private static final double MIN_CPU_LXC_PERCENT = 5;           // 5%
    private static final double MIN_CPU_IN_RESERVE_PERCENT = 10;    // 10%


    @Override
    public String getId()
    {
        return "DEFAULT-STRATEGY";
    }


    @Override
    public String getTitle()
    {
        return "Default container placement strategy";
    }


    /**
     * This method calculates placement of lxcs on physical servers. Code should check passed server metrics to figure
     * out strategy for lxc placement This is done by calling addPlacementInfo method.This method calculates on which
     * physical server to places lxc, the number of lxcs to place and their type
     *
     * @param serverMetrics - map where key is a physical agent and value is a metric
     */
    @Override
    public void calculatePlacement( int nodesCount, List<ServerMetric> serverMetrics, List<Criteria> criteria )
    {

        Map<ServerMetric, Integer> serversWithSlots = calculateSlots( nodesCount, serverMetrics );

        if ( !serversWithSlots.isEmpty() )
        {
            int numOfAvailableLxcSlots = 0;
            for ( Map.Entry<ServerMetric, Integer> srv : serversWithSlots.entrySet() )
            {
                numOfAvailableLxcSlots += srv.getValue();
            }

            if ( numOfAvailableLxcSlots >= nodesCount )
            {

                for ( int i = 0; i < nodesCount; i++ )
                {
                    Map<ServerMetric, Integer> sortedBestServers =
                            CollectionUtil.sortMapByValueDesc( serversWithSlots );

                    Map.Entry<ServerMetric, Integer> entry = sortedBestServers.entrySet().iterator().next();
                    ServerMetric physicalNode = entry.getKey();
                    Integer numOfLxcSlots = entry.getValue();
                    serversWithSlots.put( physicalNode, numOfLxcSlots - 1 );

                    Map<String, Integer> info = getPlacementInfoMap().get( physicalNode );

                    if ( info == null )
                    {
                        addPlacementInfo( physicalNode, DEFAULT_NODE_TYPE, 1 );
                    }
                    else
                    {
                        addPlacementInfo( physicalNode, DEFAULT_NODE_TYPE, info.get( DEFAULT_NODE_TYPE ) + 1 );
                    }
                }
            }
        }
    }


    /**
     * Optional method to implement if placement uses simple logic to calculate lxc slots on a physical server
     *
     * @param serverMetrics - metrics of all connected physical servers
     *
     * @return map where key is a physical agent and value is a number of lxcs this physical server can accommodate
     */
    @Override
    public Map<ServerMetric, Integer> calculateSlots( int nodesCount, List<ServerMetric> serverMetrics )
    {
        Map<ServerMetric, Integer> serverSlots = new HashMap<>();

        if ( serverMetrics != null && !serverMetrics.isEmpty() )
        {
            for ( ServerMetric metric : serverMetrics )
            {
                //                ServerMetric metric = entry.getValue();
                LOG.debug( metric.toString() );
                int numOfLxcByRam = ( int ) ( ( metric.getFreeRamMb() - MIN_RAM_IN_RESERVE_MB ) / MIN_RAM_LXC_MB );
                int numOfLxcByHdd = ( int ) ( ( metric.getFreeHddMb() - MIN_HDD_IN_RESERVE_MB ) / MIN_HDD_LXC_MB );
                int numOfLxcByCpu = ( int ) (
                        ( ( 100 - metric.getCpuLoadPercent() ) - ( MIN_CPU_IN_RESERVE_PERCENT / metric
                                .getNumOfProcessors() ) ) / ( MIN_CPU_LXC_PERCENT / metric.getNumOfProcessors() ) );
                LOG.debug( numOfLxcByRam + " | " + numOfLxcByHdd + " | " + numOfLxcByCpu );

                if ( numOfLxcByHdd > 0 && numOfLxcByRam > 0 )
                {
                    int minNumOfLxcs = Math.min( numOfLxcByHdd, numOfLxcByRam );
                    serverSlots.put( metric, minNumOfLxcs );
                }
            }
        }
        return serverSlots;
    }
}
