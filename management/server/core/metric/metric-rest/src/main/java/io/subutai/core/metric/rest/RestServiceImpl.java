package io.subutai.core.metric.rest;


import java.util.Set;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.subutai.common.environment.Environment;
import io.subutai.common.environment.EnvironmentNotFoundException;
import io.subutai.common.metric.ResourceHostMetric;
import io.subutai.common.util.JsonUtil;
import io.subutai.core.env.api.EnvironmentManager;
import io.subutai.core.metric.api.ContainerHostMetric;
import io.subutai.core.metric.api.Monitor;
import io.subutai.core.metric.api.MonitorException;


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
        catch ( Exception e )
        {
            LOG.error( "Error in getResourceHostsMetrics", e );
            return Response.serverError().entity( e ).build();
        }
    }


    @Override
    public Response getContainerHostsMetrics( final String environmentId )
    {
        try
        {

            Environment environment;
            try
            {
                environment = environmentManager.findEnvironment( environmentId );
            }
            catch ( EnvironmentNotFoundException e )
            {
                return Response.status( Response.Status.NOT_FOUND )
                               .entity( String.format( "Environment %s is not found", environmentId ) ).build();
            }

            Set<ContainerHostMetric> metrics = monitor.getContainerHostsMetrics( environment );
            return Response.ok( JsonUtil.toJson( metrics ) ).build();
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
