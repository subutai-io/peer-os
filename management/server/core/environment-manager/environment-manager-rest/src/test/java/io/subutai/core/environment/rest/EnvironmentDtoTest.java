package io.subutai.core.environment.rest;


import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import io.subutai.common.environment.EnvironmentStatus;

import com.google.common.collect.Sets;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;


@RunWith( MockitoJUnitRunner.class )
public class EnvironmentDtoTest
{
    private static final String ID = UUID.randomUUID().toString();
    private static final String NAME = "name";
    private static final EnvironmentStatus STATUS = EnvironmentStatus.UNDER_MODIFICATION;

    @Mock
    ContainerDto containerDto;

    @Mock
    ContainerDto containerDto2;

    EnvironmentDto environmentDto;


    @Before
    public void setUp() throws Exception
    {
        environmentDto = new EnvironmentDto( TestUtil.ENV_ID, TestUtil.ENV_NAME, EnvironmentStatus.HEALTHY,
                Sets.newHashSet( containerDto ) );
    }


    @Test
    public void testProperties() throws Exception
    {
        assertEquals( TestUtil.ENV_ID, environmentDto.getId() );
        assertEquals( TestUtil.ENV_NAME, environmentDto.getName() );
        assertEquals( EnvironmentStatus.HEALTHY, environmentDto.getStatus() );
        assertTrue( environmentDto.getContainers().contains( containerDto ) );
    }


    @Test
    public void testSetters() throws Exception
    {
        environmentDto.setId( ID );

        assertEquals( ID, environmentDto.getId() );

        environmentDto.setName( NAME );

        assertEquals( NAME, environmentDto.getName() );

        environmentDto.setStatus( STATUS );

        assertEquals( STATUS, environmentDto.getStatus() );

        environmentDto.setContainers( Sets.newHashSet( containerDto2 ) );

        assertTrue( environmentDto.getContainers().contains( containerDto2 ) );
    }
}
