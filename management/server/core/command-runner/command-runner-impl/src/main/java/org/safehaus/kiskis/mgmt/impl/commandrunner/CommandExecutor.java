/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.commandrunner;

import org.safehaus.kiskis.mgmt.api.commandrunner.CommandCallback;
import java.util.concurrent.ExecutorService;

/**
 *
 * @author dilshat
 */
class CommandExecutor {

    final private CommandImpl command;
    final private ExecutorService executor;
    final private CommandCallback callback;

    public CommandExecutor(CommandImpl command, ExecutorService executor, CommandCallback callback) {

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
