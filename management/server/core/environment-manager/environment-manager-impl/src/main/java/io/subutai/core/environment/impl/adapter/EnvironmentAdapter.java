package io.subutai.core.environment.impl.adapter;


import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.subutai.common.environment.EnvironmentStatus;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.EnvironmentContainerHost;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.ResourceHost;
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


    public EnvironmentAdapter( EnvironmentManagerImpl environmentManager, PeerManager peerManager, HubAdapter hubAdapter )
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
                envs.add( new ProxyEnvironment( arr.get( i ), environmentManager ) );
            }
        }
        catch ( Exception e )
        {
            log.error( "Error to parse json: ", e );
        }

        printLocalContainers();

        return envs;
    }


    private void printLocalContainers()
    {
        for ( ResourceHost rh : peerManager.getLocalPeer().getResourceHosts() )
        {
            for ( ContainerHost ch : rh.getContainerHosts() )
            {
                String ip = ch.getHostInterfaces().getAll().iterator().next().getIp();

                log.debug( "Local container: hostname={}, id={}, ip={}", ch.getHostname(), ch.getId(), ip );
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


    private void environmentContainersToJson( EnvironmentImpl env, ObjectNode json )
    {
        ArrayNode contNode = json.putArray( "containers" );

        for ( ContainerHost ch : env.getContainerHosts() )
        {
            ObjectNode peerJson = JsonUtil.createNode( "id", ch.getId() );

            peerJson.put( "name", ch.getContainerName() );

            peerJson.put( "state", ch.getState().toString() );

            peerJson.put( "template", ch.getTemplateName() );

            peerJson.put( "size", ch.getContainerSize().toString() );

            peerJson.put( "peerId", ch.getPeer().getId() );

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

        return json;
    }


    private void environmentPeersToJson( EnvironmentImpl env, ObjectNode json ) throws PeerException
    {
        ArrayNode peers = json.putArray( "peers" );

        for ( Peer peer : env.getPeers() )
        {
            ObjectNode peerJson = JsonUtil.createNode( "id", peer.getId() );

            peerJson.put( "online", peer.isOnline() );

            peers.add( peerJson );
        }
    }
}
