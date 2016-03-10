package io.subutai.core.strategy.rest;


import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.subutai.core.strategy.api.Blueprint;


/**
 * REST endpoint for environment manager
 */
public interface RestService
{
    @Path( "/{strategyId}" )
    @POST
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    Response distribute( @PathParam( "strategyId" ) String strategyId, Blueprint blueprint );
}
