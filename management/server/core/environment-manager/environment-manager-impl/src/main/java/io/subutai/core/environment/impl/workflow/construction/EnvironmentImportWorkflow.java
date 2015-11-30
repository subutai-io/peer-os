package io.subutai.core.environment.impl.workflow.construction;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.servicemix.beanflow.Workflow;

import io.subutai.common.environment.EnvironmentStatus;
import io.subutai.common.environment.Topology;
import io.subutai.common.settings.Common;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.core.environment.impl.EnvironmentManagerImpl;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.core.environment.impl.workflow.creation.steps.PEKGenerationStep;
import io.subutai.core.environment.impl.workflow.creation.steps.RegisterHostsStep;
import io.subutai.core.environment.impl.workflow.creation.steps.RegisterSshStep;
import io.subutai.core.environment.impl.workflow.creation.steps.SetSshKeyStep;
import io.subutai.core.environment.impl.workflow.creation.steps.SetupN2NStep;
import io.subutai.core.environment.impl.workflow.creation.steps.VNISetupStep;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.kurjun.api.TemplateManager;
import io.subutai.core.network.api.NetworkManager;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.security.api.SecurityManager;


public class EnvironmentImportWorkflow extends Workflow<EnvironmentImportWorkflow.Phase>
{

    private static final Logger LOG = LoggerFactory.getLogger( EnvironmentImportWorkflow.class );

    private final TemplateManager templateRegistry;
    private final NetworkManager networkManager;
    private final PeerManager peerManager;
    private final SecurityManager securityManager;
    private EnvironmentImpl environment;
    private final Topology topology;
    private final String sshKey;
    private final String defaultDomain;
    private final TrackerOperation operationTracker;
    private final EnvironmentManagerImpl environmentManager;

    private Throwable error;
    private IdentityManager identityManager;


    public EnvironmentImportWorkflow( String defaultDomain, TemplateManager templateRegistry,
                                      EnvironmentManagerImpl environmentManager, NetworkManager networkManager,
                                      PeerManager peerManager, SecurityManager securityManager,
                                      IdentityManager identityManager, EnvironmentImpl environment, Topology topology,
                                      String sshKey, TrackerOperation operationTracker )
    {
        super( Phase.INIT );
        this.identityManager = identityManager;
        this.environmentManager = environmentManager;
        this.templateRegistry = templateRegistry;
        this.peerManager = peerManager;
        this.securityManager = securityManager;
        this.networkManager = networkManager;
        this.environment = environment;
        this.topology = topology;
        this.sshKey = sshKey;
        this.operationTracker = operationTracker;
        this.defaultDomain = defaultDomain;
    }


    public enum Phase
    {
        INIT,
        GENERATE_KEYS,
        SETUP_VNI,
        SETUP_N2N,
        CONFIGURE_HOSTS,
        CONFIGURE_SSH,
        SET_ENVIRONMENT_SSH_KEY,
        FINALIZE
    }


    public Phase INIT()
    {

        environment.setStatus( EnvironmentStatus.IMPORTING );
        environment.setSuperNode( peerManager.getLocalPeerInfo().getIp() );
        environment.setSuperNodePort( Common.SUPER_NODE_PORT );

        environment = environmentManager.saveOrUpdate( environment );

        return Phase.GENERATE_KEYS;
    }


    public Phase GENERATE_KEYS()
    {
        operationTracker.addLog( "Generating PEKs" );

        try
        {
            new PEKGenerationStep( topology, environment, peerManager.getLocalPeer(), securityManager,
                    identityManager.getActiveUser() ).execute();

            environment = environmentManager.saveOrUpdate( environment );

            return Phase.SETUP_VNI;
        }
        catch ( Exception e )
        {
            setError( e );

            return null;
        }
    }


    public Phase SETUP_VNI()
    {
        operationTracker.addLog( "Setting up VNI" );

        try
        {
            new VNISetupStep( topology, environment, peerManager.getLocalPeer() ).execute();

            environment = environmentManager.saveOrUpdate( environment );

            return Phase.SETUP_N2N;
        }
        catch ( Exception e )
        {
            setError( e );

            return null;
        }
    }


    public Phase SETUP_N2N()
    {
        operationTracker.addLog( "Setting up N2N" );

        try
        {
            new SetupN2NStep( topology, environment, /*peerManager.getLocalPeer().getPeerInfo().getIp(),
                    Common.SUPER_NODE_PORT, */peerManager.getLocalPeer() ).execute();

            environment = environmentManager.saveOrUpdate( environment );

            return Phase.CONFIGURE_HOSTS;
        }
        catch ( Exception e )
        {
            setError( e );

            return null;
        }
    }


    public Phase CONFIGURE_HOSTS()
    {
        operationTracker.addLog( "Configuring /etc/hosts" );

        try
        {
            new RegisterHostsStep( environment, networkManager ).execute();

            environment = environmentManager.saveOrUpdate( environment );

            return Phase.CONFIGURE_SSH;
        }
        catch ( Exception e )
        {
            setError( e );

            return null;
        }
    }


    public Phase CONFIGURE_SSH()
    {
        operationTracker.addLog( "Configuring ssh" );

        try
        {
            new RegisterSshStep( environment, networkManager ).execute();

            environment = environmentManager.saveOrUpdate( environment );

            return Phase.SET_ENVIRONMENT_SSH_KEY;
        }
        catch ( Exception e )
        {
            setError( e );

            return null;
        }
    }


    public Phase SET_ENVIRONMENT_SSH_KEY()
    {
        operationTracker.addLog( "Setting environment ssh key to containers" );

        try
        {
            new SetSshKeyStep( sshKey, environment, networkManager ).execute();

            environment = environmentManager.saveOrUpdate( environment );

            return Phase.FINALIZE;
        }
        catch ( Exception e )
        {
            setError( e );

            return null;
        }
    }


    public void FINALIZE()
    {
        LOG.info( "Finalizing environment creation" );

        environment.setStatus( EnvironmentStatus.HEALTHY );

        environment = environmentManager.saveOrUpdate( environment );

        operationTracker.addLogDone( "Environment is created" );

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
        environment = environmentManager.saveOrUpdate( environment );
        this.error = error;
        LOG.error( "Error creating environment", error );
        operationTracker.addLogFailed( error.getMessage() );
        //stop the workflow
        stop();
    }
}
