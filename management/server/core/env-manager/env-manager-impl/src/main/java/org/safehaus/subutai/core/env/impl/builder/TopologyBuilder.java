package org.safehaus.subutai.core.env.impl.builder;


import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.safehaus.subutai.common.peer.Peer;
import org.safehaus.subutai.core.env.api.build.NodeGroup;
import org.safehaus.subutai.core.env.api.build.Topology;
import org.safehaus.subutai.core.env.impl.entity.EnvironmentContainerImpl;
import org.safehaus.subutai.core.env.impl.entity.EnvironmentImpl;
import org.safehaus.subutai.core.env.impl.exception.EnvironmentBuildException;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;


/**
 * Builds node groups across peers
 */
public class TopologyBuilder
{

    private final TemplateRegistry templateRegistry;
    private final PeerManager peerManager;


    public TopologyBuilder( final TemplateRegistry templateRegistry, final PeerManager peerManager )
    {
        Preconditions.checkNotNull( templateRegistry );
        Preconditions.checkNotNull( peerManager );

        this.templateRegistry = templateRegistry;
        this.peerManager = peerManager;
    }


    public void build( EnvironmentImpl environment, Topology topology ) throws EnvironmentBuildException
    {
        Preconditions.checkNotNull( environment );
        Preconditions.checkNotNull( topology );

        Map<Peer, Set<NodeGroup>> placement = topology.getNodeGroupPlacement();

        ExecutorService taskExecutor = Executors.newFixedThreadPool( placement.size() );

        CompletionService<Set<EnvironmentContainerImpl>> taskCompletionService =
                new ExecutorCompletionService<>( taskExecutor );

        for ( Map.Entry<Peer, Set<NodeGroup>> peerPlacement : placement.entrySet() )
        {
            taskCompletionService.submit( new NodeGroupBuilder( templateRegistry, peerManager, peerPlacement.getKey(),
                    peerPlacement.getValue() ) );
        }

        Set<Exception> errors = Sets.newHashSet();

        for ( int i = 0; i < placement.size(); i++ )
        {
            try
            {
                Future<Set<EnvironmentContainerImpl>> result = taskCompletionService.take();
                Set<EnvironmentContainerImpl> containers = result.get();
                environment.addContainers( containers );
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
