package io.subutai.core.environment.impl.workflow.modification;


import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;

import io.subutai.bazaar.share.quota.ContainerQuota;
import io.subutai.common.environment.EnvironmentStatus;
import io.subutai.common.environment.Topology;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.common.util.CollectionUtil;
import io.subutai.core.environment.api.CancellableWorkflow;
import io.subutai.core.environment.impl.EnvironmentManagerImpl;
import io.subutai.core.environment.impl.entity.LocalEnvironment;
import io.subutai.core.environment.impl.workflow.creation.steps.ContainerCloneStep;
import io.subutai.core.environment.impl.workflow.creation.steps.PrepareTemplatesStep;
import io.subutai.core.environment.impl.workflow.creation.steps.RegisterHostsStep;
import io.subutai.core.environment.impl.workflow.creation.steps.RegisterSshStep;
import io.subutai.core.environment.impl.workflow.creation.steps.SetQuotaStep;
import io.subutai.core.environment.impl.workflow.modification.steps.ChangeQuotaStep;
import io.subutai.core.environment.impl.workflow.modification.steps.DestroyContainersStep;
import io.subutai.core.environment.impl.workflow.modification.steps.PEKGenerationStep;
import io.subutai.core.environment.impl.workflow.modification.steps.RemoveHostnamesFromEtcHostsStep;
import io.subutai.core.environment.impl.workflow.modification.steps.ReservationStep;
import io.subutai.core.environment.impl.workflow.modification.steps.SetupP2PStep;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.security.api.SecurityManager;


public class EnvironmentModifyWorkflow
        extends CancellableWorkflow<EnvironmentModifyWorkflow.EnvironmentModificationPhase>
{
    private final IdentityManager identityManager;
    private final PeerManager peerManager;
    private LocalEnvironment environment;
    private final Topology topology;
    private Set<String> removedContainers;
    private Map<String, ContainerQuota> changedContainers;
    private final String defaultDomain;
    private final TrackerOperation operationTracker;
    private final EnvironmentManagerImpl environmentManager;
    private final SecurityManager securityManager;


    private boolean hasQuotaModification = false;
    private boolean hasContainerDestruction = false;
    private boolean hasContainerCreation = false;
    private Map<String, ContainerQuota> containerQuotas;
    private Set<ContainerHost> removedContainerHosts = Sets.newHashSet();


    //environment modification phases
    public enum EnvironmentModificationPhase
    {
        INIT, GENERATE_KEYS, RESERVE_NET, SETUP_P2P, PREPARE_TEMPLATES, CLONE_CONTAINERS, CONFIGURE_HOSTS,
        CONFIGURE_SSH, SET_QUOTA, MODIFY_QUOTA, DESTROY_CONTAINERS, REMOVE_HOSTNAMES, FINALIZE

    }


    public EnvironmentModifyWorkflow( String defaultDomain, IdentityManager identityManager, PeerManager peerManager,
                                      SecurityManager securityManager, LocalEnvironment environment, Topology topology,
                                      Set<String> removedContainers, Map<String, ContainerQuota> changedContainers,
                                      TrackerOperation operationTracker, EnvironmentManagerImpl environmentManager )
    {

        super( EnvironmentModificationPhase.INIT );

        this.identityManager = identityManager;
        this.peerManager = peerManager;
        this.securityManager = securityManager;
        this.environment = environment;
        this.topology = topology;
        this.operationTracker = operationTracker;
        this.defaultDomain = defaultDomain;
        this.environmentManager = environmentManager;
        this.removedContainers = removedContainers;
        this.changedContainers = changedContainers;
    }


    //********************* WORKFLOW STEPS ************


    public EnvironmentModificationPhase INIT()
    {
        hasQuotaModification = !CollectionUtil.isMapEmpty( changedContainers );
        hasContainerDestruction = !CollectionUtil.isCollectionEmpty( removedContainers );
        hasContainerCreation = topology != null && !CollectionUtil.isCollectionEmpty( topology.getAllPeers() );

        operationTracker.addLog( "Initializing environment modification" );

        environment.setStatus( EnvironmentStatus.UNDER_MODIFICATION );

        saveEnvironment();

        return hasContainerCreation ? EnvironmentModificationPhase.GENERATE_KEYS :
               ( hasQuotaModification ? EnvironmentModificationPhase.MODIFY_QUOTA :
                 EnvironmentModificationPhase.DESTROY_CONTAINERS );
    }


    public EnvironmentModificationPhase GENERATE_KEYS()
    {
        operationTracker.addLog( "Securing channel" );

        try
        {
            new PEKGenerationStep( topology, environment, peerManager, securityManager, operationTracker ).execute();

            saveEnvironment();

            return EnvironmentModificationPhase.RESERVE_NET;
        }
        catch ( Exception e )
        {
            fail( e.getMessage(), e );

            return null;
        }
    }


    public EnvironmentModificationPhase RESERVE_NET()
    {
        operationTracker.addLog( "Reserving network resources" );

        try
        {
            new ReservationStep( topology, environment, peerManager, identityManager, operationTracker ).execute();

            saveEnvironment();

            return EnvironmentModificationPhase.SETUP_P2P;
        }
        catch ( Exception e )
        {
            fail( e.getMessage(), e );

            return null;
        }
    }


    public EnvironmentModificationPhase SETUP_P2P()
    {
        operationTracker.addLog( "Setting up networking" );

        try
        {
            new SetupP2PStep( topology, environment, operationTracker ).execute();

            saveEnvironment();

            return EnvironmentModificationPhase.PREPARE_TEMPLATES;
        }
        catch ( Exception e )
        {
            fail( e.getMessage(), e );

            return null;
        }
    }


    public EnvironmentModificationPhase PREPARE_TEMPLATES()
    {
        operationTracker.addLog( "Preparing templates" );

        try
        {
            new PrepareTemplatesStep( environment, peerManager, topology,
                    identityManager.getActiveSession().getCdnToken(), operationTracker ).execute();

            saveEnvironment();

            return EnvironmentModificationPhase.CLONE_CONTAINERS;
        }
        catch ( Exception e )
        {
            fail( e.getMessage(), e );
            return null;
        }
    }


    public EnvironmentModificationPhase CLONE_CONTAINERS()
    {
        operationTracker.addLog( "Cloning containers" );

        try
        {
            containerQuotas =
                    new ContainerCloneStep( defaultDomain, topology, environment, peerManager, identityManager,
                            operationTracker ).execute();

            saveEnvironment();

            return EnvironmentModificationPhase.CONFIGURE_HOSTS;
        }
        catch ( Exception e )
        {
            fail( e.getMessage(), e );

            return null;
        }
    }


    public EnvironmentModificationPhase CONFIGURE_HOSTS()
    {
        operationTracker.addLog( "Configuring hosts" );

        try
        {
            new RegisterHostsStep( topology, environment, operationTracker ).execute();

            saveEnvironment();

            return EnvironmentModificationPhase.CONFIGURE_SSH;
        }
        catch ( Exception e )
        {
            fail( e.getMessage(), e );

            return null;
        }
    }


    public EnvironmentModificationPhase CONFIGURE_SSH()
    {
        operationTracker.addLog( "Configuring ssh" );

        try
        {
            new RegisterSshStep( topology, environment, operationTracker ).execute();

            saveEnvironment();

            return EnvironmentModificationPhase.SET_QUOTA;
        }
        catch ( Exception e )
        {
            fail( e.getMessage(), e );

            return null;
        }
    }


    public EnvironmentModificationPhase SET_QUOTA()
    {
        operationTracker.addLog( "Setting quotas" );

        try
        {
            new SetQuotaStep( environment, containerQuotas, operationTracker ).execute();

            saveEnvironment();

            return hasQuotaModification ? EnvironmentModificationPhase.MODIFY_QUOTA :
                   ( hasContainerDestruction ? EnvironmentModificationPhase.DESTROY_CONTAINERS :
                     EnvironmentModificationPhase.FINALIZE );
        }
        catch ( Exception e )
        {
            fail( e.getMessage(), e );

            return null;
        }
    }


    public EnvironmentModificationPhase MODIFY_QUOTA()
    {
        operationTracker.addLog( "Changing container quotas" );

        try
        {
            new ChangeQuotaStep( environment, changedContainers, operationTracker ).execute();

            environment = ( LocalEnvironment ) environmentManager.loadEnvironment( environment.getId() );

            return hasContainerDestruction ? EnvironmentModificationPhase.DESTROY_CONTAINERS :
                   EnvironmentModificationPhase.FINALIZE;
        }
        catch ( Exception e )
        {
            fail( e.getMessage(), e );
        }

        return null;
    }


    public EnvironmentModificationPhase DESTROY_CONTAINERS()
    {
        operationTracker.addLog( "Destroying containers" );

        try
        {
            if ( environment.getContainerHosts().size() <= removedContainers.size() )
            {
                String errMsg =
                        "Environment will have 0 containers after modification. Please, destroy environment instead. "
                                + "Container destruction has been skipped";
                if ( !( hasContainerCreation || hasQuotaModification ) )
                {
                    operationTracker.addLogFailed( errMsg );
                }
                else
                {
                    operationTracker.addLog( errMsg );
                }

                return EnvironmentModificationPhase.FINALIZE;
            }
            else
            {
                for ( String removeContainerHostId : removedContainers )
                {
                    removedContainerHosts.add( environment.getContainerHostById( removeContainerHostId ) );
                }

                environment = ( LocalEnvironment ) new DestroyContainersStep( environment, environmentManager,
                        removedContainers, operationTracker ).execute();

                saveEnvironment();

                return EnvironmentModificationPhase.REMOVE_HOSTNAMES;
            }
        }
        catch ( Exception e )
        {
            fail( e.getMessage(), e );

            return null;
        }
    }


    public EnvironmentModificationPhase REMOVE_HOSTNAMES()
    {

        operationTracker.addLog( "Removing hostnames" );

        try
        {
            new RemoveHostnamesFromEtcHostsStep( environment, removedContainerHosts, operationTracker ).execute();

            return EnvironmentModificationPhase.FINALIZE;
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

        operationTracker.addLogDone( "Environment has been modified" );

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

        operationTracker.addLogFailed( "Environment modification has been cancelled" );
    }


    protected void saveEnvironment()
    {
        environment = environmentManager.update( environment );
    }
}
