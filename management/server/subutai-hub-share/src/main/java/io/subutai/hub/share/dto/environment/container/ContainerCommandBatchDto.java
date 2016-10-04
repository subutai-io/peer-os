package io.subutai.hub.share.dto.environment.container;


import java.util.Objects;
import java.util.Set;


/**
 * Holds a batch of container command requests from Hub
 */
public class ContainerCommandBatchDto
{
    private Set<ContainerCommandRequestDto> commandRequestDtos;


    protected ContainerCommandBatchDto()
    {
    }


    public ContainerCommandBatchDto( final Set<ContainerCommandRequestDto> containerCommandRequestDtos )
    {
        Objects.requireNonNull( containerCommandRequestDtos );
        if ( containerCommandRequestDtos.isEmpty() )
        {
            throw new IllegalArgumentException();
        }

        commandRequestDtos = containerCommandRequestDtos;
    }


    public Set<ContainerCommandRequestDto> getCommandRequestDtos()
    {
        return commandRequestDtos;
    }


    @Override
    public String toString()
    {
        final StringBuffer sb = new StringBuffer( "ContainerCommandBatchDto{" );
        sb.append( "commandRequestDtos=" ).append( commandRequestDtos );
        sb.append( '}' );
        return sb.toString();
    }
}
