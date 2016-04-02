package io.subutai.core.environment.impl.adapter;


import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

import io.subutai.common.environment.EnvironmentStatus;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.core.environment.impl.EnvironmentManagerImpl;
import io.subutai.core.environment.impl.entity.EnvironmentContainerImpl;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;


// NOTE: Using environmentManager from EnvironmentImpl gives side effects. For example, empty container list.
public class ProxyEnvironment extends EnvironmentImpl
{
    private final Logger log = LoggerFactory.getLogger( getClass() );


    ProxyEnvironment( JsonNode json, EnvironmentManagerImpl environmentManager )
    {
        super(
                json.get( "name" ).asText(),
//                json.get( "subnetCidr" ).asText(),
                null,
                0L,
                "hub" // peerId
        );

        init( json );

        addContainers( parseContainers( json, environmentManager ) );
    }


    private void init( JsonNode json )
    {
        environmentId = json.get( "id" ).asText();

        envId = new EnvironmentId( environmentId );

        setP2PSubnet( json.get( "tunnelNetwork" ).asText() );
        setVni( json.get( "vni" ).asLong() );
        setVersion( 1L );
        setStatus( EnvironmentStatus.HEALTHY );
    }


    private Set<EnvironmentContainerImpl> parseContainers( JsonNode json, EnvironmentManagerImpl environmentManager )
    {
        Set<ProxyEnvironmentContainer> containers = new HashSet<>();

        JsonNode arr = json.get( "containerList" );

        for ( JsonNode node : arr )
        {
            try
            {
                containers.add( new ProxyEnvironmentContainer( node, environmentManager ) );
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


    @Override
    public String toString()
    {
        return "ProxyEnvironment:" + super.toString();
    }
}
