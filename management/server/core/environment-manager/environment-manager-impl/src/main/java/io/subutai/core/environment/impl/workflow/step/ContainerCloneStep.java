package io.subutai.core.environment.impl.workflow.step;


import java.util.Map;
import java.util.Set;

import org.apache.commons.net.util.SubnetUtils;

import com.google.common.collect.Sets;

import io.subutai.common.environment.NodeGroup;
import io.subutai.common.environment.Topology;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.Peer;
import io.subutai.core.environment.api.exception.EnvironmentCreationException;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;


/**
 * Container creation step
 */
public class ContainerCloneStep
{

    public Set<ContainerHost> execute( Topology topology, EnvironmentImpl environment )
            throws EnvironmentCreationException
    {
        Set<ContainerHost> containerHosts = Sets.newHashSet();

        Map<Peer, Set<NodeGroup>> placement = topology.getNodeGroupPlacement();

        SubnetUtils cidr = new SubnetUtils( environment.getSubnetCidr() );

        //obtain available ip address count
        int totalAvailableIpCount = cidr.getInfo().getAddressCount() - 1;//one ip is for gateway

        //subtract already used ip range
        totalAvailableIpCount -= environment.getLastUsedIpIndex();

        //obtain used ip address count
        int requestedContainerCount = 0;
        for ( Set<NodeGroup> nodeGroups : placement.values() )
        {
            for ( NodeGroup nodeGroup : nodeGroups )
            {
                requestedContainerCount += nodeGroup.getNumberOfContainers();
            }
        }

        //check if available ip addresses are enough
        if ( requestedContainerCount > totalAvailableIpCount )
        {
            throw new EnvironmentCreationException(
                    String.format( "Requested %d containers but only %d ip addresses available",
                            requestedContainerCount, totalAvailableIpCount ) );
        }

        int currentLastUsedIpIndex = environment.getLastUsedIpIndex();


        //submit parallel environment part creation tasks across peers
        for ( Map.Entry<Peer, Set<NodeGroup>> peerPlacement : placement.entrySet() )
        {
            Peer peer = peerPlacement.getKey();

            //todo create new method peer.createEnvironmentContainers(envId, nodeGroup, ipIndex)
//            taskCompletionService.submit(
//                    new NodeGroupBuilder( environment, templateRegistry, peerManager, peer, peerPlacement.getValue(),
//                            allPeers, defaultDomain, currentLastUsedIpIndex + 1 ) );

            for ( NodeGroup nodeGroup : peerPlacement.getValue() )
            {
                currentLastUsedIpIndex += nodeGroup.getNumberOfContainers();
            }


            environment.setLastUsedIpIndex( currentLastUsedIpIndex );
        }


        return containerHosts;
    }
}
