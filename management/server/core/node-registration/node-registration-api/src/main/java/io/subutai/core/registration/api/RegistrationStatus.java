package io.subutai.core.registration.api;


public enum RegistrationStatus
{
    REQUESTED, APPROVED, REJECTED, BLOCKED, REGISTERED;


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
