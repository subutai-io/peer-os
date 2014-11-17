package org.safehaus.subutai.common.protocol;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;


public class PlacementStrategy
{


    private String strategyId;
    private Set<Criteria> criteria;


    public PlacementStrategy( String strategyId )
    {
        this.strategyId = strategyId;
        criteria = new HashSet<>();
    }


    public PlacementStrategy( String strategyId, Set<Criteria> criteria )
    {
        this.strategyId = strategyId;
        this.criteria = criteria;
    }


    public PlacementStrategy( String strategyId, Criteria criteria )
    {
        this.strategyId = strategyId;
        this.criteria = Sets.newHashSet( criteria );
    }


    public String getStrategyId()
    {
        return strategyId;
    }


    public void setStrategyId( final String strategyId )
    {
        this.strategyId = strategyId;
    }


    public Set<Criteria> getCriteria()
    {
        return criteria;
    }


    public List<Criteria> getCriteriaAsList()
    {
        return new ArrayList<>( criteria );
    }


    public void setCriteria( final Set<Criteria> criteria )
    {
        this.criteria = criteria;
    }
}
