package io.subutai.core.env.impl.tasks;


import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;

import org.safehaus.subutai.common.environment.Topology;
import org.safehaus.subutai.common.network.Gateway;
import org.safehaus.subutai.common.network.Vni;
import org.safehaus.subutai.common.peer.Peer;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.common.util.ExceptionUtil;
import io.subutai.core.env.api.exception.EnvironmentCreationException;
import io.subutai.core.env.impl.EnvironmentManagerImpl;
import io.subutai.core.env.impl.entity.EnvironmentImpl;
import io.subutai.core.env.impl.exception.ResultHolder;
import org.safehaus.subutai.core.peer.api.LocalPeer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.net.util.SubnetUtils;

import com.google.common.collect.Sets;


public class CreateEnvironmentTask implements Runnable
{
    private static final Logger LOG = LoggerFactory.getLogger( CreateEnvironmentTask.class.getName() );

    private final LocalPeer localPeer;
    private final EnvironmentManagerImpl environmentManager;
    private final EnvironmentImpl environment;
    private final Topology topology;
    private final ResultHolder<EnvironmentCreationException> resultHolder;
    private final TrackerOperation op;
    protected Semaphore semaphore;
    protected ExceptionUtil exceptionUtil = new ExceptionUtil();


    public CreateEnvironmentTask( final LocalPeer localPeer, final EnvironmentManagerImpl environmentManager,
                                  final EnvironmentImpl environment, final Topology topology,
                                  final ResultHolder<EnvironmentCreationException> resultHolder,
                                  final TrackerOperation op )
    {
        this.localPeer = localPeer;
        this.environmentManager = environmentManager;
        this.environment = environment;
        this.topology = topology;
        this.resultHolder = resultHolder;
        this.op = op;
        this.semaphore = new Semaphore( 0 );
    }


    @Override
    public void run()
    {
        try
        {
            Set<Peer> allPeers = Sets.newHashSet( topology.getNodeGroupPlacement().keySet() );

            op.addLog( "Setting up secure channel..." );

            //exchange environment certificates
            environmentManager.setupEnvironmentTunnel( environment.getId(), allPeers );

            //check availability of subnet
            Map<Peer, Set<Gateway>> usedGateways = environmentManager.getUsedGateways( allPeers );

            SubnetUtils subnetUtils = new SubnetUtils( environment.getSubnetCidr() );
            String environmentGatewayIp = subnetUtils.getInfo().getLowAddress();

            for ( Map.Entry<Peer, Set<Gateway>> peerGateways : usedGateways.entrySet() )
            {
                Peer peer = peerGateways.getKey();
                Set<Gateway> gateways = peerGateways.getValue();
                for ( Gateway gateway : gateways )
                {
                    if ( gateway.getIp().equals( environmentGatewayIp ) )
                    {
                        throw new EnvironmentCreationException(
                                String.format( "Subnet %s is already used on peer %s", environment.getSubnetCidr(),
                                        peer.getName() ) );
                    }
                }
            }

            //figure out free VNI
            long vni = environmentManager.findFreeVni( allPeers );

            //reserve VNI on local peer
            Vni newVni = new Vni( vni, environment.getId() );

            op.addLog( "Reserving new vni on local peer..." );

            int vlan = localPeer.reserveVni( newVni );

            op.addLog( "Creating gateway on local peer..." );

            //setup gateway on mgmt host
            localPeer.getManagementHost().createGateway( environmentGatewayIp, vlan );

            //reserve VNI on remote getPeerInfos
            allPeers.remove( localPeer );

            for ( Peer peer : allPeers )
            {
                peer.reserveVni( newVni );
            }

            //save environment VNI
            environment.setVni( vni );

            environmentManager.growEnvironment( environment.getId(), topology, false, false, op );

            op.addLogDone( "Environment created successfully" );
        }
        catch ( Exception e )
        {
            LOG.error( String.format( "Error creating environment %s, topology %s", environment.getId(), topology ),
                    e );

            resultHolder.setResult(
                    e instanceof EnvironmentCreationException ? ( EnvironmentCreationException ) e.getCause() :
                    new EnvironmentCreationException( exceptionUtil.getRootCause( e ) ) );

            op.addLogFailed( String.format( "Error creating environment: %s", resultHolder.getResult().getMessage() ) );
        }
        finally
        {
            semaphore.release();
        }
    }


    public void waitCompletion() throws InterruptedException
    {
        semaphore.acquire();
    }
}
