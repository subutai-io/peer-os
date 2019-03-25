package io.subutai.core.bazaarmanager.impl.environment.state.snapshot;


import io.subutai.bazaar.share.dto.environment.EnvironmentPeerDto;
import io.subutai.bazaar.share.dto.snapshots.SnapshotOperationDto;
import io.subutai.bazaar.share.dto.snapshots.SnapshotOperationsDto;
import io.subutai.common.peer.ContainerId;
import io.subutai.common.peer.PeerException;
import io.subutai.core.bazaarmanager.api.RestResult;
import io.subutai.core.bazaarmanager.api.exception.BazaarManagerException;
import io.subutai.core.bazaarmanager.impl.environment.state.Context;
import io.subutai.core.bazaarmanager.impl.environment.state.StateHandler;


public class ProcessSnapshotStateHandler extends StateHandler
{
    private final String path = "/rest/v1/environments/%s/peers/%s/snapshots/operations";


    public ProcessSnapshotStateHandler( Context ctx )
    {
        super( ctx, "Container snapshot processor" );
    }


    @Override
    protected Object doHandle( final EnvironmentPeerDto peerDto ) throws BazaarManagerException
    {
        SnapshotOperationsDto operationsDto =
                ctx.restClient.getStrict( path( path, peerDto ), SnapshotOperationsDto.class );

        ContainerId containerId;

        for ( final SnapshotOperationDto operation : operationsDto.getOperations() )
        {
            containerId = new ContainerId( operation.getContainerId() );

            try
            {
                switch ( operation.getCommand() )
                {
                    case CREATE:
                        ctx.localPeer.addContainerSnapshot( containerId, operation.getPartition(), operation.getLabel(),
                                operation.isStopContainer() );
                        break;
                    case DELETE:
                        ctx.localPeer
                                .removeContainerSnapshot( containerId, operation.getPartition(), operation.getLabel() );
                        break;
                    case ROLLBACK:
                        ctx.localPeer.rollbackToContainerSnapshot( containerId, operation.getPartition(),
                                operation.getLabel(), operation.isForceRollback() );
                        break;
                }

                operation.setSuccessful( true );
            }
            catch ( PeerException e )
            {
                log.error( "Failed to process snapshot operation {}: {}", operation, e.getMessage(), e );

                operation.setSuccessful( false );
                operation.setErrors( e.getMessage() );

                break;
            }
        }

        return operationsDto;
    }


    @Override
    protected RestResult<Object> post( EnvironmentPeerDto peerDto, Object body )
    {
        return ctx.restClient.post( path( path, peerDto ), body );
    }
}
