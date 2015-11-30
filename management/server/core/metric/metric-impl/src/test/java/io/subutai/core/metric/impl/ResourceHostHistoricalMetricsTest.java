package io.subutai.core.metric.impl;


import java.util.UUID;

import org.junit.Test;

import io.subutai.common.metric.ResourceHostMetric;


/**
 * Test for ResourceHostMetric
 */
public class ResourceHostHistoricalMetricsTest
{

    @Test
    public void testSetPeerId() throws Exception
    {
        String peerId = UUID.randomUUID().toString();
        ResourceHostMetric resourceHostMetric = new ResourceHostMetric();

        //        resourceHostMetric.setPeerId( peerId );

        //        assertEquals( peerId, resourceHostMetric.getPeerId() );
    }


    @Test
    public void testToString() throws Exception
    {
        String peerId = UUID.randomUUID().toString();
        ResourceHostMetric resourceHostMetric = new ResourceHostMetric();

        //        resourceHostMetric.setPeerId( peerId );

        //        assertThat( resourceHostMetric.toString(), containsString( peerId.toString() ) );
    }
}
