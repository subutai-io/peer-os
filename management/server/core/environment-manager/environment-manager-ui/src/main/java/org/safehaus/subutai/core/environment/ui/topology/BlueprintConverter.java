package org.safehaus.subutai.core.environment.ui.topology;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.protocol.NodeGroup;
import org.safehaus.subutai.core.environment.api.NodeData;


/**
 * Created by bahadyr on 11/5/14.
 */
public class BlueprintConverter
{

    EnvironmentBlueprint blueprint;


    public BlueprintConverter( EnvironmentBlueprint blueprint )
    {
        this.blueprint = blueprint;
    }


    public List<NodeData> blueprintToPeer( UUID targetPeerId )
    {
        List<NodeData> nodeDataList = new ArrayList<>();
        for ( NodeGroup nodeGroup : blueprint.getNodeGroups() )
        {
            for ( int i = 0; i < nodeGroup.getNumberOfNodes(); i++ )
            {
                NodeData nodeData = new NodeData( nodeGroup );
                nodeData.setTargetPeerId( targetPeerId );
                addEnvironmentData( nodeData );
                nodeDataList.add( nodeData );
            }
        }
        return nodeDataList;
    }


    public List<NodeData> nodeGroupToPeer( Map<String, UUID> map )
    {
        List<NodeData> nodeDataList = new ArrayList<>();

        for ( String nodeGroupName : map.keySet() )
        {
            NodeGroup nodeGroup = getNodeGroupByName( nodeGroupName );
            for ( int i = 0; i < nodeGroup.getNumberOfNodes(); i++ )
            {
                NodeData nodeData = new NodeData( nodeGroup );
                nodeData.setTargetPeerId( map.get( nodeGroupName ) );
                addEnvironmentData( nodeData );
                nodeDataList.add( nodeData );
            }
        }

        return nodeDataList;
    }


    public List<NodeData> nodeToPeer( Map<String, UUID> nodeGroupToPeer )
    {
        List<NodeData> nodeDataList = new ArrayList<>();

        for ( String nodeGroupName : nodeGroupToPeer.keySet() )
        {
            NodeGroup nodeGroup = getNodeGroupByName( nodeGroupName );
            for ( int i = 0; i < nodeGroup.getNumberOfNodes(); i++ )
            {
                NodeData nodeData = new NodeData( nodeGroup );
                nodeData.setTargetPeerId( nodeGroupToPeer.get( nodeGroupName ) );
                addEnvironmentData( nodeData );
                nodeDataList.add( nodeData );
            }
        }

        return nodeDataList;
    }






    private void addEnvironmentData( final NodeData nodeData )
    {
        nodeData.setEnvironmentDomainName( blueprint.getDomainName() );
        nodeData.setEnvironmentExchangeSshKeys( blueprint.isExchangeSshKeys() );
        nodeData.setEnvironmentLinkHosts( blueprint.isLinkHosts() );
        nodeData.setEnvironmentName( blueprint.getName() );
    }


    private NodeGroup getNodeGroupByName( String name )
    {
        for ( NodeGroup nodeGroup : blueprint.getNodeGroups() )
        {
            if ( name.equals( nodeGroup.getName() ) )
            {
                return nodeGroup;
            }
        }
        return null;
    }
}
