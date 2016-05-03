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
import io.subutai.common.security.objects.Ownership;
import io.subutai.common.security.relation.RelationManager;
import io.subutai.common.security.relation.model.Relation;
import io.subutai.common.security.relation.model.RelationInfoMeta;
import io.subutai.common.security.relation.model.RelationMeta;
import io.subutai.common.security.relation.model.RelationStatus;
import io.subutai.common.settings.Common;
import io.subutai.common.task.CloneResponse;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.common.util.PeerUtil;
import io.subutai.core.environment.api.exception.EnvironmentCreationException;
import io.subutai.core.environment.impl.EnvironmentManagerImpl;
import io.subutai.core.environment.impl.entity.EnvironmentContainerImpl;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.core.environment.impl.workflow.creation.steps.helpers.CreatePeerEnvironmentContainersTask;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.identity.api.model.UserDelegate;
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
    private final RelationManager relationManager;
    private final IdentityManager identityManager;
    private final TrackerOperation operationTracker;
    private final String localPeerId;
    private PeerManager peerManager;


    public ContainerCloneStep( final String defaultDomain, final Topology topology, final EnvironmentImpl environment,
                               final PeerManager peerManager, final EnvironmentManagerImpl environmentManager,
                               final TrackerOperation operationTracker )
    {
        this.defaultDomain = defaultDomain;
        this.topology = topology;
        this.environment = environment;
        this.peerManager = peerManager;
        this.relationManager = environmentManager.getRelationManager();
        this.identityManager = environmentManager.getIdentityManager();
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
            succeeded &= processResponse( placement.get( peerId ), response, peerId );
        }

        if ( !succeeded )
        {
            throw new EnvironmentCreationException( "There were errors during container creation." );
        }
    }


    protected boolean processResponse( final Set<Node> nodes, final CreateEnvironmentContainersResponse responses,
                                       final String peerId )
    {
        final Set<EnvironmentContainerImpl> containers = new HashSet<>();

        boolean result = true;

        for ( Node node : nodes )
        {
            CloneResponse response = responses.findByHostname( node.getHostname() );

            if ( response != null )
            {
                EnvironmentContainerImpl c = buildContainerEntity( peerId, node, response );

                containers.add( c );
            }
            else
            {
                LOGGER.warn( "Scheduled container not found: " + node.toString() );

                result = false;
            }
        }

        if ( !containers.isEmpty() )
        {
            environment.addContainers( containers );

            buildRelationChain( environment, containers );
        }

        return result;
    }


    private EnvironmentContainerImpl buildContainerEntity( final String peerId, final Node node,
                                                           final CloneResponse cloneResponse )
    {

        final HostInterfaces interfaces = new HostInterfaces();
        interfaces.addHostInterface(
                new HostInterfaceModel( Common.DEFAULT_CONTAINER_INTERFACE, cloneResponse.getIp() ) );
        final ContainerHostInfoModel infoModel =
                new ContainerHostInfoModel( cloneResponse.getContainerId(), cloneResponse.getHostname(),
                        cloneResponse.getContainerName(), interfaces, cloneResponse.getTemplateArch(),
                        ContainerHostState.RUNNING );
        return new EnvironmentContainerImpl( localPeerId, peerId, infoModel, cloneResponse.getTemplateName(),
                cloneResponse.getTemplateArch(), node.getSshGroupId(), node.getHostsGroupId(), defaultDomain,
                node.getType(), node.getHostId(), cloneResponse.getContainerName() );
    }


    private void buildRelationChain( EnvironmentImpl environment, Set<EnvironmentContainerImpl> containers )
    {
        try
        {
            User activeUser = identityManager.getActiveUser();
            UserDelegate delegatedUser = identityManager.getUserDelegate( activeUser.getId() );
            for ( final EnvironmentContainerImpl container : containers )
            {
                //TODO create environment <-> container ownership
                RelationInfoMeta relationInfoMeta =
                        new RelationInfoMeta( true, true, true, true, Ownership.USER.getLevel() );
                Map<String, String> relationTraits = relationInfoMeta.getRelationTraits();
                relationTraits.put( "containerLimit", "unlimited" );
                relationTraits.put( "bandwidthLimit", "unlimited" );
                relationTraits.put("ownership", Ownership.USER.getName());

                RelationMeta relationMeta = new RelationMeta( delegatedUser, environment, container, "" );
                Relation relation = relationManager.buildRelation( relationInfoMeta, relationMeta );
                relation.setRelationStatus( RelationStatus.VERIFIED );
                relationManager.saveRelation( relation );            }
        }
        catch ( Exception ex )
        {
            LOGGER.error( "Error building relation", ex );
        }
    }
}
