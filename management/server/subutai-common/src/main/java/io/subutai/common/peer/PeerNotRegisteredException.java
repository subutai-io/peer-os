package io.subutai.common.peer;


public class PeerNotRegisteredException extends PeerException
{
    public PeerNotRegisteredException()
    {
    }


    public PeerNotRegisteredException( final String message )
    {
        super( message );
    }
}
