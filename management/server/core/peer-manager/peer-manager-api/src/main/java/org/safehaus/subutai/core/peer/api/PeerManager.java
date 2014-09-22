package org.safehaus.subutai.core.peer.api;


import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.container.api.ContainerManager;
import org.safehaus.subutai.core.peer.api.helpers.CloneContainersMessage;
import org.safehaus.subutai.core.peer.api.helpers.PeerCommand;
import org.safehaus.subutai.core.peer.api.message.PeerMessageException;
import org.safehaus.subutai.core.peer.api.message.PeerMessageListener;


/**
 * Created by bahadyr on 8/28/14.
 */
public interface PeerManager {

    boolean register( Peer peer );

    public UUID getSiteId();

    public List<Peer> peers();

    boolean unregister( String uuid );

    public Peer getPeerByUUID( UUID uuid );

    //    public String getRemoteId( String baseUrl );

    public void addPeerMessageListener( PeerMessageListener listener );

    public void removePeerMessageListener( PeerMessageListener listener );

    public String sendPeerMessage( Peer peer, String recipient, String message ) throws PeerMessageException;

    public String processPeerMessage( String peerId, String recipient, String message ) throws PeerMessageException;

    public boolean isPeerReachable( Peer peer ) throws PeerException;

    public Set<Agent> getConnectedAgents( String environmentId ) throws PeerException;

    public Set<Agent> getConnectedAgents( Peer peer, String environmentId ) throws PeerException;

    public Set<Agent> createContainers( CloneContainersMessage ccm );

    public boolean startContainer(PeerContainer container);

    public boolean stopContainer(PeerContainer container);

    public boolean isContainerConnected(PeerContainer container);

    public Set<PeerContainer> getContainers();

    public void addContainer(PeerContainer peerContainer);

    public boolean invoke( PeerCommand peerCommand ) throws PeerException;
}
