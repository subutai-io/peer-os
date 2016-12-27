package io.subutai.core.localpeer.impl;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.TestCase.assertEquals;


@RunWith( MockitoJUnitRunner.class )
public class LocalPeerCommandsTest
{

    LocalPeerCommands localPeerCommands;


    @Before
    public void setUp() throws Exception
    {
        localPeerCommands = new LocalPeerCommands();
    }


    @Test
    public void testGetChangeHostnameInEtcHostsCommand() throws Exception
    {
        assertEquals(
                "sed -i 's/\\bOLD\\b/NEW/g' /etc/hosts && sed -i 's/\\bOLD.intra.lan\\b/NEW.intra.lan/g' /etc/hosts",
                localPeerCommands.getChangeHostnameInEtcHostsCommand( "OLD", "NEW" ).build( "ID" ).getCommand() );
    }


    @Test
    public void testGetChangeHostnameInAuthorizedKeysCommand() throws Exception
    {
        assertEquals( "chmod 700 /root/.ssh/authorized_keys && sed -i 's/\\bOLD\\b/NEW/g' /root/.ssh/authorized_keys "
                        + "&& chmod 644 /root/.ssh/authorized_keys",
                localPeerCommands.getChangeHostnameInAuthorizedKeysCommand( "OLD", "NEW" ).build( "ID" ).getCommand() );
    }
}
