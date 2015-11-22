package io.subutai.core.lxc.quota.impl;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.quota.DiskPartition;
import io.subutai.common.quota.DiskQuota;
import io.subutai.common.quota.DiskQuotaUnit;
import io.subutai.core.lxc.quota.impl.parser.DiskQuotaParser;

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
        assertNotNull( diskQuota.getValue() );
        assertNotNull( diskQuota.getDiskPartition() );
        assertNotNull( diskQuota.getDiskQuotaUnit() );
        assertNotNull( diskQuota.getDiskQuotaValue() );
        assertNotNull( diskQuota.getKey() );
        assertNotNull( diskQuota.getValue() );
        assertNotNull( diskQuota2.getValue() );
        assertNotNull( diskQuota.getType() );
        DiskQuotaParser.getInstance( DiskPartition.OPT ).parse( "none" );
        diskQuota.hashCode();
        diskQuota.equals( "test" );
        diskQuota.equals( diskQuota );
        DiskQuotaParser.getInstance( DiskPartition.OPT ).parse( "10G" );
        DiskQuotaUnit.UNLIMITED.getName();
    }


    @Test( expected = IllegalArgumentException.class )
    public void testParse()
    {
        DiskQuotaParser.getInstance( DiskPartition.OPT ).parse( String.valueOf( DiskQuotaUnit.UNLIMITED ) );
    }
}