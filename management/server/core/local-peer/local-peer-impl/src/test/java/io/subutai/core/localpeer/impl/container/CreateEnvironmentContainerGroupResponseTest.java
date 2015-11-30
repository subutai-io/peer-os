package io.subutai.core.localpeer.impl.container;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.environment.CreateEnvironmentContainerGroupResponse;
import io.subutai.common.host.ContainerHostInfoModel;

import com.google.common.collect.Sets;

import static junit.framework.TestCase.assertEquals;


@RunWith( MockitoJUnitRunner.class )
public class CreateEnvironmentContainerGroupResponseTest
{
    @Mock
    ContainerHostInfoModel containerHostInfoModel;

    CreateEnvironmentContainerGroupResponse response;


    @Before
    public void setUp() throws Exception
    {
        response = new CreateEnvironmentContainerGroupResponse( Sets.newHashSet( containerHostInfoModel ) );
    }


    @Test
    public void testGetHosts() throws Exception
    {
        assertEquals( Sets.newHashSet( containerHostInfoModel ), response.getHosts() );
    }
}
