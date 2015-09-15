package io.subutai.core.environment.impl;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.environment.Topology;


@RunWith( MockitoJUnitRunner.class )
public class EnvironmentManagerTest
{
    EnvironmentManagerImpl environmentManager;

    @Mock
    Topology topology;


    @Before
    public void setUp() throws Exception
    {
        environmentManager = new EnvironmentManagerImpl();
    }


    @Test
    public void testCreateEnvironment() throws Exception
    {
        environmentManager.createEnvironment( "env", topology, "192.168.1.0/24", null );
    }
}
