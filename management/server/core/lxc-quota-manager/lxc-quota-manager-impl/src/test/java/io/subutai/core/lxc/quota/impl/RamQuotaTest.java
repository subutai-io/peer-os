package io.subutai.core.lxc.quota.impl;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;


import io.subutai.common.resource.MeasureUnit;
import io.subutai.common.resource.ResourceValue;
import io.subutai.core.lxc.quota.impl.parser.CommonResourceValueParser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;


@RunWith( MockitoJUnitRunner.class )
public class RamQuotaTest
{
    private ResourceValue ramQuotaInfo;


    @Before
    public void setUp() throws Exception
    {
        //        ramQuotaInfo = new RamQuota( RamQuotaUnit.BYTE, 1073741824 );
        ramQuotaInfo = new ResourceValue("1.0", MeasureUnit.GB);
    }


    @Test
    public void testGetRamQuotaUnit() throws Exception
    {

        assertNotNull( ramQuotaInfo.getValue(MeasureUnit.BYTE) );
        assertEquals( 1073741824.0, ramQuotaInfo.getValue( MeasureUnit.BYTE).doubleValue(), 0.0 );
        assertEquals( 1048576.0, ramQuotaInfo.getValue( MeasureUnit.KB ).doubleValue(), 0.0 );
        assertEquals( 1024.0, ramQuotaInfo.getValue( MeasureUnit.MB).doubleValue(), 0.00001 );
        assertEquals( 1.0, ramQuotaInfo.getValue( MeasureUnit.GB ).doubleValue(), 0.00001 );
        assertNotNull( ramQuotaInfo.hashCode() );
        assertNotNull( ramQuotaInfo.equals( "test" ) );
        assertNotNull( ramQuotaInfo.equals( ramQuotaInfo ) );
        assertNotNull( CommonResourceValueParser.getInstance().parse( "10G" ) );
    }


    @Test( expected = IllegalArgumentException.class )
    public void testParse()
    {
        CommonResourceValueParser.getInstance().parse( "test" );
    }
}