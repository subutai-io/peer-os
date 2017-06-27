package io.subutai.core.peer.rest.ui;


import io.subutai.common.peer.RegistrationData;


/**
 * Created by Dilshat on 27-Jun-17.
 */
public class PeerDto
{
    enum State
    {
        ONLINE, OFFFLINE, UNKNOWN
    }


    private State state;
    private RegistrationData registrationData;


    public State getState()
    {
        return state;
    }


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
