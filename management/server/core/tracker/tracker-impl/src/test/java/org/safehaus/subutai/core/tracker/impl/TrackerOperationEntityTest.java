package org.safehaus.subutai.core.tracker.impl;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.core.tracker.impl.entity.TrackerOperationEntity;

import static junit.framework.TestCase.assertEquals;


@RunWith( MockitoJUnitRunner.class )
public class TrackerOperationEntityTest
{
    private static final String SOURCE = "source";
    private static final String ID = "id";
    private static final long TIMESTAMP = 123;
    private static final String INFO = "info";

    TrackerOperationEntity trackerOperationEntity;


    @Before
    public void setUp() throws Exception
    {
        trackerOperationEntity = new TrackerOperationEntity( SOURCE, ID, TIMESTAMP, INFO );
    }


    @Test
    public void testProperties() throws Exception
    {
        assertEquals( INFO, trackerOperationEntity.getInfo() );
    }


    @Test
    public void testEquals() throws Exception
    {
        assertEquals( new TrackerOperationEntity( SOURCE, ID, TIMESTAMP, INFO ), trackerOperationEntity );
    }


    @Test
    public void testHashCode() throws Exception
    {
        assertEquals( new TrackerOperationEntity( SOURCE, ID, TIMESTAMP, INFO ).hashCode(),
                trackerOperationEntity.hashCode() );
    }
}
