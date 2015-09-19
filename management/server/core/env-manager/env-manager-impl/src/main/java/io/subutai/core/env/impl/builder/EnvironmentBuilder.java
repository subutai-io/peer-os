package io.subutai.core.env.impl.builder;


import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.environment.NodeGroup;
import io.subutai.common.environment.Topology;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.util.CollectionUtil;
import io.subutai.common.util.ExceptionUtil;
import io.subutai.core.env.impl.entity.EnvironmentImpl;
import io.subutai.core.env.impl.exception.EnvironmentBuildException;
import io.subutai.core.peer.api.LocalPeer;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.registry.api.TemplateRegistry;

import org.apache.commons.net.util.SubnetUtils;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;


/**
 * Builds node groups across peers
 */
public class EnvironmentBuilder
{
    private static final Logger LOGGER = LoggerFactory.getLogger( EnvironmentBuilder.class );
    private final TemplateRegistry templateRegistry;
    private final PeerManager peerManager;
    private final String defaultDomain;
    protected ExceptionUtil exceptionUtil = new ExceptionUtil();


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


        //collect all existing and new peers
        Set<Peer> allPeers = Sets.newHashSet( placement.keySet() );

        for ( Peer aPeer : environment.getPeers() )
        {
            allPeers.add( aPeer );
        }

        //setup tunnels to all participating peers on local peer in case local peer is not included as provider peer
        LocalPeer localPeer = peerManager.getLocalPeer();

        Map<String, String> peerIps = new HashMap();

        for ( Peer peer : allPeers )
        {
            if ( !peer.getId().equals( localPeer.getId() ) )
            {
                String n2nIp = environment.findN2nIp( peer.getId().toString() );
                peerIps.put( peer.getPeerInfo().getIp(), n2nIp );
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

        ExecutorService taskExecutor = getExecutor( placement.size() );

        CompletionService<Set<NodeGroupBuildResult>> taskCompletionService = getCompletionService( taskExecutor );

        //submit parallel environment part creation tasks across peers
        for ( Map.Entry<Peer, Set<NodeGroup>> peerPlacement : placement.entrySet() )
        {
            Peer peer = peerPlacement.getKey();

            taskCompletionService.submit(
                    new NodeGroupBuilder( environment, templateRegistry, peerManager, peer,
                            peerPlacement.getValue(), allPeers, defaultDomain, currentLastUsedIpIndex + 1 ) );

            for ( NodeGroup nodeGroup : peerPlacement.getValue() )
            {
                currentLastUsedIpIndex += nodeGroup.getNumberOfContainers();
            }


            environment.setLastUsedIpIndex( currentLastUsedIpIndex );
        }

        //collect results
        Set<String> errors = Sets.newHashSet();

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
                        errors.add( exceptionUtil.getRootCauseMessage( result.getException() ) );
                    }
                }
            }
            catch ( ExecutionException | InterruptedException e )
            {
                errors.add( exceptionUtil.getRootCauseMessage( e ) );
            }
        }

        taskExecutor.shutdown();

        if ( !errors.isEmpty() )
        {

            throw new EnvironmentBuildException(
                    String.format( "There were errors during container creation:  %s", errors ), null );
        }
    }


    protected ExecutorService getExecutor( int numOfThreads )
    {
        return Executors.newFixedThreadPool( numOfThreads );
    }


    protected CompletionService<Set<NodeGroupBuildResult>> getCompletionService( Executor executor )
    {
        return new ExecutorCompletionService<>( executor );
    }
}
