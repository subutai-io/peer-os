package org.safehaus.subutai.core.environment.ui;


import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.peer.Peer;
import org.safehaus.subutai.core.environment.ui.wizard.Node2PeerWizard;
import org.safehaus.subutai.core.peer.api.PeerManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import static org.mockito.Mockito.when;


/**
 * Created by bahadyr on 9/29/14.
 */
@RunWith(MockitoJUnitRunner.class)
public class EnvironmentContainerNode2PeerWizardTest
{
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private Node2PeerWizard sut;
    @Mock
    private EnvironmentManagerPortalModule module;
    @Mock
    private PeerManager peerManager;


    @Before
    public void setUp() throws Exception
    {

        when( module.getPeerManager() ).thenReturn( peerManager );
        when( peerManager.getPeers() ).thenReturn( Collections.<Peer>emptyList() );
    }


    @Test
    public void testName() throws Exception
    {


    }
}
