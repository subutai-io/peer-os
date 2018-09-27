package io.subutai.core.bazaarmanager.impl.processor.port_map;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.protocol.Protocol;
import io.subutai.core.bazaarmanager.impl.environment.state.Context;
import io.subutai.bazaar.share.dto.domain.PortMapDto;
import io.subutai.bazaar.share.dto.domain.ReservedPortMapping;


public class DestroyPortMap
{

    private final Logger log = LoggerFactory.getLogger( getClass() );

    private Context ctx;


    public DestroyPortMap( Context ctx )
    {
        this.ctx = ctx;
    }


    public void deleteMap( final PortMapDto portMapDto )
    {
        try
        {
            ContainerHost containerHost = ctx.localPeer.getContainerHostById( portMapDto.getContainerSSId() );

            ResourceHost resourceHost =
                    ctx.localPeer.getResourceHostById( containerHost.getResourceHostId().toString() );

            Protocol protocol = Protocol.valueOf( portMapDto.getProtocol().name() );


            if ( protocol == Protocol.HTTP || protocol == Protocol.HTTPS )
            {
                resourceHost.removeContainerPortDomainMapping( protocol, containerHost.getIp(),
                        portMapDto.getInternalPort(), portMapDto.getExternalPort(), portMapDto.getDomain() );
            }
            else
            {
                resourceHost.removeContainerPortMapping( protocol, containerHost.getIp(), portMapDto.getInternalPort(),
                        portMapDto.getExternalPort() );
            }

            // if it's RH, remove port mapping from MH too
            if ( !resourceHost.isManagementHost() )
            {
                boolean mappingIsInUseOnRH = false;

                // check if port mapping on MH is not used for other container on this RH
                for ( final ReservedPortMapping mapping : resourceHost.getReservedPortMappings() )
                {
                    if ( mapping.getProtocol() == portMapDto.getProtocol() && mapping.getExternalPort() == portMapDto
                            .getExternalPort() )
                    {
                        mappingIsInUseOnRH = true;
                        break;
                    }
                }


                if ( !mappingIsInUseOnRH )
                {
                    String rhIpAddr = resourceHost.getAddress();

                    if ( protocol == Protocol.HTTP || protocol == Protocol.HTTPS )
                    {
                        ctx.localPeer.getManagementHost().removeContainerPortDomainMapping( protocol, rhIpAddr,
                                portMapDto.getExternalPort(), portMapDto.getExternalPort(), portMapDto.getDomain() );
                    }
                    else
                    {
                        ctx.localPeer.getManagementHost()
                                     .removeContainerPortMapping( protocol, rhIpAddr, portMapDto.getExternalPort(),
                                             portMapDto.getExternalPort() );
                    }
                }
            }

            portMapDto.setState( PortMapDto.State.DESTROYING );
        }
        catch ( Exception e )
        {
            portMapDto.setState( PortMapDto.State.ERROR );
            portMapDto.setErrorLog( e.getMessage() );
            log.error( "*********", e );
        }
    }
}
