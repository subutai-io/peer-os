package org.safehaus.subutai.core.peer.api;


/**
 * Management host interface.
 */
public interface ManagementHost extends Host
{

    public void init() throws SubutaiInitException;

    public void addAptSource( final String host, final String ip ) throws PeerException;

    public void removeAptSource( final String host, final String ip ) throws PeerException;

//    void resetHeartbeat();
    //
    //
    //    void updateHeartbeat();
}
