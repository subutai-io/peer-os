package org.safehaus.subutai.core.metric.rest;


import java.util.Set;
import java.util.UUID;

import javax.ws.rs.core.Response;

import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.metric.api.ContainerHostMetric;
import org.safehaus.subutai.core.metric.api.Monitor;
import org.safehaus.subutai.core.metric.api.MonitorException;
import org.safehaus.subutai.core.metric.api.ResourceHostMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;


/**
 * Monitor Rest implementation
 */
public class RestServiceImpl implements RestService
{
    private static final Logger LOG = LoggerFactory.getLogger( RestServiceImpl.class.getName() );

    private Monitor monitor;
    private EnvironmentManager environmentManager;


    public RestServiceImpl( final Monitor monitor, final EnvironmentManager environmentManager )
    {
        Preconditions.checkNotNull( monitor, "Monitor is null" );
        Preconditions.checkNotNull( environmentManager, "Environment Manager is null" );

        this.monitor = monitor;
        this.environmentManager = environmentManager;
    }


    @Override
    public Response getResourceHostsMetrics()
    {
        try
        {
            Set<ResourceHostMetric> metrics = monitor.getResourceHostsMetrics();
            return Response.ok( JsonUtil.toJson( metrics ) ).build();
        }
        catch ( MonitorException e )
        {
            LOG.error( "Error in getResourceHostsMetrics", e );
            return Response.serverError().entity( e ).build();
        }
    }


    @Override
    public Response getContainerHostsMetrics( final String uuid )
    {
        try
        {
            UUID environmentId = UUID.fromString( uuid );

            Environment environment = environmentManager.getEnvironmentByUUID( environmentId );
            if ( environment != null )
            {
                Set<ContainerHostMetric> metrics = monitor.getContainerHostsMetrics( environment );
                return Response.ok( JsonUtil.toJson( metrics ) ).build();
            }
            else
            {
                return Response.status( Response.Status.NOT_FOUND )
                               .entity( String.format( "Environment %s is not found", uuid ) ).build();
            }
        }
        catch ( NullPointerException | IllegalArgumentException e )
        {
            LOG.error( "Error in getContainerHostsMetrics", e );
            return Response.status( Response.Status.BAD_REQUEST ).entity( e.getMessage() ).build();
        }
        catch ( MonitorException e )
        {
            LOG.error( "Error in getContainerHostsMetrics", e );
            return Response.serverError().entity( e ).build();
        }
    }


    @Override
    public Response alert( final String alertMetric )
    {
        try
        {
            monitor.alert( alertMetric );
            return Response.accepted().build();
        }
        catch ( MonitorException e )
        {
            LOG.error( "Error in alert", e );
            return Response.serverError().entity( e ).build();
        }
    }
}
