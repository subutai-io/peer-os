package io.subutai.hub.share.dto.environment.container;


import java.util.Objects;
import java.util.Set;

import com.google.common.collect.Sets;


/**
 * Holds a batch of container command requests from Hub
 */
public class ContainerCommandBatchDto
{
    Set<ContainerCommandRequestDto> commandRequestDtos = Sets.newHashSet();


    public ContainerCommandBatchDto( final ContainerCommandRequestDto containerCommandRequestDto )
    {
        Objects.requireNonNull( containerCommandRequestDto, "Invalid container command dto" );

        commandRequestDtos.add( containerCommandRequestDto );
    }


    public void addCommandRequest( ContainerCommandRequestDto containerCommandRequestDto )
    {
        Objects.requireNonNull( containerCommandRequestDto, "Invalid container command dto" );

        commandRequestDtos.add( containerCommandRequestDto );
    }


    public Set<ContainerCommandRequestDto> getCommandRequestDtos()
    {
        return commandRequestDtos;
    }
}
