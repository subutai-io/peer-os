package io.subutai.core.environment.impl.adapter;


import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.subutai.common.environment.Environment;
import io.subutai.common.environment.EnvironmentStatus;
import io.subutai.common.environment.PeerConf;
import io.subutai.common.environment.RhP2pIp;
import io.subutai.common.host.HostInterfaces;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.EnvironmentContainerHost;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.settings.Common;
import io.subutai.common.util.P2PUtil;
import io.subutai.core.environment.impl.EnvironmentManagerImpl;
import io.subutai.core.environment.impl.entity.EnvironmentContainerImpl;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.core.hubadapter.api.HubAdapter;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.hub.share.json.JsonUtil;


public class EnvironmentAdapter
{
    private final Logger log = LoggerFactory.getLogger( getClass() );

    private final EnvironmentManagerImpl environmentManager;

    private final ProxyContainerHelper proxyContainerHelper;

    private final PeerManager peerManager;

    private final HubAdapter hubAdapter;


    public EnvironmentAdapter( EnvironmentManagerImpl environmentManager, PeerManager peerManager,
                               HubAdapter hubAdapter )
    {
        this.environmentManager = environmentManager;

        proxyContainerHelper = new ProxyContainerHelper( peerManager );

        this.peerManager = peerManager;

        this.hubAdapter = hubAdapter;
    }


    public ProxyEnvironment get( String id )
    {
        for ( ProxyEnvironment e : getEnvironments() )
        {
            if ( e.getId().equals( id ) )
            {
                return e;
            }
        }

        return null;
    }


    public Set<ProxyEnvironment> getEnvironments()
    {
        String json = hubAdapter.getUserEnvironmentsForPeer();

        if ( json == null )
        {
            return Collections.emptySet();
        }

        log.debug( "Json with environments: {}", json );

        HashSet<ProxyEnvironment> envs = new HashSet<>();

        try
        {
            ArrayNode arr = JsonUtil.fromJson( json, ArrayNode.class );

            for ( int i = 0; i < arr.size(); i++ )
            {
                envs.add( new ProxyEnvironment( this, arr.get( i ), environmentManager, proxyContainerHelper ) );
            }
        }
        catch ( Exception e )
        {
            log.error( "Error to parse json: ", e );
        }

//        printLocalContainers();

        return envs;
    }


    private void printLocalContainers()
    {
        for ( ResourceHost rh : peerManager.getLocalPeer().getResourceHosts() )
        {
            for ( ContainerHost ch : rh.getContainerHosts() )
            {
                final HostInterfaces hostInterfaces = ch.getHostInterfaces();
                String ip = hostInterfaces.findByName( Common.DEFAULT_CONTAINER_INTERFACE ).getIp();

                log.debug( "Local container: hostname={}, id={}, ip={}, size={}", ch.getHostname(), ch.getId(), ip,
                        ch.getContainerSize() );
            }
        }
    }


    public void destroyContainer( ProxyEnvironment env, String containerId )
    {
        try
        {
            EnvironmentContainerHost ch = env.getContainerHostById( containerId );

            ( ( EnvironmentContainerImpl ) ch ).destroy();

            hubAdapter.destroyContainer( env.getId(), containerId );
        }
        catch ( Exception e )
        {
            log.error( "Error to destroy container: ", e );
        }
    }


    public void removeEnvironment( EnvironmentImpl env )
    {

        try
        {
            hubAdapter.removeEnvironment( env.getId() );
        }
        catch ( Exception e )
        {
            log.error( "Error to remove environment: ", e );
        }
    }


    public void uploadEnvironments( Collection<Environment> envs )
    {
        for ( Environment env : envs )
        {
            uploadEnvironment( ( EnvironmentImpl ) env );
        }
    }


    public void uploadEnvironment( EnvironmentImpl env )
    {
        if ( env.getStatus() != EnvironmentStatus.HEALTHY )
        {
            return;
        }

        try
        {
            ObjectNode envJson = environmentToJson( env );

            environmentPeersToJson( env, envJson );

            environmentContainersToJson( env, envJson );

            hubAdapter.uploadEnvironment( envJson.toString() );
        }
        catch ( Exception e )
        {
            log.debug( "Error to post local environment to Hub: ", e );
        }
    }


    private void environmentContainersToJson( EnvironmentImpl env, ObjectNode json ) throws PeerException
    {
        ArrayNode contNode = json.putArray( "containers" );

        for ( ContainerHost ch : env.getContainerHosts() )
        {
            ObjectNode peerJson = JsonUtil.createNode( "id", ch.getId() );

            peerJson.put( "name", ch.getContainerName() );

            peerJson.put( "hostname", ch.getHostname() );

            peerJson.put( "state", ch.getState().toString() );

            peerJson.put( "template", ch.getTemplateName() );

            peerJson.put( "size", ch.getContainerSize().toString() );

            peerJson.put( "peerId", ch.getPeer().getId() );

            peerJson.put( "rhId", ch.getResourceHostId().getId() );

            String ip = ch.getHostInterfaces().getAll().iterator().next().getIp();

            peerJson.put( "ip", ip );

            contNode.add( peerJson );
        }
    }


    private ObjectNode environmentToJson( EnvironmentImpl env )
    {
        ObjectNode json = JsonUtil.createNode( "id", env.getEnvironmentId().getId() );

        json.put( "name", env.getName() );

        json.put( "status", env.getStatus().toString() );

        json.put( "p2pHash", P2PUtil.generateHash( env.getEnvironmentId().getId() ) );

        json.put( "p2pTtl", Common.DEFAULT_P2P_SECRET_KEY_TTL_SEC );

        json.put( "p2pKey", env.getP2pKey() );

        return json;
    }


    private void environmentPeersToJson( EnvironmentImpl env, ObjectNode json ) throws PeerException
    {
        ArrayNode peers = json.putArray( "peers" );

        for ( Peer peer : env.getPeers() )
        {
            ObjectNode peerJson = JsonUtil.createNode( "id", peer.getId() );

            peerJson.put( "online", peer.isOnline() );

            putPeerResourceHostsJson( peerJson, env.getPeerConf( peer.getId() ) );

            peers.add( peerJson );
        }
    }


    private void putPeerResourceHostsJson( ObjectNode peerJson, PeerConf peerConf )
    {
        ArrayNode rhs = peerJson.putArray( "resourceHosts" );

        for ( RhP2pIp rh : peerConf.getRhP2pIps() )
        {
            ObjectNode rhJson = JsonUtil.createNode( "id", rh.getRhId() );

            rhJson.put( "p2pIp", rh.getP2pIp() );

            rhs.add( rhJson );
        }
    }


    public void onContainerStart( String envId, String contId )
    {
        hubAdapter.onContainerStart( envId, contId );
    }


    public void onContainerStop( String envId, String contId )
    {
        hubAdapter.onContainerStop( envId, contId );
    }
}
