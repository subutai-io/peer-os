package io.subutai.core.localpeer.impl.container;


import java.util.UUID;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.task.CloneResponse;
import io.subutai.common.environment.CreateEnvironmentContainerResponseCollector;
import io.subutai.common.host.ContainerHostInfoModel;


@RunWith( MockitoJUnitRunner.class )
@Ignore
public class CreateEnvironmentContainerGroupResponseTest
{
    private static final String DESC = "description";
    private static final String PEER_ID = UUID.randomUUID().toString();
    @Mock
    ContainerHostInfoModel containerHostInfoModel;

    CreateEnvironmentContainerResponseCollector responseGroup;

    @Mock
    CloneResponse response;

    @Before
    public void setUp() throws Exception
    {
        responseGroup = new CreateEnvironmentContainerResponseCollector( PEER_ID );
    }



}
