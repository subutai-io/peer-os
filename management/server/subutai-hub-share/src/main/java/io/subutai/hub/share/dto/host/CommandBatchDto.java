package io.subutai.hub.share.dto.host;


import java.util.Objects;
import java.util.Set;


/**
 * Holds a batch of host command requests from Hub
 */
public class CommandBatchDto
{
    private Set<CommandRequestDto> commandRequestDtos;


    protected CommandBatchDto()
    {
    }


    public CommandBatchDto( final Set<CommandRequestDto> commandRequestDtos )
    {
        Objects.requireNonNull( commandRequestDtos );
        if ( commandRequestDtos.isEmpty() )
        {
            throw new IllegalArgumentException();
        }

        this.commandRequestDtos = commandRequestDtos;
    }


    public Set<CommandRequestDto> getCommandRequestDtos()
    {
        return commandRequestDtos;
    }


    @Override
    public String toString()
    {
        final StringBuffer sb = new StringBuffer( "CommandBatchDto{" );
        sb.append( "CommandBatchDto=" ).append( commandRequestDtos );
        sb.append( '}' );
        return sb.toString();
    }
}
