package io.subutai.core.metric.rest.ui;


import java.util.Calendar;
import java.util.Date;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.core.Response;

import com.google.common.base.Preconditions;

import io.subutai.common.peer.EnvironmentContainerHost;
import io.subutai.common.peer.Host;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.settings.Common;
import io.subutai.common.util.JsonUtil;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.metric.api.Monitor;
import io.subutai.core.metric.rest.ui.pojo.P2PInfoPojo;


public class RestServiceImpl implements RestService
{

    private Monitor monitor;
    private EnvironmentManager environmentManager;
    private LocalPeer localPeer;


    public RestServiceImpl( final Monitor monitor, final EnvironmentManager environmentManager,
                            final LocalPeer localPeer )
    {
        Preconditions.checkNotNull( monitor );
        Preconditions.checkNotNull( environmentManager );
        Preconditions.checkNotNull( localPeer );

        this.monitor = monitor;
        this.environmentManager = environmentManager;
        this.localPeer = localPeer;
    }


    @RolesAllowed( "System-Management|Read" )
    @Override
    public Response getMetrics( final String environmentId, final String hostId, final int interval )
    {
        try
        {
            Calendar calendar = Calendar.getInstance();
            Date current =
                    new Date( calendar.getTime().getTime() - Calendar.getInstance().getTimeZone().getRawOffset() );
            calendar.add( Calendar.HOUR, -interval );
            Date start = new Date( calendar.getTime().getTime() - Calendar.getInstance().getTimeZone().getRawOffset() );

            Host host;
            if ( environmentId != null && hostId != null )
            {
                host = environmentManager.loadEnvironment( environmentId ).getContainerHostById( hostId );
            }
            else if ( Common.MANAGEMENT_HOSTNAME.equalsIgnoreCase( hostId ) )
            {
                host = localPeer.getContainerHostByName( hostId );
            }
            else if ( hostId != null )
            {
                host = localPeer.getResourceHostById( hostId );
            }
            else
            {
                host = localPeer.getManagementHost();
            }

            if ( host instanceof EnvironmentContainerHost )
            {
                return Response.ok( host.getPeer()
                                        .getHistoricalMetrics( ( ( EnvironmentContainerHost ) host ).getContainerId(),
                                                start, current ) ).build();
            }

            return Response.ok( monitor.getHistoricalMetrics( host, start, current ) ).build();
        }
        catch ( Exception e )
        {
            return Response.status( Response.Status.BAD_REQUEST ).entity( JsonUtil.toJson( e ) ).build();
        }
    }


    @RolesAllowed( "System-Management|Read" )
    @Override
    public Response getMetrics( final String hostId, final int interval )
    {
        return getMetrics( null, hostId, interval );
    }


    @RolesAllowed( "System-Management|Read" )
    @Override
    public Response getMetrics( final int interval )
    {
        return getMetrics( null, null, interval );
    }


    @RolesAllowed( "System-Management|Read" )
    @Override
    public Response getP2PStatus()
    {
        P2PInfoPojo pojo = new P2PInfoPojo();
        pojo.setP2pList( monitor.getP2PStatus() );
        String info = JsonUtil.GSON.toJson( pojo );
        return Response.status( Response.Status.OK ).entity( info ).build();
    }
}