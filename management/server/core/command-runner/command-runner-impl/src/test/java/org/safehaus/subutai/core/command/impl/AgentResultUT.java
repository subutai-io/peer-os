/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.command.impl;


import java.util.UUID;

import org.junit.Test;
import org.safehaus.subutai.common.command.AgentResultImpl;
import org.safehaus.subutai.common.protocol.Response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;


/**
 * Test of AgentResult class
 */
public class AgentResultUT {

	private final String SOME_DUMMY_OUTPUT = "some dummy output";
	private final Integer OK_EXIT_CODE = 0;
	private final UUID agentUUID = UUID.randomUUID();
	private final AgentResultImpl agentResult = new AgentResultImpl(agentUUID);


	@Test (expected = NullPointerException.class)
	public void constructorShouldFailNullAgentUUID() {
		new AgentResultImpl(null);
	}


	@Test
	public void shouldNotAppendNullResponse() {

		agentResult.appendResults(null);

		assertTrue(agentResult.getStdOut().isEmpty());
	}


	@Test
	public void shouldNotAppendAlienResponse() {
		Response response = MockUtils.getIntermediateResponse(UUID.randomUUID(), UUID.randomUUID());
		when(response.getStdOut()).thenReturn(SOME_DUMMY_OUTPUT);

		agentResult.appendResults(response);

		assertTrue(agentResult.getStdOut().isEmpty());
	}


	@Test
	public void shouldAppendOwnResponse() {
		Response response = MockUtils.getIntermediateResponse(agentUUID, UUID.randomUUID());
		when(response.getStdOut()).thenReturn(SOME_DUMMY_OUTPUT);

		agentResult.appendResults(response);

		assertEquals(SOME_DUMMY_OUTPUT, agentResult.getStdOut());
	}


	@Test
	public void shouldAppendExitCode() {
		Response response = MockUtils.getSucceededResponse(agentUUID, UUID.randomUUID());

		agentResult.appendResults(response);

		assertEquals(OK_EXIT_CODE, agentResult.getExitCode());
	}


	@Test
	public void shouldNotAppendIfExitCodeAlreadySet() {
		Response response = MockUtils.getSucceededResponse(agentUUID, UUID.randomUUID());

		agentResult.appendResults(response);
		when(response.getStdOut()).thenReturn(SOME_DUMMY_OUTPUT);
		agentResult.appendResults(response);

		assertTrue(agentResult.getStdOut().isEmpty());
	}
}
