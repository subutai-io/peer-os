/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.taskrunner;

import com.jayway.awaitility.Awaitility;
import static com.jayway.awaitility.Awaitility.to;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.hamcrest.core.Is.is;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.safehaus.kiskis.mgmt.api.commandrunner.CommandRunner;
import org.safehaus.kiskis.mgmt.impl.commandrunner.CommandRunnerImpl;
import org.junit.Test;
import static org.mockito.Mockito.*;
import org.safehaus.kiskis.mgmt.api.commandrunner.AgentResult;
import org.safehaus.kiskis.mgmt.api.commandrunner.Command;
import org.safehaus.kiskis.mgmt.api.commandrunner.CommandCallback;
import org.safehaus.kiskis.mgmt.api.commandrunner.CommandStatus;
import org.safehaus.kiskis.mgmt.api.communicationmanager.CommunicationManager;
import org.safehaus.kiskis.mgmt.impl.commandrunner.CommandImpl;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;

/**
 *
 * @author dilshat
 */
public class CommandRunnerTest {

    private static ExecutorService exec;
    private final boolean allTests = true;
    private CommunicationManager communicationManager;
    private CommandRunnerImpl commandRunner;

    @BeforeClass
    public static void setupClass() {
        exec = Executors.newCachedThreadPool();
    }

    @AfterClass
    public static void afterClass() {
        exec.shutdown();
    }

    @Before
    public void beforeMethod() {
        communicationManager = mock(CommunicationManager.class);
        commandRunner = new CommandRunnerImpl(communicationManager);
        commandRunner.init();
    }

    @After
    public void afterMethod() {
        commandRunner.destroy();
    }

    @Test
    public void shouldRunCommand() {
        Assume.assumeTrue(allTests);
        CommandRunner commandRunnerMock = mock(CommandRunner.class);
        Command command = mock(CommandImpl.class);
        CommandCallback callback = mock(CommandCallback.class);

        commandRunnerMock.runCommand(command, callback);

        verify(commandRunnerMock).runCommand(command, callback);
    }

    @Test
    public void shouldAddListenerToCommManager() {
        Assume.assumeTrue(allTests);

        verify(communicationManager).addListener(commandRunner);
    }

    @Test
    public void shouldRemoveListenerFromCommManager() {
        Assume.assumeTrue(allTests);

        commandRunner.destroy();

        verify(communicationManager).removeListener(commandRunner);
    }

    @Test
    public void shouldSendRequestToCommManager() {
        Assume.assumeTrue(allTests);

        Command command = MockUtils.getCommand(commandRunner, UUID.randomUUID(), 1);

        commandRunner.runCommand(command);

        verify(communicationManager).sendRequest(any(Request.class));
    }

    @Test
    public void commandShouldTimeout() {
        Assume.assumeTrue(allTests);

        Command command = MockUtils.getCommand(commandRunner, UUID.randomUUID(), 1);

        commandRunner.runCommand(command);

        assertEquals(CommandStatus.TIMEDOUT, command.getCommandStatus());
    }

    @Test
    public void commandShouldTimeoutAsync() {
        Assume.assumeTrue(allTests);

        Command command = MockUtils.getCommand(commandRunner, UUID.randomUUID(), 1);

        commandRunner.runCommandAsync(command);

        Awaitility.await().atMost(1050, TimeUnit.MILLISECONDS).with().pollInterval(10, TimeUnit.MILLISECONDS)
                .untilCall(to(command).getCommandStatus(), is(CommandStatus.TIMEDOUT));

    }

    @Test
    public void commandShouldSucceedAsync() throws InterruptedException {
        Assume.assumeTrue(allTests);

        UUID agentUUID = UUID.randomUUID();
        Command command = MockUtils.getCommand(commandRunner, agentUUID, 1);
        final Response response = MockUtils.getSucceededResponse(agentUUID, command.getCommandUUID());

        commandRunner.runCommandAsync(command);
        exec.execute(new Runnable() {

            public void run() {
                commandRunner.onResponse(response);
            }
        });

        Awaitility.await().atMost(1, TimeUnit.SECONDS).with().pollInterval(100, TimeUnit.MILLISECONDS)
                .untilCall(to(command).getCommandStatus(), is(CommandStatus.SUCCEEDED));

    }

    @Test
    public void commandShouldSucceed() throws InterruptedException {
        Assume.assumeTrue(allTests);

        UUID agentUUID = UUID.randomUUID();
        Command command = MockUtils.getCommand(commandRunner, agentUUID, 1);
        final Response response = MockUtils.getSucceededResponse(agentUUID, command.getCommandUUID());

        exec.execute(new Runnable() {

            public void run() {
                try {
                    Thread.sleep(100);
                    commandRunner.onResponse(response);
                } catch (InterruptedException ex) {
                    return;
                }
            }
        });
        commandRunner.runCommand(command);

        Awaitility.await().atMost(1, TimeUnit.SECONDS).with().pollInterval(100, TimeUnit.MILLISECONDS)
                .untilCall(to(command).getCommandStatus(), is(CommandStatus.SUCCEEDED));

    }

    @Test
    public void commandShouldFailAsync() throws InterruptedException {
        Assume.assumeTrue(allTests);

        UUID agentUUID = UUID.randomUUID();
        Command command = MockUtils.getCommand(commandRunner, agentUUID, 1);
        final Response response = MockUtils.getFailedResponse(agentUUID, command.getCommandUUID());

        commandRunner.runCommandAsync(command);
        exec.execute(new Runnable() {

            public void run() {
                commandRunner.onResponse(response);
            }
        });

        Awaitility.await().atMost(1, TimeUnit.SECONDS).with().pollInterval(100, TimeUnit.MILLISECONDS)
                .untilCall(to(command).getCommandStatus(), is(CommandStatus.FAILED));

    }

    @Test
    public void commandShouldFail() throws InterruptedException {
        Assume.assumeTrue(allTests);

        UUID agentUUID = UUID.randomUUID();
        Command command = MockUtils.getCommand(commandRunner, agentUUID, 1);
        final Response response = MockUtils.getFailedResponse(agentUUID, command.getCommandUUID());

        exec.execute(new Runnable() {

            public void run() {
                try {
                    Thread.sleep(100);
                    commandRunner.onResponse(response);
                } catch (InterruptedException ex) {
                    return;
                }
            }
        });

        commandRunner.runCommand(command);

        Awaitility.await().atMost(1, TimeUnit.SECONDS).with().pollInterval(100, TimeUnit.MILLISECONDS)
                .untilCall(to(command).getCommandStatus(), is(CommandStatus.FAILED));

    }

    @Test
    public void commandShouldStopAsync() throws InterruptedException {
        Assume.assumeTrue(allTests);

        UUID agentUUID = UUID.randomUUID();
        Command command = MockUtils.getCommand(commandRunner, agentUUID, 1);
        final Response response = MockUtils.getIntermediateResponse(agentUUID, command.getCommandUUID());

        final AtomicInteger atomicInteger = new AtomicInteger();
        commandRunner.runCommandAsync(command, new CommandCallback() {

            @Override
            public void onResponse(Response response, AgentResult agentResult, Command command) {
                atomicInteger.incrementAndGet();
                stop();
            }

        });
        exec.execute(new Runnable() {

            public void run() {
                commandRunner.onResponse(response);
                commandRunner.onResponse(response);

            }
        });

        Awaitility.await().atMost(1, TimeUnit.SECONDS).with().pollInterval(50, TimeUnit.MILLISECONDS)
                .and().pollDelay(100, TimeUnit.MILLISECONDS).until(new Callable<Boolean>() {

                    public Boolean call() throws Exception {
                        return atomicInteger.get() == 1;
                    }
                });

    }

    @Test
    public void commandShouldStop() throws InterruptedException {
        Assume.assumeTrue(allTests);

        UUID agentUUID = UUID.randomUUID();
        Command command = MockUtils.getCommand(commandRunner, agentUUID, 1);
        final Response response = MockUtils.getIntermediateResponse(agentUUID, command.getCommandUUID());

        final AtomicInteger atomicInteger = new AtomicInteger();
        exec.execute(new Runnable() {

            public void run() {
                try {
                    Thread.sleep(100);
                    commandRunner.onResponse(response);
                    commandRunner.onResponse(response);
                } catch (InterruptedException ex) {
                    return;
                }
            }
        });
        commandRunner.runCommand(command, new CommandCallback() {

            @Override
            public void onResponse(Response response, AgentResult agentResult, Command command) {
                atomicInteger.incrementAndGet();
                stop();
            }

        });

        Awaitility.await().atMost(1, TimeUnit.SECONDS).with().pollInterval(50, TimeUnit.MILLISECONDS)
                .and().pollDelay(200, TimeUnit.MILLISECONDS).until(new Callable<Boolean>() {

                    public Boolean call() throws Exception {
                        return atomicInteger.get() == 1;
                    }
                });

    }

}
