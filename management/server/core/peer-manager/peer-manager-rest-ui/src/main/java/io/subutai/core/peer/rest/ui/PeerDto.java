package io.subutai.core.peer.rest.ui;


import io.subutai.common.peer.RegistrationData;


public class PeerDto
{
    enum State
    {
        ONLINE, OFFLINE, UNKNOWN
    }


    private State state;
    private RegistrationData registrationData;


    public void setState( final State state )
    {
        this.state = state;
    }


    PeerDto( RegistrationData registrationData )
    {
        this.registrationData = registrationData;
        this.state = State.UNKNOWN;
    }


    RegistrationData getRegistrationData()
    {
        return registrationData;
    }
}
