package org.safehaus.subutai.core.template.wizard.api;


import java.util.EventListener;


/**
 * Created by talas on 3/18/15.
 */
public interface PhaseLifecycle
{

    public void start() throws Exception;

    ;


    public void stop() throws Exception;

    ;


    public boolean isStarting();


    public boolean isStopping();


    public boolean isFailed();


    public boolean isStarted();


    public boolean isStopped();

    /* ------------------------------------------------------------ */
    public void addLifeCycleListener( PhaseLifecycle.Listener listener );

    /* ------------------------------------------------------------ */
    public void removeLifeCycleListener( PhaseLifecycle.Listener listener );

    /* ------------------------------------------------------------ */


    /**
     * Listener. A listener for Lifecycle events.
     */
    public interface Listener extends EventListener
    {
        public void lifeCycleStarting( PhaseLifecycle event );

        public void lifeCycleStarted( PhaseLifecycle event );

        public void lifeCycleFailure( PhaseLifecycle event, Throwable cause );

        public void lifeCycleStopping( PhaseLifecycle event );

        public void lifeCycleStopped( PhaseLifecycle event );
    }
}
