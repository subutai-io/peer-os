package org.safehaus.subutai.core.strategy.api;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.safehaus.subutai.common.metric.ResourceHostMetric;
import org.safehaus.subutai.common.protocol.Criteria;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;


/**
 * This class should be extended by all container placement strategies
 */
public abstract class AbstractContainerPlacementStrategy implements ContainerPlacementStrategy
{
    public static final String DEFAULT_NODE_TYPE = "default";
    private final Map<ResourceHostMetric, Map<String, Integer>> placementInfoMap = new HashMap<>();
    private List<CriteriaDef> criteria = Lists.newArrayList();
    private List<Criteria> distributionCriteria = Lists.newArrayList();


    protected void clearPlacementInfo()
    {
        placementInfoMap.clear();
    }


    public final void addPlacementInfo( ResourceHostMetric resourceHostMetric, String nodeType, int numberOfContainers )
    {

        Preconditions.checkNotNull( resourceHostMetric, "Invalid resource host metric" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( nodeType ), "Invalid node type" );
        Preconditions.checkArgument( numberOfContainers > 0, "Invalid number of containers" );

        Map<String, Integer> placementInfo = placementInfoMap.get( resourceHostMetric );
        if ( placementInfo == null )
        {
            placementInfo = Maps.newHashMap();
            placementInfoMap.put( resourceHostMetric, placementInfo );
        }

        placementInfo.put( nodeType, numberOfContainers );
    }


    /**
     * Returns placement map
     *
     * @return map where key is a resource host metric and value is a map where key is type of node and value is a
     * number of containers to place on this server
     */
    public Map<ResourceHostMetric, Map<String, Integer>> getPlacementInfoMap()
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
     * Optional method to implement for calculating total number of container slots each resource host can accommodate
     *
     * @param serverMetrics - metrics of all connected resource hosts
     *
     * @return map where key is a resource host metric and value is a number of container this resource host can
     * accommodate
     */
    @Override
    public Map<ResourceHostMetric, Integer> calculateSlots( int nodesCount, List<ResourceHostMetric> serverMetrics )
    {
        return null;
    }


    /**
     * Returns a distribution of node counts among severs.
     *
     * @return map where key is a resource host metric and value is a number of containers to be placed on that server
     */
    @Override
    public Map<ResourceHostMetric, Integer> getPlacementDistribution()
    {
        Map<ResourceHostMetric, Integer> res = new HashMap<>();
        for ( Map.Entry<ResourceHostMetric, Map<String, Integer>> e : placementInfoMap.entrySet() )
        {
            int total = 0;
            for ( Integer i : e.getValue().values() )
            {
                total += i;
            }
            res.put( e.getKey(), total );
        }
        clearPlacementInfo();
        return res;
    }


    protected List<ResourceHostMetric> sortServers( List<ResourceHostMetric> serverMetrics ) throws StrategyException
    {
        List<ResourceHostMetric> result = new ArrayList<>( serverMetrics );

        Collections.sort( result, new Comparator<ResourceHostMetric>()
        {
            @Override
            public int compare( final ResourceHostMetric o1, final ResourceHostMetric o2 )
            {
                return o1.getHost().compareTo( o2.getHost() );
            }
        } );
        return result;
    }


    public List<Criteria> getDistributionCriteria()
    {
        return distributionCriteria;
    }


    public void setDistributionCriteria( final List<Criteria> distributionCriteria )
    {
        this.distributionCriteria = distributionCriteria;
    }
}
