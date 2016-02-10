package io.subutai.core.systemmanager.rest.pojo;


/**
 * Created by ermek on 2/6/16.
 */
public class PeerSettingsPojo
{
    private String externalIpInterface;
    private String encryptionState;
    private String restEncryptionState;
    private String integrationState;
    private String keyTrustCheckState;

    public String getExternalIpInterface()
    {
        return externalIpInterface;
    }


    public void setExternalIpInterface( final String externalIpInterface )
    {
        this.externalIpInterface = externalIpInterface;
    }


    public String getEncryptionState()
    {
        return encryptionState;
    }


    public void setEncryptionState( final String encryptionState )
    {
        this.encryptionState = encryptionState;
    }


    public String getRestEncryptionState()
    {
        return restEncryptionState;
    }


    public void setRestEncryptionState( final String restEncryptionState )
    {
        this.restEncryptionState = restEncryptionState;
    }


    public String getIntegrationState()
    {
        return integrationState;
    }


    public void setIntegrationState( final String integrationState )
    {
        this.integrationState = integrationState;
    }


    public String getKeyTrustCheckState()
    {
        return keyTrustCheckState;
    }


    public void setKeyTrustCheckState( final String keyTrustCheckState )
    {
        this.keyTrustCheckState = keyTrustCheckState;
    }
}
