package org.safehaus.subutai.core.agent.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public interface RestService {

    // should return Set<Agent> in Json format

    /**
     * Returns list of agents available in container in JSON format
     * with HTTP response status 200
     *
     * @return
     */
    @GET
    @Path("agents")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getAgents();

    //should return Set<Agent> in Json format

    /**
     * Returns list of physical agents available in container in JSON format
     * with HTTP response status 200
     *
     * @return
     */
    @GET
    @Path("agents/physical")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getPhysicalAgents();

    //Set<Agent>

    /**
     * Returns list of lxc agents available in container in JSON format
     * with HTTP response status 200
     *
     * @return
     */
    @GET
    @Path("agents/lxc")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getLxcAgents();

    //Agent

    /**
     * Returns list of agents available in container by hostname in JSON format
     * this allows to differentiate remote and host agents
     * with HTTP response status 200
     *
     * @param hostname
     * @return
     */
    @GET
    @Path("agents/by-hostname/{hostname}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getAgentByHostname(@PathParam("hostname") String hostname);

    //Agent

    /**
     * Returns list of agents available in container by uuid in JSON format
     * this allows to get specific agent
     * with HTTP response status 200
     *
     * @param uuid
     * @return
     */
    @GET
    @Path("agents/by-agent-id/{uuid}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getAgentByUUID(@PathParam("uuid") String uuid);

    //Set<Agent>

    /**
     * Returns list of agents available in container by parentHostname in JSON format
     * this allows to differentiate remote and host agents
     * with HTTP response status 200
     *
     * @param parentHostname
     * @return
     */
    @GET
    @Path("agents/{parentHostname}/children")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getLxcAgentsByParentHostname(@PathParam("parentHostname") String parentHostname);

    @GET
    @Path("agents/by-environment-id/{envId}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getAgentsByEnvironmentId(@PathParam("envId") String environmentId);

}
