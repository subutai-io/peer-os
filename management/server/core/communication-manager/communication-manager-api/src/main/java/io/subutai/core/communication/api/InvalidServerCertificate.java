package io.subutai.core.communication.api;


/**
 * Exception throws on server x509 certificate is invalid
 */
public class InvalidServerCertificate extends Exception
{
    public InvalidServerCertificate( final String fingerPrint ) {super( fingerPrint );}
}
