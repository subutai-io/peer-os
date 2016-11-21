package io.subutai.core.environment.impl.xpeer;


import java.util.Set;

import com.google.common.collect.Sets;

import io.subutai.common.environment.ContainerDto;
import io.subutai.common.environment.EnvironmentStatus;
import io.subutai.common.network.NetworkResource;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.settings.Common;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;


public class RemoteEnvironment extends EnvironmentImpl
{

    private String containerSubnet;
    private int vlan;
    private Set<ContainerHost> containers;
    private String initiatorPeerId;


    public RemoteEnvironment( final NetworkResource networkResource, final Set<ContainerHost> containers )
    {
        this.environmentId = networkResource.getEnvironmentId();
        this.containerSubnet = networkResource.getContainerSubnet();
        this.vlan = networkResource.getVlan();
        this.containers = containers;
        this.initiatorPeerId = networkResource.getInitiatorPeerId();
        setP2PSubnet( networkResource.getP2pSubnet() );
        setVni( networkResource.getVni() );
        setStatus( EnvironmentStatus.UNKNOWN );
        setName( String.format( "Of peer %s", getInitiatorPeerId() ) );
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
            containerDtos.add( new ContainerDto( host.getId(), getId(), host.getHostname(),
                    host.getInterfaceByName( Common.DEFAULT_CONTAINER_INTERFACE ).getIp(), host.getTemplateName(),
                    host.getContainerSize(), host.getArch().name(), Sets.<String>newHashSet(), host.getPeerId(),
                    host.getResourceHostId().getId(), true, "subutai", host.getState(), host.getTemplateId() ) );
        }


        return containerDtos;
    }
}
