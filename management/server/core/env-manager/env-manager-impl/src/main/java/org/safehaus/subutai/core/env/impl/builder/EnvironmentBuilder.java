package org.safehaus.subutai.core.env.impl.builder;


import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.safehaus.subutai.common.environment.NodeGroup;
import org.safehaus.subutai.common.environment.Topology;
import org.safehaus.subutai.common.peer.Peer;
import org.safehaus.subutai.common.peer.PeerException;
import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.core.env.impl.entity.EnvironmentImpl;
import org.safehaus.subutai.core.env.impl.exception.EnvironmentBuildException;
import org.safehaus.subutai.core.peer.api.LocalPeer;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;

import org.apache.commons.net.util.SubnetUtils;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;


/**
 * Builds node groups across peers
 */
public class EnvironmentBuilder
{

    private final TemplateRegistry templateRegistry;
    private final PeerManager peerManager;
    private final String defaultDomain;


    public EnvironmentBuilder( final TemplateRegistry templateRegistry, final PeerManager peerManager,
                               final String defaultDomain )
    {
        Preconditions.checkNotNull( templateRegistry );
        Preconditions.checkNotNull( peerManager );
        Preconditions.checkNotNull( !Strings.isNullOrEmpty( defaultDomain ) );

        this.templateRegistry = templateRegistry;
        this.peerManager = peerManager;
        this.defaultDomain = defaultDomain;
    }


    public void build( EnvironmentImpl environment, Topology topology ) throws EnvironmentBuildException
    {
        Preconditions.checkNotNull( environment );
        Preconditions.checkNotNull( topology );

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
            throw new EnvironmentBuildException(
                    String.format( "Requested %d containers but only %d ip addresses available",
                            requestedContainerCount, totalAvailableIpCount ), null );
        }


        ExecutorService taskExecutor = Executors.newFixedThreadPool( placement.size() );

        CompletionService<Set<NodeGroupBuildResult>> taskCompletionService =
                new ExecutorCompletionService<>( taskExecutor );


        //collect all existing and new peers
        Set<Peer> allPeers = Sets.newHashSet( placement.keySet() );

        for ( Peer aPeer : environment.getPeers() )
        {
            allPeers.add( aPeer );
        }

        //setup tunnels to all participating peers on local peer in case local peer is not included as provider peer
        LocalPeer localPeer = peerManager.getLocalPeer();

        Set<String> peerIps = Sets.newHashSet();

        for ( Peer peer : allPeers )
        {
            if ( !peer.getId().equals( localPeer.getId() ) )
            {
                peerIps.add( peer.getPeerInfo().getIp() );
            }
        }

        //setup tunnels to all remote peers
        if ( !peerIps.isEmpty() )
        {
            try
            {
                localPeer.setupTunnels( peerIps, environment.getId() );
            }
            catch ( PeerException e )
            {
                throw new EnvironmentBuildException( "Error setting up tunnels to remote peers", e );
            }
        }

        int currentLastUsedIpIndex = environment.getLastUsedIpIndex();
        //submit parallel environment part creation tasks across peers
        for ( Map.Entry<Peer, Set<NodeGroup>> peerPlacement : placement.entrySet() )
        {
            taskCompletionService.submit(
                    new NodeGroupBuilder( environment, templateRegistry, peerManager, peerPlacement.getKey(),
                            peerPlacement.getValue(), allPeers, defaultDomain, currentLastUsedIpIndex + 1 ) );

            for ( NodeGroup nodeGroup : peerPlacement.getValue() )
            {
                currentLastUsedIpIndex += nodeGroup.getNumberOfContainers();
            }

            environment.setLastUsedIpIndex( currentLastUsedIpIndex );
        }

        //collect results
        Set<Exception> errors = Sets.newHashSet();

        for ( int i = 0; i < placement.size(); i++ )
        {
            try
            {
                Future<Set<NodeGroupBuildResult>> futures = taskCompletionService.take();
                Set<NodeGroupBuildResult> results = futures.get();
                for ( NodeGroupBuildResult result : results )
                {
                    if ( !CollectionUtil.isCollectionEmpty( result.getContainers() ) )
                    {
                        environment.addContainers( result.getContainers() );
                    }

                    if ( result.getException() != null )
                    {
                        errors.add( result.getException() );
                    }
                }
            }
            catch ( ExecutionException | InterruptedException e )
            {
                errors.add( e );
            }
        }

        taskExecutor.shutdown();

        if ( !errors.isEmpty() )
        {
            throw new EnvironmentBuildException(
                    String.format( "There were errors during container creation:  %s", errors ), null );
        }
    }
}
