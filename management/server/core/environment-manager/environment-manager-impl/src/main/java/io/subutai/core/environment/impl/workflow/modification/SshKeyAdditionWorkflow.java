package io.subutai.core.environment.impl.workflow.modification;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.servicemix.beanflow.Workflow;

import com.google.common.collect.Sets;

import io.subutai.common.environment.EnvironmentStatus;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.core.environment.impl.EnvironmentManagerImpl;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.core.environment.impl.workflow.creation.steps.AddSshKeyStep;
import io.subutai.core.network.api.NetworkManager;


public class SshKeyAdditionWorkflow extends Workflow<SshKeyAdditionWorkflow.SshKeyAdditionPhase>
{
    private static final Logger LOG = LoggerFactory.getLogger( SshKeyAdditionWorkflow.class );

    private EnvironmentImpl environment;
    private final String sshKey;
    private final NetworkManager networkManager;
    private final TrackerOperation operationTracker;
    private final EnvironmentManagerImpl environmentManager;


    public enum SshKeyAdditionPhase
    {
        INIT, ADD_KEY, FINALIZE
    }


    public SshKeyAdditionWorkflow( final EnvironmentImpl environment, final String sshKey,
                                   final NetworkManager networkManager, final TrackerOperation operationTracker,
                                   final EnvironmentManagerImpl environmentManager )
    {
        super( SshKeyAdditionPhase.INIT );

        this.environment = environment;
        this.sshKey = sshKey;
        this.networkManager = networkManager;
        this.operationTracker = operationTracker;
        this.environmentManager = environmentManager;
    }


    //********************* WORKFLOW STEPS ************


    public SshKeyAdditionPhase INIT()
    {
        operationTracker.addLog( "Initializing ssh key addition" );

        environment.setStatus( EnvironmentStatus.UNDER_MODIFICATION );

        environment = environmentManager.update( environment );

        return SshKeyAdditionPhase.ADD_KEY;
    }


    public SshKeyAdditionPhase ADD_KEY()
    {

        operationTracker.addLog( "Adding ssh key to containers" );

        try
        {
            new AddSshKeyStep( Sets.newHashSet( sshKey ), environment, networkManager ).execute();

            environment = environmentManager.update( environment );

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
        LOG.info( "Finalizing ssh key addition" );

        environment.setStatus( EnvironmentStatus.HEALTHY );

        environment = environmentManager.update( environment );

        operationTracker.addLogDone( "Ssh key is added" );

        //this is a must have call
        stop();
    }


    @Override
    public void fail( final String message, final Throwable e )
    {
        super.fail( message, e );
        saveFailState();
    }


    private void saveFailState()
    {
        environment.setStatus( EnvironmentStatus.UNHEALTHY );
        environment = environmentManager.update( environment );
        operationTracker.addLogFailed( getFailedReason() );
        LOG.error( "Error adding ssh key", getFailedException() );
    }
}
