package io.subutai.core.environment.impl.workflow.creation.steps;


import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionService;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.net.util.SubnetUtils;

import io.subutai.common.environment.CreateEnvironmentContainerGroupResponse;
import io.subutai.common.environment.Node;
import io.subutai.common.environment.Topology;
import io.subutai.common.host.ContainerHostInfoModel;
import io.subutai.common.host.ContainerHostState;
import io.subutai.common.host.HostInterfaceModel;
import io.subutai.common.host.HostInterfaces;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.security.objects.Ownership;
import io.subutai.common.settings.Common;
import io.subutai.common.task.CloneResponse;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.common.util.ExceptionUtil;
import io.subutai.core.environment.api.exception.EnvironmentCreationException;
import io.subutai.core.environment.impl.EnvironmentManagerImpl;
import io.subutai.core.environment.impl.entity.EnvironmentContainerImpl;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.core.environment.impl.workflow.creation.steps.helpers.CreatePeerNodeGroupsTask;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.identity.api.model.UserDelegate;
import io.subutai.core.kurjun.api.TemplateManager;
import io.subutai.core.object.relation.api.RelationManager;
import io.subutai.core.object.relation.api.model.Relation;
import io.subutai.core.object.relation.api.model.RelationInfo;
import io.subutai.core.object.relation.api.model.RelationInfoMeta;
import io.subutai.core.object.relation.api.model.RelationMeta;
import io.subutai.core.peer.api.PeerManager;


/**
 * Container creation step
 */
public class ContainerCloneStep
{
    private static final Logger LOGGER = LoggerFactory.getLogger( ContainerCloneStep.class );
    private final TemplateManager templateRegistry;
    private final String defaultDomain;
    private final Topology topology;
    private final EnvironmentImpl environment;
    private final RelationManager relationManager;
    private final IdentityManager identityManager;
    private final TrackerOperation operationTracker;
    private final String localPeerId;
    protected ExceptionUtil exceptionUtil = new ExceptionUtil();
    private PeerManager peerManager;


    public ContainerCloneStep( final TemplateManager templateRegistry, final String defaultDomain,
                               final Topology topology, final EnvironmentImpl environment,
                               final PeerManager peerManager, final EnvironmentManagerImpl environmentManager,
                               final TrackerOperation operationTracker )
    {
        this.templateRegistry = templateRegistry;
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

        //obtain available ip address count
        int totalAvailableIpCount = cidr.getInfo().getAddressCount() - 1;//one ip is for gateway

        //subtract already used ip range
        totalAvailableIpCount -= environment.getLastUsedIpIndex();

        //obtain used ip address count
        int requestedContainerCount = 0;
        for ( Set<Node> nodes : placement.values() )
        {
            requestedContainerCount += nodes.size();
        }

        //check if available ip addresses are enough
        if ( requestedContainerCount > totalAvailableIpCount )
        {
            throw new EnvironmentCreationException(
                    String.format( "Requested %d containers but only %d ip " + "" + "" + "" + "addresses available",
                            requestedContainerCount, totalAvailableIpCount ) );
        }

        ExecutorService taskExecutor = Executors.newFixedThreadPool( placement.size() );

        CompletionService<CreateEnvironmentContainerGroupResponse> taskCompletionService =
                getCompletionService( taskExecutor );

        int currentLastUsedIpIndex = environment.getLastUsedIpIndex();


        //submit parallel environment part creation tasks across peers
        for ( Map.Entry<String, Set<Node>> peerPlacement : placement.entrySet() )
        {
            Peer peer = peerManager.getPeer( peerPlacement.getKey() );
            LOGGER.debug( String.format( "Scheduling node group task on peer %s", peer.getId() ) );

            taskCompletionService.submit( new CreatePeerNodeGroupsTask( peer, peerManager.getLocalPeer(), environment,
                            currentLastUsedIpIndex + 1, peerPlacement.getValue() ) );

            currentLastUsedIpIndex += peerPlacement.getValue().size();

            environment.setLastUsedIpIndex( currentLastUsedIpIndex );
        }

        taskExecutor.shutdown();

        //collect results
        //        Set<String> errors = Sets.newHashSet();

        for ( int i = 0; i < placement.size(); i++ )
        {
            try
            {
                Future<CreateEnvironmentContainerGroupResponse> futures = taskCompletionService.take();
                CreateEnvironmentContainerGroupResponse response = futures.get();
                processResponse( placement.get( response.getPeerId() ), response );
            }
            catch ( Exception e )
            {
                LOGGER.error( e.getMessage(), e );
                throw new EnvironmentCreationException(
                        String.format( "There were errors during container creation:  %s", e.getMessage() ) );
            }
        }
    }


    private void processResponse( final Set<Node> nodes, final CreateEnvironmentContainerGroupResponse result )
    {
        final Set<EnvironmentContainerImpl> containers = new HashSet<>();
        for ( CloneResponse cloneResponse : result.getResponses() )
        {
            LOGGER.debug( String.format( "Clone response: %s", cloneResponse ) );

            operationTracker.addLog( cloneResponse.getLog() );

            final Node node = findNodeGroup( cloneResponse.getHostname(), nodes );
            if ( node != null )
            {
                EnvironmentContainerImpl c = buildContainerEntity( result.getPeerId(), node, cloneResponse );
                containers.add( c );
            }
            else
            {
                LOGGER.error( "Node group not found." );
            }
        }

        environment.addContainers( containers );
        buildRelationChain( environment, containers );
    }


    private EnvironmentContainerImpl buildContainerEntity( final String peerId, final Node node,
                                                           final CloneResponse cloneResponse )
    {

        final HostInterfaces interfaces = new HostInterfaces();
        interfaces.addHostInterface(
                new HostInterfaceModel( Common.DEFAULT_CONTAINER_INTERFACE, cloneResponse.getIp() ) );
        final ContainerHostInfoModel infoModel =
                new ContainerHostInfoModel( cloneResponse.getAgentId(), cloneResponse.getHostname(), interfaces,
                        cloneResponse.getTemplateArch(), ContainerHostState.CLONING );
        return new EnvironmentContainerImpl( localPeerId, peerId, cloneResponse.getHostname(), infoModel,
                cloneResponse.getTemplateName(), cloneResponse.getTemplateArch(), node.getSshGroupId(),
                node.getHostsGroupId(), defaultDomain, node.getType(), node.getHostId(), node.getName() );
    }


    private Node findNodeGroup( final String hostname, final Set<Node> nodes )
    {
        for ( Node node : nodes )
        {
            if ( hostname.equals( node.getHostname() ) )
            {
                return node;
            }
        }

        return null;
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

                RelationMeta relationMeta = new RelationMeta();
                relationMeta.setSource( delegatedUser );
                relationMeta.setTarget( environment );
                relationMeta.setObject( container );

                RelationInfoMeta relationInfoMeta =
                        new RelationInfoMeta( true, true, true, true, Ownership.USER.getLevel() );
                RelationInfo relationInfo = relationManager.createTrustRelationship( relationInfoMeta );

                Relation relation = relationManager.buildTrustRelation( relationInfo, relationMeta );

                relationManager.saveRelation( relation );
            }
        }
        catch ( Exception ex )
        {
            LOGGER.error( "Error building relation", ex );
        }
    }


    protected CompletionService<CreateEnvironmentContainerGroupResponse> getCompletionService( Executor executor )
    {
        return new ExecutorCompletionService<>( executor );
    }
}
