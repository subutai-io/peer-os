package io.subutai.common.metric;


import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Preconditions;

import io.subutai.common.environment.Environment;
import io.subutai.common.host.HostId;
import io.subutai.common.peer.EnvironmentId;


/**
 * Alert class
 */
public class Alert
{
    public enum State
    {
        NEW, PROCESSING, DONE;
    }


    private Map<String, State> subscribers = new HashMap<>();

    private AlertValue alert;
    //    private State state = State.NEW;


    public Alert( final AlertValue alert )
    {
        Preconditions.checkNotNull( alert );
        this.alert = alert;
    }


    public AlertValue getAlert()
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
