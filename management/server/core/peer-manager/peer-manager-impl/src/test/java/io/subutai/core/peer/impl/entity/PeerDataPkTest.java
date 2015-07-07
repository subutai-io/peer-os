package io.subutai.core.peer.impl.entity;


import org.junit.Test;

import io.subutai.core.peer.impl.entity.PeerDataPk;

import static junit.framework.Assert.assertFalse;
import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;


public class PeerDataPkTest
{
    private static final String ID = "id";
    private static final String ID2 = "id2";
    private static final String SOURCE = "source";
    private static final String SOURCE2 = "source2";

    PeerDataPk peerDataPk = new PeerDataPk();


    @Test
    public void testSetNGetId() throws Exception
    {
        peerDataPk.setId( ID );

        assertEquals( ID, peerDataPk.getId() );
    }


    @Test
    public void testSetNGetSource() throws Exception
    {
        peerDataPk.setSource( SOURCE );

        assertEquals( SOURCE, peerDataPk.getSource() );
    }


    @Test
    public void testHascode() throws Exception
    {

        PeerDataPk peerDataPk2 = new PeerDataPk();
        peerDataPk2.setId( ID );
        peerDataPk2.setSource( SOURCE );
        peerDataPk.setId( ID );
        peerDataPk.setSource( SOURCE );

        assertEquals( peerDataPk2.hashCode(), peerDataPk.hashCode() );
    }


    @Test
    public void testEquals() throws Exception
    {

        PeerDataPk peerDataPk2 = new PeerDataPk();
        peerDataPk2.setId( ID );
        peerDataPk2.setSource( SOURCE );
        peerDataPk.setId( ID );
        peerDataPk.setSource( SOURCE );

        assertEquals( peerDataPk2, peerDataPk );

        PeerDataPk peerDataPk3 = new PeerDataPk();
        peerDataPk3.setId( ID2 );
        peerDataPk3.setSource( SOURCE );

        assertThat( peerDataPk3, not( peerDataPk ) );

        PeerDataPk peerDataPk4 = new PeerDataPk();
        peerDataPk4.setId( ID );
        peerDataPk4.setSource( SOURCE2 );

        assertThat( peerDataPk4, not( peerDataPk ) );

        assertFalse( peerDataPk.equals( new Object() ) );
    }
}
