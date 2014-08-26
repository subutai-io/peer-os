/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.impl.commandrunner;


import com.google.common.base.Preconditions;
import org.safehaus.subutai.api.commandrunner.CommandCallback;

import java.util.concurrent.ExecutorService;


/**
 * This class holds command together with associated executor and callback
 */
public class CommandExecutor {

	final private CommandImpl command;
	final private ExecutorService executor;
	final private CommandCallback callback;


	public CommandExecutor(CommandImpl command, ExecutorService executor, CommandCallback callback) {

		Preconditions.checkNotNull(command, "Command is null");
		Preconditions.checkNotNull(executor, "Executor is null");
		Preconditions.checkNotNull(callback, "Callback is null");

		this.command = command;
		this.executor = executor;
		this.callback = callback;
	}


	public CommandImpl getCommand() {
		return command;
	}


	public ExecutorService getExecutor() {
		return executor;
	}


	public CommandCallback getCallback() {
		return callback;
	}
}
