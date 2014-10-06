package org.safehaus.subutai.core.dispatcher.impl;


import java.util.List;

import org.safehaus.subutai.common.enums.ResponseType;
import org.safehaus.subutai.common.protocol.Response;
import org.safehaus.subutai.common.util.UUIDUtil;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Mock Utils
 */
public class MockUtils
{
    public static Response getRegistrationRequestFromLxcAgent()
    {
        Response response = mock( Response.class );

        when( response.getUuid() ).thenReturn( UUIDUtil.generateTimeBasedUUID() );
        when( response.isLxc() ).thenReturn( true );
        when( response.getIps() ).thenReturn( mock( List.class ) );
        when( response.getHostname() ).thenReturn( "lxchostname" );
        when( response.getParentHostName() ).thenReturn( "hostname" );
        when( response.getType() ).thenReturn( ResponseType.REGISTRATION_REQUEST );

        return response;
    }
}
