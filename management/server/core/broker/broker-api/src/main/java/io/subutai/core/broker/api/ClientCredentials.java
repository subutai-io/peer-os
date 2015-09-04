package io.subutai.core.broker.api;


/**
 * DTO object that contains client,crt, client.key and ca.crt for a client to connect to broker via SSL
 */
public class ClientCredentials
{
    private String clientCertificate;
    private String clientKey;
    private String caCertificate;


    public ClientCredentials( final String clientCertificate, final String clientKey, final String caCertificate )
    {
        this.clientCertificate = clientCertificate;
        this.clientKey = clientKey;
        this.caCertificate = caCertificate;
    }


    public String getClientCertificate()
    {
        return clientCertificate;
    }


    public String getClientKey()
    {
        return clientKey;
    }


    public String getCaCertificate()
    {
        return caCertificate;
    }
}
