package io.subutai.core.environment.impl.xpeer;


import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import io.subutai.common.environment.ContainerDto;
import io.subutai.common.environment.ContainerQuotaDto;
import io.subutai.common.environment.EnvironmentStatus;
import io.subutai.common.network.NetworkResource;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.settings.Common;
import io.subutai.core.environment.impl.entity.LocalEnvironment;


public class RemoteEnvironment extends LocalEnvironment
{
    private static final Logger LOG = LoggerFactory.getLogger( RemoteEnvironment.class );

    private String initiatorPeerId;
    private Set<ContainerHost> containers;
    private String username;
    private String userId;


    public RemoteEnvironment( final NetworkResource networkResource, final String name,
                              final Set<ContainerHost> containers )
    {
        this.environmentId = networkResource.getEnvironmentId();
        this.containers = containers;
        this.initiatorPeerId = networkResource.getInitiatorPeerId();
        this.username = networkResource.getUsername();
        this.userId = networkResource.getUserId();
        setP2PSubnet( networkResource.getP2pSubnet() );
        setVni( networkResource.getVni() );
        setStatus( EnvironmentStatus.UNKNOWN );
        setName( name );
    }


    public String getUsername()
    {
        return username;
    }


    public String getRemoteUserId()
    {
        return userId;
    }


    public String getInitiatorPeerId()
    {
        return initiatorPeerId;
    }


    public Set<ContainerDto> getContainerDtos()
    {
        Set<ContainerDto> containerDtos = Sets.newHashSet();


        for ( ContainerHost host : containers )
        {
            ContainerDto containerDto =
                    new ContainerDto( host.getId(), getId(), host.getHostname(), host.getIp(), host.getTemplateName(),
                            host.getContainerSize(), host.getArch().name(), Sets.<String>newHashSet(), host.getPeerId(),
                            host.getResourceHostId().getId(), true,
                            Common.BAZAAR_ID.equals( initiatorPeerId ) ? Common.BAZAAR_ID : Common.SUBUTAI_ID,
                            host.getState(), host.getTemplateId(), host.getContainerName(),
                            host.getResourceHostId().getId() );
            try
            {
                ContainerQuotaDto quota = new ContainerQuotaDto( host.getQuota() );
                containerDto.setQuota( quota );
            }
            catch ( Exception e )
            {
                LOG.error( "Error getting container quota: {}", e.getMessage() );
            }

            containerDtos.add( containerDto );
        }


        return containerDtos;
    }
}
