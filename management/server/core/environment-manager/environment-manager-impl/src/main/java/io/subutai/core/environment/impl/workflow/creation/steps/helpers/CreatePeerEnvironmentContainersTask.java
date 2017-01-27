package io.subutai.core.environment.impl.workflow.creation.steps.helpers;


import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.subutai.common.environment.CreateEnvironmentContainersRequest;
import io.subutai.common.environment.CreateEnvironmentContainersResponse;
import io.subutai.common.environment.Environment;
import io.subutai.common.environment.Node;
import io.subutai.common.host.HostArchitecture;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.Peer;
import io.subutai.common.task.CloneRequest;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.common.util.CollectionUtil;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.hub.share.quota.ContainerQuota;


public class CreatePeerEnvironmentContainersTask implements Callable<CreateEnvironmentContainersResponse>
{
    private static final Logger LOG = LoggerFactory.getLogger( CreatePeerEnvironmentContainersTask.class );

    private final IdentityManager identityManager;
    private final Peer peer;
    private final Set<Node> nodes;
    private final LocalPeer localPeer;
    private final Environment environment;
    private final List<String> ipAddresses;
    private final TrackerOperation trackerOperation;


    public CreatePeerEnvironmentContainersTask( final IdentityManager identityManager, final Peer peer,
                                                final LocalPeer localPeer, final Environment environment,
                                                final List<String> ipAddresses, final Set<Node> nodes,
                                                final TrackerOperation trackerOperation )
    {
        Preconditions.checkNotNull( identityManager );
        Preconditions.checkNotNull( peer );
        Preconditions.checkNotNull( localPeer );
        Preconditions.checkNotNull( environment );
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( ipAddresses ) );
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( nodes ) );
        Preconditions.checkArgument( ipAddresses.size() == nodes.size() );

        this.identityManager = identityManager;
        this.peer = peer;
        this.nodes = nodes;
        this.localPeer = localPeer;
        this.environment = environment;
        this.ipAddresses = ipAddresses;
        this.trackerOperation = trackerOperation;
    }


    @Override
    public CreateEnvironmentContainersResponse call() throws Exception
    {
        String maskLength = environment.getSubnetCidr().split( "/" )[1];

        final CreateEnvironmentContainersRequest request =
                new CreateEnvironmentContainersRequest( environment.getId(), localPeer.getId(),
                        localPeer.getOwnerId() );

        Iterator<String> ipAddressIterator = ipAddresses.iterator();

        for ( Node node : nodes )
        {
            LOG.debug( String.format( "Scheduling on %s %s", node.getPeerId(), node.getName() ) );

            final String ip = ipAddressIterator.next();

            CloneRequest cloneRequest =
                    new CloneRequest( node.getHostId(), node.getHostname(), node.getName(), ip + "/" + maskLength,
                            node.getTemplateId(), HostArchitecture.AMD64, new ContainerQuota( node.getType() ),
                            identityManager.getActiveSession().getKurjunToken() );

            request.addRequest( cloneRequest );
        }

        CreateEnvironmentContainersResponse response = peer.createEnvironmentContainers( request );

        for ( String message : response.getMessages() )
        {
            trackerOperation.addLog( message );
        }

        return response;
    }
}
