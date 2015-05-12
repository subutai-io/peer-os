package org.safehaus.subutai.common.quota;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;


@RunWith( MockitoJUnitRunner.class )
public class RamQuotaTest
{
    private RamQuota ramQuota;


    @Before
    public void setUp() throws Exception
    {
        ramQuota = new RamQuota( RamQuotaUnit.BYTE, 55 );
    }


    @Test
    public void testGetRamQuotaUnit() throws Exception
    {
        assertNotNull( ramQuota.getQuotaKey() );
        assertNotNull( ramQuota.getRamQuotaUnit() );
        assertNotNull( ramQuota.getRamQuotaValue() );
        assertNotNull( ramQuota.getQuotaType() );
        assertNotNull( ramQuota.getQuotaValue() );
        assertNotNull( ramQuota.hashCode() );
        assertNotNull( ramQuota.equals( "test" ) );
        assertNotNull( ramQuota.equals( ramQuota ) );
        assertNotNull( RamQuota.parse( "10G" ) );
        assertNotNull( QuotaType.getQuotaType( "cpu" ) );
        assertNotNull( QuotaType.getQuotaType( "diskhome" ) );
        assertNotNull( QuotaType.getQuotaType( "diskvar" ) );
        assertNotNull( QuotaType.getQuotaType( "diskRootfs" ) );
        assertNotNull( QuotaType.getQuotaType( "ram" ) );
        assertNotNull( QuotaType.getQuotaType( "json" ) );
        assertNull( QuotaType.getQuotaType( "test" ) );
    }


    @Test( expected = IllegalArgumentException.class )
    public void testParse()
    {
        RamQuota.parse( "test" );
    }
}