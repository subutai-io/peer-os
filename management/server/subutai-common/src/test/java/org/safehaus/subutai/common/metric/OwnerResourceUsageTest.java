package org.safehaus.subutai.common.metric;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertNotNull;


@RunWith( MockitoJUnitRunner.class )
public class OwnerResourceUsageTest
{
    private OwnerResourceUsage ownerResourceUsage;


    @Before
    public void setUp() throws Exception
    {
        ownerResourceUsage = new OwnerResourceUsage( 5, 5, 5.0, 5.0, 5.0, 5.0 );
    }


    @Test
    public void testProperties() throws Exception
    {
        assertNotNull( ownerResourceUsage.getUsedCpu() );
        assertNotNull( ownerResourceUsage.getUsedDiskHome() );
        assertNotNull( ownerResourceUsage.getUsedDiskOpt() );
        assertNotNull( ownerResourceUsage.getUsedDiskRootfs() );
        assertNotNull( ownerResourceUsage.getUsedDiskVar() );
        assertNotNull( ownerResourceUsage.getUsedRam() );
    }
}