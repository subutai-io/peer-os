package org.safehaus.subutai.plugin.common.mock;


import java.util.Map;
import java.util.UUID;

import org.safehaus.subutai.core.command.api.command.AgentResult;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.command.api.command.CommandCallback;
import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandStatus;


public class CommandMock implements Command
{

    private String description;
    private boolean succeeded;


    @Override
    public boolean hasCompleted()
    {
        return false;
    }


    @Override
    public boolean hasSucceeded()
    {
        return succeeded;
    }


    @Override
    public CommandStatus getCommandStatus()
    {
        return null;
    }


    @Override
    public Map<UUID, AgentResult> getResults()
    {
        return null;
    }


    @Override
    public UUID getCommandUUID()
    {
        return null;
    }


    @Override
    public Object getData()
    {
        return null;
    }


    @Override
    public void setData( final Object data )
    {

    }


    @Override
    public String getAllErrors()
    {
        return null;
    }


    @Override
    public String getDescription()
    {
        return description;
    }


    @Override
    public void execute( final CommandCallback callback ) throws CommandException
    {

    }


    @Override
    public void executeAsync( final CommandCallback callback ) throws CommandException
    {

    }


    @Override
    public void execute() throws CommandException
    {

    }


    @Override
    public void executeAsync() throws CommandException
    {

    }


    public CommandMock setDescription( final String description )
    {
        this.description = description;
        return this;
    }


    public CommandMock setSucceeded( boolean succeeded )
    {
        this.succeeded = succeeded;
        return this;
    }
}
