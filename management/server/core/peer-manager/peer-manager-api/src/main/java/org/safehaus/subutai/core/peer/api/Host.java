package org.safehaus.subutai.core.peer.api;


import java.io.Serializable;
import java.util.UUID;

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.command.CommandCallback;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;


/**
 * Base Host interface.
 */
public interface Host extends Serializable
{
    public Agent getAgent();

    public Agent getParentAgent();

    public void setParentAgent( Agent agent );

    public UUID getPeerId();

    public UUID getId();

    public String getParentHostname();

    public String getHostname();

    public CommandResult execute( RequestBuilder requestBuilder ) throws CommandException;

    public CommandResult execute( RequestBuilder requestBuilder, CommandCallback callback ) throws CommandException;

    public void executeAsync( RequestBuilder requestBuilder, CommandCallback callback ) throws CommandException;

    public void executeAsync( RequestBuilder requestBuilder ) throws CommandException;

    public boolean isConnected();

    public long getLastHeartbeat();
}
