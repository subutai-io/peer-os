package io.subutai.core.systemmanager.impl.pojo;


import io.subutai.core.systemmanager.api.pojo.PeerSettings;


/**
 * Created by ermek on 2/6/16.
 */
public class PeerSettingsPojo implements PeerSettings
{
    private String externalIpInterface;
    private boolean encryptionState;
    private boolean restEncryptionState;
    private boolean integrationState;
    private boolean keyTrustCheckState;


    public String getExternalIpInterface()
    {
        return externalIpInterface;
    }


    public void setExternalIpInterface( final String externalIpInterface )
    {
        this.externalIpInterface = externalIpInterface;
    }


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
