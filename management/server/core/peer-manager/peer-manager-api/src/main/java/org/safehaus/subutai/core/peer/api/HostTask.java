package org.safehaus.subutai.core.peer.api;


import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


abstract public class HostTask<H extends Host, P extends HostTaskParam, R extends HostTaskResult> implements Runnable
{
    protected static final Logger LOG = LoggerFactory.getLogger( HostTask.class );
    private String id;
    protected P param;
    protected volatile Phase phase = Phase.NEW;
    protected R result;
    private Exception exception;
    protected H host;


    public HostTask( H host, P parameter )
    {
        this.id = UUID.randomUUID().toString();
        this.host = host;
        this.param = parameter;
    }


    public String getId()
    {
        return this.id;
    }


    public P getParameter()
    {
        return this.param;
    }


    public Phase getPhase()
    {
        return phase;
    }


    public abstract R getResult();


    public void fail( Exception exception )
    {
        this.exception = exception;
    }


    public Exception getException()
    {
        return exception;
    }


    public void done()
    {
        this.phase = Phase.DONE;
    }


    public H getHost()
    {
        return host;
    }


    public abstract void start();

    public abstract void execute();


    @Override
    public void run()
    {
        LOG.info( String.format( "Task %s started...", id ) );
        host.fireEvent( new HostEvent( host, HostEvent.EventType.HOST_TASK_STARTED, id ) );
        phase = Phase.RUNNING;
        try
        {
            execute();
        }
        catch ( Exception e )
        {
        }
        phase = Phase.DONE;
        host.fireEvent( new HostEvent( host, HostEvent.EventType.HOST_TASK_DONE, id ) );
        LOG.info( String.format( "Task %s finished.", getId() ) );
    }


    public enum Phase
    {
        NEW, RUNNING, DONE
    }
}
