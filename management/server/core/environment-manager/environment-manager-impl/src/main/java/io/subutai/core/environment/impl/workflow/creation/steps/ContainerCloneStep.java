package io.subutai.core.environment.impl.workflow.creation.steps;


import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.net.util.SubnetUtils;

import com.google.common.collect.Lists;

import io.subutai.common.environment.CreateEnvironmentContainersResponse;
import io.subutai.common.environment.Node;
import io.subutai.common.environment.Topology;
import io.subutai.common.host.ContainerHostInfoModel;
import io.subutai.common.host.ContainerHostState;
import io.subutai.common.host.HostInterfaceModel;
import io.subutai.common.host.HostInterfaces;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.settings.Common;
import io.subutai.common.task.CloneResponse;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.common.util.PeerUtil;
import io.subutai.core.environment.api.exception.EnvironmentCreationException;
import io.subutai.core.environment.impl.entity.EnvironmentContainerImpl;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.core.environment.impl.workflow.creation.steps.helpers.CreatePeerEnvironmentContainersTask;
import io.subutai.core.peer.api.PeerManager;


/**
 * Container creation step
 */
public class ContainerCloneStep
{
    private static final Logger LOGGER = LoggerFactory.getLogger( ContainerCloneStep.class );
    private final String defaultDomain;
    private final Topology topology;
    private final EnvironmentImpl environment;
    private final TrackerOperation operationTracker;
    private final String localPeerId;
    private PeerManager peerManager;


    public ContainerCloneStep( final String defaultDomain, final Topology topology, final EnvironmentImpl environment,
                               final PeerManager peerManager, final TrackerOperation operationTracker )
    {
        this.defaultDomain = defaultDomain;
        this.topology = topology;
        this.environment = environment;
        this.peerManager = peerManager;
        this.operationTracker = operationTracker;
        this.localPeerId = peerManager.getLocalPeer().getId();
    }


    public void execute() throws EnvironmentCreationException, PeerException
    {

        Map<String, Set<Node>> placement = topology.getNodeGroupPlacement();

        SubnetUtils cidr = new SubnetUtils( environment.getSubnetCidr() );

        List<String> addresses = Lists.newArrayList( cidr.getInfo().getAllAddresses() );

        //remove gw IP
        addresses.remove( cidr.getInfo().getLowAddress() );

        //remove already used container IPs
        for ( ContainerHost containerHost : environment.getContainerHosts() )
        {
            addresses.remove( containerHost.getInterfaceByName( Common.DEFAULT_CONTAINER_INTERFACE ).getIp() );
        }

        //obtain available ip address count
        int totalAvailableIpCount = addresses.size();

        //obtain requested ip address count
        int requestedContainerCount = 0;

        for ( Set<Node> nodes : placement.values() )
        {
            requestedContainerCount += nodes.size();
        }

        //check if available ip addresses are enough
        if ( requestedContainerCount > totalAvailableIpCount )
        {
            throw new EnvironmentCreationException(
                    String.format( "Requested %d containers but only %d IP addresses available",
                            requestedContainerCount, totalAvailableIpCount ) );
        }

        PeerUtil<CreateEnvironmentContainersResponse> cloneUtil = new PeerUtil<>();

        int currentOffset = 0;

        //submit parallel environment part creation tasks across peers
        for ( Map.Entry<String, Set<Node>> peerPlacement : placement.entrySet() )
        {
            Peer peer = peerManager.getPeer( peerPlacement.getKey() );

            cloneUtil.addPeerTask( new PeerUtil.PeerTask<>( peer,
                    new CreatePeerEnvironmentContainersTask( peer, peerManager.getLocalPeer(), environment,
                            addresses.subList( currentOffset, currentOffset + peerPlacement.getValue().size() ),
                            peerPlacement.getValue(), operationTracker ) ) );

            currentOffset += peerPlacement.getValue().size();
        }

        PeerUtil.PeerTaskResults<CreateEnvironmentContainersResponse> cloneResults = cloneUtil.executeParallel();

        //collect results
        boolean succeeded = true;

        for ( PeerUtil.PeerTaskResult<CreateEnvironmentContainersResponse> cloneResult : cloneResults
                .getPeerTaskResults() )
        {
            CreateEnvironmentContainersResponse response = cloneResult.getResult();
            String peerId = cloneResult.getPeer().getId();
            succeeded &= processResponse( response, peerId );
        }

        if ( !succeeded )
        {
            throw new EnvironmentCreationException( "There were errors during container creation." );
        }
    }


    private boolean processResponse( final CreateEnvironmentContainersResponse responses, final String peerId )
    {
        final Set<EnvironmentContainerImpl> containers = new HashSet<>();

        for ( CloneResponse response : responses.getResponses() )
        {
            EnvironmentContainerImpl c = buildContainerEntity( peerId, response );

            containers.add( c );
        }

        if ( !responses.hasSucceeded() )
        {
            LOGGER.warn( responses.getMessages().toString() );
        }

        if ( !containers.isEmpty() )
        {
            environment.addContainers( containers );
        }

        return responses.hasSucceeded();
    }


    private EnvironmentContainerImpl buildContainerEntity( final String peerId, final CloneResponse cloneResponse )
    {
        final HostInterfaces interfaces = new HostInterfaces();

        interfaces.addHostInterface(
                new HostInterfaceModel( Common.DEFAULT_CONTAINER_INTERFACE, cloneResponse.getIp() ) );

        final ContainerHostInfoModel infoModel =
                new ContainerHostInfoModel( cloneResponse.getContainerId(), cloneResponse.getHostname(),
                        cloneResponse.getContainerName(), interfaces, cloneResponse.getTemplateArch(),
                        ContainerHostState.RUNNING );

        return new EnvironmentContainerImpl( localPeerId, peerId, infoModel, cloneResponse.getTemplateId(),
                defaultDomain, cloneResponse.getContainerSize(), cloneResponse.getResourceHostId() );
    }
}
