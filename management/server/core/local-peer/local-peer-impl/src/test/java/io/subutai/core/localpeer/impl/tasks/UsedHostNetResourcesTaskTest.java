package io.subutai.core.localpeer.impl.tasks;


import org.junit.Test;

import io.subutai.common.settings.Common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class UsedHostNetResourcesTaskTest
{

    @Test
    public void testRegex() throws Exception
    {
        assertTrue( "p2p1".matches( Common.P2P_INTERFACE_NAME_REGEX ) );

        assertEquals( 1, Integer.parseInt( "p2p1".replace( Common.P2P_INTERFACE_PREFIX, "" ) ) );

        assertEquals( "p2p100", String.format( "%s%d", Common.P2P_INTERFACE_PREFIX, 100 ) );
    }
}
