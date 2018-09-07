package io.subutai.core.metric.rest.ui;


import java.security.AccessControlException;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.core.Response;

import com.google.common.base.Preconditions;

import io.subutai.common.environment.ContainerDto;
import io.subutai.common.environment.ContainerHostNotFoundException;
import io.subutai.common.environment.EnvironmentDto;
import io.subutai.common.environment.EnvironmentNotFoundException;
import io.subutai.common.peer.ContainerId;
import io.subutai.common.peer.EnvironmentContainerHost;
import io.subutai.common.peer.Host;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.security.objects.PermissionObject;
import io.subutai.common.security.objects.PermissionOperation;
import io.subutai.common.security.objects.PermissionScope;
import io.subutai.common.settings.Common;
import io.subutai.common.util.JsonUtil;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.metric.api.Monitor;
import io.subutai.core.metric.api.pojo.P2PInfo;
import io.subutai.core.peer.api.PeerManager;


public class RestServiceImpl implements RestService
{

    private Monitor monitor;
    private EnvironmentManager environmentManager;
    private LocalPeer localPeer;
    private IdentityManager identityManager;
    private PeerManager peerManager;


    public RestServiceImpl( final Monitor monitor, final EnvironmentManager environmentManager,
                            final LocalPeer localPeer, final IdentityManager identityManager,
                            final PeerManager peerManager )
    {
        Preconditions.checkNotNull( monitor );
        Preconditions.checkNotNull( environmentManager );
        Preconditions.checkNotNull( localPeer );
        Preconditions.checkNotNull( identityManager );
        Preconditions.checkNotNull( peerManager );

        this.monitor = monitor;
        this.environmentManager = environmentManager;
        this.localPeer = localPeer;
        this.identityManager = identityManager;
        this.peerManager = peerManager;
    }


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
                try
                {
                    host = environmentManager.loadEnvironment( environmentId ).getContainerHostById( hostId );
                }
                catch ( EnvironmentNotFoundException e )
                {
                    Optional<EnvironmentDto> envDto = environmentManager.getTenantEnvironments().stream().filter(
                            env -> environmentId.equalsIgnoreCase( env.getId() ) ).findFirst();

                    if ( envDto.isPresent() && !Common.BAZAAR_ID.equalsIgnoreCase( envDto.get().getDataSource() ) )
                    {
                        Optional<ContainerDto> containerDto = envDto.get().getContainers().stream().filter(
                                cont -> hostId.equalsIgnoreCase( cont.getId() ) ).findFirst();
                        if ( containerDto.isPresent() )
                        {
                            return Response.ok( peerManager.getPeer( containerDto.get().getPeerId() )
                                                           .getHistoricalMetrics(
                                                                   new ContainerId( containerDto.get().getHostId() ),
                                                                   start, current ) ).build();
                        }
                        else
                        {
                            throw new ContainerHostNotFoundException( "Container " + hostId + " not found" );
                        }
                    }
                    else
                    {
                        throw e;
                    }
                }
            }
            else if ( Common.MANAGEMENT_HOSTNAME.equalsIgnoreCase( hostId ) )
            {
                host = localPeer.getContainerHostByContainerName( hostId );
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
            else
            {
                //for anything besides env containers check for System-Management permission
                if ( !identityManager
                        .isUserPermitted( identityManager.getActiveUser(), PermissionObject.SYSTEM_MANAGEMENT,
                                PermissionScope.ALL_SCOPE, PermissionOperation.READ ) )
                {
                    throw new AccessControlException( "Access denied" );
                }
            }

            return Response.ok( monitor.getHistoricalMetrics( host, start, current ) ).build();
        }
        catch ( Exception e )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( JsonUtil.toJson( e ) ).build();
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


    @Override
    public Response getP2PStatus( final String hostId )
    {
        try
        {
            P2PInfo p2PInfo = monitor.getP2pStatus( hostId );

            return Response.ok( p2PInfo.getP2pStatus() == 0 ).build();
        }
        catch ( Exception e )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( JsonUtil.toJson( e ) ).build();
        }
    }
}