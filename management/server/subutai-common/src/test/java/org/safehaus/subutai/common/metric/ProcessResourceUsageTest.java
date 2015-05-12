package org.safehaus.subutai.common.metric;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith( MockitoJUnitRunner.class )
public class ProcessResourceUsageTest
{
    private ProcessResourceUsage processResourceUsage;

    @Before
    public void setUp() throws Exception
    {
        processResourceUsage = new ProcessResourceUsage();
    }


    @Test
    public void testGetHost() throws Exception
    {
        processResourceUsage.getUsedCpu();
        processResourceUsage.getHost();
        processResourceUsage.getUsedRam();
    }
}