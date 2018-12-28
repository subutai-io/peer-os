package io.subutai.core.environment.metadata.api;


import io.subutai.bazaar.share.dto.environment.EnvironmentInfoDto;
import io.subutai.bazaar.share.events.Event;
import io.subutai.core.identity.api.exception.TokenCreateException;


public interface EnvironmentMetadataManager
{
    void init();

    void dispose();

    /**
     * Issues JWT token for specified container
     */

    void issueToken( String containerIp ) throws TokenCreateException;

    EnvironmentInfoDto getEnvironmentInfoDto( String environmentId );

    void pushEvent( Event event );
}
