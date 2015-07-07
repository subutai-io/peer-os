package io.subutai.common.peer;


/**
 * Registration status of peer
 */
public enum PeerStatus
{
    REQUESTED, REGISTERED, REJECTED, BLOCKED, BLOCKED_PEER, APPROVED, REQUEST_SENT;


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
