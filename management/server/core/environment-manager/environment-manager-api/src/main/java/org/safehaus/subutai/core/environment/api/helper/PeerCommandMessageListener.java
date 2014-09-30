package org.safehaus.subutai.core.environment.api.helper;


import org.safehaus.subutai.common.protocol.PeerCommandMessage;


/**
 * Created by timur on 9/26/14.
 */
public interface PeerCommandMessageListener
{
    public Environment getEnvironment();
    public void onCommandMessage( PeerCommandMessage commandMessage );
}
