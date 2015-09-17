package io.subutai.core.environment.impl.workflow.modification;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.servicemix.beanflow.Workflow;

import io.subutai.common.environment.EnvironmentStatus;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.core.environment.impl.workflow.creation.steps.SetSshKeyStep;
import io.subutai.core.network.api.NetworkManager;


public class SshKeyModificationWorkflow extends Workflow<SshKeyModificationWorkflow.SshKeyModificationPhase>
{
    private static final Logger LOG = LoggerFactory.getLogger( SshKeyModificationWorkflow.class );

    private final EnvironmentImpl environment;
    private final String sshKey;
    private final NetworkManager networkManager;
    private final TrackerOperation operationTracker;

    private Throwable error;


    public static enum SshKeyModificationPhase
    {
        INIT, REPLACE_KEY, FINALIZE
    }


    public SshKeyModificationWorkflow( final EnvironmentImpl environment, final String sshKey,
                                       final NetworkManager networkManager, final TrackerOperation operationTracker )
    {
        super( SshKeyModificationPhase.INIT );

        this.environment = environment;
        this.sshKey = sshKey;
        this.networkManager = networkManager;
        this.operationTracker = operationTracker;
    }


    //********************* WORKFLOW STEPS ************


    public SshKeyModificationPhase INIT()
    {
        operationTracker.addLog( "Initializing ssh key modification" );

        environment.setStatus( EnvironmentStatus.UNDER_MODIFICATION );

        return SshKeyModificationPhase.REPLACE_KEY;
    }


    public SshKeyModificationPhase REPLACE_KEY()
    {

        operationTracker.addLog( "Modifying ssh key in containers" );

        try
        {
            new SetSshKeyStep( sshKey, environment, networkManager ).execute();

            return SshKeyModificationPhase.FINALIZE;
        }
        catch ( Exception e )
        {
            setError( e );

            return null;
        }
    }


    public void FINALIZE()
    {
        LOG.info( "Finalizing ssh key modification" );

        environment.setStatus( EnvironmentStatus.HEALTHY );

        operationTracker.addLogDone( "Ssh key is modified" );

        //this is a must have call
        stop();
    }


    public Throwable getError()
    {
        return error;
    }


    public void setError( final Throwable error )
    {
        environment.setStatus( EnvironmentStatus.UNHEALTHY );
        this.error = error;
        LOG.error( "Error modifying ssh key", error );
        operationTracker.addLogFailed( error.getMessage() );
        //stop the workflow
        stop();
    }
}
