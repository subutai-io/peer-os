package io.subutai.core.peer.api;


/**
 * Peer action response class.
 */
public class PeerActionResponse
{
    private enum ResponseType
    {
        OK, FAIL;
    }

    public boolean isOk()
    {
        return this.type == ResponseType.OK;
    }

    private ResponseType type;

    private final Object data;


    private PeerActionResponse( ResponseType type, final Object... data )
    {
        this.type = type;
        this.data = data;
    }


    public static PeerActionResponse Ok( Object... data )
    {
        return new PeerActionResponse( ResponseType.OK );
    }


    public static PeerActionResponse Fail( Object... data )
    {
        return new PeerActionResponse( ResponseType.FAIL, data );
    }


    public ResponseType getType()
    {
        return type;
    }


    public Object getData()
    {
        return data;
    }
}
