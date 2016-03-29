package io.subutai.core.environment.impl.adapter;


import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.node.ArrayNode;

import io.subutai.common.environment.Environment;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.ResourceHost;
import io.subutai.core.environment.impl.EnvironmentManagerImpl;
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


    public ProxyEnvironment get( final String id )
    {
        return null;
    }


    public Set<Environment> getEnvironments()
    {
        String json = hubAdapter.getUserEnvironmentsForPeer();

        if ( json == null )
        {
            return Collections.emptySet();
        }

        log.debug( "Json with environments: {}", json );

        Map<String, ContainerHost> localContainersByHostname = getLocalContainersByHostname();

        HashSet<Environment> envs = new HashSet<>();

        try
        {
            ArrayNode arr = JsonUtil.fromJson( json, ArrayNode.class );

            for ( int i = 0; i < arr.size(); i++ )
            {
                envs.add( new ProxyEnvironment( arr.get( i ), environmentManager, localContainersByHostname ) );
            }
        }
        catch ( Exception e )
        {
            log.error( "Error to parse json: ", e );
        }

        return envs;
    }


    // Fix for: SS container hostname is stored as id on Hub
    private Map<String, ContainerHost> getLocalContainersByHostname()
    {
        HashMap<String, ContainerHost> map = new HashMap<>();

        for ( ResourceHost rh : peerManager.getLocalPeer().getResourceHosts() )
        {
            for ( ContainerHost ch : rh.getContainerHosts() )
            {
                String ip = ch.getHostInterfaces().getAll().iterator().next().getIp();

                log.debug( "Local container: hostname={}, id={}, ip={}", ch.getHostname(), ch.getId(), ip );

                map.put( ch.getHostname(), ch );
            }
        }

        return map;
    }
}
