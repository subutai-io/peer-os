package org.safehaus.subutai.common.cache;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertNotNull;


@RunWith( MockitoJUnitRunner.class )
public class CacheEntryTest
{
    private CacheEntry cacheEntry;

    @Mock
    Object object;


    @Before
    public void setUp() throws Exception
    {
        cacheEntry = new CacheEntry( object, 500 );
    }


    @Test
    public void testGetValue() throws Exception
    {
        assertNotNull( cacheEntry.getValue() );
    }


    @Test
    public void testResetCreationTimestamp() throws Exception
    {
        cacheEntry.resetCreationTimestamp();
    }


    @Test
    public void testIsExpired() throws Exception
    {
        cacheEntry.isExpired();
    }
}