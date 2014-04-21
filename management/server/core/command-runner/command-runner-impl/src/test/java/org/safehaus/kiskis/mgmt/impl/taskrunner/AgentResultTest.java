/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.taskrunner;

import java.util.UUID;
import static org.junit.Assert.*;
import org.junit.Test;
import static org.mockito.Mockito.when;
import org.safehaus.kiskis.mgmt.api.commandrunner.AgentResult;
import org.safehaus.kiskis.mgmt.impl.commandrunner.AgentResultImpl;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;

/**
 *
 * @author dilshat
 */
public class AgentResultTest {

    private final String SOME_DUMMY_OUTPUT = "some dummy output";
    private final Integer OK_EXIT_CODE = 0;

    @Test(expected = NullPointerException.class)
    public void constructorShouldFailNullAgentUUID() {
        AgentResult agentResult = new AgentResultImpl(null);
    }

    @Test
    public void shouldNotAppendNullResponse() {
        AgentResultImpl agentResult = new AgentResultImpl(UUID.randomUUID());

        agentResult.appendResults(null);

        assertTrue(agentResult.getStdOut().isEmpty());
    }

    @Test
    public void shouldNotAppendAlienResponse() {
        AgentResultImpl agentResult = new AgentResultImpl(UUID.randomUUID());
        Response response = MockUtils.getIntermediateResponse(UUID.randomUUID(), UUID.randomUUID());
        when(response.getStdOut()).thenReturn(SOME_DUMMY_OUTPUT);

        agentResult.appendResults(response);

        assertTrue(agentResult.getStdOut().isEmpty());
    }

    @Test
    public void shouldAppendOwnResponse() {
        UUID agentUUID = UUID.randomUUID();
        AgentResultImpl agentResult = new AgentResultImpl(agentUUID);
        Response response = MockUtils.getIntermediateResponse(agentUUID, UUID.randomUUID());
        when(response.getStdOut()).thenReturn(SOME_DUMMY_OUTPUT);

        agentResult.appendResults(response);

        assertEquals(SOME_DUMMY_OUTPUT, agentResult.getStdOut());
    }

    @Test
    public void shouldAppendExitCode() {
        UUID agentUUID = UUID.randomUUID();
        AgentResultImpl agentResult = new AgentResultImpl(agentUUID);
        Response response = MockUtils.getSucceededResponse(agentUUID, UUID.randomUUID());

        agentResult.appendResults(response);

        assertEquals(OK_EXIT_CODE, agentResult.getExitCode());
    }

    @Test
    public void shouldNotAppendIfExitCodeAlreadySet() {
        UUID agentUUID = UUID.randomUUID();
        AgentResultImpl agentResult = new AgentResultImpl(agentUUID);
        Response response = MockUtils.getSucceededResponse(agentUUID, UUID.randomUUID());

        agentResult.appendResults(response);
        when(response.getStdOut()).thenReturn(SOME_DUMMY_OUTPUT);
        agentResult.appendResults(response);

        assertTrue(agentResult.getStdOut().isEmpty());
    }
}
