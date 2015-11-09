package io.subutai.core.localpeer.impl.container;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import io.subutai.common.host.HostInfoModel;

import com.google.common.collect.Sets;

import static junit.framework.TestCase.assertEquals;


@RunWith( MockitoJUnitRunner.class )
public class CreateEnvironmentContainerGroupResponseTest
{
    @Mock
    HostInfoModel hostInfoModel;

    CreateEnvironmentContainerGroupResponse response;


    @Before
    public void setUp() throws Exception
    {
        response = new CreateEnvironmentContainerGroupResponse( Sets.newHashSet( hostInfoModel ) );
    }


    @Test
    public void testGetHosts() throws Exception
    {
        assertEquals( Sets.newHashSet( hostInfoModel ), response.getHosts() );
    }
}
