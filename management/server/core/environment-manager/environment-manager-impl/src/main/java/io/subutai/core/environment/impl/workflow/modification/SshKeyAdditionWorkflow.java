package io.subutai.core.environment.impl.workflow.modification;


import org.apache.servicemix.beanflow.Workflow;

import io.subutai.common.environment.EnvironmentStatus;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.core.environment.impl.EnvironmentManagerImpl;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.core.environment.impl.workflow.creation.steps.AddSshKeyStep;


public class SshKeyAdditionWorkflow extends Workflow<SshKeyAdditionWorkflow.SshKeyAdditionPhase>
{
    private EnvironmentImpl environment;
    private final String sshKey;
    private final TrackerOperation operationTracker;
    private final EnvironmentManagerImpl environmentManager;


    public enum SshKeyAdditionPhase
    {
        INIT, ADD_KEY, FINALIZE
    }


    public SshKeyAdditionWorkflow( final EnvironmentImpl environment, final String sshKey,
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


    protected void saveEnvironment()
    {
        environment = environmentManager.update( environment );
    }
}
