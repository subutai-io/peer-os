package io.subutai.hub.share.dto.environment.container;


import java.util.Set;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;


/**
 * Holds a batch of container command requests from Hub
 */
public class ContainerCommandBatchDto
{
    private final Set<ContainerCommandRequestDto> commandRequestDtos;


    public ContainerCommandBatchDto( final Set<ContainerCommandRequestDto> containerCommandRequestDtos )
    {
        Preconditions.checkNotNull( containerCommandRequestDtos );
        Preconditions.checkArgument( !containerCommandRequestDtos.isEmpty() );

        commandRequestDtos = containerCommandRequestDtos;
    }


    public Set<ContainerCommandRequestDto> getCommandRequestDtos()
    {
        return commandRequestDtos;
    }


    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper( this ).add( "commandRequestDtos", commandRequestDtos ).toString();
    }
}
