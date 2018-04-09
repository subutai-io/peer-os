package io.subutai.core.environment.metadata.api;


import io.subutai.core.identity.api.exception.TokenCreateException;
import io.subutai.hub.share.dto.environment.EnvironmentInfoDto;
import io.subutai.hub.share.event.Event;


public interface EnvironmentMetadataManager
{
    void init();

    void dispose();

    /**
     * Issues JWT token for specified container
     */

    void issueToken( String containerIp ) throws TokenCreateException;

    EnvironmentInfoDto getEnvironmentInfoDto( String environmentId );

    /***
     * Pushes event to consumers
     * @param event @see Event
     */
    void pushEvent( Event event );
}
