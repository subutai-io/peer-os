package io.subutai.common.peer;


import java.io.Serializable;

import io.subutai.common.command.CommandCallback;
import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.host.HostInfo;


/**
 * Base Host interface.
 */
public interface Host extends HostInfo, Serializable
{


    /**
     * Returns reference to parent peer
     *
     * @return returns Peer interface
     */
    public Peer getPeer();

    public String getPeerId();

    @Deprecated
    public String getHostId();

    public String getHostname();

    public CommandResult execute( RequestBuilder requestBuilder ) throws CommandException;

    public CommandResult execute( RequestBuilder requestBuilder, CommandCallback callback ) throws CommandException;

    public void executeAsync( RequestBuilder requestBuilder, CommandCallback callback ) throws CommandException;

    public void executeAsync( RequestBuilder requestBuilder ) throws CommandException;

    public boolean isConnected();

    public long getLastHeartbeat();


    public String getIpByInterfaceName( String interfaceName );

    public String getMacByInterfaceName( String interfaceName );
}
