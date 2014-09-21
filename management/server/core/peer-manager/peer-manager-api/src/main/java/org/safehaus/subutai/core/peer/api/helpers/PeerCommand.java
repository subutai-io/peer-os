package org.safehaus.subutai.core.peer.api.helpers;


import java.util.UUID;


/**
 * Created by timur on 9/20/14.
 */
public class PeerCommand {
    private PeerCommandMessage peerCommandMessage;
    private PeerCommandType type;


    public PeerCommandMessage getPeerCommandMessage()
    {
        return peerCommandMessage;
    }


    public void setPeerCommandMessage( final PeerCommandMessage peerCommandMessage )
    {
        this.peerCommandMessage = peerCommandMessage;
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
