package org.safehaus.subutai.core.template.wizard.impl;


import java.util.concurrent.CopyOnWriteArrayList;

import org.safehaus.subutai.core.template.wizard.api.PhaseLifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by talas on 3/18/15.
 */
public abstract class AbstractPhaseLifecycle implements PhaseLifecycle
{
    private static final Logger LOG = LoggerFactory.getLogger( AbstractPhaseLifecycle.class );

    public static final String STOPPED = "STOPPED";
    public static final String FAILED = "FAILED";
    public static final String STARTING = "STARTING";
    public static final String STARTED = "STARTED";
    public static final String STOPPING = "STOPPING";
    public static final String RUNNING = "RUNNING";

    private final Object _lock = new Object();
    private final int __FAILED = -1, __STOPPED = 0, __STARTING = 1, __STARTED = 2, __STOPPING = 3;
    private volatile int _state = __STOPPED;

    protected final CopyOnWriteArrayList<PhaseLifecycle.Listener> _listeners =
            new CopyOnWriteArrayList<PhaseLifecycle.Listener>();


    protected void doStart() throws Exception
    {
    }


    protected void doStop() throws Exception
    {
    }


    public final void start() throws Exception
    {
        synchronized ( _lock )
        {
            try
            {
                if ( _state == __STARTED || _state == __STARTING )
                {
                    return;
                }
                setStarting();
                doStart();
                setStarted();
            }
            catch ( Exception | Error e )
            {
                setFailed( e );
                throw e;
            }
        }
    }


    public final void stop() throws Exception
    {
        synchronized ( _lock )
        {
            try
            {
                if ( _state == __STOPPING || _state == __STOPPED )
                {
                    return;
                }
                setStopping();
                doStop();
                setStopped();
            }
            catch ( Exception | Error e )
            {
                setFailed( e );
                throw e;
            }
        }
    }


    public boolean isRunning()
    {
        final int state = _state;

        return state == __STARTED || state == __STARTING;
    }


    public boolean isStarted()
    {
        return _state == __STARTED;
    }


    public boolean isStarting()
    {
        return _state == __STARTING;
    }


    public boolean isStopping()
    {
        return _state == __STOPPING;
    }


    public boolean isStopped()
    {
        return _state == __STOPPED;
    }


    public boolean isFailed()
    {
        return _state == __FAILED;
    }


    public void addLifeCycleListener( PhaseLifecycle.Listener listener )
    {
        _listeners.add( listener );
    }


    public void removeLifeCycleListener( PhaseLifecycle.Listener listener )
    {
        _listeners.remove( listener );
    }


    public String getState()
    {
        switch ( _state )
        {
            case __FAILED:
                return FAILED;
            case __STARTING:
                return STARTING;
            case __STARTED:
                return STARTED;
            case __STOPPING:
                return STOPPING;
            case __STOPPED:
                return STOPPED;
        }
        return null;
    }


    public static String getState( PhaseLifecycle lc )
    {
        if ( lc.isStarting() )
        {
            return STARTING;
        }
        if ( lc.isStarted() )
        {
            return STARTED;
        }
        if ( lc.isStopping() )
        {
            return STOPPING;
        }
        if ( lc.isStopped() )
        {
            return STOPPED;
        }
        return FAILED;
    }


    private void setStarted()
    {
        _state = __STARTED;
        LOG.debug( STARTED + " {}", this );
        for ( Listener listener : _listeners )
        {
            listener.lifeCycleStarted( this );
        }
    }


    private void setStarting()
    {
        LOG.debug( "starting {}", this );
        _state = __STARTING;
        for ( Listener listener : _listeners )
        {
            listener.lifeCycleStarting( this );
        }
    }


    private void setStopping()
    {
        LOG.debug( "stopping {}", this );
        _state = __STOPPING;
        for ( Listener listener : _listeners )
        {
            listener.lifeCycleStopping( this );
        }
    }


    private void setStopped()
    {
        _state = __STOPPED;
        LOG.debug( "{} {}", STOPPED, this );
        for ( Listener listener : _listeners )
        {
            listener.lifeCycleStopped( this );
        }
    }


    private void setFailed( Throwable th )
    {
        _state = __FAILED;
        LOG.warn( FAILED + " " + this + ": " + th, th );
        for ( Listener listener : _listeners )
        {
            listener.lifeCycleFailure( this, th );
        }
    }


    public static abstract class AbstractPhaseLifeCycleListener implements PhaseLifecycle.Listener
    {
        public void lifeCycleFailure( PhaseLifecycle event, Throwable cause )
        {
        }


        public void lifeCycleStarted( PhaseLifecycle event )
        {
        }


        public void lifeCycleStarting( PhaseLifecycle event )
        {
        }


        public void lifeCycleStopped( PhaseLifecycle event )
        {
        }


        public void lifeCycleStopping( PhaseLifecycle event )
        {
        }
    }
}
