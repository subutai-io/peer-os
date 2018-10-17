package io.subutai.core.environment.impl.adapter;


import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.reflect.TypeToken;

import io.subutai.common.command.CommandException;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.environment.ContainerHostNotFoundException;
import io.subutai.common.environment.EnvironmentPeer;
import io.subutai.common.environment.EnvironmentStatus;
import io.subutai.common.environment.RhP2pIp;
import io.subutai.common.exception.ActionFailedException;
import io.subutai.common.host.ContainerHostInfo;
import io.subutai.common.peer.EnvironmentContainerHost;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.security.SshKey;
import io.subutai.common.security.SshKeys;
import io.subutai.common.settings.Common;
import io.subutai.common.util.P2PUtil;
import io.subutai.common.util.ServiceLocator;
import io.subutai.core.environment.impl.EnvironmentManagerImpl;
import io.subutai.core.environment.impl.entity.EnvironmentContainerImpl;
import io.subutai.core.environment.impl.entity.LocalEnvironment;
import io.subutai.core.bazaarmanager.api.BazaarManager;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.bazaar.share.common.BazaaarAdapter;
import io.subutai.bazaar.share.json.JsonUtil;


public class EnvironmentAdapter
{
    private final Logger log = LoggerFactory.getLogger( getClass() );

    private final EnvironmentManagerImpl environmentManager;

    private final ProxyContainerHelper proxyContainerHelper;

    private final BazaaarAdapter bazaaarAdapter;
    private final IdentityManager identityManager;


    public EnvironmentAdapter( EnvironmentManagerImpl environmentManager, PeerManager peerManager,
                               BazaaarAdapter bazaaarAdapter, IdentityManager identityManager )
    {
        this.environmentManager = environmentManager;

        proxyContainerHelper = new ProxyContainerHelper( peerManager );

        this.bazaaarAdapter = bazaaarAdapter;

        this.identityManager = identityManager;
    }


    public BazaaarAdapter getBazaaarAdapter()
    {
        return bazaaarAdapter;
    }


    public BazaarEnvironment get( String id )
    {
        try
        {
            for ( BazaarEnvironment e : getEnvironments( identityManager.isTenantManager() ) )
            {
                if ( e.getId().equals( id ) )
                {
                    return e;
                }
            }
        }
        catch ( ActionFailedException e )
        {
            //ignore
        }
        catch ( Exception e )
        {
            log.warn( e.getMessage() );
        }

        return null;
    }


    /**
     * Returns bazaar environments for this peer. Throws {@code ActionFailedException} if requests tobazaar failed for some
     * reason
     *
     * @param all true: returns all environments, false: returns current user environments
     */
    public Set<BazaarEnvironment> getEnvironments( boolean all )
    {
        if ( !canWorkWithBazaar() )
        {
            throw new ActionFailedException( "Peer is not registered with Bazaar or connection to Bazaar failed" );
        }

        String json = all ? bazaaarAdapter.getAllEnvironmentsForPeer() : bazaaarAdapter.getUserEnvironmentsForPeer();

        if ( json == null )
        {
            throw new ActionFailedException( "Failed to obtain environments from Bazaar" );
        }

        log.debug( "Json with environments: {}", json );

        Set<BazaarEnvironment> envs = new HashSet<>();

        try
        {
            ArrayNode arr = JsonUtil.fromJson( json, ArrayNode.class );

            for ( int i = 0; i < arr.size(); i++ )
            {
                envs.add( new BazaarEnvironment( this, arr.get( i ), environmentManager, proxyContainerHelper ) );
            }
        }
        catch ( Exception e )
        {
            log.error( "Failed to parse environments from Bazaar", e );

            throw new ActionFailedException( "Failed to parse environments from Bazaar: " + e.getMessage() );
        }

        return envs;
    }


    public Set<String> getDeletedEnvironmentsIds()
    {

        if ( !canWorkWithBazaar() )
        {
            throw new ActionFailedException( "Peer is not registered with Bazaar or connection to Bazaar failed" );
        }

        String json = bazaaarAdapter.getDeletedEnvironmentsForPeer();

        if ( json == null )
        {
            throw new ActionFailedException( "Failed to obtain deleted environments from Bazaar" );
        }

        log.debug( "Json with deleted environments: {}", json );

        try
        {
            return io.subutai.common.util.JsonUtil.fromJson( json, new TypeToken<Set<String>>()
            {
            }.getType() );
        }
        catch ( Exception e )
        {
            log.error( "Error to parse json: ", e );

            throw new ActionFailedException( "Failed to parse deleted environments from Bazaar: " + e.getMessage() );
        }
    }


    public void destroyContainer( BazaarEnvironment env, String containerId )
    {
        if ( !canWorkWithBazaar() )
        {
            return;
        }

        if ( env.getContainerHosts().size() == 1 )
        {
            throw new IllegalStateException(
                    "Environment will have 0 containers after modification. Please, destroy environment instead" );
        }

        try
        {
            EnvironmentContainerHost ch = env.getContainerHostById( containerId );

            ( ( EnvironmentContainerImpl ) ch ).destroy( false );

            bazaaarAdapter.destroyContainer( env.getId(), containerId );
        }
        catch ( Exception e )
        {
            log.error( "Error to destroy container: ", e );
        }
    }


    public boolean removeEnvironment( String envId )
    {
        if ( !canWorkWithBazaar() )
        {
            return false;
        }

        try
        {
            bazaaarAdapter.removeEnvironment( envId );

            return true;
        }
        catch ( Exception e )
        {
            log.error( "Error to remove environment: ", e );
        }

        return false;
    }


    public boolean removeEnvironment( LocalEnvironment env )
    {
        return removeEnvironment( env.getId() );
    }


    public boolean canWorkWithBazaar()
    {
        return isBazaarReachable() && isRegisteredWithBazaar();
    }


    public boolean isBazaarReachable()
    {
        BazaarManager bazaarManager = ServiceLocator.getServiceOrNull( BazaarManager.class );

        return bazaarManager != null && bazaarManager.isBazaarReachable();
    }


    public boolean isRegisteredWithBazaar()
    {
        BazaarManager bazaarManager = ServiceLocator.getServiceOrNull( BazaarManager.class );

        return bazaarManager != null && bazaarManager.isRegisteredWithBazaar();
    }


    public boolean uploadEnvironment( LocalEnvironment env )
    {
        if ( !canWorkWithBazaar() )
        {
            return false;
        }

        if ( env.getStatus() != EnvironmentStatus.HEALTHY )
        {
            return false;
        }

        try
        {
            ObjectNode envJson = environmentToJson( env );

            environmentPeersToJson( env, envJson );

            environmentContainersToJson( env, envJson );

            bazaaarAdapter.uploadEnvironment( envJson.toString() );

            return true;
        }
        catch ( Exception e )
        {
            log.debug( "Error to post local environment to Bazaar: ", e );

            return false;
        }
    }


    public boolean uploadPeerOwnerEnvironment( LocalEnvironment env )
    {
        if ( !canWorkWithBazaar() )
        {
            return false;
        }

        if ( env.getStatus() != EnvironmentStatus.HEALTHY )
        {
            return false;
        }

        try
        {
            ObjectNode envJson = environmentToJson( env );

            environmentPeersToJson( env, envJson );

            environmentContainersToJson( env, envJson );

            return bazaaarAdapter.uploadPeerOwnerEnvironment( envJson.toString() );
        }
        catch ( Exception e )
        {
            log.debug( "Error to post local environment to Bazaar: ", e );
        }

        return false;
    }


    public void removeSshKey( String envId, String sshKey )
    {
        if ( !canWorkWithBazaar() )
        {
            return;
        }

        bazaaarAdapter.removeSshKey( envId, sshKey );
    }


    public void addSshKey( String envId, String sshKey )
    {
        if ( !canWorkWithBazaar() )
        {
            return;
        }

        bazaaarAdapter.addSshKey( envId, sshKey );
    }


    private void environmentContainersToJson( LocalEnvironment env, ObjectNode json ) throws PeerException
    {
        ArrayNode contNode = json.putArray( "containers" );

        for ( EnvironmentContainerHost ch : env.getContainerHosts() )
        {
            ObjectNode peerJson = JsonUtil.createNode( "id", ch.getId() );

            peerJson.put( "name", ch.getContainerName() );

            peerJson.put( "hostname", ch.getHostname() );

            peerJson.put( "containerName", ch.getContainerName() );

            peerJson.put( "state", ch.getState().toString() );

            peerJson.put( "template", ch.getTemplateName() );

            peerJson.put( "size", ch.getContainerSize().toString() );

            peerJson.put( "peerId", ch.getPeer().getId() );

            peerJson.put( "rhId", ch.getResourceHostId().getId() );

            String ip = ch.getIp();

            peerJson.put( "ip", ip );


            ArrayNode sshKeys = peerJson.putArray( "sshkeys" );

            SshKeys chSshKeys = ch.getAuthorizedKeys();

            for ( SshKey sshKey : chSshKeys.getKeys() )
            {
                sshKeys.add( sshKey.getPublicKey() );
            }

            contNode.add( peerJson );
        }
    }


    private ObjectNode environmentToJson( LocalEnvironment env )
    {
        ObjectNode json = JsonUtil.createNode( "id", env.getEnvironmentId().getId() );

        json.put( "name", env.getName() );

        json.put( "status", env.getStatus().toString() );

        json.put( "p2pHash", P2PUtil.generateHash( env.getEnvironmentId().getId() ) );

        json.put( "p2pTtl", Common.DEFAULT_P2P_SECRET_KEY_TTL_SEC );

        json.put( "p2pKey", env.getP2pKey() );

        json.put( "vni", env.getVni() );

        return json;
    }


    private void environmentPeersToJson( LocalEnvironment env, ObjectNode json ) throws PeerException
    {
        ArrayNode peers = json.putArray( "peers" );

        for ( Peer peer : env.getPeers() )
        {
            ObjectNode peerJson = JsonUtil.createNode( "id", peer.getId() );

            peerJson.put( "online", peer.isOnline() );

            putPeerResourceHostsJson( peerJson, env.getEnvironmentPeer( peer.getId() ) );

            peers.add( peerJson );
        }
    }


    private void putPeerResourceHostsJson( ObjectNode peerJson, EnvironmentPeer environmentPeer )
    {
        ArrayNode rhs = peerJson.putArray( "resourceHosts" );

        for ( RhP2pIp rh : environmentPeer.getRhP2pIps() )
        {
            ObjectNode rhJson = JsonUtil.createNode( "id", rh.getRhId() );

            rhJson.put( "p2pIp", rh.getP2pIp() );

            rhs.add( rhJson );
        }
    }


    public void handleHostnameChange( final ContainerHostInfo containerInfo, final String previousHostname,
                                      final String currentHostname )
    {
        BazaarEnvironment environment = null;

        for ( BazaarEnvironment bazaarEnvironment : getEnvironments( true ) )
        {
            try
            {
                bazaarEnvironment.getContainerHostById( containerInfo.getId() );
                environment = bazaarEnvironment;

                break;
            }
            catch ( ContainerHostNotFoundException e )
            {
                //ignore
            }
        }

        if ( environment == null )
        {
            return;
        }

        for ( EnvironmentContainerHost containerHost : environment.getContainerHosts() )
        {
            try
            {
                containerHost.execute( getChangeHostnameInEtcHostsCommand( previousHostname, currentHostname ) );
            }
            catch ( CommandException e )
            {
                log.warn( "Error updating /etc/hosts file on container {} with container hostname change: [{}] -> [{}]",
                        containerHost.getHostname(), previousHostname, currentHostname );
            }
        }
    }


    private RequestBuilder getChangeHostnameInEtcHostsCommand( String oldHostname, String newHostname )
    {
        return new RequestBuilder(
                String.format( "sed -i 's/\\b%1$s\\b/%2$s/g' %4$s && sed -i 's/\\b%1$s.%3$s\\b/%2$s.%3$s/g' %4$s",
                        oldHostname, newHostname, Common.DEFAULT_DOMAIN_NAME, Common.ETC_HOSTS_FILE ) );
    }
}
