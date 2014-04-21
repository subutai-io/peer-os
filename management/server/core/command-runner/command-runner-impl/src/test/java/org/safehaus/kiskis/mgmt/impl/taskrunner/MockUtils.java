/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.taskrunner;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import static org.mockito.Mockito.*;
import org.safehaus.kiskis.mgmt.api.commandrunner.Command;
import org.safehaus.kiskis.mgmt.api.commandrunner.CommandStatus;
import org.safehaus.kiskis.mgmt.api.commandrunner.RequestBuilder;
import org.safehaus.kiskis.mgmt.impl.commandrunner.CommandImpl;
import org.safehaus.kiskis.mgmt.impl.commandrunner.CommandRunnerImpl;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.RequestType;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.ResponseType;

/**
 *
 * @author dilshat
 */
public class MockUtils {

    public static Response getIntermediateResponse(UUID agentUUID, UUID commandUUID) {
        Response response = mock(Response.class);
        when(response.getUuid()).thenReturn(agentUUID);
        when(response.getTaskUuid()).thenReturn(commandUUID);
        when(response.getExitCode()).thenReturn(null);
        when(response.getType()).thenReturn(ResponseType.EXECUTE_RESPONSE);
        when(response.isFinal()).thenReturn(false);
        when(response.hasSucceeded()).thenReturn(false);

        return response;
    }

    public static Response getFailedResponse(UUID agentUUID, UUID commandUUID) {
        Response response = getIntermediateResponse(agentUUID, commandUUID);
        when(response.getExitCode()).thenReturn(123);
        when(response.getType()).thenReturn(ResponseType.EXECUTE_RESPONSE_DONE);
        when(response.isFinal()).thenReturn(true);
        when(response.hasSucceeded()).thenReturn(false);

        return response;
    }

    public static Response getSucceededResponse(UUID agentUUID, UUID commandUUID) {
        Response response = getIntermediateResponse(agentUUID, commandUUID);
        when(response.getExitCode()).thenReturn(0);
        when(response.getType()).thenReturn(ResponseType.EXECUTE_RESPONSE_DONE);
        when(response.isFinal()).thenReturn(true);
        when(response.hasSucceeded()).thenReturn(true);

        return response;
    }

    public static Response getTimedOutResponse(UUID agentUUID, UUID commandUUID) {
        Response response = getIntermediateResponse(agentUUID, commandUUID);
        when(response.isFinal()).thenReturn(true);
        when(response.getType()).thenReturn(ResponseType.EXECUTE_TIMEOUTED);

        return response;
    }

    public static Agent getAgent(UUID agentUUID) {
        Agent agent = mock(Agent.class);
        when(agent.getUuid()).thenReturn(agentUUID);
        when(agent.getUuid()).thenReturn(agentUUID);

        return agent;
    }

    public static Set<Agent> getAgents(UUID... agentUUIDs) {
        Set<Agent> agents = new HashSet<Agent>();
        for (UUID agentUUID : agentUUIDs) {
            agents.add(getAgent(agentUUID));
        }

        return agents;
    }

    public static Request getRequest(UUID agentUUID, UUID commandUUID, int timeout) {
        Request request = mock(Request.class);
        when(request.getUuid()).thenReturn(agentUUID);
        when(request.getType()).thenReturn(RequestType.EXECUTE_REQUEST);
        when(request.getTaskUuid()).thenReturn(commandUUID);
        when(request.getTimeout()).thenReturn(timeout);

        return request;
    }

    public static Set<Request> getRequests(UUID agentUUID, UUID commandUUID, int timeout) {
        Set<Request> requests = new HashSet<Request>();
        requests.add(getRequest(agentUUID, commandUUID, timeout));

        return requests;
    }

    public static RequestBuilder getRequestBuilder(int timeout) {
        RequestBuilder requestBuilder = mock(RequestBuilder.class);
        when(requestBuilder.getTimeout()).thenReturn(timeout);

        return requestBuilder;
    }

    public static Command getCommand(CommandRunnerImpl commandRunner, UUID agentUUID, int timeout) {

        Command command = commandRunner.createCommand(
                getRequestBuilder(timeout),
                getAgents(agentUUID));

        return command;
    }

}
