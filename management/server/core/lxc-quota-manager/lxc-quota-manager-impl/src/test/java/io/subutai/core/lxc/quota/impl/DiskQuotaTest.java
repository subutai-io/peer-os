package io.subutai.core.lxc.quota.impl;


import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.resource.MeasureUnit;
import io.subutai.common.resource.ResourceValue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


@RunWith( MockitoJUnitRunner.class )
public class DiskQuotaTest
{
    private ResourceValue diskQuota;
    private ResourceValue diskQuota2;
    private ResourceValue diskQuota3;


    @Before
    public void setUp() throws Exception
    {
        diskQuota = new ResourceValue( new BigDecimal( "5.5" ), MeasureUnit.UNLIMITED );
        diskQuota2 = new ResourceValue( new BigDecimal( "1.0" ), MeasureUnit.EB );
        diskQuota3 = new ResourceValue( new BigDecimal( "1.0" ), MeasureUnit.GB );
    }


    @Test( expected = IllegalStateException.class )
    public void testProperties() throws Exception
    {
        assertNotNull( diskQuota.getMeasureUnit() );
        assertNotNull( diskQuota2.getWriteValue( MeasureUnit.MB ) );
        assertNotNull( diskQuota2.getMeasureUnit() );
        assertNotNull( diskQuota3.getWriteValue( MeasureUnit.MB ) );
        assertNotNull( diskQuota3.getMeasureUnit() );
        assertEquals( 1024, diskQuota2.getValue( MeasureUnit.PB ).intValue() );
        assertEquals( 1024 * 1024, diskQuota2.getValue( MeasureUnit.TB ).intValue() );
        assertEquals( 1024 * 1024 * 1024, diskQuota2.getValue( MeasureUnit.GB ).intValue() );
        assertEquals( 1.0, diskQuota3.getValue( MeasureUnit.GB ).doubleValue(), 0.0 );
        assertEquals( 1024, diskQuota3.getValue( MeasureUnit.MB ).intValue() );
        assertEquals( 1024 * 1024, diskQuota3.getValue( MeasureUnit.KB ).intValue() );
        assertEquals( 1024 * 1024 * 1024, diskQuota3.getValue( MeasureUnit.BYTE ).intValue() );
        diskQuota.hashCode();
        diskQuota.equals( "test" );
        diskQuota.equals( diskQuota );
        MeasureUnit.UNLIMITED.getName();
        assertNotNull( diskQuota.getPrintValue() );
        assertNotNull( diskQuota.getWriteValue( MeasureUnit.UNLIMITED ) );
    }
}