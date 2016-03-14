package io.subutai.core.peer.cli;


import org.apache.karaf.shell.commands.Command;

import io.subutai.common.protocol.PingDistance;
import io.subutai.common.protocol.PingDistances;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.peer.api.PeerManager;


@Command( scope = "peer", name = "distances" )
public class GetCommunityDistancesCommand extends SubutaiShellCommandSupport
{

    private PeerManager peerManager;


    public void setPeerManager( final PeerManager peerManager )
    {
        this.peerManager = peerManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        PingDistances distances = peerManager.getCommunityDistances();
        for ( PingDistance distance : distances.getAll() )
        {
            if ( distance.isValid() )
            {
                System.out.println(
                        String.format( "%s -> %s %.3f/%.3f/%.3f/%.3f", distance.getSourceIp(), distance.getTargetIp(),
                                distance.getMin(), distance.getAvg(), distance.getMax(), distance.getMdev() ) );
            }
            else
            {
                System.out
                        .println( String.format( "%s -> %s unknown", distance.getSourceIp(), distance.getTargetIp() ) );
            }
        }
        return null;
    }
}
