package org.safehaus.subutai.core.peer.impl;


import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.Response;

import com.google.common.base.Strings;


/**
 * Peer utility class.
 */
public class PeerUtils
{
    public static Agent buildAgent( Response response )
    {
        //create agent from response
        return new Agent( response.getUuid(),
                Strings.isNullOrEmpty( response.getHostname() ) ? response.getUuid().toString() :
                response.getHostname(), response.getParentHostName(), response.getMacAddress(), response.getIps(),
                !Strings.isNullOrEmpty( response.getParentHostName() ),
                //TODO pass proper site & environment ids
                response.getTransportId() );
    }
}
