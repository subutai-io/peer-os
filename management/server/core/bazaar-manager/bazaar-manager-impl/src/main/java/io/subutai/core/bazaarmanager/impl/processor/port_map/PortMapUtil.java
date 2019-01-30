package io.subutai.core.bazaarmanager.impl.processor.port_map;


import org.apache.commons.lang3.StringUtils;

import io.subutai.bazaar.share.dto.domain.PortMapDto;
import io.subutai.common.command.CommandException;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.peer.ResourceHostException;
import io.subutai.common.protocol.LoadBalancing;
import io.subutai.common.protocol.Protocol;


class PortMapUtil
{
    static void mapPortToIp( PortMapDto portMapDto, ResourceHost resourceHost, String ipAddr, int internalPort )
            throws ResourceHostException, CommandException
    {
        Protocol protocol = Protocol.valueOf( portMapDto.getProtocol().name() );

        if ( !resourceHost.isPortMappingReserved( protocol, portMapDto.getExternalPort(), ipAddr, internalPort,
                portMapDto.getDomain() ) )
        {
            if ( !protocol.isHttpOrHttps() )
            {
                resourceHost.mapContainerPort( protocol, ipAddr, internalPort, portMapDto.getExternalPort() );
            }
            else
            {
                String sslCertPath =
                        protocol == Protocol.HTTPS ? saveSslCertificateToFilesystem( portMapDto, resourceHost ) : null;

                resourceHost.mapContainerPortToDomain( protocol, ipAddr, internalPort, portMapDto.getExternalPort(),
                        portMapDto.getDomain(), sslCertPath, LoadBalancing.ROUND_ROBIN, portMapDto.isSslBackend(),
                        portMapDto.isRedirectHttpToHttps(), portMapDto.isHttp2() );
            }
        }
    }


    private static String saveSslCertificateToFilesystem( PortMapDto portMapDto, ResourceHost rh )
            throws CommandException
    {
        if ( StringUtils.isBlank( portMapDto.getSslCertPem() ) )
        {
            return null;
        }

        String fileName = String.format( "%s-%d-%d", portMapDto.getDomain(), portMapDto.getExternalPort(),
                portMapDto.getInternalPort() );

        String sslCertPath = "/tmp/" + fileName + ".pem";

        rh.execute(
                new RequestBuilder( String.format( "echo \"%s\" > %s", portMapDto.getSslCertPem(), sslCertPath ) ) );

        return sslCertPath;
    }


    static boolean destroyPortMapping( PortMapDto portMapDto, ResourceHost resourceHost, String ipAddr,
                                       int internalPort ) throws ResourceHostException
    {
        Protocol protocol = Protocol.valueOf( portMapDto.getProtocol().name() );

        if ( resourceHost.isPortMappingReserved( protocol, portMapDto.getExternalPort(), ipAddr, internalPort,
                portMapDto.getDomain() ) )
        {
            if ( protocol.isHttpOrHttps() )
            {
                resourceHost
                        .removeContainerPortDomainMapping( protocol, ipAddr, internalPort, portMapDto.getExternalPort(),
                                portMapDto.getDomain() );
            }
            else
            {
                resourceHost.removeContainerPortMapping( protocol, ipAddr, internalPort, portMapDto.getExternalPort() );
            }

            return true;
        }

        return false;
    }
}
