package io.subutai.core.environment.rest;


import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.subutai.common.environment.EnvironmentModificationException;
import io.subutai.common.environment.Topology;
import io.subutai.core.environment.api.exception.EnvironmentCreationException;


/**
 * REST endpoint for environment manager
 */
public interface RestService
{
    @Path( "/" )
    @POST
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    Response createEnvironment( Topology topology ) throws EnvironmentCreationException;

    Response growEnvironment( String environmentId, Topology topology )
            throws EnvironmentModificationException;

    @Path( "/" )
    @POST
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    Response listEnvironments();
}
