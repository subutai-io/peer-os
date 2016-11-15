package io.subutai.core.hubmanager.impl.environment.state.change;


import org.json.JSONObject;

import org.apache.commons.lang3.StringUtils;

import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.network.ProxyLoadBalanceStrategy;
import io.subutai.common.peer.ResourceHost;
import io.subutai.core.hubmanager.api.exception.HubManagerException;
import io.subutai.core.hubmanager.impl.environment.state.Context;
import io.subutai.core.hubmanager.impl.environment.state.StateHandler;
import io.subutai.core.hubmanager.impl.http.RestResult;
import io.subutai.core.hubmanager.impl.tunnel.TunnelHelper;
import io.subutai.core.hubmanager.impl.tunnel.TunnelProcessor;
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


    private final static String REVERSE_PROXY_PORT_MAPPING_CMD =
            "echo 'server { listen    %s; server_name  %s ; location / { proxy_pass "
                    + "http://%s:%s; proxy_set_header   X-Real-IP $remote_addr; proxy_set_header   Host "
                    + "$http_host; proxy_set_header   X-Forwarded-For $proxy_add_x_forwarded_for;}}' > "
                    + "/var/lib/apps/subutai/current/nginx-includes/%s.conf";


    /**
     * TODO balanceStrategy should come from Hub
     */
    @Override
    protected Object doHandle( EnvironmentPeerDto peerDto ) throws HubManagerException
    {
        try
        {
            logStart();

            boolean isAddedToDmain = false;

            ResourceHost resourceHost = ctx.localPeer.getResourceHosts().iterator().next();

            EnvironmentDto envDto =
                    ctx.restClient.getStrict( path( "/rest/v1/environments/%s", peerDto ), EnvironmentDto.class );

            EnvironmentInfoDto env = peerDto.getEnvironmentInfo();


            resourceHost.execute( new RequestBuilder( "systemctl restart *nginx*" ) );

            if ( env.getVEHS() != null && !env.getVEHS().isEmpty() )
            {
                resourceHost = ctx.localPeer.getResourceHosts().iterator().next();
                createTunnel( resourceHost, env );
                return peerDto;
            }

            if ( StringUtils.isNotEmpty( env.getDomainName() ) )
            {
                ProxyLoadBalanceStrategy balanceStrategy = ProxyLoadBalanceStrategy.LOAD_BALANCE;

                String existingDomain = ctx.localPeer.getVniDomain( env.getVni() );

                if ( existingDomain != null )
                {
                    if ( !existingDomain.trim().equalsIgnoreCase( env.getDomainName().trim() ) )
                    {
                        ctx.localPeer.removeVniDomain( env.getVni() );
                        ctx.localPeer.setVniDomain( env.getVni(), env.getDomainName().trim(), balanceStrategy,
                                env.getSslCertPath() );
                    }
                }
                else
                {
                    ctx.localPeer
                            .setVniDomain( env.getVni(), env.getDomainName(), balanceStrategy, env.getSslCertPath() );
                }


                for ( EnvironmentNodesDto nodesDto : envDto.getNodes() )
                {
                    if ( nodesDto.getPeerId().equals( ctx.localPeer.getId() ) )
                    {
                        for ( EnvironmentNodeDto nodeDto : nodesDto.getNodes() )
                        {

                            if ( !isAddedToDmain )
                            {
                                try
                                {
                                    String ip = nodeDto.getIp().replace( "/24", "" );
                                    int vlan = ctx.localPeer.getReservedNetworkResources().findByVni( env.getVni() )
                                                            .getVlan();

                                    String hostListenPort = nodeDto.getHostListenPort();
                                    String containerListenPort = nodeDto.getContainerListenPort();
                                    String domain = nodeDto.getDomain();

                                    String cmd = format( REVERSE_PROXY_PORT_MAPPING_CMD, hostListenPort, domain, ip,
                                            containerListenPort, vlan );

                                    CommandResult commandResult = resourceHost.execute( new RequestBuilder( cmd ) );


                                    resourceHost.execute( new RequestBuilder( "systemctl restart *nginx*" ) );

                                    isAddedToDmain = true;
                                    //                                if ( !ctx.localPeer.isIpInVniDomain( ip, env
                                    // .getVni
                                    // () ) )
                                    //                                {
                                    //                                    ctx.localPeer.addIpToVniDomain( ip, env.getVni
                                    // () );
                                    //                                }
                                }
                                catch ( Exception e )
                                {
                                    log.error( "Could not add container IP to domain: " + nodeDto.getContainerName() );
                                }
                            }
                        }
                    }
                }
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


    private void createTunnel( final ResourceHost resourceHost, EnvironmentInfoDto env )
    {

        JSONObject dnsData = new JSONObject( env.getVEHS() );

        dnsData.get( "ip" );
        dnsData.get( "port" );

        if ( resourceHost.isConnected() )
        {
            try
            {
                CommandResult commandResult = resourceHost.execute( new RequestBuilder(
                        format( TunnelProcessor.CREATE_TUNNEL_COMMAND, dnsData.get( "ip" ), dnsData.get( "port" ),
                                "" ) ) );

                env.setVEHS( commandResult.getStdOut() );
            }
            catch ( CommandException e )
            {
                e.printStackTrace();
            }
        }
    }


    @Override
    protected RestResult<Object> post( EnvironmentPeerDto peerDto, Object body )
    {
        return ctx.restClient.post( path( "/rest/v1/environments/%s/peers/%s/domain", peerDto ), body );
    }
}