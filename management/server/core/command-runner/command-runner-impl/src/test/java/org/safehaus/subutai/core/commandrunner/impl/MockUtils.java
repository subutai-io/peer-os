/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.commandrunner.impl;


import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.safehaus.subutai.core.commandrunner.api.Command;
import org.safehaus.subutai.core.commandrunner.api.RequestBuilder;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.Request;
import org.safehaus.subutai.common.protocol.Response;
import org.safehaus.subutai.common.enums.RequestType;
import org.safehaus.subutai.common.enums.ResponseType;
import org.safehaus.subutai.core.commandrunner.impl.CommandRunnerImpl;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.*;


/**
 * Test mocking utilities
 */
public class MockUtils {

	public static Response getFailedResponse(UUID agentUUID, UUID commandUUID) {
		Response response = getIntermediateResponse(agentUUID, commandUUID);
		when(response.getExitCode()).thenReturn(123);
		when(response.getType()).thenReturn(ResponseType.EXECUTE_RESPONSE_DONE);
		when(response.isFinal()).thenReturn(true);
		when(response.hasSucceeded()).thenReturn(false);

		return response;
	}


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
		when(response.getType()).thenReturn(ResponseType.EXECUTE_TIMEOUT);

		return response;
	}


	public static Set<Request> getRequests(String program, UUID agentUUID, UUID commandUUID, int timeout) {
		Set<Request> requests = new HashSet<>();
		requests.add(getRequest(program, agentUUID, commandUUID, timeout));

		return requests;
	}


	public static Request getRequest(String program, UUID agentUUID, UUID commandUUID, int timeout) {
		Request request = mock(Request.class);
		when(request.getUuid()).thenReturn(agentUUID);
		when(request.getType()).thenReturn(RequestType.EXECUTE_REQUEST);
		when(request.getProgram()).thenReturn(program);
		when(request.getTaskUuid()).thenReturn(commandUUID);
		when(request.getTimeout()).thenReturn(timeout);

		return request;
	}


	public static RequestBuilder getRequestBuilder(final String program, final int timeout, Set<Agent> agents) {
		RequestBuilder requestBuilder = getRequestBuilder(program, timeout);
		when(requestBuilder.build(any(UUID.class), any(UUID.class))).
				thenAnswer(new Answer<Request>() {

					public Request answer(
							InvocationOnMock
									invocation)
							throws Throwable {
						Object[] args = invocation
								.getArguments();
						UUID agentUUID =
								(UUID) args[0];
						UUID commandUUID =
								(UUID) args[1];

						return getRequest(program,
								agentUUID,
								commandUUID,
								timeout);
					}
				});

		return requestBuilder;
	}


	public static RequestBuilder getRequestBuilder(String command, int timeout) {
		RequestBuilder requestBuilder = mock(RequestBuilder.class);
		when(requestBuilder.getTimeout()).thenReturn(timeout);

		return requestBuilder;
	}


	public static Command getCommand(String program, CommandRunnerImpl commandRunner, UUID agentUUID, int timeout) {

		return commandRunner.createCommand(getRequestBuilder(program, timeout), getAgents(agentUUID));
	}


	public static Set<Agent> getAgents(UUID... agentUUIDs) {
		Set<Agent> agents = new HashSet<>();
		for (UUID agentUUID : agentUUIDs) {
			agents.add(getAgent(agentUUID));
		}

		return agents;
	}


	public static Agent getAgent(UUID agentUUID) {
		Agent agent = mock(Agent.class);
		when(agent.getUuid()).thenReturn(agentUUID);

		return agent;
	}
}
