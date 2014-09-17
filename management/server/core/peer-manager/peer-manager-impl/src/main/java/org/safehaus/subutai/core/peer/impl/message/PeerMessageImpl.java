package org.safehaus.subutai.core.peer.impl.message;


import java.util.logging.Logger;

import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.peer.api.message.PeerMessage;


/**
 * Handy peer message implementation
 */
public class PeerMessageImpl implements PeerMessage {
    private final static Logger LOG = Logger.getLogger( PeerMessageImpl.class.getName() );

    private String message;
    private String messageType;


    public PeerMessageImpl( final Object message ) {
        if ( message != null ) {
            this.message = JsonUtil.toJson( message );
            messageType = message.getClass().getTypeName();
        }
    }


    @Override
    public Object getMessage() {
        if ( message != null ) {
            try {
                return JsonUtil.fromJson( message, Class.forName( messageType ) );
            }
            catch ( ClassNotFoundException e ) {
                LOG.severe( String.format( "Error deserializing message: %s", e.getMessage() ) );
            }
        }
        return null;
    }
}
