package io.subutai.core.bazaarmanager.impl.snapshot;


import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.bazaar.share.dto.snapshots.SnapshotOperationDto;
import io.subutai.bazaar.share.dto.snapshots.SnapshotOperationsDto;
import io.subutai.common.peer.ContainerId;
import io.subutai.common.peer.PeerException;
import io.subutai.core.bazaarmanager.api.RestResult;
import io.subutai.core.bazaarmanager.api.StateLinkProcessor;
import io.subutai.core.bazaarmanager.api.exception.BazaarManagerException;
import io.subutai.core.bazaarmanager.impl.environment.state.Context;


public class SnapshotProcessor implements StateLinkProcessor
{
    private final Logger log = LoggerFactory.getLogger( getClass() );

    private final String WHOLE_CONTAINER_SNAPSHOT_LABEL = "config";

    private Context ctx;


    public SnapshotProcessor( final Context ctx )
    {
        this.ctx = ctx;
    }


    @Override
    public boolean processStateLinks( final Set<String> stateLinks ) throws BazaarManagerException
    {
        for ( final String stateLink : stateLinks )
        {
            if ( stateLink.contains( "/snapshots" ) )
            {
                processSnapshotCommands( stateLink );
            }
        }

        return false;
    }


    private void processSnapshotCommands( String stateLink )
    {
        RestResult<SnapshotOperationsDto> restResult = ctx.restClient.get( stateLink, SnapshotOperationsDto.class );

        if ( !restResult.isSuccess() )
        {
            log.error( "Failed to get data from Bazaar: {}", restResult.getError() );
            return;
        }

        SnapshotOperationsDto operationsDto = restResult.getEntity();

        ContainerId containerId;

        for ( final SnapshotOperationDto operation : operationsDto.getOperations() )
        {
            containerId = new ContainerId( operation.getContainerId() );

            try
            {
                switch ( operation.getCommand() )
                {
                    case CREATE:
                        ctx.localPeer.addContainerSnapshot( containerId, WHOLE_CONTAINER_SNAPSHOT_LABEL,
                                operation.getSnapshot().getLabel(), operation.isStopContainer() );
                        break;
                    case DELETE:
                        ctx.localPeer.removeContainerSnapshot( containerId, WHOLE_CONTAINER_SNAPSHOT_LABEL,
                                operation.getSnapshot().getLabel() );
                        break;
                    case ROLLBACK:
                        ctx.localPeer.rollbackToContainerSnapshot( containerId, WHOLE_CONTAINER_SNAPSHOT_LABEL,
                                operation.getSnapshot().getLabel(), true );
                        break;
                }

                operation.setSuccessful( true );
            }
            catch ( PeerException e )
            {
                log.error( "Failed to process snapshot operation {}: {}", operation, e.getMessage(), e );

                operation.setSuccessful( false );
                operation.setErrors( e.getMessage() );
            }
        }
    }
}
