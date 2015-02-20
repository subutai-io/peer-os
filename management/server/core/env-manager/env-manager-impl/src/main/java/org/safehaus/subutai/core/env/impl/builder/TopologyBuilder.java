package org.safehaus.subutai.core.env.impl.builder;


import java.util.Collections;
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
import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.core.env.impl.entity.EnvironmentImpl;
import org.safehaus.subutai.core.env.impl.exception.EnvironmentBuildException;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;


/**
 * Builds node groups across peers
 */
public class TopologyBuilder
{

    private final TemplateRegistry templateRegistry;
    private final PeerManager peerManager;
    private final String defaultDomain;


    public TopologyBuilder( final TemplateRegistry templateRegistry, final PeerManager peerManager,
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

        ExecutorService taskExecutor = Executors.newFixedThreadPool( placement.size() );

        CompletionService<Set<NodeGroupBuildResult>> taskCompletionService =
                new ExecutorCompletionService<>( taskExecutor );

        for ( Map.Entry<Peer, Set<NodeGroup>> peerPlacement : placement.entrySet() )
        {
            taskCompletionService.submit(
                    new NodeGroupBuilder( environment, templateRegistry, peerManager, peerPlacement.getKey(),
                            peerPlacement.getValue(), Collections.unmodifiableSet( placement.keySet() ),
                            defaultDomain ) );
        }

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
                    String.format( "There were errors during node group creation:  %s", errors ), null );
        }
    }
}
