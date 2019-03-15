package io.subutai.core.environment.impl.workflow.creation.steps;


import java.util.Set;
import java.util.concurrent.Callable;

import com.google.common.collect.Sets;

import io.subutai.common.environment.Containers;
import io.subutai.common.environment.Topology;
import io.subutai.common.exception.ActionFailedException;
import io.subutai.common.peer.Host;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.security.SshEncryptionType;
import io.subutai.common.security.SshKeys;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.common.util.CollectionUtil;
import io.subutai.common.util.PeerUtil;
import io.subutai.core.environment.api.exception.EnvironmentManagerException;
import io.subutai.core.environment.impl.entity.LocalEnvironment;


public class RegisterSshStep
{
    private final Topology topology;
    private final LocalEnvironment environment;
    private final TrackerOperation trackerOperation;
    protected PeerUtil<Object> peerUtil = new PeerUtil<>();


    public RegisterSshStep( final Topology topology, final LocalEnvironment environment,
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
            SshKeys sshKeys = new SshKeys();

            sshKeys.addStringKeys( userKeys );

            appendSshKeys( sshKeys );
        }
    }


    protected void exchangeSshKeys( Set<String> userKeys ) throws EnvironmentManagerException, PeerException
    {
        final SshKeys sshKeys = new SshKeys();

        if ( !CollectionUtil.isCollectionEmpty( userKeys ) )
        {
            sshKeys.addStringKeys( userKeys );
        }

        sshKeys.addKeys( readOrCreateSshKeys().getKeys() );

        appendSshKeys( sshKeys );
    }


    protected void appendSshKeys( final SshKeys sshKeys ) throws EnvironmentManagerException, PeerException
    {
        Set<Peer> peers = environment.getPeers();


        for ( final Peer peer : peers )
        {
            peerUtil.addPeerTask( new PeerUtil.PeerTask<>( peer, new Callable<Object>()
            {
                @Override
                public Object call() throws Exception
                {
                    Containers failedHosts = peer.configureSshInEnvironment( environment.getEnvironmentId(), sshKeys );

                    if ( !failedHosts.getContainers().isEmpty() )
                    {
                        throw new ActionFailedException( "Failed to add ssh keys on each host" );
                    }

                    return null;
                }
            } ) );
        }

        PeerUtil.PeerTaskResults<Object> appendResults = peerUtil.executeParallel();

        for ( PeerUtil.PeerTaskResult appendResult : appendResults.getResults() )
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


    protected SshKeys readOrCreateSshKeys() throws EnvironmentManagerException, PeerException
    {

        final SshKeys allSshKeys = new SshKeys();

        Set<Peer> peers = environment.getPeers();


        for ( final Peer peer : peers )
        {
            peerUtil.addPeerTask( new PeerUtil.PeerTask<>( peer, new Callable<Object>()
            {
                @Override
                public Object call() throws Exception
                {
                    SshKeys sshPublicKeys = peer.readOrCreateSshKeysForEnvironment( environment.getEnvironmentId(),
                            topology.getSshKeyType() == SshEncryptionType.UNKNOWN ? SshEncryptionType.RSA :
                            topology.getSshKeyType() );

                    allSshKeys.addKeys( sshPublicKeys.getKeys() );

                    return null;
                }
            } ) );
        }

        PeerUtil.PeerTaskResults<Object> createResults = peerUtil.executeParallel();

        for ( PeerUtil.PeerTaskResult createResult : createResults.getResults() )
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

        return allSshKeys;
    }
}
