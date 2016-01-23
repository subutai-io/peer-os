package io.subutai.core.localpeer.cli;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.Peer;
import io.subutai.common.protocol.ControlNetworkConfig;
import io.subutai.common.util.ControlNetworkException;
import io.subutai.common.util.ControlNetworkUtil;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.peer.api.PeerManager;


@Command( scope = "localpeer", name = "control-network-add" )
public class UpdateControlNetworkCommand extends SubutaiShellCommandSupport
{

    private PeerManager peerManager;


    public UpdateControlNetworkCommand( final PeerManager peerManager )
    {
        this.peerManager = peerManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        List<ControlNetworkConfig> configs = new ArrayList<>();
        final String localPeerId = peerManager.getLocalPeer().getId();

        for ( Peer peer : peerManager.getPeers() )
        {
            if ( !peer.isOnline() )
            {
                System.out.println(
                        String.format( "Peer '%s' is down at this moment. Skipping this peer.", peer.getId() ) );
                continue;
            }

            configs.add( peer.getControlNetworkConfig( localPeerId ) );
        }

        String newNetwork = ControlNetworkUtil.findFreeNetwork( configs );

        final String secretKey = DigestUtils.md5Hex( UUID.randomUUID().toString() );
        List<ControlNetworkConfig> result = ControlNetworkUtil
                .rebuild( localPeerId, newNetwork, secretKey, ControlNetworkUtil.DEFAULT_TTL, configs );

        for ( ControlNetworkConfig config : result )
        {
            System.out.println(
                    String.format( "%s %s %s", config.getPeerId(), config.getFingerprint(), config.getAddress() ) );
        }
        return null;
    }
}
