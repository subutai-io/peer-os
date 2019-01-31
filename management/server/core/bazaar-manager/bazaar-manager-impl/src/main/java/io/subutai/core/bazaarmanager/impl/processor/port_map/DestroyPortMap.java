package io.subutai.core.bazaarmanager.impl.processor.port_map;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.bazaar.share.dto.domain.PortMapDto;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.protocol.Protocol;
import io.subutai.common.protocol.ReservedPort;
import io.subutai.core.bazaarmanager.impl.environment.state.Context;


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

            if ( !PortMapUtil.destroyPortMapping( portMapDto, resourceHost, containerHost.getIp(),
                    portMapDto.getInternalPort() ) )
            {
                // skip not existing port mapping
                portMapDto.setState( PortMapDto.State.DELETED );
                return;
            }

            // if it's RH, remove port mapping from MH too
            if ( !resourceHost.isManagementHost() )
            {
                boolean mappingIsInUseOnRH = false;
                Protocol protocol = Protocol.valueOf( portMapDto.getProtocol().name() );

                // check if port mapping on MH is not used for other container on this RH
                for ( final ReservedPort mapping : resourceHost.getReservedPortMappings() )
                {
                    if ( mapping.getProtocol() == protocol && mapping.getPort() == portMapDto.getExternalPort() )
                    {
                        mappingIsInUseOnRH = true;
                        break;
                    }
                }

                if ( !mappingIsInUseOnRH )
                {
                    PortMapUtil.destroyPortMapping( portMapDto, ctx.localPeer.getManagementHost(),
                            resourceHost.getAddress(), portMapDto.getExternalPort() );
                }
            }

            portMapDto.setState( PortMapDto.State.DELETED );
        }
        catch ( Exception e )
        {
            portMapDto.setState( PortMapDto.State.ERROR );
            portMapDto.setErrorLog( e.getMessage() );
            log.error( "*********", e );
        }
    }
}
