package io.subutai.core.bazaarmanager.impl.environment.state.backup;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.StringUtils;

import io.subutai.bazaar.share.dto.backup.BackupCommandsDto;
import io.subutai.bazaar.share.dto.backup.ContainerBackupCommandDto;
import io.subutai.bazaar.share.dto.environment.EnvironmentPeerDto;
import io.subutai.common.command.CommandException;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.host.Snapshot;
import io.subutai.common.host.Snapshots;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.peer.ResourceHostException;
import io.subutai.core.bazaarmanager.api.RestResult;
import io.subutai.core.bazaarmanager.api.exception.BazaarManagerException;
import io.subutai.core.bazaarmanager.impl.environment.state.Context;
import io.subutai.core.bazaarmanager.impl.environment.state.StateHandler;
import io.subutai.core.bazaarmanager.impl.util.Utils;


public class BackupStateHandler extends StateHandler
{
    private final String path = "/rest/v1/environments/%s/peers/%s/backups";
    static final String DEFAULT_PARTITION = "all";

    private ExecutorService executor = Executors.newFixedThreadPool( 5 );


    public BackupStateHandler( Context ctx )
    {
        super( ctx, "Container backup processor" );
    }


    @Override
    protected Object doHandle( final EnvironmentPeerDto peerDto ) throws BazaarManagerException
    {
        BackupCommandsDto backupOperations = ctx.restClient.getStrict( path( path, peerDto ), BackupCommandsDto.class );

        processBackupCommands( backupOperations, /*peerDto.getCdnToken()*/ backupOperations.getCdnToken() );

        return backupOperations;
    }


    private void processBackupCommands( BackupCommandsDto backupCommandsDto, String cdnToken )
    {
        for ( final ContainerBackupCommandDto backup : backupCommandsDto.getBackupCommands() )
        {
            ResourceHost rh = null;
            ContainerHost containerHost = null;
            String pathToBackupFile = null, pathToEncryptedBackupFile = null;

            try
            {
                containerHost = ctx.localPeer.getContainerHostById( backup.getContainerId() );
                rh = ctx.localPeer.getResourceHostByContainerId( backup.getContainerId() );

                // check if snapshot with given label exists. If not, create it.
                boolean isNewTempSnapshotCreated;
                try
                {
                    isNewTempSnapshotCreated = createSnapshotIfNotExist( rh, containerHost, backup );
                }
                catch ( ResourceHostException | PeerException e )
                {
                    throw new ResourceHostException( "Failed to create snapshot to take backup from: " + e.getMessage(),
                            e );
                }

                // do backup
                try
                {
                    pathToBackupFile = rh.saveContainerFilesystem( containerHost,
                            backup.isIncremental() ? backup.getFromSnapshotLabel() : backup.getSnapshotLabel(),
                            backup.isIncremental() ? backup.getSnapshotLabel() : null, null );
                    log.debug( "backup of container {} is saved in {}", backup.getContainerId(), pathToBackupFile );
                }
                catch ( ResourceHostException e )
                {
                    throw new ResourceHostException( "Failed to export container file system: " + e.getMessage(), e );
                }

                // encrypt backup file
                String password = Utils.generatePassword( 8, 15, true, true, true );

                try
                {
                    pathToEncryptedBackupFile = rh.encryptFile( pathToBackupFile, password );
                    log.debug( "encrypted backup is saved in {}", pathToEncryptedBackupFile );
                }
                catch ( ResourceHostException e )
                {
                    throw new ResourceHostException( "Failed to encrypt backup file: " + e.getMessage(), e );
                }

                // upload backup to CDN
                try
                {
                    String fileIdOnCdn = rh.uploadRawFileToCdn( pathToEncryptedBackupFile, cdnToken );
                    backup.getResult().setBackupCdnId( fileIdOnCdn );
                    backup.getResult().setEncryptionPassword( password );
                    log.debug( "backup CDN ID is {}", fileIdOnCdn );
                }
                catch ( ResourceHostException e )
                {
                    throw new ResourceHostException( "Failed to upload backup to CDN: " + e.getMessage(), e );
                }

                // cleanup
                if ( backup.isIncremental() && backup.isRemoveStartingSnapshot() )
                {
                    removeSnapshotAsync( containerHost, backup.getFromSnapshotLabel() );
                }
                if ( backup.isRemoveCreatedSnapshots() && isNewTempSnapshotCreated )
                {
                    removeSnapshotAsync( containerHost, backup.getSnapshotLabel() );
                }
            }
            catch ( Exception e )
            {
                log.error( "Failed to take backup: {}", e.getMessage(), e );
                backup.getResult().setError( e.getMessage() );
            }
            finally
            {
                // cleanup
                try
                {
                    if ( !backup.isKeepBackupFiles() )
                    {
                        if ( pathToBackupFile != null )
                        {
                            removeBackupFileAsync( rh, pathToBackupFile );
                        }
                        if ( pathToEncryptedBackupFile != null )
                        {
                            removeBackupFileAsync( rh, pathToEncryptedBackupFile );
                        }
                    }
                }
                catch ( Exception e )
                {
                    log.error( "Error deleting backup files", e );
                }
            }
        }
    }


    private Snapshot findSnapshot( ResourceHost rh, ContainerHost containerHost, String label )
            throws ResourceHostException
    {
        Snapshots snapshots = rh.listContainerHostSnapshots( containerHost );

        for ( final Snapshot snapshot : snapshots.getSnapshots() )
        {
            if ( snapshot.getLabel().equals( label ) )
            {
                return snapshot;
            }
        }

        return null;
    }


    private boolean createSnapshotIfNotExist( ResourceHost rh, ContainerHost containerHost,
                                              ContainerBackupCommandDto backup )
            throws ResourceHostException, PeerException
    {
        Snapshot snapshot = null;

        if ( StringUtils.isBlank( backup.getSnapshotLabel() ) )
        {
            backup.setSnapshotLabel( generateBackupSnapshotLabel( new Date() ) );
        }
        else
        {
            snapshot = findSnapshot( rh, containerHost, backup.getSnapshotLabel() );
        }

        if ( snapshot == null )
        {
            log.debug( "Snapshot {} for backup of container {} doesn't exist. Creating.", backup.getSnapshotLabel(),
                    containerHost.getContainerName() );
            ctx.localPeer
                    .addContainerSnapshot( containerHost.getContainerId(), DEFAULT_PARTITION, backup.getSnapshotLabel(),
                            true );

            snapshot = findSnapshot( rh, containerHost, backup.getSnapshotLabel() );
            backup.getResult().setSnapshotDate( snapshot.getCreated() );

            return true;
        }

        return false;
    }


    private String generateBackupSnapshotLabel( Date toDate )
    {
        return "backup-" + new SimpleDateFormat( "yyyyMMdd-HHmmss" ).format( toDate );
    }


    private void removeBackupFileAsync( final ResourceHost rh, final String pathToFile )
    {
        executor.execute( new Runnable()
        {
            @Override
            public void run()
            {
                log.debug( "Removing backup file: {}", pathToFile );
                try
                {
                    ctx.localPeer.execute( new RequestBuilder( String.format( "rm %s", pathToFile ) ), rh, null );
                }
                catch ( CommandException e )
                {
                    log.error( "Failed to remove backup file {}", pathToFile, e );
                }
            }
        } );
    }


    private void removeSnapshotAsync( final ContainerHost containerHost, final String createdTempSnapshotLabel )
    {
        executor.execute( new Runnable()
        {
            @Override
            public void run()
            {
                log.debug( "Removing temp snapshot {} of container {}", createdTempSnapshotLabel,
                        containerHost.getContainerName() );
                try
                {
                    ctx.localPeer.removeContainerSnapshot( containerHost.getContainerId(), DEFAULT_PARTITION,
                            createdTempSnapshotLabel );
                }
                catch ( PeerException e )
                {
                    log.error( "Failed to remove snapshot {} by container {}", createdTempSnapshotLabel,
                            containerHost.getContainerName(), e );
                }
            }
        } );
    }


    @Override
    protected RestResult<Object> post( EnvironmentPeerDto peerDto, Object body )
    {
        return ctx.restClient.post( path( path, peerDto ), body );
    }
}
