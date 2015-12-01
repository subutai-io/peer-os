package io.subutai.core.metric.rest.ui;


import java.util.Calendar;
import java.util.Date;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;

import io.subutai.common.gson.required.RequiredDeserializer;
import io.subutai.common.peer.Host;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.util.JsonUtil;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.metric.api.Monitor;


public class RestServiceImpl implements RestService
{
    private static final Logger LOG = LoggerFactory.getLogger( RestServiceImpl.class );

    private Gson gson = RequiredDeserializer.createValidatingGson();

    private Monitor monitor;
    private EnvironmentManager environmentManager;
    private LocalPeer localPeer;


    public RestServiceImpl( final Monitor monitor, final EnvironmentManager environmentManager, final LocalPeer localPeer )
    {
        Preconditions.checkNotNull( monitor );
        Preconditions.checkNotNull( environmentManager );
        Preconditions.checkNotNull( localPeer );

        this.monitor = monitor;
        this.environmentManager = environmentManager;
        this.localPeer = localPeer;
    }


    @Override
    public Response getMetrics( final String environmentId, final String hostId, final int interval )
    {
        try
        {
            Calendar calendar = Calendar.getInstance();
            Date current = new Date( calendar.getTime().getTime() );
            calendar.add( Calendar.HOUR, -1 );
            Date start = calendar.getTime();

            Host host;
            if( environmentId != null && hostId != null )
            {
                host = environmentManager.loadEnvironment( environmentId ).getContainerHostById( hostId );
            }
            else if( hostId != null )
            {
                host = localPeer.getResourceHostById( hostId );
            }
            else
            {
                host = localPeer.getManagementHost();
            }

            return Response.ok( JsonUtil.toJson( monitor.getHistoricalMetrics( host,
                    start,
                    current ))).build();
        }
        catch ( Exception e )
        {
            return Response.status( Response.Status.BAD_REQUEST ).entity( JsonUtil.toJson( e ) ).build();
        }

    }


    @Override
    public Response getMetrics( final String hostId, final int interval )
    {
        return getMetrics( null, hostId, interval );
    }


    @Override
    public Response getMetrics( final int interval )
    {
        return getMetrics( null, null, interval );
    }
}