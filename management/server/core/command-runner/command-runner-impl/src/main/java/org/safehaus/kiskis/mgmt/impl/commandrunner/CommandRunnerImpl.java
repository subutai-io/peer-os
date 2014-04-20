/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.commandrunner;

import org.safehaus.kiskis.mgmt.api.commandrunner.CommandRunner;
import org.safehaus.kiskis.mgmt.api.commandrunner.CommandCallback;
import org.safehaus.kiskis.mgmt.api.commandrunner.Command;
import org.safehaus.kiskis.mgmt.api.commandrunner.CommandStatus;
import com.google.common.base.Preconditions;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.api.commandrunner.RequestBuilder;
import org.safehaus.kiskis.mgmt.api.communicationmanager.CommunicationManager;
import org.safehaus.kiskis.mgmt.api.communicationmanager.ResponseListener;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;

/**
 *
 * @author dilshat
 */
public class CommandRunnerImpl implements CommandRunner, ResponseListener {

    private static final Logger LOG = Logger.getLogger(CommandRunnerImpl.class.getName());

    private final CommunicationManager communicationManager;
    private ExpiringCache<UUID, CommandExecutor> commandExecutors;

    public CommandRunnerImpl(CommunicationManager communicationManager) {
        Preconditions.checkNotNull(communicationManager, "Communication Manager is null");

        this.communicationManager = communicationManager;
    }

    public void init() {
        communicationManager.addListener(this);
        commandExecutors = new ExpiringCache<UUID, CommandExecutor>();
    }

    public void destroy() {
        communicationManager.removeListener(this);
        Map<UUID, CacheEntry<CommandExecutor>> entries = commandExecutors.getEntries();
        //shutdown all executors which are still there
        for (Map.Entry<UUID, CacheEntry<CommandExecutor>> entry : entries.entrySet()) {
            try {
                entry.getValue().getValue().getExecutor().shutdown();
            } catch (Exception e) {
            }
        }
        commandExecutors.dispose();

    }

    public void runCommandAsync(final Command command, CommandCallback commandCallback) {
        Preconditions.checkNotNull(command, "Command is null");
        Preconditions.checkArgument(command instanceof CommandImpl, "Command is of wrong type");
        Preconditions.checkNotNull(commandCallback, "Callback is null");
        final CommandImpl commandImpl = (CommandImpl) command;
        Preconditions.checkArgument(commandExecutors.get(commandImpl.getCommandUUID()) == null, ""
                + "This command has been already queued for execution");

        ExecutorService executor = Executors.newSingleThreadExecutor();
        CommandExecutor commandExecutor = new CommandExecutor(commandImpl, executor, commandCallback);

        boolean queued = commandExecutors.put(commandImpl.getCommandUUID(), commandExecutor, commandImpl.getTimeout() * 1000, new EntryExpiryCallback<CommandExecutor>() {

            public void onEntryExpiry(CommandExecutor entry) {
                try {
                    //obtain command lock 
                    entry.getCommand().getUpdateLock();
                    try {
                        //set command status to TIMEDOUT if it is not completed or interrupted yet
                        if (!(entry.getCommand().hasCompleted() || entry.getCallback().isStopped())) {
                            entry.getCommand().setCommandStatus(CommandStatus.TIMEDOUT);
                        }
                        //call this to notify all waiting threads that command timed out
                        entry.getCommand().notifyWaitingThreads();
                        //shutdown command executor
                        entry.getExecutor().shutdown();
                    } finally {
                        entry.getCommand().releaseUpdateLock();
                    }
                } catch (Exception e) {
                }
            }
        });

        if (queued) {
            //set command status to RUNNING
            commandImpl.setCommandStatus(CommandStatus.RUNNING);
            //execute command
            for (Request request : commandImpl.getRequests()) {
                communicationManager.sendRequest(request);
            }
//            executor.execute(new Runnable() {
//
//                public void run() {
//                    for (Request request : commandImpl.getRequests()) {
//                        communicationManager.sendRequest(request);
//                    }
//                }
//            });

        }
    }

    public void onResponse(final Response response) {
        if (response != null && response.getUuid() != null) {
            final CommandExecutor commandExecutor = commandExecutors.get(response.getTaskUuid());

            if (commandExecutor != null) {

                //process command response
                commandExecutor.getExecutor().execute(new Runnable() {

                    public void run() {
                        //obtain command lock
                        commandExecutor.getCommand().getUpdateLock();
                        try {
                            if (commandExecutors.get(response.getTaskUuid()) != null) {
                                //append results to command
                                commandExecutor.getCommand().appendResult(response);

                                //call command callback
                                try {
                                    commandExecutor.getCallback().onResponse(
                                            response,
                                            commandExecutor.getCommand().getResults().get(response.getUuid()),
                                            commandExecutor.getCommand());
                                } catch (Exception e) {
                                    LOG.log(Level.SEVERE, "Error in callback {0}", e);
                                }

                                //do cleanup on command completion or interruption by user
                                if (commandExecutor.getCommand().hasCompleted()
                                        || commandExecutor.getCallback().isStopped()) {
                                    //remove command executor so that 
                                    //if response comes from agent it is not processed by callback
                                    commandExecutors.remove(commandExecutor.getCommand().getCommandUUID());
                                    //call this to notify all waiting threads that command completed
                                    commandExecutor.getCommand().notifyWaitingThreads();
                                    //shutdown command executor
                                    commandExecutor.getExecutor().shutdown();
                                }
                            }
                        } finally {
                            commandExecutor.getCommand().releaseUpdateLock();
                        }
                    }

                });
            }
        }
    }

    public Command createCommand(RequestBuilder requestBuilder, Set<Agent> agents) {
        return new CommandImpl(requestBuilder, agents);
    }

    public void runCommand(Command command, CommandCallback commandCallback) {
        runCommandAsync(command, commandCallback);
        ((CommandImpl) command).waitCompletion();
    }

    public void runCommandAsync(Command command) {
        runCommandAsync(command, new CommandCallback());
    }

    public void runCommand(Command command) {
        runCommandAsync(command, new CommandCallback());
        ((CommandImpl) command).waitCompletion();
    }

}
