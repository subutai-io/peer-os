package org.safehaus.subutai.core.peer.command.dispatcher.api;


import org.safehaus.subutai.common.protocol.PeerCommandMessage;


/**
 * This class allows to send commands to local and remote agents.
 */
public interface PeerCommandDispatcher
{

    public void invoke( PeerCommandMessage peerCommand ) throws PeerCommandException;
}
