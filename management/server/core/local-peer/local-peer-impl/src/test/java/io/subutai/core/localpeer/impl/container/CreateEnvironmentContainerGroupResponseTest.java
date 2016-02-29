package io.subutai.core.localpeer.impl.container;


import java.util.UUID;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.task.CloneResponse;
import io.subutai.common.environment.CreateEnvironmentContainerGroupResponse;
import io.subutai.common.host.ContainerHostInfoModel;

import com.google.common.collect.Sets;

import static junit.framework.TestCase.assertEquals;


@RunWith( MockitoJUnitRunner.class )
@Ignore
public class CreateEnvironmentContainerGroupResponseTest
{
    private static final String DESC = "description";
    private static final String PEER_ID = UUID.randomUUID().toString();
    @Mock
    ContainerHostInfoModel containerHostInfoModel;

    CreateEnvironmentContainerGroupResponse responseGroup;

    @Mock
    CloneResponse response;

    @Before
    public void setUp() throws Exception
    {
        responseGroup = new CreateEnvironmentContainerGroupResponse( PEER_ID );
        responseGroup.addResponse( response );
    }



}
