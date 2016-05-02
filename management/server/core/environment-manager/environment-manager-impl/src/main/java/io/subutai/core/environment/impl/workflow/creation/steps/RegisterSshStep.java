package io.subutai.core.environment.impl.workflow.creation.steps;


import java.util.Set;
import java.util.concurrent.Callable;

import com.google.common.collect.Sets;

import io.subutai.common.environment.SshPublicKeys;
import io.subutai.common.environment.Topology;
import io.subutai.common.peer.Host;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.common.util.CollectionUtil;
import io.subutai.common.util.PeerUtil;
import io.subutai.core.environment.api.exception.EnvironmentManagerException;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;


public class RegisterSshStep
{
    private final Topology topology;
    private final EnvironmentImpl environment;
    private final TrackerOperation trackerOperation;


    public RegisterSshStep( final Topology topology, final EnvironmentImpl environment,
                            final TrackerOperation trackerOperation )
    {
        this.topology = topology;
        this.environment = environment;
        this.trackerOperation = trackerOperation;
    }


    public void execute() throws EnvironmentManagerException, PeerException
    {
        Set<Host> hosts = Sets.newHashSet();

        hosts.addAll( environment.getContainerHosts() );

        Set<String> userKeys = environment.getSshKeys();

        if ( !hosts.isEmpty() && topology.exchangeSshKeys() )
        {
            exchangeSshKeys( userKeys );
        }
        else if ( !CollectionUtil.isCollectionEmpty( userKeys ) )
        {
            appendSshKeys( userKeys );
        }
    }


    protected void exchangeSshKeys( Set<String> userKeys ) throws EnvironmentManagerException, PeerException
    {
        final Set<String> sshKeys = Sets.newHashSet();

        if ( !CollectionUtil.isCollectionEmpty( userKeys ) )
        {
            sshKeys.addAll( userKeys );
        }

        sshKeys.addAll( createSshKeys() );

        appendSshKeys( sshKeys );
    }


    protected void appendSshKeys( final Set<String> sshKeys ) throws EnvironmentManagerException, PeerException
    {
        Set<Peer> peers = environment.getPeers();

        PeerUtil<Object> appendUtil = new PeerUtil<>();

        for ( final Peer peer : peers )
        {
            appendUtil.addPeerTask( new PeerUtil.PeerTask<>( peer, new Callable<Object>()
            {
                @Override
                public Object call() throws Exception
                {
                    peer.configureSshInEnvironment( environment.getEnvironmentId(), new SshPublicKeys( sshKeys ) );

                    return null;
                }
            } ) );
        }

        PeerUtil.PeerTaskResults<Object> appendResults = appendUtil.executeParallel();

        for ( PeerUtil.PeerTaskResult appendResult : appendResults.getPeerTaskResults() )
        {
            if ( appendResult.hasSucceeded() )
            {
                trackerOperation
                        .addLog( String.format( "Registered ssh keys on peer %s", appendResult.getPeer().getName() ) );
            }
            else
            {
                trackerOperation.addLog( String.format( "Failed to register ssh keys on peer %s. Reason: %s",
                        appendResult.getPeer().getName(), appendResult.getFailureReason() ) );
            }
        }

        if ( appendResults.hasFailures() )
        {
            throw new EnvironmentManagerException( "Failed to register ssh keys on all peers" );
        }
    }


    protected Set<String> createSshKeys() throws EnvironmentManagerException, PeerException
    {

        final Set<String> keys = Sets.newConcurrentHashSet();

        Set<Peer> peers = environment.getPeers();

        PeerUtil<Object> createUtil = new PeerUtil<>();

        for ( final Peer peer : peers )
        {
            createUtil.addPeerTask( new PeerUtil.PeerTask<>( peer, new Callable<Object>()
            {
                @Override
                public Object call() throws Exception
                {
                    SshPublicKeys sshPublicKeys = peer.generateSshKeyForEnvironment( environment.getEnvironmentId() );

                    keys.addAll( sshPublicKeys.getSshPublicKeys() );

                    return null;
                }
            } ) );
        }

        PeerUtil.PeerTaskResults<Object> createResults = createUtil.executeParallel();

        for ( PeerUtil.PeerTaskResult createResult : createResults.getPeerTaskResults() )
        {
            if ( createResult.hasSucceeded() )
            {
                trackerOperation
                        .addLog( String.format( "Generated ssh keys on peer %s", createResult.getPeer().getName() ) );
            }
            else
            {
                trackerOperation.addLog( String.format( "Failed to generate ssh keys on peer %s. Reason: %s",
                        createResult.getPeer().getName(), createResult.getFailureReason() ) );
            }
        }

        if ( createResults.hasFailures() )
        {
            throw new EnvironmentManagerException( "Failed to generate ssh keys on all peers" );
        }

        return keys;
    }
}
