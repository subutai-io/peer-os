package io.subutai.common.peer;


/**
 * Registration status of peer
 */
public enum RegistrationStatus
{
    REQUESTED, CANCELLED, APPROVED, REJECTED, WAIT, UNREGISTERED;


/*
    public RegistrationStatus setRegistered()
    {
        RegistrationStatus result = this;
        if ( this == REQUESTED )
        {
            result = REGISTERED;
        }
        return result;
    }


    public RegistrationStatus setRejected()
    {
        RegistrationStatus result = this;
        if ( this == REQUESTED )
        {
            result = REJECTED;
        }
        return result;
    }


    public RegistrationStatus setBlocked()
    {
        RegistrationStatus result = this;
        if ( this == REGISTERED )
        {
            result = BLOCKED;
        }
        return result;
    }


    public RegistrationStatus setUnblock()
    {
        RegistrationStatus result = this;
        if ( this == BLOCKED )
        {
            result = REGISTERED;
        }
        return result;
    }
*/
}
