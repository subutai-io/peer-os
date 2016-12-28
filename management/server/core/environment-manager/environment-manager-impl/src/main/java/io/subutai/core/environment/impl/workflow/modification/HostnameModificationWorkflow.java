package io.subutai.core.environment.impl.workflow.modification;


import io.subutai.common.environment.EnvironmentStatus;
import io.subutai.common.peer.ContainerId;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.core.environment.api.CancellableWorkflow;
import io.subutai.core.environment.impl.EnvironmentManagerImpl;
import io.subutai.core.environment.impl.entity.LocalEnvironment;
import io.subutai.core.environment.impl.workflow.modification.steps.ChangeHostnameStep;
import io.subutai.core.environment.impl.workflow.modification.steps.UpdateAuthorizedKeysStep;
import io.subutai.core.environment.impl.workflow.modification.steps.UpdateEtcHostsStep;


public class HostnameModificationWorkflow
        extends CancellableWorkflow<HostnameModificationWorkflow.HostnameModificationPhase>
{
    private final TrackerOperation operationTracker;
    private final EnvironmentManagerImpl environmentManager;
    private final ContainerId containerId;
    private String newHostname;
    private boolean skipHostnameUpdate = false;
    private String oldHostname;

    private LocalEnvironment environment;


    public enum HostnameModificationPhase
    {
        INIT, CHANGE_HOSTNAME, UPDATE_ETC_HOSTS_FILE, UPDATE_AUTHORIZED_KEYS_FILE, FINALIZE
    }


    public HostnameModificationWorkflow( final LocalEnvironment environment, final ContainerId containerId,
                                         final String newHostname, final TrackerOperation operationTracker,
                                         final EnvironmentManagerImpl environmentManager, final String oldHostname )
    {
        this( environment, containerId, newHostname, operationTracker, environmentManager );
        this.skipHostnameUpdate = true;
        this.oldHostname = oldHostname;
    }


    public HostnameModificationWorkflow( final LocalEnvironment environment, final ContainerId containerId,
                                         final String newHostname, final TrackerOperation operationTracker,
                                         final EnvironmentManagerImpl environmentManager )
    {
        super( HostnameModificationPhase.INIT );

        this.environment = environment;
        this.operationTracker = operationTracker;
        this.environmentManager = environmentManager;
        this.containerId = containerId;
        this.newHostname = newHostname;
    }


    //********************* WORKFLOW STEPS ************


    public HostnameModificationPhase INIT()
    {
        operationTracker.addLog( "Initializing hostname modification" );

        environment.setStatus( EnvironmentStatus.UNDER_MODIFICATION );

        saveEnvironment();

        return skipHostnameUpdate ? HostnameModificationPhase.UPDATE_ETC_HOSTS_FILE :
               HostnameModificationPhase.CHANGE_HOSTNAME;
    }


    public HostnameModificationPhase CHANGE_HOSTNAME()
    {

        operationTracker.addLog( "Modifying container hostname" );

        ChangeHostnameStep changeHostnameStep =
                new ChangeHostnameStep( environmentManager, environment, containerId, newHostname );

        try
        {
            environment = ( LocalEnvironment ) changeHostnameStep.execute();

            oldHostname = changeHostnameStep.getOldHostname();
            newHostname = changeHostnameStep.getNewHostname();

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

        operationTracker.addLog( "Modifying hosts files" );

        try
        {
            new UpdateEtcHostsStep( environment, oldHostname, newHostname, operationTracker ).execute();

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

        operationTracker.addLog( "Modifying authorized_keys files" );

        try
        {
            new UpdateAuthorizedKeysStep( environment, oldHostname, newHostname, operationTracker ).execute();

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
