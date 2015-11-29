package io.subutai.core.kurjun.rest;


import javax.ws.rs.core.Response;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;


public abstract class RestManagerBase
{

    protected abstract Logger getLogger();


    /**
     * Decodes supplied md5 checksum to binary. If supplied md5 is invalid,
     * {@code null} is returned without throwing exceptions. This method is
     * useful for request handling methods that expect md5 checksum values.
     *
     * @param md5 strin md5 checksum
     * @return binary form of md5 checksum
     */
    protected byte[] decodeMd5( String md5 )
    {
        if ( md5 != null )
        {
            try
            {
                return Hex.decodeHex( md5.toCharArray() );
            }
            catch ( DecoderException ex )
            {
                getLogger().error( "Invalid md5 checksum", ex );
            }
        }
        return null;
    }


    protected Response badRequest( String msg )
    {
        return Response.status( Response.Status.BAD_REQUEST ).entity( msg ).build();
    }


    protected Response notFoundResponse( String msg )
    {
        return Response.status( Response.Status.NOT_FOUND ).entity( msg ).build();
    }


    protected Response packageNotFoundResponse()
    {
        return notFoundResponse( "Package not found." );
    }


    protected Response forbiddenResponse( String msg )
    {
        return Response.status( Response.Status.FORBIDDEN ).entity( msg ).build();
    }


    protected Response forbiddenResponse()
    {
        return forbiddenResponse( "No permission." );
    }
}
