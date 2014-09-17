package org.safehaus.subutai.core.peer.api.message;


/**
 * Peer message
 */
public interface PeerMessage {

    public String getRecipientName();

    public Object getMessage();
}
