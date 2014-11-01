package org.safehaus.subutai.core.peer.api;


import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.container.api.ContainerCreateException;


public interface PeerManager
{

    @Deprecated
    boolean register( PeerInfo peerInfo );

    @Deprecated
    boolean update( PeerInfo peerInfo );


    @Deprecated
    public List<PeerInfo> peers();

    public PeerInfo getLocalPeerInfo();

    public PeerInfo getPeerInfo( UUID uuid );

    @Deprecated
    boolean unregister( String uuid );


    @Deprecated
    /**
     * Please use Peer interface
     */
    public boolean startContainer( PeerContainer container );

    @Deprecated
    /**
     * Please use Peer interface
     */
    public boolean stopContainer( PeerContainer container );

    @Deprecated
    public boolean isContainerConnected( PeerContainer container );

    @Deprecated
    /**
     * Please use ContainerHost class
     */

    public Set<PeerContainer> getContainers();


    List<PeerGroup> peersGroups();

    void deletePeerGroup( PeerGroup group );

    boolean savePeerGroup( PeerGroup group );

    public Peer getPeer( UUID peerId );

    public List<Peer> getPeers();


    public LocalPeer getLocalPeer();

    PeerGroup getPeerGroup( UUID peerGroupId );
}
