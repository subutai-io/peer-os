package org.safehaus.subutai.core.peer.api;


import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.PeerCommandMessage;
import org.safehaus.subutai.core.container.api.ContainerCreateException;
import org.safehaus.subutai.core.peer.api.message.PeerMessageException;
import org.safehaus.subutai.core.peer.api.message.PeerMessageListener;


/**
 * Created by bahadyr on 8/28/14.
 */
public interface PeerManager
{

    @Deprecated
    boolean register( PeerInfo peerInfo );

    @Deprecated
    boolean update( PeerInfo peerInfo );

    @Deprecated
    /**
     * Please use getId() of Peer interface
     */
    public UUID getPeerId();

    @Deprecated
    public List<PeerInfo> peers();

    public PeerInfo getLocalPeerInfo();

    public PeerInfo getPeerInfo( UUID uuid );

    @Deprecated
    boolean unregister( String uuid );


    //    public String getRemoteId( String baseUrl );

    public void addPeerMessageListener( PeerMessageListener listener );

    public void removePeerMessageListener( PeerMessageListener listener );

    public String sendPeerMessage( PeerInfo peerInfo, String recipient, String message )
            throws PeerMessageException;

    public String processPeerMessage( String peerId, String recipient, String message ) throws PeerMessageException;

    @Deprecated
    /**
     * Please use method isOnline of RemotePeer interface
     */
    public boolean isPeerReachable( PeerInfo peerInfo ) throws PeerException;

    @Deprecated
    /**
     * Please use Peer interface
     */
    public Set<Agent> getConnectedAgents( String environmentId ) throws PeerException;

    @Deprecated
    /**
     * Please use Peer interface
     */
    public Set<Agent> getConnectedAgents( PeerInfo peerInfo, String environmentId ) throws PeerException;

    @Deprecated
    /**
     * Please use Peer interface
     */
    public Set<Agent> createContainers( UUID envId, UUID peerId, String template, int numberOfNodes, String strategy )
            throws ContainerCreateException;

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

    @Deprecated
    /**
     * Please use ContainerHost class
     */

    public void addContainer( PeerContainer peerContainer );

    @Deprecated
    /**
     * Please use Peer interface
     */
    public void invoke( PeerCommandMessage peerCommandMessage );

    List<PeerGroup> peersGroups();

    void deletePeerGroup( PeerGroup group );

    boolean savePeerGroup( PeerGroup group );

    public Peer getPeer( UUID peerId );

    public List<Peer> getPeers();


    //    public Set<ContainerHost> createContainers( UUID envId, String templateName, int quantity, String strategyId,
    //                                                List<Criteria> criteria ) throws ContainerCreateException;

    //    @Deprecated
    //    public boolean isConnected( Host host );

    //    public ManagementHost getManagementHost();

    public LocalPeer getLocalPeer();

    PeerGroup getPeerGroup( UUID peerGroupId );
}
