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
    private static final String DESC = "description";
    @Mock
    ContainerHostInfoModel containerHostInfoModel;

    CreateEnvironmentContainerGroupResponse response;


    @Before
    public void setUp() throws Exception
    {
        response = new CreateEnvironmentContainerGroupResponse(  );
        response.addHostInfo( containerHostInfoModel );
    }


    @Test
    public void testGetHosts() throws Exception
    {
        assertEquals( Sets.newHashSet( containerHostInfoModel ), response.getHosts() );
    }


    @Test
    public void testGetDescription() throws Exception
    {
        assertEquals( 1, response.getMessages().size() );
        assertEquals( DESC, response.getMessages().get(0).getValue() );
    }
}
