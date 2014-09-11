package org.safehaus.subutai.core.agent.rest;


import org.safehaus.subutai.core.agent.api.AgentManager;

import javax.ws.rs.core.Response;
import java.util.logging.Logger;


public class RestServiceImpl implements RestService {

    private static final Logger logger = Logger.getLogger(RestServiceImpl.class.getName());
    AgentManager agentManager;

    public void setAgentManager(AgentManager agentManager) {
        this.agentManager = agentManager;
    }


    @Override
    public Response getListeners() {
        String listeners = agentManager.getListeners();
        return null;
    }

    @Override
    public Response getPhysicalAgents() {
        return null;
    }

    @Override
    public Response getLxcAgents() {
        return null;
    }

    @Override
    public Response getAgentByHostname(String hostname) {
        return null;
    }

    @Override
    public Response getAgentByUUID(String uuid) {
        return null;
    }

    @Override
    public Response getLxcAgentsByParentHostname(String parentHostname) {
        return null;
    }

    @Override
    public Response addListener(String agentListener) {
        return null;
    }

    @Override
    public Response removeListener(String agentListener) {
        return null;
    }

    @Override
    public Response getAgentsByHostNames(String hostNames) {
        return null;
    }

    @Override
    public Response addAgent(String agent) {
        return null;
    }
}
