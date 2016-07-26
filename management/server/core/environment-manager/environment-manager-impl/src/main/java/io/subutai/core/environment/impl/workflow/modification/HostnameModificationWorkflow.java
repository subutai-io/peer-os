package io.subutai.core.environment.impl.workflow.modification;


import io.subutai.common.environment.EnvironmentStatus;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.core.environment.api.CancellableWorkflow;
import io.subutai.core.environment.impl.EnvironmentManagerImpl;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;


public class HostnameModificationWorkflow
        extends CancellableWorkflow<HostnameModificationWorkflow.HostnameModificationPhase>
{
    private EnvironmentImpl environment;
    private final ContainerHost containerHost;
    private final String newHostname;
    private final TrackerOperation operationTracker;
    private final EnvironmentManagerImpl environmentManager;


    public enum HostnameModificationPhase
    {
        INIT, CHANGE_HOSTNAME, UPDATE_ETC_HOSTS_FILE, UPDATE_AUTHORIZED_KEYS_FILE, FINALIZE
    }


    public HostnameModificationWorkflow( final EnvironmentImpl environment, final ContainerHost containerHost,
                                         final String newHostname, final TrackerOperation operationTracker,
                                         final EnvironmentManagerImpl environmentManager )
    {
        super( HostnameModificationPhase.INIT );

        this.environment = environment;
        this.newHostname = newHostname;
        this.containerHost = containerHost;
        this.operationTracker = operationTracker;
        this.environmentManager = environmentManager;
    }


    //********************* WORKFLOW STEPS ************


    public HostnameModificationPhase INIT()
    {
        operationTracker.addLog( "Initializing hostname modification" );

        environment.setStatus( EnvironmentStatus.UNDER_MODIFICATION );

        saveEnvironment();

        return HostnameModificationPhase.CHANGE_HOSTNAME;
    }


    public HostnameModificationPhase CHANGE_HOSTNAME()
    {

        operationTracker.addLog( "Modifying container hostname" );

        try
        {
            //todo here

            saveEnvironment();

            return HostnameModificationPhase.UPDATE_ETC_HOSTS_FILE;
        }
        catch ( Exception e )
        {
            fail( e.getMessage(), e );

            return null;
        }
    }


    public HostnameModificationPhase UPDATE_ETC_HOSTS_FILE()
    {

        operationTracker.addLog( "Modifying /etc/hosts files" );

        try
        {
            //todo here

            saveEnvironment();

            return HostnameModificationPhase.UPDATE_AUTHORIZED_KEYS_FILE;
        }
        catch ( Exception e )
        {
            fail( e.getMessage(), e );

            return null;
        }
    }


    public HostnameModificationPhase UPDATE_AUTHORIZED_KEYS_FILE()
    {

        operationTracker.addLog( "Modifying /root/.ssh/authorized_keys files" );

        try
        {
            //todo here

            saveEnvironment();

            return HostnameModificationPhase.FINALIZE;
        }
        catch ( Exception e )
        {
            fail( e.getMessage(), e );

            return null;
        }
    }


    public void FINALIZE()
    {
        environment.setStatus( EnvironmentStatus.HEALTHY );

        saveEnvironment();

        operationTracker.addLogDone( "Hostname is modified" );

        //this is a must have call
        stop();
    }


    @Override
    public void fail( final String message, final Throwable e )
    {
        environment.setStatus( EnvironmentStatus.UNHEALTHY );

        saveEnvironment();

        operationTracker.addLogFailed( message );

        super.fail( message, e );
    }


    @Override
    public void onCancellation()
    {
        environment.setStatus( EnvironmentStatus.CANCELLED );

        saveEnvironment();

        operationTracker.addLogFailed( "Hostname modification was cancelled" );
    }


    protected void saveEnvironment()
    {
        environment = environmentManager.update( environment );
    }
}
