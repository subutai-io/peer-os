package io.subutai.core.systemmanager.api.pojo;


/**
 * Created by ermek on 2/6/16.
 */
public interface SecuritySettings
{
    public boolean getEncryptionState();

    public void setEncryptionState( final boolean encryptionState );

    public boolean getRestEncryptionState();

    public void setRestEncryptionState( final boolean restEncryptionState );

    public boolean getIntegrationState();

    public void setIntegrationState( final boolean integrationState );

    public boolean getKeyTrustCheckState();

    public void setKeyTrustCheckState( final boolean keyTrustCheckState );
}
