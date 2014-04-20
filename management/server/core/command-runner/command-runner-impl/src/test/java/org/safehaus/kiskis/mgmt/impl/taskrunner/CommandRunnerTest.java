/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.taskrunner;

import com.jayway.awaitility.Awaitility;
import static com.jayway.awaitility.Awaitility.to;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import static org.hamcrest.core.Is.is;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.safehaus.kiskis.mgmt.api.commandrunner.CommandRunner;
import org.safehaus.kiskis.mgmt.impl.commandrunner.CommandRunnerImpl;
import org.junit.Test;
import static org.mockito.Mockito.*;
import org.safehaus.kiskis.mgmt.api.commandrunner.Command;
import org.safehaus.kiskis.mgmt.api.commandrunner.CommandCallback;
import org.safehaus.kiskis.mgmt.api.commandrunner.CommandStatus;
import org.safehaus.kiskis.mgmt.api.commandrunner.RequestBuilder;
import org.safehaus.kiskis.mgmt.api.communicationmanager.CommunicationManager;
import org.safehaus.kiskis.mgmt.impl.commandrunner.CommandImpl;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;

/**
 *
 * @author dilshat
 */
public class CommandRunnerTest {

    private static ExecutorService exec;

    @BeforeClass
    public static void setupClass() {
        exec = Executors.newCachedThreadPool();
    }

    @AfterClass
    public static void afterClass() {
        exec.shutdown();
    }

    @Test
    public void shouldRunCommand() {
        CommandRunner commandRunner = mock(CommandRunnerImpl.class);
        Command command = mock(Command.class);
        CommandCallback callback = mock(CommandCallback.class);

        commandRunner.runCommand(command, callback);

        verify(commandRunner).runCommand(command, callback);
    }

    @Test
    public void shouldAddListenerToCommManager() {
        CommunicationManager communicationManager = mock(CommunicationManager.class);
        CommandRunnerImpl commandRunnerImpl = new CommandRunnerImpl(communicationManager);

        commandRunnerImpl.init();

        verify(communicationManager).addListener(commandRunnerImpl);
    }

    @Test
    public void shouldRemoveListenerFromCommManager() {
        CommunicationManager communicationManager = mock(CommunicationManager.class);
        CommandRunnerImpl commandRunnerImpl = new CommandRunnerImpl(communicationManager);
        commandRunnerImpl.init();

        commandRunnerImpl.destroy();

        verify(communicationManager).removeListener(commandRunnerImpl);
    }

    @Test
    public void shouldSendRequestToCommManager() {
        CommunicationManager communicationManager = mock(CommunicationManager.class);
        CommandRunnerImpl commandRunnerImpl = new CommandRunnerImpl(communicationManager);
        commandRunnerImpl.init();
        UUID uuid = UUID.randomUUID();
        CommandImpl commandImpl = mock(CommandImpl.class);
        when(commandImpl.getCommandUUID()).thenReturn(uuid);
        when(commandImpl.getTimeout()).thenReturn(1);
        Request request = mock(Request.class);
        Set<Request> requests = new HashSet<Request>();
        requests.add(request);
        when(commandImpl.getRequests()).thenReturn(requests);

        commandRunnerImpl.runCommand(commandImpl);

        verify(communicationManager).sendRequest(any(Request.class));
    }

    @Test
    public void commandShouldTimeout() {
        CommunicationManager communicationManager = mock(CommunicationManager.class);
        CommandRunnerImpl commandRunnerImpl = new CommandRunnerImpl(communicationManager);
        Agent agent = mock(Agent.class);
        Set<Agent> agents = new HashSet<Agent>();
        agents.add(agent);
        RequestBuilder builder = mock(RequestBuilder.class);
        when(builder.getTimeout()).thenReturn(1);
        Command command = commandRunnerImpl.createCommand(builder, agents);
        commandRunnerImpl.init();

        commandRunnerImpl.runCommand(command);

        assertEquals(CommandStatus.TIMEDOUT, command.getCommandStatus());
    }

    @Test
    public void commandShouldTimeoutAsync() {
        CommunicationManager communicationManager = mock(CommunicationManager.class);
        CommandRunnerImpl commandRunnerImpl = new CommandRunnerImpl(communicationManager);
        Agent agent = mock(Agent.class);
        Set<Agent> agents = new HashSet<Agent>();
        agents.add(agent);
        RequestBuilder builder = mock(RequestBuilder.class);
        when(builder.getTimeout()).thenReturn(1);
        commandRunnerImpl.init();
        final Command command = commandRunnerImpl.createCommand(builder, agents);

        commandRunnerImpl.runCommandAsync(command);

        Awaitility.await().atMost(1050, TimeUnit.MILLISECONDS).with().pollInterval(10, TimeUnit.MILLISECONDS)
                .untilCall(to(command).getCommandStatus(), is(CommandStatus.TIMEDOUT));

    }

    @Test
    public void commandShouldSucceed() throws InterruptedException {
        CommunicationManager communicationManager = mock(CommunicationManager.class);
        final CommandRunnerImpl commandRunnerImpl = new CommandRunnerImpl(communicationManager);
        commandRunnerImpl.init();
        Agent agent = mock(Agent.class);
        Set<Agent> agents = new HashSet<Agent>();
        agents.add(agent);
        UUID agentUUID = UUID.randomUUID();
        when(agent.getUuid()).thenReturn(agentUUID);
        RequestBuilder builder = mock(RequestBuilder.class);
        when(builder.getTimeout()).thenReturn(1);
        final Command command = commandRunnerImpl.createCommand(builder, agents);
        UUID commandUUID = ((CommandImpl) command).getCommandUUID();
        final Response response = mock(Response.class);
        when(response.getUuid()).thenReturn(agentUUID);
        when(response.getTaskUuid()).thenReturn(commandUUID);
        when(response.isFinal()).thenReturn(true);
        when(response.hasSucceeded()).thenReturn(true);

        commandRunnerImpl.runCommandAsync(command);
        exec.execute(new Runnable() {

            public void run() {
                commandRunnerImpl.onResponse(response);
            }
        });

        Awaitility.await().atMost(1, TimeUnit.SECONDS).with().pollInterval(100, TimeUnit.MILLISECONDS)
                .untilCall(to(command).getCommandStatus(), is(CommandStatus.SUCCEEDED));

    }

}
