package org.safehaus.subutai.core.peer.cli;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.core.peer.api.LocalPeer;
import org.safehaus.subutai.core.peer.api.PeerManager;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class TagCommandTest
{
    private static final String TAG = "tag";
    @Mock
    PeerManager peerManager;
    @Mock
    LocalPeer localPeer;
    @Mock
    ContainerHost containerHost;

    TagCommand command;


    @Before
    public void setUp() throws Exception
    {
        command = new TagCommand();
        command.setPeerManager( peerManager );
        when( peerManager.getLocalPeer() ).thenReturn( localPeer );
        when( localPeer.getContainerHostByName( anyString() ) ).thenReturn( containerHost );
        command.tag = TAG;
    }


    @Test
    public void testDoExecute() throws Exception
    {
        command.doExecute();

        verify( containerHost ).addTag( TAG );
    }
}
