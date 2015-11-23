package io.subutai.core.lxc.quota.impl;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.quota.QuotaType;
import io.subutai.common.quota.RamQuota;
import io.subutai.common.quota.RamQuotaUnit;
import io.subutai.core.lxc.quota.impl.parser.RamQuotaParser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;


@RunWith( MockitoJUnitRunner.class )
public class RamQuotaTest
{
    private RamQuota ramQuotaInfo;


    @Before
    public void setUp() throws Exception
    {
        //        ramQuotaInfo = new RamQuota( RamQuotaUnit.BYTE, 1073741824 );
        ramQuotaInfo = new RamQuota( RamQuotaUnit.GB, 1 );
    }


    @Test
    public void testGetRamQuotaUnit() throws Exception
    {
        assertNotNull( ramQuotaInfo.getKey() );
        assertNotNull( ramQuotaInfo.getRamQuotaUnit() );
        assertNotNull( ramQuotaInfo.getRamQuotaValue( RamQuotaUnit.BYTE ) );
        assertNotNull( ramQuotaInfo.getType() );
        assertNotNull( ramQuotaInfo.getValue() );
        assertEquals( 1073741824.0, ramQuotaInfo.getRamQuotaValue( RamQuotaUnit.BYTE ), 0.0 );
        assertEquals( 1048576.0, ramQuotaInfo.getRamQuotaValue( RamQuotaUnit.KB ), 0.0 );
        assertEquals( 1024.0, ramQuotaInfo.getRamQuotaValue( RamQuotaUnit.MB ), 0.00001 );
        assertEquals( 1.0, ramQuotaInfo.getRamQuotaValue( RamQuotaUnit.GB ), 0.00001 );
        assertNotNull( ramQuotaInfo.hashCode() );
        assertNotNull( ramQuotaInfo.equals( "test" ) );
        assertNotNull( ramQuotaInfo.equals( ramQuotaInfo ) );
        assertNotNull( RamQuotaParser.getInstance().parse( "10G" ) );
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
        RamQuotaParser.getInstance().parse( "test" );
    }
}