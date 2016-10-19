package io.subutai.hub.share.dto.host;


import java.util.Objects;
import java.util.Set;


/**
 * Holds a batch of resource host command requests from Hub
 */
public class ResourceHostCommandBatchDto
{
    private Set<ResourceHostCommandRequestDto> resourceHostCommandRequestDtos;


    protected ResourceHostCommandBatchDto()
    {
    }


    public ResourceHostCommandBatchDto( final Set<ResourceHostCommandRequestDto> resourceHostCommandRequestDtos )
    {
        Objects.requireNonNull( resourceHostCommandRequestDtos );

        if ( resourceHostCommandRequestDtos.isEmpty() )
        {
            throw new IllegalArgumentException();
        }

        this.resourceHostCommandRequestDtos = resourceHostCommandRequestDtos;
    }


    public Set<ResourceHostCommandRequestDto> getResourceHostCommandRequestDtos()
    {
        return resourceHostCommandRequestDtos;
    }


    @Override
    public String toString()
    {
        final StringBuffer sb = new StringBuffer( "ResourceHostCommandBatchDto{" );
        sb.append( "resourceHostCommandRequestDtos=" ).append( resourceHostCommandRequestDtos );
        sb.append( '}' );
        return sb.toString();
    }
}
