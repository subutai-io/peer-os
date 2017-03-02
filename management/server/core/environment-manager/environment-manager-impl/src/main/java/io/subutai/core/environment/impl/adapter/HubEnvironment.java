package io.subutai.core.environment.impl.adapter;


import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

import io.subutai.common.environment.EnvironmentStatus;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.settings.Common;
import io.subutai.core.environment.impl.EnvironmentManagerImpl;
import io.subutai.core.environment.impl.entity.EnvironmentContainerImpl;
import io.subutai.core.environment.impl.entity.LocalEnvironment;


/**
 * NOTE: Using environmentManager from parent LocalEnvironment gives side effects. For example, empty container list.
 */
public class HubEnvironment extends LocalEnvironment implements io.subutai.common.environment.HubEnvironment
{
    private final static Logger log = LoggerFactory.getLogger( HubEnvironment.class );

    private transient final EnvironmentAdapter environmentAdapter;

    private String owner;
    private String ownerHubId;


    HubEnvironment( EnvironmentAdapter environmentAdapter, JsonNode json, EnvironmentManagerImpl environmentManager,
                    ProxyContainerHelper proxyContainerHelper )
    {
        super( json.get( "name" ).asText(), null, 0L, Common.HUB_ID );

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

        this.owner = json.get( "owner" ).asText();
        this.ownerHubId = json.get( "ownerHubId" ).asText();
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

                ch.setEnvironment( this );

                containers.add( ch );
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


    public String getOwner()
    {
        return owner;
    }


    public String getOwnerHubId()
    {
        return ownerHubId;
    }


    @Override
    public String toString()
    {
        return "ProxyEnvironment:" + super.toString();
    }
}
