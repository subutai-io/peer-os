package io.subutai.core.environment.metadata.api;


import io.subutai.bazaar.share.event.payload.Payload;
import io.subutai.core.identity.api.exception.TokenCreateException;


public interface EnvironmentMetadataManager
{
    void init();

    void dispose();

    /**
     * Issues JWT token for specified container
     */

    void issueToken( String containerIp ) throws TokenCreateException;

    Payload getEnvironmentInfoDto( String environmentId, String type );

    void pushEvent( Payload eventMessage );
}
