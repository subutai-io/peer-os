package org.safehaus.subutai.core.environment.ui;


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.protocol.EnvironmentBuildTask;
import org.safehaus.subutai.common.protocol.NodeGroup;
import org.safehaus.subutai.common.protocol.PlacementStrategy;
import org.safehaus.subutai.common.util.UUIDUtil;
import org.safehaus.subutai.core.environment.api.helper.EnvironmentBuildProcess;
import org.safehaus.subutai.core.environment.ui.manage.EnvironmentBuildWizard;
import org.safehaus.subutai.core.peer.api.Peer;
import org.safehaus.subutai.core.peer.api.PeerManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Created by bahadyr on 9/29/14.
 */
public class EnvironmentBuildWizardTest
{
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private EnvironmentBuildWizard sut;
    private EnvironmentManagerPortalModule module;
    private PeerManager peerManager;


    @Before
    public void setUp() throws Exception
    {
        module = mock( EnvironmentManagerPortalModule.class );
        peerManager = mock( PeerManager.class );
        EnvironmentBuildTask task = getTask();
        when( module.getPeerManager() ).thenReturn( peerManager );
        when( peerManager.peers() ).thenReturn( Collections.<Peer>emptyList() );
        sut = new EnvironmentBuildWizard( "Wizard", module, task );
    }


    private EnvironmentBuildTask getTask()
    {
        EnvironmentBuildTask task = new EnvironmentBuildTask();
        EnvironmentBlueprint eb = new EnvironmentBlueprint();
        eb.setName( "blueprint" );

        NodeGroup one = genNodeGroup( "hadoop", 5, "intra.lan", "name", true, true, PlacementStrategy.BEST_SERVER );
        NodeGroup two = genNodeGroup( "cassandra", 2, "intra.lan", "name", true, true, PlacementStrategy.BEST_SERVER );
        eb.addNodeGroup( one );
        eb.addNodeGroup( two );

        task.setEnvironmentBlueprint( eb );
        return task;
    }


    private NodeGroup genNodeGroup( String templateName, int non, String domainName, String name, boolean ek,
                                    boolean lh, PlacementStrategy ps )
    {
        NodeGroup ng = new NodeGroup();
        ng.setTemplateName( templateName );
        ng.setNumberOfNodes( non );
        ng.setDomainName( domainName );
        ng.setName( name );
        ng.setExchangeSshKeys( ek );
        ng.setLinkHosts( lh );
        ng.setPlacementStrategy( ps );
        return ng;
    }


    @Test
    public void shouldCreateBuildProcess() throws Exception
    {

        Peer peer1 = new Peer();
        peer1.setId( UUIDUtil.generateTimeBasedUUID() );

        Peer peer2 = new Peer();
        peer2.setId( UUIDUtil.generateTimeBasedUUID() );
        Peer[] peers = { peer1, peer2 };

        Map<Object, Peer> topology = new HashMap<>();

        EnvironmentBuildTask ebp = getTask();
        Map<Object, NodeGroup> map = new HashMap<>();

        int itemId = 0;
        for ( NodeGroup ng : ebp.getEnvironmentBlueprint().getNodeGroups() )
        {
            for ( int i = 0; i < ng.getNumberOfNodes(); i++ )
            {
                map.put( itemId, ng );
                topology.put( itemId++, peers[getRandom()] );
            }
        }

        sut.setNodeGroupMap( map );
        EnvironmentBuildProcess process = sut.createEnvironmentBuildProcess( ebp, topology );
        assertNotNull(process);
        System.out.println( GSON.toJson( process ) );
    }


    private int getRandom()
    {
        return ( int ) Math.floor( Math.random() * 2 );
    }
}
