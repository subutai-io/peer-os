package io.subutai.core.peer.impl.container;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.peer.HostInfoModel;

import com.google.common.collect.Sets;

import io.subutai.core.peer.impl.container.CreateContainerGroupResponse;

import static junit.framework.TestCase.assertEquals;


@RunWith( MockitoJUnitRunner.class )
public class CreateContainerGroupResponseTest
{
    @Mock
    HostInfoModel hostInfoModel;

    CreateContainerGroupResponse response;


    @Before
    public void setUp() throws Exception
    {
        response = new CreateContainerGroupResponse( Sets.newHashSet( hostInfoModel ) );
    }


    @Test
    public void testGetHosts() throws Exception
    {
        assertEquals( Sets.newHashSet( hostInfoModel ), response.getHosts() );
    }
}
