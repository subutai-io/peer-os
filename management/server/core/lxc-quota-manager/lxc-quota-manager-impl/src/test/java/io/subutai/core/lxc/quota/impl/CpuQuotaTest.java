package io.subutai.core.lxc.quota.impl;


import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.resource.CpuResource;

import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;


@RunWith( MockitoJUnitRunner.class )
public class CpuQuotaTest
{
    private CpuResource cpuQuota;


    @Before
    public void setUp() throws Exception
    {
        cpuQuota = new CpuResource( new BigDecimal( "55.00" ), 0.0, "model", 4, 16, 64, 128, 2.1, 0.5 );
    }


    @Test
    public void testProperties() throws Exception
    {
        assertNotNull( cpuQuota.getResourceValue() );
        assertEquals( 55.00, cpuQuota.getResourceValue().getValue().doubleValue(), 0.0 );
        assertThat( cpuQuota.getWriteValue(), containsString( "55" ) );
        assertThat( cpuQuota.getPrintValue(), containsString( "55" ) );
    }
}