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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.hamcrest.core.Is.is;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.safehaus.kiskis.mgmt.api.commandrunner.CommandRunner;
import org.safehaus.kiskis.mgmt.impl.commandrunner.CommandRunnerImpl;
import org.junit.Test;
import static org.mockito.Mockito.*;
import org.safehaus.kiskis.mgmt.api.commandrunner.AgentResult;
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
    private final boolean allTests = true;

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
        Assume.assumeTrue(allTests);
        CommandRunner commandRunner = mock(CommandRunnerImpl.class);
        Command command = mock(Command.class);
        CommandCallback callback = mock(CommandCallback.class);

        commandRunner.runCommand(command, callback);

        verify(commandRunner).runCommand(command, callback);
    }

    @Test
    public void shouldAddListenerToCommManager() {
        Assume.assumeTrue(allTests);
        CommunicationManager communicationManager = mock(CommunicationManager.class);
        CommandRunnerImpl commandRunnerImpl = new CommandRunnerImpl(communicationManager);

        commandRunnerImpl.init();

        verify(communicationManager).addListener(commandRunnerImpl);
    }

    @Test
    public void shouldRemoveListenerFromCommManager() {
        Assume.assumeTrue(allTests);
        CommunicationManager communicationManager = mock(CommunicationManager.class);
        CommandRunnerImpl commandRunnerImpl = new CommandRunnerImpl(communicationManager);
        commandRunnerImpl.init();

        commandRunnerImpl.destroy();

        verify(communicationManager).removeListener(commandRunnerImpl);
    }

    @Test
    public void shouldSendRequestToCommManager() {
        Assume.assumeTrue(allTests);
        CommunicationManager communicationManager = mock(CommunicationManager.class);
        CommandRunnerImpl commandRunnerImpl = new CommandRunnerImpl(communicationManager);
        commandRunnerImpl.init();
        Command command = MockUtils.getCommand(commandRunnerImpl, UUID.randomUUID(), 1);

        commandRunnerImpl.runCommand(command);

        verify(communicationManager).sendRequest(any(Request.class));
    }

    @Test
    public void commandShouldTimeout() {
        Assume.assumeTrue(allTests);
        CommunicationManager communicationManager = mock(CommunicationManager.class);
        CommandRunnerImpl commandRunnerImpl = new CommandRunnerImpl(communicationManager);
        commandRunnerImpl.init();
        Command command = MockUtils.getCommand(commandRunnerImpl, UUID.randomUUID(), 1);

        commandRunnerImpl.runCommand(command);

        assertEquals(CommandStatus.TIMEDOUT, command.getCommandStatus());
    }

    @Test
    public void commandShouldTimeoutAsync() {
        Assume.assumeTrue(allTests);
        CommunicationManager communicationManager = mock(CommunicationManager.class);
        CommandRunnerImpl commandRunnerImpl = new CommandRunnerImpl(communicationManager);
        commandRunnerImpl.init();
        Command command = MockUtils.getCommand(commandRunnerImpl, UUID.randomUUID(), 1);

        commandRunnerImpl.runCommandAsync(command);

        Awaitility.await().atMost(1050, TimeUnit.MILLISECONDS).with().pollInterval(10, TimeUnit.MILLISECONDS)
                .untilCall(to(command).getCommandStatus(), is(CommandStatus.TIMEDOUT));

    }

    @Test
    public void commandShouldSucceed() throws InterruptedException {
        Assume.assumeTrue(allTests);
        CommunicationManager communicationManager = mock(CommunicationManager.class);
        final CommandRunnerImpl commandRunnerImpl = new CommandRunnerImpl(communicationManager);
        commandRunnerImpl.init();
        UUID agentUUID = UUID.randomUUID();
        CommandImpl command = (CommandImpl) MockUtils.getCommand(commandRunnerImpl, agentUUID, 1);
        UUID commandUUID = command.getCommandUUID();
        final Response response = MockUtils.getSucceededResponse(agentUUID, commandUUID);

        commandRunnerImpl.runCommandAsync(command);
        exec.execute(new Runnable() {

            public void run() {
                commandRunnerImpl.onResponse(response);
            }
        });

        Awaitility.await().atMost(1, TimeUnit.SECONDS).with().pollInterval(100, TimeUnit.MILLISECONDS)
                .untilCall(to(command).getCommandStatus(), is(CommandStatus.SUCCEEDED));

    }

    @Test
    public void commandShouldFail() throws InterruptedException {
        Assume.assumeTrue(allTests);
        CommunicationManager communicationManager = mock(CommunicationManager.class);
        final CommandRunnerImpl commandRunnerImpl = new CommandRunnerImpl(communicationManager);
        commandRunnerImpl.init();
        UUID agentUUID = UUID.randomUUID();
        CommandImpl command = (CommandImpl) MockUtils.getCommand(commandRunnerImpl, agentUUID, 1);
        UUID commandUUID = command.getCommandUUID();
        final Response response = MockUtils.getFailedResponse(agentUUID, commandUUID);

        commandRunnerImpl.runCommandAsync(command);
        exec.execute(new Runnable() {

            public void run() {
                commandRunnerImpl.onResponse(response);
            }
        });

        Awaitility.await().atMost(1, TimeUnit.SECONDS).with().pollInterval(100, TimeUnit.MILLISECONDS)
                .untilCall(to(command).getCommandStatus(), is(CommandStatus.FAILED));

    }

    @Test
    public void commandShouldStop() throws InterruptedException {
        Assume.assumeTrue(allTests);
        CommunicationManager communicationManager = mock(CommunicationManager.class);
        final CommandRunnerImpl commandRunnerImpl = new CommandRunnerImpl(communicationManager);
        commandRunnerImpl.init();
        UUID agentUUID = UUID.randomUUID();
        CommandImpl command = (CommandImpl) MockUtils.getCommand(commandRunnerImpl, agentUUID, 1);
        UUID commandUUID = command.getCommandUUID();
        final Response response = MockUtils.getIntermediateResponse(agentUUID, commandUUID);

        final AtomicInteger atomicInteger = new AtomicInteger();
        commandRunnerImpl.runCommandAsync(command, new CommandCallback() {

            @Override
            public void onResponse(Response response, AgentResult agentResult, Command command) {
                atomicInteger.incrementAndGet();
                stop();
            }

        });
        exec.execute(new Runnable() {

            public void run() {
                commandRunnerImpl.onResponse(response);
                commandRunnerImpl.onResponse(response);

            }
        });

        Awaitility.await().atMost(1, TimeUnit.SECONDS).with().pollInterval(50, TimeUnit.MILLISECONDS)
                .and().pollDelay(100, TimeUnit.MILLISECONDS).until(new Callable<Boolean>() {

                    public Boolean call() throws Exception {
                        return atomicInteger.get() == 1;
                    }
                });

    }

}
