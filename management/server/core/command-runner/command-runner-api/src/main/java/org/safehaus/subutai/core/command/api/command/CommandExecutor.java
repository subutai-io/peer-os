/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.command.api.command;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.common.base.Preconditions;


/**
 * This class holds command together with associated executor and callback
 */
public class CommandExecutor
{

    final private AbstractCommand command;
    final private ExecutorService executor;
    final private CommandCallback callback;


    public CommandExecutor( AbstractCommand command, CommandCallback callback )
    {

        Preconditions.checkNotNull( command, "Command is null" );
        Preconditions.checkNotNull( callback, "Callback is null" );

        this.command = command;
        this.executor = Executors.newSingleThreadExecutor();
        this.callback = callback;
    }


    /**
     * Returns associated command
     *
     * @return {@code Command}
     */
    public AbstractCommand getCommand()
    {
        return command;
    }


    /**
     * Returns associated executor service
     *
     * @return {@code ExecutorService}
     */
    public ExecutorService getExecutor()
    {
        return executor;
    }


    /**
     * Returns associated callback
     *
     * @return {@code CommandCallback}
     */
    public CommandCallback getCallback()
    {
        return callback;
    }
}
