package io.subutai.core.hubmanager.impl.environment;


import java.util.concurrent.CompletionService;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;

import io.subutai.common.environment.CreateEnvironmentContainerGroupRequest;
import io.subutai.common.environment.CreateEnvironmentContainerResponseCollector;
import io.subutai.common.host.HostArchitecture;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.task.CloneRequest;
import io.subutai.common.task.CloneResponse;


class ContainerCloneHelper extends Helper
{
    ContainerCloneHelper( LocalPeer localPeer )
    {
        super( localPeer );
    }


    @Override
    void execute( PeerEnvironmentDto dto ) throws PeerException
    {
        final CreateEnvironmentContainerGroupRequest request = new CreateEnvironmentContainerGroupRequest( dto.getEnvironmentId() );

        CloneRequest cloneRequest = new CloneRequest(
                getFirstResourceHostId( localPeer ),
                dto.getContainerHostname(),
                dto.getContainerName(),
                dto.getContainerIp() + "/24",
                dto.getEnvironmentId(),
                localPeer.getId(),
                localPeer.getOwnerId(),
                dto.getTemplateName(),
                HostArchitecture.AMD64,
                dto.getContainerSize()
        );

        request.addRequest( cloneRequest );

        CreateEnvironmentContainerResponseCollector response = localPeer.createEnvironmentContainerGroup( request );

        for ( CloneResponse cloneResponse : response.getResponses() )
        {
            log.debug( "{}", cloneResponse );
        }
    }

    protected CompletionService<CreateEnvironmentContainerResponseCollector> getEnvCompletionService( Executor executor )
    {
        return new ExecutorCompletionService<>( executor );
    }
}
