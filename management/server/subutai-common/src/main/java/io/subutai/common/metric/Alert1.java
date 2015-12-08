package io.subutai.common.metric;


import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Preconditions;


/**
 * Alert class
 */
//TODO: remove it
public class Alert1
{
    public enum State
    {
        NEW, PROCESSING, DONE;
    }


    private Map<String, State> subscribers = new HashMap<>();

    private Alert alert;
    //    private State state = State.NEW;


    public Alert1( final Alert alert )
    {
        Preconditions.checkNotNull( alert );
        this.alert = alert;
    }


    public Alert getAlert()
    {
        return alert;
    }


    public String getId()
    {
        return alert.getId();
    }


    public void take( String subscriberId )
    {
        subscribers.put( subscriberId, State.PROCESSING );
    }


    public void done( String subscriberId )
    {
        subscribers.put( subscriberId, State.DONE );
    }


    public State getState()
    {
        if ( subscribers.size() == 0 )
        {
            return State.NEW;
        }
        for ( State state : subscribers.values() )
        {
            if ( state == State.PROCESSING )
            {
                return State.PROCESSING;
            }
        }

        return State.DONE;
    }
}
