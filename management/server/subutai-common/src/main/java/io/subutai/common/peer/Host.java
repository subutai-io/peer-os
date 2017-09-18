package io.subutai.common.peer;


import io.subutai.common.command.CommandCallback;
import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.host.HostInfo;
import io.subutai.common.host.HostInterface;
import io.subutai.common.security.relation.RelationLink;


/**
 * Base Host interface.
 */
public interface Host extends HostInfo, RelationLink
{
    /**
     * Returns reference to parent peer
     *
     * @return returns Peer interface
     */
    Peer getPeer();

    String getPeerId();

    CommandResult execute( RequestBuilder requestBuilder ) throws CommandException;

    CommandResult execute( RequestBuilder requestBuilder, CommandCallback callback ) throws CommandException;

    void executeAsync( RequestBuilder requestBuilder, CommandCallback callback ) throws CommandException;

    void executeAsync( RequestBuilder requestBuilder ) throws CommandException;

    boolean isConnected();
}
