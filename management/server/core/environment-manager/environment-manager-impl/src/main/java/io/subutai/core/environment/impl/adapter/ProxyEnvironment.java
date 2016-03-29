package io.subutai.core.environment.impl.adapter;


import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

import io.subutai.common.environment.EnvironmentStatus;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.core.environment.impl.EnvironmentManagerImpl;
import io.subutai.core.environment.impl.entity.EnvironmentContainerImpl;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;


class ProxyEnvironment extends EnvironmentImpl
{
    private final Logger log = LoggerFactory.getLogger( getClass() );


    ProxyEnvironment( JsonNode json, EnvironmentManagerImpl environmentManager, Map<String, ContainerHost> localContainersByHostname )
    {
        super(
                json.get( "name" ).asText(),
                json.get( "subnetCidr" ).asText(),
                null,
                3L,
                "hub" // peerId
        );

        init( json );

        addContainers( parseContainers( json, localContainersByHostname ) );
    }


    private void init( JsonNode json )
    {
        environmentId = json.get( "id" ).asText();

        log.debug( "environmentId: {}", environmentId );

        envId = new EnvironmentId( environmentId );

        setP2PSubnet( json.get( "tunnelNetwork" ).asText() );
        setVni( json.get( "vni" ).asLong() );
        setVersion( 1L );
        setStatus( EnvironmentStatus.HEALTHY );
    }


    private Set<EnvironmentContainerImpl> parseContainers( JsonNode json, Map<String, ContainerHost> localContainersByHostname )
    {
        Set<ProxyEnvironmentContainer> containers = new HashSet<>();

        JsonNode arr = json.get( "containerList" );

        for ( JsonNode node : arr )
        {
            try
            {
                ProxyEnvironmentContainer con = parseContainer( node, localContainersByHostname );

                if ( con != null )
                {
                    containers.add( con );
                }
            }
            catch ( Exception e )
            {
                log.error( "Error to parse container json: ", e );
            }
        }

//        proxyContainerHelper.setProxyToRemoteContainers( envContainers );

        Set<EnvironmentContainerImpl> resultSet = new HashSet<>();

        resultSet.addAll( containers );

        return resultSet;
    }


    // May return null b/c of bug in SS: not all containers in environment has corresponding CH registered in MH.
    private ProxyEnvironmentContainer parseContainer( JsonNode node, Map<String, ContainerHost> localContainersByHostname )
    {
        // Fix for: SS container hostname is stored as id on Hub
        String hostname = node.get( "id" ).asText();

        ContainerHost ch = localContainersByHostname.get( hostname );

        return ch != null
               ? new ProxyEnvironmentContainer( node, ch.getId() )
               : null;
    }


    @Override
    public String toString()
    {
        return "ProxyEnvironment:" + super.toString();
    }


    // TODO. setEnvironmentTransientFields( environment );
    // NOTE: Using environmentManager from EnvironmentImpl gives side effects. For example, empty container list.
//    private EnvironmentManagerImpl environmentManager;
//    @Override
//    public void setEnvironmentManager( final EnvironmentManagerImpl environmentManager )
//    {
//        this.environmentManager = environmentManager;
//    }
}
