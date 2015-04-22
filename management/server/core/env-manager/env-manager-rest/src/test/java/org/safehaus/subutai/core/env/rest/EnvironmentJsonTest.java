package org.safehaus.subutai.core.env.rest;


import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.environment.EnvironmentStatus;

import com.google.common.collect.Sets;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;


@RunWith( MockitoJUnitRunner.class )
public class EnvironmentJsonTest
{
    private static final UUID ID = UUID.randomUUID();
    private static final String NAME = "name";
    private static final EnvironmentStatus STATUS = EnvironmentStatus.UNDER_MODIFICATION;

    @Mock
    ContainerJson containerJson;

    @Mock
    ContainerJson containerJson2;

    EnvironmentJson environmentJson;


    @Before
    public void setUp() throws Exception
    {
        environmentJson = new EnvironmentJson( TestUtil.ENV_ID, TestUtil.ENV_NAME, EnvironmentStatus.HEALTHY,
                Sets.newHashSet( containerJson ) );
    }


    @Test
    public void testProperties() throws Exception
    {
        assertEquals( TestUtil.ENV_ID, environmentJson.getId() );
        assertEquals( TestUtil.ENV_NAME, environmentJson.getName() );
        assertEquals( EnvironmentStatus.HEALTHY, environmentJson.getStatus() );
        assertTrue( environmentJson.getContainers().contains( containerJson ) );
    }


    @Test
    public void testSetters() throws Exception
    {
        environmentJson.setId( ID );

        assertEquals( ID, environmentJson.getId() );

        environmentJson.setName( NAME );

        assertEquals( NAME, environmentJson.getName() );

        environmentJson.setStatus( STATUS );

        assertEquals( STATUS, environmentJson.getStatus() );

        environmentJson.setContainers( Sets.newHashSet( containerJson2 ) );

        assertTrue( environmentJson.getContainers().contains( containerJson2 ) );
    }
}
