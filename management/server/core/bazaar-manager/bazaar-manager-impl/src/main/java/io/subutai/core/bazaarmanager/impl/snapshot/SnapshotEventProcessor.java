package io.subutai.core.bazaarmanager.impl.snapshot;


import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.bazaar.share.dto.snapshots.PeerSnapshotsDto;
import io.subutai.bazaar.share.dto.snapshots.SnapshotDto;
import io.subutai.common.host.Snapshot;
import io.subutai.common.host.SnapshotEventListener;
import io.subutai.common.host.Snapshots;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.ContainerId;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.peer.ResourceHostException;
import io.subutai.core.bazaarmanager.api.BazaarManager;
import io.subutai.core.bazaarmanager.api.BazaarRequester;
import io.subutai.core.bazaarmanager.api.RestClient;
import io.subutai.core.bazaarmanager.api.RestResult;
import io.subutai.core.bazaarmanager.api.exception.BazaarManagerException;


public class SnapshotEventProcessor extends BazaarRequester implements SnapshotEventListener
{
    private final Logger log = LoggerFactory.getLogger( getClass() );

    // name of the partition, which indicates whole container
    private final String SNAPSHOT_CONFIG_PARTITION = "config";

    private LocalPeer localPeer;


    public SnapshotEventProcessor( final BazaarManager bazaarManager, final RestClient restClient,
                                   final LocalPeer localPeer )
    {
        super( bazaarManager, restClient );
        this.localPeer = localPeer;
    }


    @Override
    public void onSnapshotCreate( final ContainerId containerId )
    {
        syncSnapshotsWithBazaar();
    }


    @Override
    public void onSnapshotRestore( final ContainerId containerId )
    {
        syncSnapshotsWithBazaar();
    }


    @Override
    public void onSnapshotDelete( final ContainerId containerId )
    {
        syncSnapshotsWithBazaar();
    }


    @Override
    public void request() throws BazaarManagerException
    {
        syncSnapshotsWithBazaar();
    }


    private void syncSnapshotsWithBazaar()
    {
        // TODO: synchronize calls to this method. Prevent spamming Bazaar on each back-to-back snapshot event.

        PeerSnapshotsDto peerSnapshotsDto = new PeerSnapshotsDto();

        Set<ContainerHost> peerRegisteredContainers = this.localPeer.getRegisteredContainers();


        for ( final ContainerHost container : peerRegisteredContainers )
        {
            Snapshots snapshots;
            try
            {
                /* via LocalPeer it requires authorization
                snapshots = this.localPeer.listContainerHostSnapshots( container.getContainerId() );
                */
                ResourceHost rh = this.localPeer.getResourceHostById( container.getResourceHostId().getId() );
                snapshots = rh.listContainerHostSnapshots( localPeer.getContainerHostById( container.getId() ) );
            }
            catch ( PeerException | ResourceHostException e )
            {
                log.error( "Failed to get list of snapshots for container {}: {}", container, e.getMessage() );
                peerSnapshotsDto.getOfflineContainers().add( container.getId() );
                continue;
            }

            for ( final Snapshot snapshot : snapshots.getSnapshots() )
            {
                // omit component partitions, filter only container parent(whole container) partition
                if ( SNAPSHOT_CONFIG_PARTITION.equals( snapshot.getPartition() ) )
                {
                    peerSnapshotsDto.getSnapshots().add( new SnapshotDto( container.getId(), snapshot.getLabel(),
                            snapshot.getCreated() ) );
                }
            }
        }

        try
        {
            RestResult<Object> restResult = restClient.post( "/rest/v1/peer/snapshots", peerSnapshotsDto );

            if ( !restResult.isSuccess() )
            {
                throw new BazaarManagerException( restResult.getError() );
            }
        }
        catch ( Exception e )
        {
            log.error( "Failed to synchronize snapshots with Bazaar: {}", e.getMessage(), e );
        }
    }
}
