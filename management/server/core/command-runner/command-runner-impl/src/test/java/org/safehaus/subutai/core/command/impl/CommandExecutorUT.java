/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.command.impl;


import org.junit.Test;
import org.safehaus.subutai.core.command.api.CommandCallback;
import org.safehaus.subutai.core.command.impl.CommandExecutor;
import org.safehaus.subutai.core.command.impl.CommandImpl;

import java.util.concurrent.ExecutorService;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;


/**
 * Test for CommandExecutor class
 */
public class CommandExecutorUT {

	@Test (expected = NullPointerException.class)
	public void constructorShouldFailNullCommand() {
		new CommandExecutor(null, mock(ExecutorService.class), mock(CommandCallback.class));
	}


	@Test (expected = NullPointerException.class)
	public void constructorShouldFailNullExecutor() {
		new CommandExecutor(mock(CommandImpl.class), null, mock(CommandCallback.class));
	}


	@Test (expected = NullPointerException.class)
	public void constructorShouldFailNullCallback() {
		new CommandExecutor(mock(CommandImpl.class), mock(ExecutorService.class), null);
	}


	@Test
	public void constructorShouldPass() {
		new CommandExecutor(mock(CommandImpl.class), mock(ExecutorService.class), mock(CommandCallback.class));
	}


	@Test
	public void shouldReturnValidValues() {
		CommandImpl command = mock(CommandImpl.class);
		ExecutorService executorService = mock(ExecutorService.class);
		CommandCallback callback = mock(CommandCallback.class);
		CommandExecutor commandExecutor = new CommandExecutor(command, executorService, callback);

		assertEquals(command, commandExecutor.getCommand());
		assertEquals(executorService, commandExecutor.getExecutor());
		assertEquals(callback, commandExecutor.getCallback());
	}
}
