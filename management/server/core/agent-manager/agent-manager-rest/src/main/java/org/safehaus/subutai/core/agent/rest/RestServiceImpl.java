package org.safehaus.subutai.core.agent.rest;


import com.google.gson.reflect.TypeToken;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.agent.api.AgentListener;
import org.safehaus.subutai.core.agent.api.AgentManager;

import javax.ws.rs.core.Response;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;


public class RestServiceImpl implements RestService {

    private static final Logger logger = Logger.getLogger(RestServiceImpl.class.getName());
    AgentManager agentManager;

    public void setAgentManager(AgentManager agentManager) {
        this.agentManager = agentManager;
    }

    @Override
    public Response getAgents() {
        ArrayList<String> agentList = new ArrayList<>();
        Set<Agent> agents = agentManager.getAgents();
        for (Agent agent : agents) {
            agentList.add(JsonUtil.toJson(agent));
        }
        logger.info("Agent Manager Log");
        logger.info(JsonUtil.toJson(agentList));
        return Response.status(Response.Status.OK).entity(JsonUtil.toJson(agentList)).build();
    }

    @Override
    public Response getPhysicalAgents() {
        Set<Agent> agents = agentManager.getPhysicalAgents();
        List<String> agentList = new ArrayList<>();
        for (Agent agent : agents) {
            agentList.add(JsonUtil.toJson(agent));
        }
        return Response.status(Response.Status.OK).entity(JsonUtil.toJson(agentList)).build();
    }

    @Override
    public Response getLxcAgents() {
        Set<Agent> agents = agentManager.getLxcAgents();
        List<String> agentList = new ArrayList<>();
        for (Agent agent : agents) {
            agentList.add(JsonUtil.toJson(agent));
        }
        return Response.status(Response.Status.OK).entity(JsonUtil.toJson(agentList)).build();
    }

    @Override
    public Response getAgentByHostname(String hostname) {
        Agent agent = agentManager.getAgentByHostname(hostname);
        return Response.status(Response.Status.OK).entity(JsonUtil.toJson(agent)).build();
    }

    @Override
    public Response getAgentByUUID(String uuid) {
        UUID agentUuid = JsonUtil.fromJson(uuid, UUID.class);
        Agent agent = agentManager.getAgentByUUID(agentUuid);
        return Response.status(Response.Status.OK).entity(JsonUtil.toJson(agent)).build();
    }

    @Override
    public Response getLxcAgentsByParentHostname(String parentHostname) {
        Set<Agent> agents = agentManager.getLxcAgentsByParentHostname(parentHostname);
        List<String> agentList = new ArrayList<>();
        for (Agent agent : agents) {
            agentList.add(JsonUtil.toJson(agent));
        }
        return Response.status(Response.Status.OK).entity(JsonUtil.toJson(agentList)).build();
    }

    @Override
    public Response addListener(String agentListener) {
        AgentListener listener = JsonUtil.fromJson(agentListener, AgentListener.class);
        agentManager.addListener(listener);
        return Response.status(Response.Status.CREATED).build();
    }

    @Override
    public Response removeListener(String agentListener) {
        AgentListener listener = JsonUtil.fromJson(agentListener, AgentListener.class);
        agentManager.removeListener(listener);
        return Response.status(Response.Status.ACCEPTED).build();
    }

    @Override
    public Response getAgentsByHostNames(String hostNames) {
        Type listType = new TypeToken<Set<String>>() {
        }.getType();
        Set<String> names = JsonUtil.GSON.fromJson(hostNames, listType);
        Set<Agent> agents = agentManager.getAgentsByHostnames(names);
        return Response.status(Response.Status.OK).entity(JsonUtil.toJson(agents)).build();
    }
}
