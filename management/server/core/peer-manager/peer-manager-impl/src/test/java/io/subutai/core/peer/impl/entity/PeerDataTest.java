package io.subutai.core.peer.impl.entity;


import org.junit.Test;

import io.subutai.core.peer.impl.entity.PeerData;

import static junit.framework.TestCase.assertEquals;


public class PeerDataTest
{
    private static final String ID = "id";
    private static final String SOURCE = "source";
    private static final String INFO = "info";

    PeerData peerData = new PeerData(ID, INFO);


    @Test
    public void testSetNGetId() throws Exception
    {
        peerData.setId( ID );

        assertEquals( ID, peerData.getId() );
    }


//    @Test
//    public void testSetNGetSource() throws Exception
//    {
//        peerData.setSource( SOURCE );
//
//        assertEquals( SOURCE, peerData.getSource() );
//    }


    @Test
    public void testSetNGetInfo() throws Exception
    {
        peerData.setInfo( INFO );

        assertEquals( INFO, peerData.getInfo() );
    }
}
