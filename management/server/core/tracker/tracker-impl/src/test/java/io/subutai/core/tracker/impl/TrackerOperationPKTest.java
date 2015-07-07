package io.subutai.core.tracker.impl;


import org.junit.Before;
import org.junit.Test;
import io.subutai.core.tracker.impl.entity.TrackerOperationPK;

import static junit.framework.TestCase.assertEquals;


public class TrackerOperationPKTest
{
    private static final String SOURCE = "source";
    private static final String ID = "id";

    TrackerOperationPK trackerOperationPK;


    @Before
    public void setUp() throws Exception
    {
        trackerOperationPK = new TrackerOperationPK();
        trackerOperationPK.setOperationTrackId( ID );
        trackerOperationPK.setSource( SOURCE );
    }


    @Test
    public void testProperties() throws Exception
    {
        assertEquals( SOURCE, trackerOperationPK.getSource() );
        assertEquals( ID, trackerOperationPK.getOperationTrackId() );
    }


    @Test
    public void testEquals() throws Exception
    {
        TrackerOperationPK trackerOperationPK2 = new TrackerOperationPK();
        trackerOperationPK2.setOperationTrackId( ID );
        trackerOperationPK2.setSource( SOURCE );

        assertEquals( trackerOperationPK2, trackerOperationPK );
    }


    @Test
    public void testHashCode() throws Exception
    {
        TrackerOperationPK trackerOperationPK2 = new TrackerOperationPK();
        trackerOperationPK2.setOperationTrackId( ID );
        trackerOperationPK2.setSource( SOURCE );

        assertEquals( trackerOperationPK2.hashCode(), trackerOperationPK.hashCode() );
    }
}
