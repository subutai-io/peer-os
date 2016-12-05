package io.subutai.core.environment.impl.workflow.modification.steps;


import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.net.util.SubnetUtils;

import com.google.common.collect.Sets;

import io.subutai.common.environment.Environment;
import io.subutai.common.environment.EnvironmentModificationException;
import io.subutai.common.environment.RhP2pIp;
import io.subutai.common.environment.Topology;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.protocol.P2PConfig;
import io.subutai.common.protocol.P2pIps;
import io.subutai.common.settings.Common;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.common.util.CollectionUtil;
import io.subutai.common.util.P2PUtil;
import io.subutai.common.util.PeerUtil;
import io.subutai.core.environment.impl.entity.LocalEnvironment;
import io.subutai.core.environment.impl.entity.RhP2PIpEntity;
import io.subutai.core.environment.impl.workflow.creation.steps.helpers.SetupTunnelTask;
import io.subutai.core.environment.impl.workflow.modification.steps.helpers.SetupP2PConnectionTask;


public class SetupP2PStep
{
    private final Topology topology;
    private final LocalEnvironment environment;
    private final TrackerOperation trackerOperation;
    protected PeerUtil<Object> peerUtil = new PeerUtil<>();


    public SetupP2PStep( final Topology topology, final LocalEnvironment environment,
                         final TrackerOperation trackerOperation )
    {
        this.topology = topology;
        this.environment = environment;
        this.trackerOperation = trackerOperation;
    }


    public void execute() throws EnvironmentModificationException, PeerException
    {
        // create p2p subnet util
        SubnetUtils.SubnetInfo p2pSubnetInfo =
                new SubnetUtils( environment.getP2pSubnet(), P2PUtil.P2P_SUBNET_MASK ).getInfo();

        //get all subnet ips
        final Set<String> p2pAddresses = Sets.newHashSet( p2pSubnetInfo.getAllAddresses() );

        //subtract already used ips
        for ( RhP2pIp rhP2pIp : environment.getP2pIps().getP2pIps() )
        {
            p2pAddresses.remove( rhP2pIp.getP2pIp() );
        }

        //obtain target RHs
        Map<String, Set<String>> peerRhIds = topology.getPeerRhIds();

        //count total requested
        int totalIps = 0;

        P2pIps envP2pIps = environment.getP2pIps();

        for ( Set<String> rhIds : peerRhIds.values() )
        {
            for ( String rhId : rhIds )
            {
                if ( envP2pIps.findByRhId( rhId ) == null )
                {
                    totalIps++;
                }
            }
        }

        if ( totalIps > p2pAddresses.size() )
        {
            throw new EnvironmentModificationException(
                    String.format( "Requested IP count %d is more than available %d", totalIps, p2pAddresses.size() ) );
        }


        //p2p setup
        setupP2p( environment, peerRhIds, p2pAddresses );

        // tunnel setup
        setupTunnel( environment );
    }


    private void setupP2p( LocalEnvironment environment, Map<String, Set<String>> peerRhIds, Set<String> p2pAddresses )
            throws EnvironmentModificationException, PeerException
    {
        //generate p2p secret key
        String sharedKey = DigestUtils.md5Hex( UUID.randomUUID().toString() );

        Iterator<String> p2pAddressIterator = p2pAddresses.iterator();

        for ( Peer peer : environment.getPeers() )
        {
            P2PConfig config = new P2PConfig( peer.getId(), environment.getId(), environment.getP2PHash(), sharedKey,
                    Common.DEFAULT_P2P_SECRET_KEY_TTL_SEC );

            //obtain target RHs
            Set<String> rhIds = peerRhIds.get( peer.getId() );

            if ( !CollectionUtil.isCollectionEmpty( rhIds ) )
            {
                //remove already participating peers
                for ( RhP2pIp rhP2pIp : environment.getP2pIps().getP2pIps() )
                {
                    rhIds.remove( rhP2pIp.getRhId() );
                }

                //assign p2p IPs to new RHs
                for ( String rhId : rhIds )
                {
                    config.addRhP2pIp( new RhP2PIpEntity( rhId, p2pAddressIterator.next() ) );
                }
            }

            peerUtil.addPeerTask( new PeerUtil.PeerTask<>( peer, new SetupP2PConnectionTask( peer, config ) ) );
        }

        PeerUtil.PeerTaskResults<Object> p2pResults = peerUtil.executeParallel();

        for ( PeerUtil.PeerTaskResult<Object> p2pResult : p2pResults.getResults() )
        {
            if ( p2pResult.hasSucceeded() )
            {
                environment.getEnvironmentPeer( p2pResult.getPeer().getId() )
                           .addRhP2pIps( ( ( P2PConfig ) p2pResult.getResult() ).getRhP2pIps() );

                trackerOperation
                        .addLog( String.format( "P2P setup succeeded on peer %s", p2pResult.getPeer().getName() ) );
            }
            else
            {
                trackerOperation.addLog(
                        String.format( "P2P setup failed on peer %s. Reason: %s", p2pResult.getPeer().getName(),
                                p2pResult.getFailureReason() ) );
            }
        }

        if ( p2pResults.hasFailures() )
        {
            throw new EnvironmentModificationException( "Failed to setup P2P connection across all peers" );
        }
    }


    private void setupTunnel( Environment environment ) throws EnvironmentModificationException, PeerException
    {

        for ( Peer peer : environment.getPeers() )
        {
            peerUtil.addPeerTask( new PeerUtil.PeerTask<>( peer,
                    new SetupTunnelTask( peer, environment.getId(), environment.getP2pIps() ) ) );
        }

        PeerUtil.PeerTaskResults<Object> tunnelResults = peerUtil.executeParallel();

        for ( PeerUtil.PeerTaskResult tunnelResult : tunnelResults.getResults() )
        {
            if ( tunnelResult.hasSucceeded() )
            {
                trackerOperation.addLog(
                        String.format( "Tunnel setup succeeded on peer %s", tunnelResult.getPeer().getName() ) );
            }
            else
            {
                trackerOperation.addLog(
                        String.format( "Tunnel setup failed on peer %s. Reason: %s", tunnelResult.getPeer().getName(),
                                tunnelResult.getFailureReason() ) );
            }
        }

        if ( tunnelResults.hasFailures() )
        {
            throw new EnvironmentModificationException( "Failed to setup tunnel across all peers" );
        }
    }
}