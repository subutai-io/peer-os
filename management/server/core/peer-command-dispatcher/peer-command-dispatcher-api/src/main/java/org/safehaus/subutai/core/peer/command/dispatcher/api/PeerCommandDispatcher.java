package org.safehaus.subutai.core.peer.command.dispatcher.api;


import org.safehaus.subutai.core.peer.api.helpers.PeerCommand;


/**
 * This class allows to send commands to local and remote agents.
 */
public interface PeerCommandDispatcher {

    public boolean invoke(PeerCommand peerCommand) throws PeerCommandException;
}
