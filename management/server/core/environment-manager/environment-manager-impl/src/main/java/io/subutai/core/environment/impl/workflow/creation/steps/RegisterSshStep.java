package io.subutai.core.environment.impl.workflow.creation.steps;


import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;

import io.subutai.common.command.CommandResult;
import io.subutai.common.command.CommandUtil;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.peer.Host;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.settings.Common;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.common.util.CollectionUtil;
import io.subutai.core.environment.api.exception.EnvironmentManagerException;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;


public class RegisterSshStep
{
    private static final Logger LOG = LoggerFactory.getLogger( RegisterSshStep.class );

    private final EnvironmentImpl environment;
    private final TrackerOperation trackerOperation;
    protected CommandUtil commandUtil = new CommandUtil();


    public RegisterSshStep( final EnvironmentImpl environment, final TrackerOperation trackerOperation )
    {
        this.environment = environment;
        this.trackerOperation = trackerOperation;
    }


    /**
     * IMPORTANT: Containers always need access to each other via SSH. For example: ssh root@192.168.1.1 date. This is a
     * workaround for: https://github.com/optdyn/hub/issues/413.
     */
    public void execute( Set<String> userKeys ) throws EnvironmentManagerException, PeerException
    {
        Set<Host> hosts = Sets.newHashSet();

        hosts.addAll( environment.getContainerHosts() );

        if ( hosts.size() > 1 )
        {
            exchangeSshKeys( hosts, userKeys );
        }
        else if ( !CollectionUtil.isCollectionEmpty( userKeys ) )
        {
            appendSshKeys( userKeys );
        }
    }


    protected void exchangeSshKeys( Set<Host> hosts, Set<String> userKeys )
            throws EnvironmentManagerException, PeerException
    {
        final Set<String> sshKeys = Sets.newHashSet();

        if ( !CollectionUtil.isCollectionEmpty( userKeys ) )
        {
            sshKeys.addAll( userKeys );
        }

        sshKeys.addAll( createSshKeys( hosts ) );

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
                    peer.addSshKeysToEnvironment( environment.getEnvironmentId(), sshKeys );
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


    protected Set<String> createSshKeys( Set<Host> hosts ) throws EnvironmentManagerException
    {
        Set<String> keys = Sets.newHashSet();

        Map<Host, CommandResult> results = commandUtil.executeParallelSilent( getCreateNReadSSHCommand(), hosts );

        Set<Host> succeededHosts = Sets.newHashSet();
        Set<Host> failedHosts = Sets.newHashSet( hosts );

        for ( Map.Entry<Host, CommandResult> resultEntry : results.entrySet() )
        {
            Host host = resultEntry.getKey();
            CommandResult result = resultEntry.getValue();
            if ( result.hasSucceeded() && !Strings.isNullOrEmpty( result.getStdOut() ) )
            {
                keys.add( result.getStdOut() );

                succeededHosts.add( host );
            }
            else
            {
                LOG.debug( String.format( "Error: %s, Exit Code %d", result.getStdErr(), result.getExitCode() ) );
            }
        }


        failedHosts.removeAll( succeededHosts );

        for ( Host failedHost : failedHosts )
        {
            trackerOperation.addLog( String.format( "SSH key creation failed on host %s", failedHost.getHostname() ) );
        }

        if ( !failedHosts.isEmpty() )
        {
            throw new EnvironmentManagerException( "Failed to create SSH key on all hosts" );
        }

        return keys;
    }


    public RequestBuilder getCreateNReadSSHCommand()
    {
        return new RequestBuilder( String.format( "rm -rf %1$s && " +
                        "mkdir -p %1$s && " +
                        "chmod 700 %1$s && " +
                        "ssh-keygen -t dsa -P '' -f %1$s/id_dsa -q && " + "cat %1$s/id_dsa.pub",
                Common.CONTAINER_SSH_FOLDER ) );
    }
}
