package org.safehaus.subutai.common.peer;


import java.io.Serializable;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.command.CommandCallback;
import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.host.HostArchitecture;
import org.safehaus.subutai.common.host.Interface;


/**
 * Base Host interface.
 */
public interface Host extends Serializable
{

    @Deprecated
    public void fireEvent( HostEvent hostEvent );

    /**
     * Returns reference to parent peer
     *
     * @return returns Peer interface
     */
    public Peer getPeer();

    public String getPeerId();

    public UUID getId();

    public String getHostId();

    public String getHostname();

    public CommandResult execute( RequestBuilder requestBuilder ) throws CommandException;

    public CommandResult execute( RequestBuilder requestBuilder, CommandCallback callback ) throws CommandException;

    public void executeAsync( RequestBuilder requestBuilder, CommandCallback callback ) throws CommandException;

    public void executeAsync( RequestBuilder requestBuilder ) throws CommandException;

    public boolean isConnected();

    public long getLastHeartbeat();

    void init();

    public Set<Interface> getNetInterfaces();

    public String getIpByInterfaceName( String interfaceName );

    public String getMacByInterfaceName( String interfaceName );

    public HostArchitecture getHostArchitecture();
}
