package org.safehaus.subutai.core.strategy.impl;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.core.strategy.api.AbstractContainerPlacementStrategy;
import org.safehaus.subutai.core.strategy.api.Criteria;
import org.safehaus.subutai.core.strategy.api.ServerMetric;


/**
 * This is a default container placement strategy. According to metrics and limits calculates number of containers that
 * each connected physical host can accommodate
 */
public class DefaultContainerPlacementStrategy extends AbstractContainerPlacementStrategy
{
    public static final String defaultNodeType = "default";
    private final double MIN_HDD_LXC_MB = 5 * 1024;
    private final double MIN_HDD_IN_RESERVE_MB = 20 * 1024;
    private final double MIN_RAM_LXC_MB = 512;          // 1G
    private final double MIN_RAM_IN_RESERVE_MB = 1024;   // 1G
    private final double MIN_CPU_LXC_PERCENT = 5;           // 5%
    private final double MIN_CPU_IN_RESERVE_PERCENT = 10;    // 10%
    Logger LOG = Logger.getLogger( DefaultContainerPlacementStrategy.class.getName() );


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
    public void calculatePlacement( int nodesCount, Map<Agent, ServerMetric> serverMetrics, List<Criteria> criteria )
    {

        Map<Agent, Integer> serversWithSlots = calculateSlots( nodesCount, serverMetrics );

        if ( !serversWithSlots.isEmpty() )
        {
            int numOfAvailableLxcSlots = 0;
            for ( Map.Entry<Agent, Integer> srv : serversWithSlots.entrySet() )
            {
                numOfAvailableLxcSlots += srv.getValue();
            }

            if ( numOfAvailableLxcSlots >= nodesCount )
            {

                for ( int i = 0; i < nodesCount; i++ )
                {
                    Map<Agent, Integer> sortedBestServers = CollectionUtil.sortMapByValueDesc( serversWithSlots );

                    Map.Entry<Agent, Integer> entry = sortedBestServers.entrySet().iterator().next();
                    Agent physicalNode = entry.getKey();
                    Integer numOfLxcSlots = entry.getValue();
                    serversWithSlots.put( physicalNode, numOfLxcSlots - 1 );

                    Map<String, Integer> info = getPlacementInfoMap().get( physicalNode );

                    if ( info == null )
                    {
                        addPlacementInfo( physicalNode, defaultNodeType, 1 );
                    }
                    else
                    {
                        addPlacementInfo( physicalNode, defaultNodeType, info.get( defaultNodeType ) + 1 );
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
    public Map<Agent, Integer> calculateSlots( int nodesCount, Map<Agent, ServerMetric> serverMetrics )
    {
        Map<Agent, Integer> serverSlots = new HashMap<Agent, Integer>();

        if ( serverMetrics != null && !serverMetrics.isEmpty() )
        {
            for ( Map.Entry<Agent, ServerMetric> entry : serverMetrics.entrySet() )
            {
                ServerMetric metric = entry.getValue();
                LOG.log( Level.WARNING, metric.toString() );
                int numOfLxcByRam = ( int ) ( ( metric.getFreeRamMb() - MIN_RAM_IN_RESERVE_MB ) / MIN_RAM_LXC_MB );
                int numOfLxcByHdd = ( int ) ( ( metric.getFreeHddMb() - MIN_HDD_IN_RESERVE_MB ) / MIN_HDD_LXC_MB );
                int numOfLxcByCpu = ( int ) (
                        ( ( 100 - metric.getCpuLoadPercent() ) - ( MIN_CPU_IN_RESERVE_PERCENT / metric
                                .getNumOfProcessors() ) ) / ( MIN_CPU_LXC_PERCENT / metric.getNumOfProcessors() ) );
                LOG.log( Level.WARNING, numOfLxcByRam + " | " + numOfLxcByHdd + " | " + numOfLxcByCpu );

                //                if ( numOfLxcByCpu > 0 && numOfLxcByHdd > 0 && numOfLxcByRam > 0 ) {
                //                    int minNumOfLxcs = Math.min( Math.min( numOfLxcByCpu, numOfLxcByHdd ),
                // numOfLxcByRam );
                //                    serverSlots.put( entry.getKey(), minNumOfLxcs );
                //                }
                if ( numOfLxcByHdd > 0 && numOfLxcByRam > 0 )
                {
                    int minNumOfLxcs = Math.min( numOfLxcByHdd, numOfLxcByRam );
                    serverSlots.put( entry.getKey(), minNumOfLxcs );
                }
            }
        }
        return serverSlots;
    }
    //
    //    @Override
    //    public PlacementStrategy getStrategy() {
    //        return PlacementStrategy.FILLUP_PROCEED;
    //    }
}
