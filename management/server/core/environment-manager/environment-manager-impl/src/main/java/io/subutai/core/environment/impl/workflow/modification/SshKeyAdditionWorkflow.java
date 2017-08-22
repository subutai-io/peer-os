package io.subutai.core.environment.impl.workflow.modification;


import io.subutai.common.environment.EnvironmentStatus;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.core.environment.impl.EnvironmentManagerImpl;
import io.subutai.core.environment.impl.entity.LocalEnvironment;
import io.subutai.core.environment.api.CancellableWorkflow;
import io.subutai.core.environment.impl.workflow.creation.steps.AddSshKeyStep;


public class SshKeyAdditionWorkflow extends CancellableWorkflow<SshKeyAdditionWorkflow.SshKeyAdditionPhase>
{
    private LocalEnvironment environment;
    private final String sshKey;
    private final TrackerOperation operationTracker;
    private final EnvironmentManagerImpl environmentManager;


    public enum SshKeyAdditionPhase
    {
        INIT, ADD_KEY, FINALIZE
    }


    public SshKeyAdditionWorkflow( final LocalEnvironment environment, final String sshKey,
                                   final TrackerOperation operationTracker,
                                   final EnvironmentManagerImpl environmentManager )
    {
        super( SshKeyAdditionPhase.INIT );

        this.environment = environment;
        this.sshKey = sshKey;
        this.operationTracker = operationTracker;
        this.environmentManager = environmentManager;
    }


    //********************* WORKFLOW STEPS ************


    public SshKeyAdditionPhase INIT()
    {
        operationTracker.addLog( "Initializing ssh key addition" );

        environment.setStatus( EnvironmentStatus.UNDER_MODIFICATION );

        saveEnvironment();

        return SshKeyAdditionPhase.ADD_KEY;
    }


    public SshKeyAdditionPhase ADD_KEY()
    {

        operationTracker.addLog( "Adding ssh key to containers" );

        try
        {
            environment.addSshKey( sshKey );

            new AddSshKeyStep( sshKey, environment, operationTracker ).execute();

            saveEnvironment();

            return SshKeyAdditionPhase.FINALIZE;
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

        operationTracker.addLogDone( "Ssh key is added" );

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

        operationTracker.addLogFailed( "Ssh key addition was cancelled" );
    }


    protected void saveEnvironment()
    {
        environment = environmentManager.update( environment );
    }
}
