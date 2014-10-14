package org.safehaus.subutai.core.peer.api;


/**
 * Created by talas on 9/24/14.
 */
public enum PeerStatus
{
    REQUESTED, REGISTERED, REJECTED, BLOCKED, APPROVED, REQUEST_SENT;


    public PeerStatus setRegistered()
    {
        PeerStatus result = this;
        if ( this == REQUESTED )
        {
            result = REGISTERED;
        }
        return result;
    }


    public PeerStatus setRejected()
    {
        PeerStatus result = this;
        if ( this == REQUESTED )
        {
            result = REJECTED;
        }
        return result;
    }


    public PeerStatus setBlocked()
    {
        PeerStatus result = this;
        if ( this == REGISTERED )
        {
            result = BLOCKED;
        }
        return result;
    }


    public PeerStatus setUnblock()
    {
        PeerStatus result = this;
        if ( this == BLOCKED )
        {
            result = REGISTERED;
        }
        return result;
    }
}
