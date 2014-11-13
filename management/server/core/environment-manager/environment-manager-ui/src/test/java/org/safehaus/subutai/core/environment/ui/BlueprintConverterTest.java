package org.safehaus.subutai.core.environment.ui;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.protocol.NodeGroup;
import org.safehaus.subutai.common.protocol.PlacementStrategy;
import org.safehaus.subutai.common.util.UUIDUtil;
import org.safehaus.subutai.core.environment.api.NodeData;
import org.safehaus.subutai.core.environment.ui.topology.BlueprintConverter;

import static org.junit.Assert.assertEquals;


/**
 * Created by bahadyr on 11/5/14.
 */

@RunWith( MockitoJUnitRunner.class )
public class BlueprintConverterTest
{

    BlueprintConverter converter;


    @Before
    public void setUp() throws Exception
    {
        this.converter = new BlueprintConverter( environmentBlueprint() );
    }


    UUID peer1 = UUIDUtil.generateTimeBasedUUID();
    UUID peer2 = UUIDUtil.generateTimeBasedUUID();


    @Test
    public void shouldDistributeAllNodesToPeer() throws Exception
    {
        List<NodeData> list = converter.blueprintToPeer( peer1 );
        assertEquals( list.size(), 4 );
    }


    @Test
    public void shouldDistributeNodesToPeers() throws Exception
    {
        Map<String, UUID> map = new HashMap<>();
        map.put( "name1", peer1 );
        map.put( "name2", peer2 );
        List<NodeData> list = converter.nodeGroupToPeer( map );
        assertEquals( list.size(), 4 );
    }


    private EnvironmentBlueprint environmentBlueprint()
    {
        EnvironmentBlueprint blueprint = new EnvironmentBlueprint( "name", "domain.name", true, true );
        NodeGroup ng1 =
                nodeGroup( "name1", "template1", "domain.name", 2, new PlacementStrategy( "BEST_SERVER" ), true );
        NodeGroup ng2 =
                nodeGroup( "name2", "template2", "domain.name", 2, new PlacementStrategy( "BEST_SERVER" ), true );
        blueprint.addNodeGroup( ng1 );
        blueprint.addNodeGroup( ng2 );
        return blueprint;
    }


    private NodeGroup nodeGroup( String name, String template, String domainName, int numberOfNodes,
                                 PlacementStrategy ps, boolean sshKeys )
    {
        NodeGroup nodeGroup = new NodeGroup();
        nodeGroup.setName( name );
        nodeGroup.setDomainName( domainName );
        nodeGroup.setExchangeSshKeys( sshKeys );
        nodeGroup.setNumberOfNodes( numberOfNodes );
        nodeGroup.setTemplateName( template );
        nodeGroup.setPlacementStrategy( ps );
        nodeGroup.setLinkHosts( true );
        return nodeGroup;
    }
}
