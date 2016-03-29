package io.subutai.core.environment.impl.workflow.creation.steps;


import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import io.subutai.common.environment.SshPublicKeys;
import io.subutai.common.peer.Host;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.common.util.CollectionUtil;
import io.subutai.core.environment.api.exception.EnvironmentManagerException;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;


public class RegisterSshStep
{
    private static final Logger LOG = LoggerFactory.getLogger( RegisterSshStep.class );

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

        ExecutorService executorService = Executors.newFixedThreadPool( peers.size() );
        ExecutorCompletionService<Peer> completionService = new ExecutorCompletionService<>( executorService );

        for ( final Peer peer : peers )
        {
            completionService.submit( new Callable<Peer>()
            {
                @Override
                public Peer call() throws Exception
                {
                    peer.configureSshInEnvironment( environment.getEnvironmentId(), new SshPublicKeys( sshKeys ) );
                    return peer;
                }
            } );
        }

        Set<Peer> succeededPeers = Sets.newHashSet();
        for ( Peer ignored : peers )
        {
            try
            {
                Future<Peer> f = completionService.take();
                succeededPeers.add( f.get() );
            }
            catch ( Exception e )
            {
                LOG.error( "Problems registering ssh keys in environment", e );
            }
        }

        for ( Peer succeededPeer : succeededPeers )
        {
            trackerOperation.addLog( String.format( "Registered ssh keys on peer %s", succeededPeer.getName() ) );
        }

        Set<Peer> failedPeers = Sets.newHashSet( peers );
        failedPeers.removeAll( succeededPeers );

        for ( Peer failedPeer : failedPeers )
        {
            trackerOperation.addLog( String.format( "Failed to register ssh keys on peer %s", failedPeer.getName() ) );
        }

        if ( !failedPeers.isEmpty() )
        {
            throw new EnvironmentManagerException( "Failed to register ssh keys on all peers" );
        }
    }


    protected Set<String> createSshKeys() throws EnvironmentManagerException, PeerException
    {

        final Set<String> keys = Sets.newHashSet();

        Set<Peer> peers = environment.getPeers();

        ExecutorService executorService = Executors.newFixedThreadPool( peers.size() );
        ExecutorCompletionService<Peer> completionService = new ExecutorCompletionService<>( executorService );

        for ( final Peer peer : peers )
        {
            completionService.submit( new Callable<Peer>()
            {
                @Override
                public Peer call() throws Exception
                {
                    SshPublicKeys sshPublicKeys = peer.generateSshKeyForEnvironment( environment.getEnvironmentId() );
                    keys.addAll( sshPublicKeys.getSshPublicKeys() );
                    return peer;
                }
            } );
        }

        Set<Peer> succeededPeers = Sets.newHashSet();
        for ( Peer ignored : peers )
        {
            try
            {
                Future<Peer> f = completionService.take();
                succeededPeers.add( f.get() );
            }
            catch ( Exception e )
            {
                LOG.error( "Problems generating ssh keys in environment", e );
            }
        }

        for ( Peer succeededPeer : succeededPeers )
        {
            trackerOperation.addLog( String.format( "Generated ssh keys on peer %s", succeededPeer.getName() ) );
        }

        Set<Peer> failedPeers = Sets.newHashSet( peers );
        failedPeers.removeAll( succeededPeers );

        for ( Peer failedPeer : failedPeers )
        {
            trackerOperation.addLog( String.format( "Failed to generate ssh keys on peer %s", failedPeer.getName() ) );
        }

        if ( !failedPeers.isEmpty() )
        {
            throw new EnvironmentManagerException( "Failed to generate ssh keys on all peers" );
        }

        return keys;
    }
}
