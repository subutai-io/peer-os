package io.subutai.core.environment.metadata.api;


public interface EnvironmentMetadataManager {

    /**
     * Issues JWT token for specified container
     */

    void issueToken(String containerIp);

}
