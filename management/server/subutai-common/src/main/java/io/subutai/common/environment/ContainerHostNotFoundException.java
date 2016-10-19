package io.subutai.common.environment;


import io.subutai.common.peer.HostNotFoundException;


public class ContainerHostNotFoundException extends HostNotFoundException
{
    public ContainerHostNotFoundException( final String message )
    {
        super( message );
    }
}
