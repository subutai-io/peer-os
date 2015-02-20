package org.safehaus.subutai.core.peer.api;


import java.io.IOException;
import java.util.Set;

import org.safehaus.subutai.common.peer.Host;
import org.safehaus.subutai.common.peer.PeerException;


/**
 * Management host interface.
 */
public interface ManagementHost extends Host
{

    public void addAptSource( final String host, final String ip ) throws PeerException;

    public void removeAptSource( final String host, final String ip ) throws PeerException;

    public String readFile( String path ) throws IOException;

    public void setupTunnels( Set<String> peerIps, long vni, boolean newVni ) throws PeerException;

    public Set<Long> getTakenVniIds() throws PeerException;
}
