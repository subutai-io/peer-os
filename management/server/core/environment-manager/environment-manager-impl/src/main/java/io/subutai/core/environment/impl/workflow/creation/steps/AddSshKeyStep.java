package io.subutai.core.environment.impl.workflow.creation.steps;


import java.util.Set;
import java.util.concurrent.Callable;

import com.google.common.base.Strings;

import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.common.util.PeerUtil;
import io.subutai.core.environment.api.exception.EnvironmentManagerException;
import io.subutai.core.environment.impl.entity.LocalEnvironment;


public class AddSshKeyStep
{
    private final String sshKey;
    private final LocalEnvironment environment;
    private final TrackerOperation trackerOperation;
    protected PeerUtil<Object> keyUtil = new PeerUtil<>();


    public AddSshKeyStep( final String sshKey, final LocalEnvironment environment,
                          final TrackerOperation trackerOperation )
    {
        this.sshKey = sshKey;
        this.environment = environment;
        this.trackerOperation = trackerOperation;
    }


    public void execute() throws EnvironmentManagerException, PeerException
    {

        if ( !Strings.isNullOrEmpty( sshKey ) )
        {

            Set<Peer> peers = environment.getPeers();

            for ( final Peer peer : peers )
            {
                keyUtil.addPeerTask( new PeerUtil.PeerTask<>( peer, new Callable<Object>()
                {
                    @Override
                    public Object call() throws Exception
                    {
                        peer.addToAuthorizedKeys( environment.getEnvironmentId(), sshKey );

                        return null;
                    }
                } ) );
            }

            PeerUtil.PeerTaskResults<Object> keyResults = keyUtil.executeParallel();

            for ( PeerUtil.PeerTaskResult keyResult : keyResults.getResults() )
            {
                if ( keyResult.hasSucceeded() )
                {
                    trackerOperation
                            .addLog( String.format( "SSH key added on peer %s", keyResult.getPeer().getName() ) );
                }
                else
                {
                    trackerOperation.addLog( String.format( "SSH key addition failed on peer %s. Reason: %s",
                            keyResult.getPeer().getName(), keyResult.getFailureReason() ) );
                }
            }

            if ( keyResults.hasFailures() )
            {
                throw new EnvironmentManagerException( "Failed to add SSH key on all peers" );
            }

        }
    }
}
