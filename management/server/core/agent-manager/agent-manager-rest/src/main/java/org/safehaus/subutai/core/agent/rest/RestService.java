package org.safehaus.subutai.core.agent.rest;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


public interface RestService
{


    /**
     * Returns list of agents available in container in JSON format with HTTP response status 200
     */
    @GET
    @Path("agents")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response getAgents();


    /**
     * Returns list of physical agents available in container in JSON format with HTTP response status 200
     */
    @GET
    @Path("agents/physical")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response getPhysicalAgents();


    /**
     * Returns list of lxc agents available in container in JSON format with HTTP response status 200
     */
    @GET
    @Path("agents/lxc")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response getLxcAgents();


    /**
     * Returns list of agents available in container by hostname in JSON format this allows to differentiate remote and
     * host agents with HTTP response status 200
     */
    @GET
    @Path("agents/by-hostname/{hostname}")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response getAgentByHostname( @PathParam("hostname") String hostname );


    /**
     * Returns list of agents available in container by uuid in JSON format this allows to get specific agent with HTTP
     * response status 200
     */
    @GET
    @Path("agents/by-agent-id/{uuid}")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response getAgentByUUID( @PathParam("uuid") String uuid );


    /**
     * Returns list of agents available in container by parentHostname in JSON format this allows to differentiate
     * remote and host agents with HTTP response status 200
     */
    @GET
    @Path("agents/{parentHostname}/children")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response getLxcAgentsByParentHostname( @PathParam("parentHostname") String parentHostname );

//    /**
//     * Returning set of agents belonging to an environment
//     */
//    @GET
//    @Path("agents/by-environment-id/{envId}")
//    @Produces({ MediaType.APPLICATION_JSON })
//    public Response getAgentsByEnvironmentId( @PathParam("envId") String environmentId );
}
