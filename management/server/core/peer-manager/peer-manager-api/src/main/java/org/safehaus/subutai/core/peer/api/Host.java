package org.safehaus.subutai.core.peer.api;


import org.safehaus.subutai.common.command.CommandCallback;
import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.exception.SubutaiException;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.hostregistry.api.HostInfo;

import java.io.Serializable;
import java.util.Set;
import java.util.UUID;


/**
 * Base Host interface.
 */
public interface Host extends Serializable
{
    public Agent getAgent();

    @Deprecated
    public Agent getParentAgent();

    @Deprecated
    public void setParentAgent( Agent agent );

    public String getPeerId();

    //    public void setPeerId( UUID peerId );

    public UUID getId();

    public String getHostId();

    public String getHostname();

    void addListener( HostEventListener hostEventListener );

    void removeListener( HostEventListener hostEventListener );

    void fireEvent( HostEvent hostEvent );

    public CommandResult execute( RequestBuilder requestBuilder ) throws CommandException;

    public CommandResult execute( RequestBuilder requestBuilder, CommandCallback callback ) throws CommandException;

    public void executeAsync( RequestBuilder requestBuilder, CommandCallback callback ) throws CommandException;

    public void executeAsync( RequestBuilder requestBuilder ) throws CommandException;

    void updateHostInfo( HostInfo hostInfo );

    public boolean isConnected();

    public long getLastHeartbeat();

    String getIpByMask( String mask );

    void addIpHostToEtcHosts( String domainName, Set<Host> others, String mask ) throws SubutaiException;

    void init();
}
