package org.safehaus.subutai.common.protocol;


import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertNotNull;


@RunWith( MockitoJUnitRunner.class )
public class PlacementStrategyTest
{
    private PlacementStrategy placementStrategy;
    private Setting setting;

    @Mock
    Criteria criteria;


    @Before
    public void setUp() throws Exception
    {
        Set<Criteria> mySet = new HashSet<>();
        mySet.add( criteria );

        placementStrategy = new PlacementStrategy( "testStrategyId" );
        placementStrategy = new PlacementStrategy( "testStrategyId", mySet );
        placementStrategy = new PlacementStrategy( "testStrategyId", criteria );
        setting = new Setting();

        placementStrategy.setCriteria( mySet );
        placementStrategy.setStrategyId( "testStrategyId" );
    }


    @Test
    public void testProperties() throws Exception
    {
        assertNotNull( placementStrategy.getCriteria() );
        assertNotNull( placementStrategy.getStrategyId() );
        assertNotNull( placementStrategy.getCriteriaAsList() );


    }
}