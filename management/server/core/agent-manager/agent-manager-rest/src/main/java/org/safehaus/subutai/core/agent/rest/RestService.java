package org.safehaus.subutai.core.agent.rest;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public interface RestService {

    // should return Set<Agent> in Json format

    /**
     * Returns list of agents available in container in JSON format
     * with HTTP response status 200
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
     * @param hostname
     * @return
     */
    @GET
    @Path("agents/{hostname}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getAgentByHostname(@PathParam("hostname") String hostname);

    //Agent

    /**
     * Returns list of agents available in container by uuid in JSON format
     * this allows to get specific agent
     * with HTTP response status 200
     * @param uuid
     * @return
     */
    @GET
    @Path("agents/{uuid}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getAgentByUUID(@PathParam("uuid") String uuid);

    //Set<Agent>

    /**
     * Returns list of agents available in container by parentHostname in JSON format
     * this allows to differentiate remote and host agents
     * with HTTP response status 200
     * @param parentHostname
     * @return
     */
    @GET
    @Path("agents/lxc/{parentHostname}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getLxcAgentsByParentHostname(@PathParam("parentHostname") String parentHostname);

    //Adds AgentListener

    /**
     * Create and add listener to agent
     * as agentListener passed AgentListener representation in
     * JSON format
     * @param agentListener
     * @return
     */
    @POST
    @Path("agents/listener")
    public Response addListener(String agentListener);

    //removes AgentListener

    /**
     * Remove agentListener from agent
     * as param passed json representation of AgentListener
     * @param agentListener
     * @return
     */
    @DELETE
    @Path("agents/listener")
    public Response removeListener(String agentListener);

    //Set<Agent>

    /**
     * Returns agents by hostNames
     * @param hostNames
     * @return
     */
    @GET
    @Path("agents/{hostNames}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getAgentsByHostNames(@PathParam("hostNames") String hostNames);
}
