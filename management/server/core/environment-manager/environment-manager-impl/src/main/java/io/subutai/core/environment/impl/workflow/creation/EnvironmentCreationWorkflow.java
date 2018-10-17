package io.subutai.core.environment.impl.workflow.creation;


import java.util.Map;

import io.subutai.common.environment.EnvironmentStatus;
import io.subutai.common.environment.Topology;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.core.environment.api.CancellableWorkflow;
import io.subutai.core.environment.impl.EnvironmentManagerImpl;
import io.subutai.core.environment.impl.entity.LocalEnvironment;
import io.subutai.core.environment.impl.workflow.creation.steps.ContainerCloneStep;
import io.subutai.core.environment.impl.workflow.creation.steps.PEKGenerationStep;
import io.subutai.core.environment.impl.workflow.creation.steps.PrepareTemplatesStep;
import io.subutai.core.environment.impl.workflow.creation.steps.RegisterHostsStep;
import io.subutai.core.environment.impl.workflow.creation.steps.RegisterSshStep;
import io.subutai.core.environment.impl.workflow.creation.steps.ReservationStep;
import io.subutai.core.environment.impl.workflow.creation.steps.SetQuotaStep;
import io.subutai.core.environment.impl.workflow.creation.steps.SetupP2PStep;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.bazaar.share.quota.ContainerQuota;


public class EnvironmentCreationWorkflow
        extends CancellableWorkflow<EnvironmentCreationWorkflow.EnvironmentCreationPhase>
{
    private final PeerManager peerManager;
    private final IdentityManager identityManager;
    private final SecurityManager securityManager;
    private LocalEnvironment environment;
    private final Topology topology;
    private final String sshKey;
    private final String defaultDomain;
    private final TrackerOperation operationTracker;
    private final EnvironmentManagerImpl environmentManager;
    private Map<String, ContainerQuota> containerQuotas;


    //environment creation phases
    public enum EnvironmentCreationPhase
    {
        INIT, GENERATE_KEYS, RESERVE_NET, SETUP_P2P, PREPARE_TEMPLATES, CLONE_CONTAINERS, CONFIGURE_HOSTS,
        CONFIGURE_SSH, SET_QUOTA, FINALIZE

    }


    public EnvironmentCreationWorkflow( String defaultDomain, IdentityManager identityManager,
                                        EnvironmentManagerImpl environmentManager, PeerManager peerManager,
                                        SecurityManager securityManager, LocalEnvironment environment,
                                        Topology topology, String sshKey, TrackerOperation operationTracker )
    {
        super( EnvironmentCreationPhase.INIT );

        this.identityManager = identityManager;
        this.environmentManager = environmentManager;
        this.peerManager = peerManager;
        this.securityManager = securityManager;
        this.environment = environment;
        this.topology = topology;
        this.sshKey = sshKey;
        this.operationTracker = operationTracker;
        this.defaultDomain = defaultDomain;
    }


    //********************* WORKFLOW STEPS ************


    public EnvironmentCreationPhase INIT()
    {
        operationTracker.addLog( "Initializing environment creation" );

        environment.setStatus( EnvironmentStatus.UNDER_MODIFICATION );

        saveEnvironment();

        return EnvironmentCreationPhase.GENERATE_KEYS;
    }


    public EnvironmentCreationPhase GENERATE_KEYS()
    {
        operationTracker.addLog( "Securing channel" );

        try
        {
            new PEKGenerationStep( topology, environment, peerManager, securityManager, operationTracker ).execute();

            saveEnvironment();

            return EnvironmentCreationPhase.RESERVE_NET;
        }
        catch ( Exception e )
        {
            fail( e.getMessage(), e );

            return null;
        }
    }


    public EnvironmentCreationPhase RESERVE_NET()
    {
        operationTracker.addLog( "Reserving network resources" );

        try
        {
            new ReservationStep( topology, environment, peerManager, identityManager, operationTracker ).execute();

            saveEnvironment();

            return EnvironmentCreationPhase.SETUP_P2P;
        }
        catch ( Exception e )
        {
            fail( e.getMessage(), e );

            return null;
        }
    }


    public EnvironmentCreationPhase SETUP_P2P()
    {
        operationTracker.addLog( "Setting up networking" );

        try
        {
            new SetupP2PStep( topology, environment, operationTracker ).execute();

            saveEnvironment();

            return EnvironmentCreationPhase.PREPARE_TEMPLATES;
        }
        catch ( Exception e )
        {
            fail( e.getMessage(), e );

            return null;
        }
    }


    public EnvironmentCreationPhase PREPARE_TEMPLATES()
    {
        operationTracker.addLog( "Preparing templates" );

        try
        {
            new PrepareTemplatesStep( environment, peerManager, topology,
                    identityManager.getActiveSession().getCdnToken(), operationTracker ).execute();

            saveEnvironment();

            return EnvironmentCreationPhase.CLONE_CONTAINERS;
        }
        catch ( Exception e )
        {
            fail( e.getMessage(), e );

            return null;
        }
    }


    public EnvironmentCreationPhase CLONE_CONTAINERS()
    {
        operationTracker.addLog( "Cloning containers" );

        try
        {
            containerQuotas =
                    new ContainerCloneStep( defaultDomain, topology, environment, peerManager, identityManager,
                            operationTracker ).execute();

            saveEnvironment();

            return EnvironmentCreationPhase.CONFIGURE_HOSTS;
        }
        catch ( Exception e )
        {
            fail( e.getMessage(), e );

            return null;
        }
    }


    public EnvironmentCreationPhase CONFIGURE_HOSTS()
    {
        operationTracker.addLog( "Configuring hosts" );

        try
        {
            new RegisterHostsStep( topology, environment, operationTracker ).execute();

            saveEnvironment();

            return EnvironmentCreationPhase.CONFIGURE_SSH;
        }
        catch ( Exception e )
        {
            fail( e.getMessage(), e );

            return null;
        }
    }


    public EnvironmentCreationPhase CONFIGURE_SSH()
    {
        operationTracker.addLog( "Configuring ssh" );

        try
        {
            environment.addSshKey( sshKey );

            new RegisterSshStep( topology, environment, operationTracker ).execute();

            saveEnvironment();

            return EnvironmentCreationPhase.SET_QUOTA;
        }
        catch ( Exception e )
        {
            fail( e.getMessage(), e );

            return null;
        }
    }


    public EnvironmentCreationPhase SET_QUOTA()
    {
        operationTracker.addLog( "Settings quotas" );

        try
        {

            new SetQuotaStep( environment, containerQuotas, operationTracker ).execute();

            saveEnvironment();

            return EnvironmentCreationPhase.FINALIZE;
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

        operationTracker.addLogDone( "Environment is created" );

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

        operationTracker.addLogFailed( "Environment creation was cancelled" );
    }


    protected void saveEnvironment()
    {
        environment = environmentManager.update( environment );
    }
}
