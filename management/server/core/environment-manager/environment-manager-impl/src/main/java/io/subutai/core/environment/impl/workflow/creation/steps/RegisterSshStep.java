package io.subutai.core.environment.impl.workflow.creation.steps;


import java.util.Set;
import java.util.concurrent.Callable;

import com.google.common.collect.Sets;

import io.subutai.common.environment.SshPublicKeys;
import io.subutai.common.peer.Host;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.common.util.CollectionUtil;
import io.subutai.core.environment.api.exception.EnvironmentManagerException;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.core.environment.impl.workflow.PeerUtil;


public class RegisterSshStep
{
    private final EnvironmentImpl environment;
    private final TrackerOperation trackerOperation;


    public RegisterSshStep( final EnvironmentImpl environment, final TrackerOperation trackerOperation )
    {
        this.environment = environment;
        this.trackerOperation = trackerOperation;
    }


    /**
     * IMPORTANT: Containers always need access to each other via SSH. For example: ssh root@192.168.1.1 date. This is a
     * workaround for: https://github.com/optdyn/hub/issues/413.
     */
    public void execute() throws EnvironmentManagerException, PeerException
    {
        Set<Host> hosts = Sets.newHashSet();

        hosts.addAll( environment.getContainerHosts() );

        Set<String> userKeys = environment.getSshKeys();

        if ( hosts.size() > 1 )
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
            appendUtil.addPeerTask( new PeerUtil.PeerTask<Object>( peer, new Callable<Object>()
            {
                @Override
                public Object call() throws Exception
                {
                    peer.configureSshInEnvironment( environment.getEnvironmentId(), new SshPublicKeys( sshKeys ) );

                    return null;
                }
            } ) );
        }

        Set<PeerUtil.PeerTaskResult<Object>> appendResults = appendUtil.executeParallel();

        boolean hasFailures = false;

        for ( PeerUtil.PeerTaskResult appendResult : appendResults )
        {
            if ( appendResult.hasSucceeded() )
            {
                trackerOperation
                        .addLog( String.format( "Registered ssh keys on peer %s", appendResult.getPeer().getName() ) );
            }
            else
            {
                hasFailures = true;

                trackerOperation.addLog( String.format( "Failed to register ssh keys on peer %s. Reason: %s",
                        appendResult.getPeer().getName(), appendResult.getFailureReason() ) );
            }
        }

        if ( hasFailures )
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
            createUtil.addPeerTask( new PeerUtil.PeerTask<Object>( peer, new Callable<Object>()
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

        Set<PeerUtil.PeerTaskResult<Object>> createResults = createUtil.executeParallel();

        boolean hasFailures = false;

        for ( PeerUtil.PeerTaskResult createResult : createResults )
        {
            if ( createResult.hasSucceeded() )
            {
                trackerOperation
                        .addLog( String.format( "Generated ssh keys on peer %s", createResult.getPeer().getName() ) );
            }
            else
            {
                hasFailures = true;

                trackerOperation.addLog( String.format( "Failed to generate ssh keys on peer %s. Reason: %s",
                        createResult.getPeer().getName(), createResult.getFailureReason() ) );
            }
        }

        if ( hasFailures )
        {
            throw new EnvironmentManagerException( "Failed to generate ssh keys on all peers" );
        }

        return keys;
    }
}
