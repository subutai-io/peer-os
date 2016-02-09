package io.subutai.core.systemmanager.api.pojo;


/**
 * Created by ermek on 2/6/16.
 */
public interface PeerSettings
{
    public String getExternalIpInterface();

    public void setExternalIpInterface( final String externalIpInterface );

    public String getEncryptionState();

    public void setEncryptionState( final String encryptionState );

    public String getRestEncryptionState();

    public void setRestEncryptionState( final String restEncryptionState );

    public String getIntegrationState();

    public void setIntegrationState( final String integrationState );

    public String getKeyTrustCheckState();

    public void setKeyTrustCheckState( final String keyTrustCheckState );
}
