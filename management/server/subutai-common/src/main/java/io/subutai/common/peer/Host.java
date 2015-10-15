package io.subutai.common.peer;


import java.io.Serializable;

import io.subutai.common.command.CommandCallback;
import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.host.HostInfo;
import io.subutai.common.host.HostInterface;
import io.subutai.common.peer.Peer;


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

    public String getHostname();

    public CommandResult execute( RequestBuilder requestBuilder ) throws CommandException;

    public CommandResult execute( RequestBuilder requestBuilder, CommandCallback callback ) throws CommandException;

    public void executeAsync( RequestBuilder requestBuilder, CommandCallback callback ) throws CommandException;

    public void executeAsync( RequestBuilder requestBuilder ) throws CommandException;

    public boolean isConnected();


    /**
     * @deprecated use {@link #getInterfaceByName(String)} (String interfaceName)} instead.
     */
    @Deprecated
    public String getIpByInterfaceName( String interfaceName );

    /**
     * @deprecated use {@link #getInterfaceByName(String)} (String interfaceName)} instead.
     */
    @Deprecated
    public String getMacByInterfaceName( String interfaceName );

    HostInterface getInterfaceByName( String interfaceName );
}
