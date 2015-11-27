package io.subutai.common.metric;


import java.util.Calendar;
import java.util.Date;

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


    private HostId hostId;
    private EnvironmentId environmentId;
    private AlertValue value;
    private String processorName;
    private Date processingStarted;
    private State state = State.NEW;


    public Alert( final HostId hostId, final AlertValue value )
    {
        this.hostId = hostId;
        this.value = value;
    }


    public HostId getHostId()
    {
        return hostId;
    }


    public AlertValue getValue()
    {
        return value;
    }


    public void take( String processorName, Environment environment )
    {
        if ( environmentId.equals( environment.getEnvironmentId() ) )
        {
            this.processingStarted = Calendar.getInstance().getTime();
            this.state = State.PROCESSING;
            this.processorName = processorName;
        }
    }


    public Date getProcessingStarted()
    {
        return processingStarted;
    }


    public State getState()
    {
        return state;
    }


    public String getProcessorName()
    {
        return processorName;
    }
}
