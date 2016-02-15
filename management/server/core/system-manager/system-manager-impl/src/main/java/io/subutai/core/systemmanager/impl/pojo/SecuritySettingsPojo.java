package io.subutai.core.systemmanager.impl.pojo;


import io.subutai.core.systemmanager.api.pojo.SecuritySettings;


/**
 * Created by ermek on 2/6/16.
 */
public class SecuritySettingsPojo implements SecuritySettings
{
    private boolean encryptionState;
    private boolean restEncryptionState;
    private boolean integrationState;
    private boolean keyTrustCheckState;


    public boolean getEncryptionState()
    {
        return encryptionState;
    }


    public void setEncryptionState( final boolean encryptionState )
    {
        this.encryptionState = encryptionState;
    }


    public boolean getRestEncryptionState()
    {
        return restEncryptionState;
    }


    public void setRestEncryptionState( final boolean restEncryptionState )
    {
        this.restEncryptionState = restEncryptionState;
    }


    public boolean getIntegrationState()
    {
        return integrationState;
    }


    public void setIntegrationState( final boolean integrationState )
    {
        this.integrationState = integrationState;
    }


    public boolean getKeyTrustCheckState()
    {
        return keyTrustCheckState;
    }


    public void setKeyTrustCheckState( final boolean keyTrustCheckState )
    {
        this.keyTrustCheckState = keyTrustCheckState;
    }
}
