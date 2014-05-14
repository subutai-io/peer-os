package org.safehaus.kiskis.mgmt.impl.solr.util;


import java.util.Map;
import java.util.UUID;

import org.safehaus.kiskis.mgmt.api.commandrunner.AgentResult;
import org.safehaus.kiskis.mgmt.api.commandrunner.Command;
import org.safehaus.kiskis.mgmt.api.commandrunner.CommandStatus;


public class CommandMock implements Command {

    private String description;

    public CommandMock( String description ) {
        this.description = description;
    }

    @Override
    public String getDescription() {
        return description;
    }


    @Override
    public boolean hasCompleted() {
        return false;
    }


    @Override
    public boolean hasSucceeded() {
        return false;
    }


    @Override
    public CommandStatus getCommandStatus() {
        return null;
    }


    @Override
    public Map<UUID, AgentResult> getResults() {
        return null;
    }


    @Override
    public UUID getCommandUUID() {
        return null;
    }


    @Override
    public Object getData() {
        return null;
    }


    @Override
    public void setData( final Object data ) { }


    @Override
    public String getAllErrors() {
        return null;
    }
}
