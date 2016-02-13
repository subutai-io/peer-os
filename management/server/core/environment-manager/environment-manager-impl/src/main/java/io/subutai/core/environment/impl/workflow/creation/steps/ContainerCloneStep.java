package io.subutai.core.environment.impl.workflow.creation.steps;


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

import org.apache.commons.net.util.SubnetUtils;

import com.google.common.collect.Sets;

import io.subutai.common.environment.NodeGroup;
import io.subutai.common.environment.Topology;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.security.objects.Ownership;
import io.subutai.common.util.CollectionUtil;
import io.subutai.common.util.ExceptionUtil;
import io.subutai.core.environment.api.exception.EnvironmentCreationException;
import io.subutai.core.environment.impl.EnvironmentManagerImpl;
import io.subutai.core.environment.impl.entity.EnvironmentContainerImpl;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.core.environment.impl.workflow.creation.steps.helpers.CreatePeerNodeGroupsTask;
import io.subutai.core.environment.impl.workflow.creation.steps.helpers.NodeGroupBuildResult;
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
    private static final Logger logger = LoggerFactory.getLogger( ContainerCloneStep.class );
    private final TemplateManager templateRegistry;
    private final String defaultDomain;
    private final Topology topology;
    private final EnvironmentImpl environment;
    private final RelationManager relationManager;
    private final IdentityManager identityManager;
    protected ExceptionUtil exceptionUtil = new ExceptionUtil();
    private PeerManager peerManager;


    public ContainerCloneStep( final TemplateManager templateRegistry, final String defaultDomain,
                               final Topology topology, final EnvironmentImpl environment,
                               final PeerManager peerManager, final EnvironmentManagerImpl environmentManager )
    {
        this.templateRegistry = templateRegistry;
        this.defaultDomain = defaultDomain;
        this.topology = topology;
        this.environment = environment;
        this.peerManager = peerManager;
        this.relationManager = environmentManager.getRelationManager();
        this.identityManager = environmentManager.getIdentityManager();
    }


    public void execute() throws EnvironmentCreationException, PeerException
    {

        Map<String, Set<NodeGroup>> placement = topology.getNodeGroupPlacement();

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
                requestedContainerCount++;
            }
        }

        //check if available ip addresses are enough
        if ( requestedContainerCount > totalAvailableIpCount )
        {
            throw new EnvironmentCreationException(
                    String.format( "Requested %d containers but only %d ip " + "" + "" + "" + "addresses available",
                            requestedContainerCount, totalAvailableIpCount ) );
        }

        ExecutorService taskExecutor = getExecutor( placement.size() );

        CompletionService<Set<NodeGroupBuildResult>> taskCompletionService = getCompletionService( taskExecutor );

        int currentLastUsedIpIndex = environment.getLastUsedIpIndex();


        //submit parallel environment part creation tasks across peers
        for ( Map.Entry<String, Set<NodeGroup>> peerPlacement : placement.entrySet() )
        {
            Peer peer = peerManager.getPeer( peerPlacement.getKey() );
            logger.debug( String.format( "Scheduling node group task on peer %s", peer.getId() ) );

            taskCompletionService.submit(
                    new CreatePeerNodeGroupsTask( peer, peerPlacement.getValue(), peerManager.getLocalPeer(),
                            environment, currentLastUsedIpIndex + 1, templateRegistry, defaultDomain ) );

            for ( NodeGroup nodeGroup : peerPlacement.getValue() )
            {
                currentLastUsedIpIndex++;
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
                    logger.debug( String.format( "Node group build result: %s", result ) );
                    if ( !CollectionUtil.isCollectionEmpty( result.getContainers() ) )
                    {
                        environment.addContainers( result.getContainers() );
                        buildRelationChain( environment, result.getContainers() );
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

            throw new EnvironmentCreationException(
                    String.format( "There were errors during container creation:  %s", errors ) );
        }
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
            logger.info( "Error building relation", ex );
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
