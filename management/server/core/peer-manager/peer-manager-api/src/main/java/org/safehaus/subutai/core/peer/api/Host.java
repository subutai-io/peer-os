package org.safehaus.subutai.core.peer.api;


import java.io.Serializable;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.command.api.command.CommandException;
import org.safehaus.subutai.core.command.api.command.RequestBuilder;


/**
 * Base Host interface.
 */
public interface Host extends Serializable
{
    public Agent getAgent();

    public void setAgent( Agent agent );

    public Agent getParentAgent();

    public void setParentAgent( Agent agent );

    public UUID getPeerId();

    public UUID getId();

    public String getParentHostname();

    public String getHostname();

    public Command execute( RequestBuilder requestBuilder ) throws CommandException;

    public boolean isConnected() throws PeerException;

    public Peer getPeer( UUID peerId ) throws PeerException;

    public void updateHeartbeat();
}
