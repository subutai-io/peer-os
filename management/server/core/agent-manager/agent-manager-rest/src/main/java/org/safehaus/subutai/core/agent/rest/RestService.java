package org.safehaus.subutai.core.agent.rest;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

public interface RestService {

    // should return Set<Agent> in Json format
    @GET
    @Path("agents")
    public Response getAgents();

    //should return Set<Agent> in Json format
    @GET
    @Path("agents/physical")
    public Response getPhysicalAgents();

    //Set<Agent>
    @GET
    @Path("agents/lxc")
    public Response getLxcAgents();

    //Agent
    @GET
    @Path("agents/{hostname}")
    public Response getAgentByHostname(@PathParam("hostname") String hostname);

    //Agent
    @GET
    @Path("agents/{uuid}")
    public Response getAgentByUUID(@PathParam("uuid") String uuid);

    //Set<Agent>
    @GET
    @Path("agents/lxc/{parentHostname}")
    public Response getLxcAgentsByParentHostname(@PathParam("parentHostname") String parentHostname);

    //Adds AgentListener
    @POST
    @Path("agents/listener")
    public Response addListener(String agentListener);

    //removes AgentListener
    @DELETE
    @Path("agents/listener")
    public Response removeListener(String agentListener);

    //Set<Agent>
    @GET
    @Path("agents/{hostNames}")
    public Response getAgentsByHostNames(@PathParam("hostNames") String hostNames);
}
