package io.subutai.core.environment.impl.workflow.modification;


import java.util.Map;

import io.subutai.common.environment.EnvironmentStatus;
import io.subutai.common.host.HostId;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.core.environment.api.CancellableWorkflow;
import io.subutai.core.environment.impl.EnvironmentManagerImpl;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.core.environment.impl.workflow.modification.steps.ChangeHostnameStep;
import io.subutai.core.environment.impl.workflow.modification.steps.UpdateAuthorizedKeysStep;
import io.subutai.core.environment.impl.workflow.modification.steps.UpdateEtcHostsStep;


public class HostnameModificationWorkflow
        extends CancellableWorkflow<HostnameModificationWorkflow.HostnameModificationPhase>
{
    private EnvironmentImpl environment;
    private final Map<HostId, String> newContainerHostNames;
    private final TrackerOperation operationTracker;
    private final EnvironmentManagerImpl environmentManager;

    private ChangeHostnameStep changeHostnameStep;


    public enum HostnameModificationPhase
    {
        INIT, CHANGE_HOSTNAME, UPDATE_ETC_HOSTS_FILE, UPDATE_AUTHORIZED_KEYS_FILE, FINALIZE
    }


    public HostnameModificationWorkflow( final EnvironmentImpl environment,
                                         final Map<HostId, String> newContainerHostNames, final String newHostname,
                                         final TrackerOperation operationTracker,
                                         final EnvironmentManagerImpl environmentManager )
    {
        super( HostnameModificationPhase.INIT );

        this.environment = environment;
        this.newContainerHostNames = newContainerHostNames;
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

        changeHostnameStep =
                new ChangeHostnameStep( environmentManager, environment, newContainerHostNames, operationTracker );

        try
        {
            environment = ( EnvironmentImpl ) changeHostnameStep.execute();

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
            new UpdateEtcHostsStep( environment, changeHostnameStep.getOldHostNames(), newContainerHostNames,
                    operationTracker ).execute();

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
            new UpdateAuthorizedKeysStep( environment, changeHostnameStep.getOldHostNames(), newContainerHostNames,
                    operationTracker ).execute();

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
