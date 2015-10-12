package io.subutai.core.environment.impl.workflow.construction;

import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.servicemix.beanflow.Workflow;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import io.subutai.common.environment.Environment;
import io.subutai.common.environment.EnvironmentStatus;
import io.subutai.common.environment.Topology;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.settings.Common;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.core.environment.impl.EnvironmentManagerImpl;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.core.environment.impl.workflow.creation.steps.*;
import io.subutai.core.network.api.NetworkManager;
import io.subutai.core.peer.api.LocalPeer;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.registry.api.TemplateRegistry;


public class EnvironmentImportWorkflow extends Workflow<EnvironmentImportWorkflow.Phase>
{

    private static final Logger LOG = LoggerFactory.getLogger( EnvironmentImportWorkflow.class );

    private final TemplateRegistry templateRegistry;
    private final NetworkManager networkManager;
    private final PeerManager peerManager;
    private final EnvironmentImpl environment;
    private final Topology topology;
    private final String sshKey;
    private final String defaultDomain;
    private final TrackerOperation operationTracker;
    private final EnvironmentManagerImpl environmentManager;

    private Throwable error;

    public EnvironmentImportWorkflow( String defaultDomain, TemplateRegistry templateRegistry,
                                      EnvironmentManagerImpl environmentManager, NetworkManager networkManager,
                                      PeerManager peerManager, EnvironmentImpl environment, Topology topology,
                                      String sshKey, TrackerOperation operationTracker )
    {
        super( Phase.INIT );
        this.environmentManager = environmentManager;
        this.templateRegistry = templateRegistry;
        this.peerManager = peerManager;
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

        environmentManager.saveOrUpdate( environment );

        return Phase.GENERATE_KEYS;
    }

    public Phase GENERATE_KEYS()
    {
        operationTracker.addLog( "Generating PEKs" );

        try
        {
            new io.subutai.core.environment.impl.workflow.creation.steps.PEKGenerationStep( topology, environment, peerManager.getLocalPeer() ).execute();

            environmentManager.saveOrUpdate( environment );

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

            environmentManager.saveOrUpdate( environment );

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

            environmentManager.saveOrUpdate( environment );

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

            environmentManager.saveOrUpdate( environment );

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

            environmentManager.saveOrUpdate( environment );

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

            environmentManager.saveOrUpdate( environment );

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

        environmentManager.saveOrUpdate( environment );

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
        this.error = error;
        LOG.error( "Error creating environment", error );
        operationTracker.addLogFailed( error.getMessage() );
        //stop the workflow
        stop();
    }
}
