package io.subutai.core.hubmanager.impl.environment.state.change;


import java.io.File;

import org.json.JSONObject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import io.subutai.common.command.CommandResult;
import io.subutai.common.network.ProxyLoadBalanceStrategy;
import io.subutai.common.peer.ResourceHost;
import io.subutai.core.hubmanager.api.RestResult;
import io.subutai.core.hubmanager.api.exception.HubManagerException;
import io.subutai.core.hubmanager.impl.environment.state.Context;
import io.subutai.core.hubmanager.impl.environment.state.StateHandler;
import io.subutai.core.hubmanager.impl.tunnel.TunnelHelper;
import io.subutai.core.hubmanager.impl.tunnel.TunnelProcessor;
import io.subutai.hub.share.dto.TunnelInfoDto;
import io.subutai.hub.share.dto.environment.EnvironmentDto;
import io.subutai.hub.share.dto.environment.EnvironmentInfoDto;
import io.subutai.hub.share.dto.environment.EnvironmentNodeDto;
import io.subutai.hub.share.dto.environment.EnvironmentNodesDto;
import io.subutai.hub.share.dto.environment.EnvironmentPeerDto;

import static java.lang.String.format;


// TODO refactor
public class DomainStateHandler extends StateHandler
{
    public DomainStateHandler( Context ctx )
    {
        super( ctx, "Domain configuration" );
    }


    /**
     * TODO balanceStrategy should come from Hub
     */
    @Override
    protected Object doHandle( EnvironmentPeerDto peerDto ) throws HubManagerException
    {
        try
        {
            logStart();

            EnvironmentDto envDto =
                    ctx.restClient.getStrict( path( "/rest/v1/environments/%s", peerDto ), EnvironmentDto.class );


            boolean hasDirectAccess = "true".equals(
                    ctx.restClient.getStrict( format( "/rest/v1/nated/%s", peerDto.getPeerId() ), String.class ) );

            EnvironmentInfoDto env = peerDto.getEnvironmentInfo();

            String sslCertPath = createSSLTempFile( env );

            JSONObject tunnelData = new JSONObject();

            if ( StringUtils.isNotEmpty( env.getDomainName() ) )
            {
                ProxyLoadBalanceStrategy balanceStrategy = ProxyLoadBalanceStrategy.LOAD_BALANCE;

                String existingDomain = ctx.localPeer.getVniDomain( env.getVni() );

                if ( existingDomain != null )
                {
                    ctx.localPeer.removeVniDomain( env.getVni() );

                    ctx.localPeer
                            .setVniDomain( env.getVni(), env.getDomainName().trim(), balanceStrategy, sslCertPath );
                }

                else
                {
                    ctx.localPeer.setVniDomain( env.getVni(), env.getDomainName(), balanceStrategy, sslCertPath );
                }

                for ( EnvironmentNodesDto nodesDto : envDto.getNodes() )
                {
                    if ( nodesDto.getPeerId().equals( ctx.localPeer.getId() ) )
                    {
                        for ( EnvironmentNodeDto nodeDto : nodesDto.getNodes() )
                        {
                            try
                            {
                                if ( nodeDto.isHasDomain() )
                                {
                                    String ip = nodeDto.getIp().replace( "/24", "" );

                                    String port = nodeDto.getPort() == null || nodeDto.getPort().isEmpty() ? "" :
                                                  nodeDto.getPort();

                                    if ( hasDirectAccess )

                                    {
                                        if ( !ctx.localPeer.isIpInVniDomain( ip + ":" + port, env.getVni() ) )
                                        {
                                            ctx.localPeer.addIpToVniDomain( ip + ":" + port, env.getVni() );
                                        }
                                    }
                                    else
                                    {

                                        ResourceHost resourceHost = ctx.localPeer.getResourceHostByContainerId( nodeDto.getContainerId() );

                                        CommandResult commandResult = TunnelHelper
                                                .execute( resourceHost,
                                                        format( TunnelProcessor.CREATE_TUNNEL_COMMAND, ip, port, "" ) );
                                        TunnelInfoDto tunnelInfoDto = TunnelHelper
                                                .parseResult( "", commandResult.getStdOut(), null,
                                                        new TunnelInfoDto() );
                                        JSONObject ipPort = new JSONObject();
                                        ipPort.put( "ip", tunnelInfoDto.getOpenedIp() );
                                        ipPort.put( "port", tunnelInfoDto.getOpenedPort().replaceAll( "\n", "" ) );

                                        tunnelData.put( nodeDto.getContainerId(), ipPort );
                                    }
                                }
                            }
                            catch ( Exception e )
                            {
                                log.error( "Could not add container IP to domain: " + nodeDto.getContainerName() );
                            }
                        }
                    }
                }


                String path = format( "/rest/v1/environments/%s/peers/%s/containers/tunnel",
                        peerDto.getEnvironmentInfo().getId(), peerDto.getPeerId() );

                path = path.trim();

                RestResult restResult = ctx.restClient.post( path, tunnelData.toString() );

                log.info( "rest = {}", restResult.getStatus() );
            }
            else
            {
                ctx.localPeer.removeVniDomain( env.getVni() );
            }

            logEnd();

            return peerDto;
        }
        catch ( Exception e )
        {
            throw new HubManagerException( e );
        }
    }


    private String createSSLTempFile( EnvironmentInfoDto env )
    {

        if ( env.getSslCertPath() == null || env.getSslCertPath().isEmpty() )
        {
            return "";
        }

        try
        {
            File file = new File( "/opt/subutai-mng/data/tmp/" + env.getId() );
            if ( !file.createNewFile() )
            {
                log.info( "Domain ssl cert exists, overwriting..." );
            }

            FileUtils.writeStringToFile( file, env.getSslCertPath() );

            return "/mnt/lib/lxc/management/opt/subutai-mng/data/tmp/" + env.getId();
        }
        catch ( Exception e )
        {
            log.error( e.getMessage() );
        }

        return "";
    }


    @Override
    protected RestResult<Object> post( EnvironmentPeerDto peerDto, Object body )
    {
        return ctx.restClient.post( path( "/rest/v1/environments/%s/peers/%s/domain", peerDto ), body );
    }
}