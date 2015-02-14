package org.safehaus.subutai.core.strategy.impl;


import java.util.List;
import java.util.Map;

import org.safehaus.subutai.common.metric.ResourceHostMetric;
import org.safehaus.subutai.common.protocol.Criteria;
import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.core.strategy.api.AbstractContainerPlacementStrategy;

import com.google.common.collect.Maps;


/**
 * This is a default container placement strategy. According to metrics and limits calculates number of containers that
 * each connected resource host can accommodate
 */
public class DefaultContainerPlacementStrategy extends AbstractContainerPlacementStrategy
{
    private static final double MIN_HDD_LXC_MB = 5 * 1024;
    private static final double MIN_HDD_IN_RESERVE_MB = 20 * 1024;
    private static final double MIN_RAM_LXC_MB = 512;
    private static final double MIN_RAM_IN_RESERVE_MB = 1024;


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
     * This method calculates placement of containers across resource hosts. Code should check passed server metrics to
     * figure out strategy for container placement This is done by calling addPlacementInfo method.This method
     * calculates on which resource host to places containers, number of containers to place and their type
     */
    @Override
    public void calculatePlacement( int nodesCount, List<ResourceHostMetric> serverMetrics, List<Criteria> criteria )
    {

        Map<ResourceHostMetric, Integer> serversWithSlots = calculateSlots( nodesCount, serverMetrics );

        if ( !serversWithSlots.isEmpty() )
        {
            int numOfAvailableLxcSlots = 0;
            for ( Map.Entry<ResourceHostMetric, Integer> srv : serversWithSlots.entrySet() )
            {
                numOfAvailableLxcSlots += srv.getValue();
            }

            if ( numOfAvailableLxcSlots >= nodesCount )
            {

                for ( int i = 0; i < nodesCount; i++ )
                {
                    Map<ResourceHostMetric, Integer> sortedBestServers =
                            CollectionUtil.sortMapByValueDesc( serversWithSlots );

                    Map.Entry<ResourceHostMetric, Integer> entry = sortedBestServers.entrySet().iterator().next();
                    ResourceHostMetric physicalNode = entry.getKey();
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
     * Optional method to implement if placement uses simple logic to calculate container slots on a resource host
     *
     * @param serverMetrics - metrics of all connected resource hots
     *
     * @return map where key is a resource host metric and value is a number of containers this resource host can
     * accommodate
     */
    @Override
    public Map<ResourceHostMetric, Integer> calculateSlots( int nodesCount, List<ResourceHostMetric> serverMetrics )
    {
        Map<ResourceHostMetric, Integer> serverSlots = Maps.newHashMap();

        if ( !CollectionUtil.isCollectionEmpty( serverMetrics ) )
        {
            for ( ResourceHostMetric metric : serverMetrics )
            {
                int numOfLxcByRam = ( int ) ( ( getBytesInMb( metric.getAvailableRam() ) - MIN_RAM_IN_RESERVE_MB )
                        / MIN_RAM_LXC_MB );
                int numOfLxcByHdd = ( int ) ( ( getBytesInMb( metric.getAvailableDiskVar() ) - MIN_HDD_IN_RESERVE_MB )
                        / MIN_HDD_LXC_MB );

                if ( numOfLxcByHdd > 0 && numOfLxcByRam > 0 )
                {
                    int minNumOfLxcs = Math.min( numOfLxcByHdd, numOfLxcByRam );
                    serverSlots.put( metric, minNumOfLxcs );
                }
            }
        }
        return serverSlots;
    }


    private double getBytesInMb( double bytes )
    {
        if ( bytes > 0 )
        {
            return bytes / ( 1024 * 1024 );
        }
        return 0;
    }
}
