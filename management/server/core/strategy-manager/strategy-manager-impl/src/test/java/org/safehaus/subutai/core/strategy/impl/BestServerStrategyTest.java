package org.safehaus.subutai.core.strategy.impl;


import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.core.strategy.api.CriteriaDef;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


/**
 * Created by ermek on 10/9/14.
 */
public class BestServerStrategyTest
{
    BestServerStrategy bestServerStrategy;


    @Before
    public void setUp() throws Exception
    {
        bestServerStrategy = new BestServerStrategy();
    }


    @Test
    public void testGetId() throws Exception
    {
        assertEquals( "BEST_SERVER", bestServerStrategy.getId() );
    }


    @Test
    public void testGetTitle() throws Exception
    {
        assertEquals( "Best server placement strategy", bestServerStrategy.getTitle() );
    }


    @Test
    public void testHasCriteria() throws Exception
    {
        assertTrue( bestServerStrategy.hasCriteria() );
    }


    @Test
    public void testGetCriteria() throws Exception
    {
        List<CriteriaDef> result = bestServerStrategy.getCriteriaDef();
        assertEquals( 3, result.size() );
        assertNotNull( bestServerStrategy.getCriteriaDef() );
    }
}