package io.subutai.core.registration.api;


/**
 * Created by talas on 8/24/15.
 */
public enum RegistrationStatus
{
    REQUESTED, APPROVED, REJECTED, BLOCKED;


    public RegistrationStatus setRegistered()
    {
        RegistrationStatus result = this;
        if ( this == REQUESTED )
        {
            result = APPROVED;
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
        if ( this == APPROVED )
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
            result = APPROVED;
        }
        return result;
    }
}
