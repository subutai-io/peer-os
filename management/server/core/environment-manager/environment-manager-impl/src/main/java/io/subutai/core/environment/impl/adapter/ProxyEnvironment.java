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


/**
 * NOTE: Using environmentManager from parent EnvironmentImpl gives side effects. For example, empty container list.
 */
public class ProxyEnvironment extends EnvironmentImpl
{
    private final Logger log = LoggerFactory.getLogger( getClass() );

    private final EnvironmentAdapter environmentAdapter;


    ProxyEnvironment( EnvironmentAdapter environmentAdapter, JsonNode json, EnvironmentManagerImpl environmentManager,
                      ProxyContainerHelper proxyContainerHelper )
    {
        super( json.get( "name" ).asText(), json.get( "subnetCidr" ).asText(), 0L, "hub" // peerId
             );

        init( json );

        this.environmentAdapter = environmentAdapter;

        addContainers( parseContainers( json, environmentManager, proxyContainerHelper ) );
    }


    private void init( JsonNode json )
    {
        environmentId = json.get( "id" ).asText();

        envId = new EnvironmentId( environmentId );

        setP2PSubnet( json.get( "p2pSubnet" ).asText() );

        setVni( json.get( "vni" ).asLong() );

        setStatus( EnvironmentStatus.HEALTHY );
    }


    private Set<EnvironmentContainerImpl> parseContainers( JsonNode json, EnvironmentManagerImpl environmentManager,
                                                           ProxyContainerHelper proxyContainerHelper )
    {
        Set<ProxyEnvironmentContainer> containers = new HashSet<>();

        JsonNode arr = json.get( "containerList" );

        Set<String> localContainerIds = proxyContainerHelper.getLocalContainerIds();

        try
        {
            for ( JsonNode node : arr )
            {

                ProxyEnvironmentContainer ch =
                        new ProxyEnvironmentContainer( node, environmentManager, localContainerIds );

                //skip remote containers
                if ( ch.isLocal() )
                {

                    ch.setEnvironment( this );

                    ch.setEnvironmentAdapter( environmentAdapter );

                    containers.add( ch );
                }
            }
        }
        catch ( Exception e )
        {
            log.error( "Error to parse container json: ", e );
        }

        proxyContainerHelper.setProxyToRemoteContainers( containers );

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
