package io.subutai.core.keyserver.rest;


import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.security.crypto.pgp.PGPKeyUtil;
import io.subutai.common.util.JsonUtil;
import io.subutai.core.keyserver.api.KeyServer;
import io.subutai.core.keyserver.api.model.SecurityKey;


/**
 * Implementation of Key Server Rest
 */
public class KeyServerRestImpl implements KeyServerRest
{

    private static final Logger LOG = LoggerFactory.getLogger( KeyServerRestImpl.class.getName() );

    // Keyserver service
    private KeyServer keyServer;


    /********************************
     *
     */
    public KeyServerRestImpl( KeyServer keyServer )
    {
        this.keyServer = keyServer;
    }


    /********************************
     *
     */
    @Override
    public Response addKey( String[] keyTexts )
    {
        if ( keyTexts != null )
        {
            try
            {
                for ( String keyText : keyTexts )
                {
                    keyServer.addSecurityKey( keyText );
                }

                return Response.ok().build();
            }
            catch ( Exception ex )
            {
                return Response.status( Response.Status.BAD_REQUEST ).build();
            }
        }
        else
        {
            return Response.status( Response.Status.BAD_REQUEST ).entity( "Invalid input parameters" ).build();
        }
    }


    /********************************
     *
     */
    @Override
    public Response lookupKey( String operation, UriInfo uriInfo )
    {

        String searchParam;
        searchParam = uriInfo.getQueryParameters().getFirst( "search" );

        if ( operation == null )
        {
            return Response.status( Response.Status.NOT_IMPLEMENTED ).entity( "Invalid {operation} parameter" ).build();
        }
        else if ( searchParam == null )
        {
            return Response.status( Response.Status.NOT_IMPLEMENTED ).entity( "Invalid {search} parameter" ).build();
        }
        else
        {

            if ( KeyServerParams.HKP_OPERATION_GET.equalsIgnoreCase( operation ) )
            {
                return handleGetOperation(searchParam);
            }
            else if ( KeyServerParams.HKP_OPERATION_INDEX.equalsIgnoreCase( operation ) )
            {

            }
            else if ( KeyServerParams.HKP_OPERATION_VINDEX.equalsIgnoreCase( operation ) )
            {

            }

            return Response.ok( searchParam ).build();
        }
    }


    /********************************
     *     */
    @Override
    public Response saveSecurityKey( String keyId, String fingerprint, short keyType, String keyData )
    {
        keyServer.saveSecurityKey( keyId, fingerprint, keyType, keyData.getBytes() );

        return Response.ok().build();
    }


    /********************************
     *
     */
    @Override
    public Response getSecurityKey( String keyId )
    {
        SecurityKey securityKey = keyServer.getSecurityKey( keyId );

        return Response.ok( JsonUtil.toJson( securityKey ) ).build();
    }


    /* *******************************
     *
     */
    @Override
    public Response getSecurityKeys()
    {
        List<SecurityKey> securityKeyList = keyServer.getSecurityKeyList();

        return Response.ok( JsonUtil.toJson( securityKeyList ) ).build();
    }


    /* *******************************
     *
     */
    @Override
    public Response removeSecurityKey( String keyId )
    {
        keyServer.removeSecurityKeyByKeyId( keyId );

        return Response.ok().build();
    }


    /* *******************************
     *
     */
    public KeyServer getKeyServer()
    {
        return keyServer;
    }


    /* *******************************
     *
     */
    public void setKeyServer( KeyServer keyServer )
    {
        this.keyServer = keyServer;
    }


    /* *******************************
     *
     */
    private Response handleGetOperation( String searchParam )
    {
        if ( searchParam.length() > 2 && "0x".equalsIgnoreCase( searchParam.substring( 0, 2 ) ) )
        {
            if ( searchParam.substring( 2 ).length() == PGPKeyUtil.HEX_V3_FINGERPRINT_LENGTH )
            {
                return Response.status( Response.Status.NOT_IMPLEMENTED )
                               .entity( "Search by V3 fingerprint is not supported" ).build();
            }
            else
            {
                //remove 0x
                searchParam = searchParam.substring( 2 );

                if ( PGPKeyUtil.isLongKeyId( searchParam ) )
                {
                    return getKey( searchParam, ( short ) 1 );
                }
                else if ( PGPKeyUtil.isFingerprint( searchParam ) )
                {
                    return getKey( searchParam, (short)2);
                }
                else if ( PGPKeyUtil.isShortKeyId( searchParam ) )
                {
                    return getKey( searchParam, (short)3);
                }
                else
                {
                    return Response.status( Response.Status.NOT_IMPLEMENTED ).entity( "Invalid input parameter" ).build();
                }
           }
        }
        else
        {
            return Response.status( Response.Status.NOT_IMPLEMENTED ).entity( "Invalid input parameter" ).build();
        }
    }


    /* *******************************
     *
     */
    private Response getKey( String keyId,short searchBy )
    {
        SecurityKey securityKey = null;

        try
        {
            if(searchBy == 1)
            {
                securityKey = keyServer.getSecurityKey( keyId );
            }
            else if(searchBy == 2)
            {
                securityKey = keyServer.getSecurityKeyByFingerprint( keyId );
            }
            else if(searchBy == 3)
            {
                securityKey = keyServer.getSecurityKeyByShortKeyId( keyId );
            }
            else
            {
                return Response.status( Response.Status.NOT_IMPLEMENTED ).entity( "Invalid input parameter" ).build();
            }


            if(securityKey!=null)
            {
                return Response.ok( PGPKeyUtil.exportAscii( keyServer.convertKey(securityKey))).build();
            }
            else
            {
                return Response.status( Response.Status.NOT_FOUND ).entity( "Key not found" ).build();
            }
        }
        catch(Exception ex)
        {
            return Response.status( Response.Status.NOT_IMPLEMENTED ).entity( "Invalid input parameter" ).build();
        }
    }


    /* *******************************
     *
     */
    private Response handleIndexOperation( String searchParam )
    {
        return null;
    }
}
