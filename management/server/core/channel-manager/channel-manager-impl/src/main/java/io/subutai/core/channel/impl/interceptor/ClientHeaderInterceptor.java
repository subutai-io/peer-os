package io.subutai.core.channel.impl.interceptor;


import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

import io.subutai.common.settings.Common;
import io.subutai.core.channel.impl.util.InterceptorState;
import io.subutai.core.peer.api.PeerManager;


/**
 *
 */
public class ClientHeaderInterceptor extends AbstractPhaseInterceptor<Message>
{
    private final PeerManager peerManager;


    //******************************************************************
    public ClientHeaderInterceptor( PeerManager peerManager )
    {
        super( Phase.POST_LOGICAL );
        this.peerManager = peerManager;
    }
    //******************************************************************


    @Override
    public void handleMessage( final Message message )
    {
        try
        {
            if ( InterceptorState.CLIENT_OUT.isActive( message ) )
            {
                Map<String, List> headers = ( Map<String, List> ) message.get( Message.PROTOCOL_HEADERS );
                try
                {
                    headers.put( Common.SUBUTAI_HTTP_HEADER,
                            Collections.singletonList( peerManager.getLocalPeer().getId() ) );
                }
                catch ( Exception ce )
                {
                    throw new Fault( ce );
                }
            }
        }
        catch ( Exception ex )
        {
            throw new Fault( ex );
        }
    }
}
