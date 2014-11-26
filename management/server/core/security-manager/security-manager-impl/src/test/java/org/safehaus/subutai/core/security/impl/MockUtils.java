package org.safehaus.subutai.core.security.impl;


import java.util.UUID;

import org.safehaus.subutai.common.protocol.Container;

import com.google.common.collect.Lists;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Created by dilshat on 9/30/14.
 */
public class MockUtils
{


    public static Container getContainer( UUID agentUUID, String hostname, String ip )
    {
        Container container = mock( Container.class );
        when( container.getAgentId() ).thenReturn( agentUUID );
        when( container.getHostname() ).thenReturn( hostname );
        when( container.getIps() ).thenReturn( Lists.newArrayList( ip ) );

        return container;
    }
}
