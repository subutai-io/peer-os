package org.safehaus.subutai.core.peer.impl.message;


import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.peer.api.message.PeerMessage;

import com.google.common.base.Preconditions;


/**
 * Peer message which is essentially a wrapper around an underlying message object
 */
public class PeerMessageWrapper {

    private String serializedPayload;
    private String payloadType;


    public PeerMessageWrapper( PeerMessage peerMessage ) {
        Preconditions.checkNotNull( peerMessage, "Peer Message is null" );
        Preconditions.checkNotNull( peerMessage.getMessage(), "Message Payload is null" );

        Object payload = peerMessage.getMessage();
        this.serializedPayload = JsonUtil.toJson( payload );
        this.payloadType = payload.getClass().getTypeName();
    }


    public Object getMessage() throws ClassNotFoundException {
        return JsonUtil.fromJson( serializedPayload, Class.forName( payloadType ) );
    }
}
