package io.subutai.common.metric;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.peer.ContainerId;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class ProcessResourceUsageTest
{
    private static final String CONTAINER_ID = "con_id";
    private static final int PID = 1;
    private ProcessResourceUsage processResourceUsage;
    @Mock
    ContainerId containerId;


    @Before
    public void setUp() throws Exception
    {
        when( containerId.getId() ).thenReturn( CONTAINER_ID );
        processResourceUsage = new ProcessResourceUsage( containerId, PID );
    }


    @Test
    public void testGetHost() throws Exception
    {
        assertEquals( processResourceUsage.getContainerId().getId(), CONTAINER_ID );
        assertEquals( processResourceUsage.getPid(), PID );
    }
}