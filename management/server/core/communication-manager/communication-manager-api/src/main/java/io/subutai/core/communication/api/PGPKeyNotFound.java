package io.subutai.core.communication.api;


/**
 * Exception throws on GPG key not found
 */
public class PGPKeyNotFound extends Exception
{
    public PGPKeyNotFound( final String filePath )
    {
        super( filePath );
    }
}
