package org.safehaus.subutai.core.peer.api;


import java.io.Serializable;
import java.util.UUID;

import org.safehaus.subutai.common.exception.CommandException;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.CommandCallback;
import org.safehaus.subutai.common.protocol.CommandResult;
import org.safehaus.subutai.common.protocol.RequestBuilder;


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

    public CommandResult execute( RequestBuilder requestBuilder, CommandCallback commandCallback )
            throws CommandException;

    public void executeAsync( RequestBuilder requestBuilder, CommandCallback commandCallback ) throws CommandException;

    public void executeAsync( RequestBuilder requestBuilder ) throws CommandException;

    public boolean isConnected( Host host );

    public Peer getPeer( UUID peerId ) throws PeerException;

    //    public void updateHeartbeat();
}
