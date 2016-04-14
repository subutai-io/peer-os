package io.subutai.core.object.relation.rest;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


/**
 * Created by ape-craft on 3/19/16.
 */
public interface RelationManagerRest
{
    @GET
    @Produces( { MediaType.TEXT_PLAIN} )
    @Path("/challenge")
    public Response getRelationChallenge();
}
