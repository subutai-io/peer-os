package io.subutai.core.lxc.quota.impl;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;


import io.subutai.common.resource.MeasureUnit;
import io.subutai.common.resource.ResourceValue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


@RunWith( MockitoJUnitRunner.class )
public class CpuQuotaTest
{
    private ResourceValue cpuQuota;


    @Before
    public void setUp() throws Exception
    {
        cpuQuota = new ResourceValue( "55.00", MeasureUnit.PERCENT );
    }


    @Test
    public void testProperties() throws Exception
    {
        assertNotNull( cpuQuota.getValue( MeasureUnit.PERCENT ) );
        assertEquals( 55.00, cpuQuota.getValue( MeasureUnit.PERCENT ).doubleValue(), 0.0 );
        assertEquals( "55", cpuQuota.getWriteValue( MeasureUnit.PERCENT ) );
    }
}