package io.subutai.common.quota;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.quota.CpuQuotaInfo;

import static org.junit.Assert.assertNotNull;


@RunWith( MockitoJUnitRunner.class )
public class CpuQuotaInfoTest
{
    private CpuQuotaInfo cpuQuotaInfo;



    @Before
    public void setUp() throws Exception
    {
        cpuQuotaInfo = new CpuQuotaInfo( "555" );
        cpuQuotaInfo.setPercentage( 5 );
    }


    @Test
    public void testProperties() throws Exception
    {
        assertNotNull( cpuQuotaInfo.getPercentage() );
        assertNotNull( cpuQuotaInfo.getQuotaKey() );
        assertNotNull( cpuQuotaInfo.getQuotaType() );
        assertNotNull( cpuQuotaInfo.getQuotaValue() );
        cpuQuotaInfo.toString();

    }
}