package io.subutai.core.template.wizard.impl;


import java.util.concurrent.CopyOnWriteArrayList;

import io.subutai.core.template.wizard.api.PhaseLifecycle;
import io.subutai.core.template.wizard.api.exception.TemplateWizardException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class AbstractPhaseLifecycle implements PhaseLifecycle
{
    private static final Logger LOG = LoggerFactory.getLogger( AbstractPhaseLifecycle.class );

    public static final String STOPPED = "STOPPED";
    public static final String FAILED = "FAILED";
    public static final String STARTING = "STARTING";
    public static final String STARTED = "STARTED";
    public static final String STOPPING = "STOPPING";
    public static final String RUNNING = "RUNNING";

    private final Object lock = new Object();
    private final int FAILED_CODE = -1, STOPPED_CODE = 0, STARTING_CODE = 1, STARTED_CODE = 2, STOPPING_CODE = 3;
    private volatile int state = STOPPED_CODE;

    protected final CopyOnWriteArrayList<PhaseLifecycle.Listener> _listeners = new CopyOnWriteArrayList<>();


    protected void doStart() throws TemplateWizardException
    {
    }


    protected void doStop() throws TemplateWizardException
    {
    }


    public final void start() throws TemplateWizardException
    {
        synchronized ( lock )
        {
            try
            {
                if ( state == STARTED_CODE || state == STARTING_CODE )
                {
                    return;
                }
                setStarting();
                doStart();
                setStarted();
            }
            catch ( Exception e )
            {
                setFailed( e );
                throw e;
            }
        }
    }


    public final void stop() throws TemplateWizardException
    {
        synchronized ( lock )
        {
            try
            {
                if ( state == STOPPING_CODE || state == STOPPED_CODE )
                {
                    return;
                }
                setStopping();
                doStop();
                setStopped();
            }
            catch ( Exception e )
            {
                setFailed( e );
                throw new TemplateWizardException( e );
            }
        }
    }


    public boolean isRunning()
    {
        final int state = this.state;

        return state == STARTED_CODE || state == STARTING_CODE;
    }


    public boolean isStarted()
    {
        return state == STARTED_CODE;
    }


    public boolean isStarting()
    {
        return state == STARTING_CODE;
    }


    public boolean isStopping()
    {
        return state == STOPPING_CODE;
    }


    public boolean isStopped()
    {
        return state == STOPPED_CODE;
    }


    public boolean isFailed()
    {
        return state == FAILED_CODE;
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
        switch ( state )
        {
            case FAILED_CODE:
                return FAILED;
            case STARTING_CODE:
                return STARTING;
            case STARTED_CODE:
                return STARTED;
            case STOPPING_CODE:
                return STOPPING;
            case STOPPED_CODE:
                return STOPPED;
            default:
                return null;
        }
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
        state = STARTED_CODE;
        LOG.debug( STARTED + " {}", this );
        for ( Listener listener : _listeners )
        {
            listener.lifeCycleStarted( this );
        }
    }


    private void setStarting()
    {
        LOG.debug( "starting {}", this );
        state = STARTING_CODE;
        for ( Listener listener : _listeners )
        {
            listener.lifeCycleStarting( this );
        }
    }


    private void setStopping()
    {
        LOG.debug( "stopping {}", this );
        state = STOPPING_CODE;
        for ( Listener listener : _listeners )
        {
            listener.lifeCycleStopping( this );
        }
    }


    private void setStopped()
    {
        state = STOPPED_CODE;
        LOG.debug( "{} {}", STOPPED, this );
        for ( Listener listener : _listeners )
        {
            listener.lifeCycleStopped( this );
        }
    }


    private void setFailed( Throwable th )
    {
        state = FAILED_CODE;
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
