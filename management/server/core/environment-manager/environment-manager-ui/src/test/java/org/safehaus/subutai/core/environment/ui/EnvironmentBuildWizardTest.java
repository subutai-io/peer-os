package org.safehaus.subutai.core.environment.ui;


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.protocol.EnvironmentBuildTask;
import org.safehaus.subutai.common.protocol.NodeGroup;
import org.safehaus.subutai.common.protocol.PlacementStrategy;
import org.safehaus.subutai.core.environment.api.helper.EnvironmentBuildProcess;
import org.safehaus.subutai.core.environment.ui.manage.EnvironmentBuildWizard;
import org.safehaus.subutai.core.peer.api.Peer;
import org.safehaus.subutai.core.peer.api.PeerManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Created by bahadyr on 9/29/14.
 */
public class EnvironmentBuildWizardTest
{
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    EnvironmentBuildWizard sub;
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
        sub = new EnvironmentBuildWizard( "Wizard", module, task );
    }


    @Test
    public void testName() throws Exception
    {
        Map<String, Peer> topology = new HashMap<>();

        Peer peer1 = new Peer();
        peer1.setId( UUID.randomUUID() );

        for ( NodeGroup ng : getTask().getEnvironmentBlueprint().getNodeGroups() )
        {
            topology.put( ng.getTemplateName(), peer1 );
        }

//        EnvironmentBuildProcess process = sub.createBackgroundEnvironmentBuildProcess( getTask(), topology );
//        System.out.println( GSON.toJson( process ) );
    }


    private EnvironmentBuildTask getTask()
    {
        EnvironmentBuildTask task = new EnvironmentBuildTask();
        EnvironmentBlueprint eb = new EnvironmentBlueprint();
        eb.setName( "blueprint" );

        NodeGroup one = genNodeGroup( "hadoop", 3, "intra.lan", "name", true, true, PlacementStrategy.BEST_SERVER );
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
}
