package org.safehaus.subutai.common.protocol;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;


public class PlacementStrategy
{

    //    MORE_RAM( "More RAM" ), MORE_HDD( "More HDD" ), MORE_CPU( "More CPU" ), BEST_SERVER( "Best server strategy" ),
    //    ROUND_ROBIN( "Round robin strategy" ), FILLUP_PROCEED( "Fillip proceed strategy" );
    //    String value;
    //
    //
    //    PlacementStrategy( String value )
    //    {
    //        this.value = value;
    //    }
    //
    //
    //    String getValue()
    //    {
    //        return value;
    //    }

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
