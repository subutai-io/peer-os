package org.safehaus.subutai.core.peer.api;


import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.network.Gateway;
import org.safehaus.subutai.common.network.Vni;
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

    public int setupTunnels( Set<String> peerIps, UUID environmentId ) throws PeerException;

    public Set<Vni> getReservedVnis() throws PeerException;

    public int reserveVni( Vni vni ) throws PeerException;

    public Set<Gateway> getGateways() throws PeerException;

    public void createGateway( String gatewayIp, int vlan ) throws PeerException;

    public void removeGateway( int vlan ) throws PeerException;

    public void cleanupEnvironmentNetworkSettings( final UUID environmentId ) throws PeerException;

    public void removeTunnel( int tunnelId ) throws PeerException;
}
