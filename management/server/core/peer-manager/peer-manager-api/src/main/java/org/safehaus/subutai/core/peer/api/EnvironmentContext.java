package org.safehaus.subutai.core.peer.api;


import java.util.UUID;


/**
 * Environment context
 */
public class EnvironmentContext
{
    private UUID environmentId;
    private String gpgKey;
    private String x509Cert;


    public UUID getEnvironmentId()
    {
        return environmentId;
    }


    public void setEnvironmentId( final UUID environmentId )
    {
        this.environmentId = environmentId;
    }


    public String getGpgKey()
    {
        return gpgKey;
    }


    public void setGpgKey( final String gpgKey )
    {
        this.gpgKey = gpgKey;
    }


    public String getX509Cert()
    {
        return x509Cert;
    }


    public void setX509Cert( final String x509Cert )
    {
        this.x509Cert = x509Cert;
    }
}
