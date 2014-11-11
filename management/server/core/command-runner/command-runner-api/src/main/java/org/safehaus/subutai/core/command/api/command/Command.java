/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.command.api.command;


import java.util.Map;
import java.util.UUID;

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandStatus;


/**
 * Command to execute on agent(s)
 */
public interface Command
{

    /**
     * Shows if command has completed. The same as checking command.getCommandStatus == CommandStatus.SUCCEEDED ||
     * command.getCommandStatus == CommandStatus.FAILED
     *
     * @return - true if completed, false otherwise
     */
    public boolean hasCompleted();

    /**
     * Shows if command has succeeded. The same as checking command.getCommandStatus == CommandStatus.SUCCEEDED
     *
     * @return - true if succeeded, false otherwise
     */
    public boolean hasSucceeded();

    /**
     * Returns command status
     *
     * @return - status of command
     */
    public CommandStatus getCommandStatus();

    /**
     * Returns map of results from agents where key is agent's UUID and value is instance of AgentResult
     *
     * @return - map of agents' results
     */
    public Map<UUID, AgentResult> getResults();

    /**
     * Returns command UUID
     *
     * @return - command UUID
     */
    public UUID getCommandUUID();

    /**
     * Returns custom object assigned to this command
     *
     * @return - custom object assigned to this command or null
     */
    public Object getData();

    /**
     * Lets assign custom object to this command
     *
     * @param data - custom object
     */
    public void setData( Object data );

    /**
     * Returns all std err outputs from agents joined in one string
     *
     * @return - all std err outputs from agents joined in one string
     */
    public String getAllErrors();

    /**
     * Returns description of command or null
     *
     * @return - description of command or null
     */
    public String getDescription();


    public void execute( CommandCallback callback ) throws CommandException;

    public void executeAsync( CommandCallback callback ) throws CommandException;

    public void execute() throws CommandException;

    public void executeAsync() throws CommandException;
}
