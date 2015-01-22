package org.safehaus.subutai.common.peer;


import java.io.Serializable;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.command.CommandCallback;
import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.exception.SubutaiException;
import org.safehaus.subutai.common.host.HostArchitecture;
import org.safehaus.subutai.common.host.HostInfo;
import org.safehaus.subutai.common.host.Interface;


/**
 * Base Host interface.
 */
public interface Host extends Serializable
{
    /**
     * Returns reference to parent peer
     *
     * @return returns Peer interface
     */
    public Peer getPeer();

    public void setPeer( Peer peer );


    public String getPeerId();

    public UUID getId();

    public String getHostId();

    public String getHostname();

    public void addListener( HostEventListener hostEventListener );

    public void removeListener( HostEventListener hostEventListener );

    public void fireEvent( HostEvent hostEvent );

    public CommandResult execute( RequestBuilder requestBuilder ) throws CommandException;

    public CommandResult execute( RequestBuilder requestBuilder, CommandCallback callback ) throws CommandException;

    public void executeAsync( RequestBuilder requestBuilder, CommandCallback callback ) throws CommandException;

    public void executeAsync( RequestBuilder requestBuilder ) throws CommandException;

    void updateHostInfo( HostInfo hostInfo );

    public boolean isConnected();

    public long getLastHeartbeat();

    public String getIpByMask( String mask );

    void addIpHostToEtcHosts( String domainName, Set<Host> others, String mask ) throws SubutaiException;

    void init();

    public Set<Interface> getNetInterfaces();

    public String getIpByInterfaceName( String interfaceName );

    public String getMacByInterfaceName( String interfaceName );

    public HostArchitecture getHostArchitecture();
}
