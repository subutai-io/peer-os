package org.safehaus.subutai.core.peer.api.message;


/**
 * Exception thrown by PeerManager.sendPeerMessage when sending message to peer
 */
public class PeerMessageException extends Exception {

    public PeerMessageException( final String message ) {
        super( message );
    }
}
