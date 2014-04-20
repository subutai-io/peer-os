/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.taskrunner;

import org.safehaus.kiskis.mgmt.api.commandrunner.CommandCallback;
import org.safehaus.kiskis.mgmt.api.commandrunner.CommandRunner;
import org.safehaus.kiskis.mgmt.impl.commandrunner.CommandRunnerImpl;
import org.junit.Test;
import static org.mockito.Mockito.*;
import org.safehaus.kiskis.mgmt.api.communicationmanager.CommunicationManager;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;

/**
 *
 * @author dilshat
 */
public class CommandRunnerTest {

//    @Test
//    public void shouldRunCommand() {
//        Agent agent = mock(Agent.class);
//        CommandRunner commandRunner = mock(CommandRunnerImpl.class);
//
//        commandRunner.runCommand("ps", agent, 10);
//
//        verify(commandRunner).runCommand("ps", agent, 10);
//    }
//
//    @Test
//    public void shouldRunCommandAsync() {
//        Agent agent = mock(Agent.class);
//        CommandRunner commandRunner = mock(CommandRunnerImpl.class);
//
//        commandRunner.runCommandAsync("ps", agent, 10);
//
//        verify(commandRunner).runCommandAsync("ps", agent, 10);
//    }
//
//    @Test
//    public void shouldRunCommandWithCallback() {
//        CommandCallback commandCallback = mock(CommandCallback.class);
//        Agent agent = mock(Agent.class);
//        CommandRunner commandRunner = mock(CommandRunnerImpl.class);
//
//        commandRunner.runCommand("ps", agent, 10, commandCallback);
//
//        verify(commandRunner).runCommand("ps", agent, 10, commandCallback);
//    }
//
//    @Test
//    public void shouldRunCommandWithCallbackAsync() {
//        CommandCallback commandCallback = mock(CommandCallback.class);
//        Agent agent = mock(Agent.class);
//        CommandRunner commandRunner = mock(CommandRunnerImpl.class);
//
//        commandRunner.runCommandAsync("ps", agent, 10, commandCallback);
//
//        verify(commandRunner).runCommandAsync("ps", agent, 10, commandCallback);
//    }
//
//    @Test(expected = NullPointerException.class)
//    public void shouldFailConstructorWithNullCommunicationManager() {
//        CommandRunner commandRunner = new CommandRunnerImpl(null);
//    }
//
//    @Test
//    public void shouldPassConstructorWithValidCommunicationManager() {
//        CommunicationManager communicationManager = mock(CommunicationManager.class);
//
//        CommandRunner commandRunner = new CommandRunnerImpl(communicationManager);
//
//    }
//
//    @Test
//    public void shouldAddListenerToCommunicationManager() {
//        CommunicationManager communicationManager = mock(CommunicationManager.class);
//        CommandRunnerImpl commandRunner = new CommandRunnerImpl(communicationManager);
//
//        commandRunner.init();
//
//        verify(communicationManager).addListener(commandRunner);
//    }
//
//    @Test
//    public void shouldRemoveListenerFromCommunicationManager() {
//        CommunicationManager communicationManager = mock(CommunicationManager.class);
//        CommandRunnerImpl commandRunner = new CommandRunnerImpl(communicationManager);
//
//        commandRunner.init();
//        commandRunner.destroy();
//
//        verify(communicationManager).removeListener(commandRunner);
//    }
//
//    @Test
//    public void shouldCreateExecutor() {

//    }

//    @Test
//    public void shouldCallCommunicationManagerSendRequest() {
//        CommunicationManager communicationManager = mock(CommunicationManager.class);
//        CommandCallback commandCallback = mock(CommandCallback.class);
//        Agent agent = mock(Agent.class);
//        CommandRunner commandRunner = new CommandRunnerImpl(communicationManager);
//
//        commandRunner.runCommandAsync("ps", agent, 10, commandCallback);
//
//        verify(communicationManager).sendRequest((Request) any());
//    }
}
