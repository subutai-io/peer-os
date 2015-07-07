package io.subutai.common.peer;


import org.junit.Test;

import io.subutai.common.peer.PeerStatus;

import static org.junit.Assert.assertNotNull;


public class PeerStatusTest
{
    private PeerStatus peerStatus;


    @Test
    public void testSetRegistered() throws Exception
    {
        PeerStatus registered = PeerStatus.REQUESTED;
        assertNotNull( registered.setRegistered() );
    }


    @Test
    public void testSetRejected() throws Exception
    {
        PeerStatus rejected = PeerStatus.REQUESTED;
        rejected.setRejected();
    }


    @Test
    public void testSetBlocked() throws Exception
    {
        PeerStatus rejected = PeerStatus.REGISTERED;
        rejected.setBlocked();
    }


    @Test
    public void testSetUnblock() throws Exception
    {
        PeerStatus rejected = PeerStatus.BLOCKED;
        rejected.setUnblock();
    }
}