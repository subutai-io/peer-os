package io.subutai.core.peer.impl.entity;


import org.junit.Test;

import static junit.framework.TestCase.assertEquals;


public class PeerDataTest
{
    private static final String ID = "id";
    private static final String SOURCE = "source";
    private static final String INFO = "info";

    PeerData peerData = new PeerData( ID, INFO, "", "{}", 1 );


    @Test
    public void testSetNGetInfo() throws Exception
    {
        peerData.setInfo( INFO );

        assertEquals( INFO, peerData.getInfo() );
    }
}
