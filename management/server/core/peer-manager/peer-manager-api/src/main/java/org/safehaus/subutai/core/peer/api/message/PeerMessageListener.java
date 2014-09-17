package org.safehaus.subutai.core.peer.api.message;


/**
 * Interested modules should implement this interface to receive messages
 */
public interface PeerMessageListener {

    /**
     * This handler is triggered when a remote message arrives
     *
     * @param message message sent by remote counterpart
     */
    public void onMessage( Object message );

    /**
     * This is a copy of recipient name returned by PeerMessageListener implementations
     *
     * @return name of listener to which messages are routed
     */
    public String getName();
}
