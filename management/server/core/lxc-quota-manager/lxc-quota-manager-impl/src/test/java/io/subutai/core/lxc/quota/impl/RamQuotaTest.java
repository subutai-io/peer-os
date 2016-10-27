package io.subutai.core.lxc.quota.impl;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;


import io.subutai.hub.share.resource.ByteUnit;
import io.subutai.hub.share.resource.ByteValueResource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


@RunWith( MockitoJUnitRunner.class )
public class RamQuotaTest
{
    private ByteValueResource ramQuotaInfo;


    @Before
    public void setUp() throws Exception
    {
        ramQuotaInfo = new ByteValueResource( "1073741824.0" );
    }


    @Test
    public void testGetRamQuotaUnit() throws Exception
    {

        assertNotNull( ramQuotaInfo.getValue( ByteUnit.BYTE ) );
        assertEquals( 1073741824.0, ramQuotaInfo.getValue( ByteUnit.BYTE ).doubleValue(), 0.0 );
        assertEquals( 1048576.0, ramQuotaInfo.getValue( ByteUnit.KB ).doubleValue(), 0.0 );
        assertEquals( 1024.0, ramQuotaInfo.getValue( ByteUnit.MB ).doubleValue(), 0.00001 );
        assertEquals( 1.0, ramQuotaInfo.getValue( ByteUnit.GB ).doubleValue(), 0.00001 );
        assertNotNull( ramQuotaInfo.hashCode() );
        assertNotNull( ramQuotaInfo.equals( "test" ) );
        assertNotNull( ramQuotaInfo.equals( ramQuotaInfo ) );
        //        assertNotNull( ByteResourceValueParser.getInstance().parse( "10G" ) );
    }

    //
    //    @Test( expected = IllegalArgumentException.class )
    //    public void testParse()
    //    {
    //        ByteResourceValueParser.getInstance().parse( "test" );
    //    }
}