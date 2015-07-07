package io.subutai.common.quota;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.quota.DiskPartition;
import io.subutai.common.quota.DiskQuota;
import io.subutai.common.quota.DiskQuotaUnit;

import static org.junit.Assert.assertNotNull;


@RunWith( MockitoJUnitRunner.class )
public class DiskQuotaTest
{
    private DiskQuota diskQuota;
    private DiskQuota diskQuota2;

    @Before
    public void setUp() throws Exception
    {
        diskQuota = new DiskQuota( DiskPartition.OPT, DiskQuotaUnit.UNLIMITED, 5.5 );
        diskQuota2 = new DiskQuota( DiskPartition.OPT, DiskQuotaUnit.BYTE, 5.5 );
    }


    @Test
    public void testProperties() throws Exception
    {
        assertNotNull( diskQuota.getQuotaValue() );
        assertNotNull( diskQuota.getDiskPartition() );
        assertNotNull( diskQuota.getDiskQuotaUnit() );
        assertNotNull( diskQuota.getDiskQuotaValue() );
        assertNotNull( diskQuota.getQuotaKey() );
        assertNotNull( diskQuota.getQuotaValue() );
        assertNotNull( diskQuota2.getQuotaValue() );
        assertNotNull( diskQuota.getQuotaType() );
        DiskQuota.parse( DiskPartition.OPT, "none" );
        diskQuota.hashCode();
        diskQuota.equals( "test" );
        diskQuota.equals( diskQuota );
        DiskQuota.parse( DiskPartition.OPT, "10G" );
        DiskQuotaUnit.UNLIMITED.getName();
    }


    @Test (expected = IllegalArgumentException.class)
    public void testParse()
    {
        DiskQuota.parse( DiskPartition.OPT, String.valueOf( DiskQuotaUnit.UNLIMITED ) );
    }
}