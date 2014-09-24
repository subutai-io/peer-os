package org.safehaus.subutai.common.protocol;


/**
 * Created by timur on 9/20/14.
 */
public class PeerCommand
{
    private PeerCommandMessage message;
    private PeerCommandType type;


    public PeerCommand( PeerCommandType commandType, PeerCommandMessage message )
    {
        this.type = commandType;
        this.message = message;
    }


    public PeerCommandMessage getMessage()
    {
        return message;
    }


    public void setMessage( final PeerCommandMessage message )
    {
        this.message = message;
    }


    public PeerCommandType getType()
    {
        return type;
    }


    public void setType( final PeerCommandType type )
    {
        this.type = type;
    }
}
