package io.subutai.core.lxc.quota.impl;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.quota.CpuQuota;

import static org.junit.Assert.assertNotNull;


@RunWith( MockitoJUnitRunner.class )
public class CpuQuotaTest
{
    private CpuQuota cpuQuota;



    @Before
    public void setUp() throws Exception
    {
        cpuQuota = new CpuQuota( 555 );
        cpuQuota.setPercentage( 5 );
    }


    @Test
    public void testProperties() throws Exception
    {
        assertNotNull( cpuQuota.getPercentage() );
        assertNotNull( cpuQuota.getKey() );
        assertNotNull( cpuQuota.getType() );
        assertNotNull( cpuQuota.getValue() );
        cpuQuota.toString();

    }
}