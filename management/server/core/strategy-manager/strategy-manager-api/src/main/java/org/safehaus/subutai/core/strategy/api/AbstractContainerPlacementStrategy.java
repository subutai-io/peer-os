package org.safehaus.subutai.core.strategy.api;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * This class should be extended by all container placement strategies
 */
public abstract class AbstractContainerPlacementStrategy implements ContainerPlacementStrategy
{
    private final Map<ServerMetric, Map<String, Integer>> placementInfoMap = new HashMap<>();
    private List<CriteriaDef> criteria = new ArrayList<>();


    protected void clearPlacementInfo()
    {
        placementInfoMap.clear();
    }


    public final void addPlacementInfo( ServerMetric physicalNode, String nodeType, int numberOfLxcsToCreate )
    {
        if ( physicalNode == null )
        {
            throw new IllegalArgumentException( "Physical node is null" );
        }
        if ( nodeType == null || nodeType.isEmpty() )
        {
            throw new IllegalArgumentException( "Node type is null or empty" );
        }
        if ( numberOfLxcsToCreate <= 0 )
        {
            throw new IllegalArgumentException( "Number of lxcs must be greater than 0" );
        }

        Map<String, Integer> placementInfo = placementInfoMap.get( physicalNode );
        if ( placementInfo == null )
        {
            placementInfo = new HashMap<>();
            placementInfoMap.put( physicalNode, placementInfo );
        }

        placementInfo.put( nodeType, numberOfLxcsToCreate );
    }


    /**
     * Returns placement map
     *
     * @return map where key is a physical server and value is a map where key is type of node and value is a number of
     * lxcs to place on this server
     */
    public Map<ServerMetric, Map<String, Integer>> getPlacementInfoMap()
    {
        return Collections.unmodifiableMap( placementInfoMap );
    }


    @Override
    public boolean hasCriteria()
    {
        return false;
    }


    @Override
    public List<CriteriaDef> getCriteriaDef()
    {
        return Collections.unmodifiableList( criteria );
    }


    /**
     * Optional method to implement for calculating total number of lxc slots each physical server can accommodate
     *
     * @param serverMetrics - metrics from all connected physical servers
     *
     * @return map where key is a physical agent and value is a number of lxcs this physical server can accommodate
     */
    @Override
    public Map<ServerMetric, Integer> calculateSlots( int nodesCount, List<ServerMetric> serverMetrics )
    {
        return null;
    }


    /**
     * Returns a distribution of node counts among severs.
     *
     * @return map where key is a physical server and value is a number of containers to be placed on that server
     */
    @Override
    public Map<ServerMetric, Integer> getPlacementDistribution()
    {
        Map<ServerMetric, Integer> res = new HashMap<>();
        for ( Map.Entry<ServerMetric, Map<String, Integer>> e : placementInfoMap.entrySet() )
        {
            int total = 0;
            for ( Integer i : e.getValue().values() )
            {
                total += i;
            }
            res.put( e.getKey(), total );
        }
        return res;
    }


    protected List<ServerMetric> sortServers( List<ServerMetric> serverMetrics ) throws StrategyException
    {
        List<ServerMetric> result = new ArrayList<>( serverMetrics );

        Collections.sort( result, new Comparator<ServerMetric>()
        {
            @Override
            public int compare( final ServerMetric o1, final ServerMetric o2 )
            {
                return o1.getHostname().compareTo( o2.getHostname() );
            }
        } );
        return result;
    }
}
