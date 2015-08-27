package io.subutai.core.security.rest;


import java.io.InputStream;
import javax.ws.rs.core.Response;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.base.Strings;
import io.subutai.common.security.crypto.pgp.PGPEncryptionUtil;
import io.subutai.core.security.api.SecurityManager;


/**
 * Implementation of Key Server Rest
 */
public class SecurityManagerRestImpl implements SecurityManagerRest
{

    private static final Logger LOG = LoggerFactory.getLogger( SecurityManagerRestImpl.class.getName() );

    // SecurityManager service
    private SecurityManager securityManager;

    /* ******************************
     *
     */
    public SecurityManagerRestImpl(SecurityManager securityManager)
    {
        this.securityManager = securityManager;
    }


    /* ******************************
     *
     */
    @Override
    public Response addPublicKey( final String hostId,final String keyText )
    {
        securityManager.getKeyManager().savePublicKey(hostId,keyText);

        return Response.ok().build();
    }


    /* ******************************
     *
     */
    @Override
    public Response addSecurityKey( final String hostId,final String keyText, short keyType)
    {
        securityManager.getKeyManager().savePublicKey(hostId,keyText);

        return Response.ok().build();
    }

    /* ******************************
     *
     */
    @Override
    public Response removePublicKey( final String hostId )
    {
        securityManager.getKeyManager().removePublicKey( hostId );

        return Response.ok().build();
    }


    /* ******************************
     *
     */
    @Override
    public Response getPublicKey( final String hostId )
    {
        String key = securityManager.getKeyManager().getPublicKeyAsASCII( hostId);

        if ( Strings.isNullOrEmpty( key ) )
        {
            return Response.status( Response.Status.NOT_FOUND ).entity( "Object Not found" ).build();
        }
        else
        {
            return Response.ok( key).build();
        }
    }

    /* ******************************
     *
     */
    @Override
    public Response getPublicKeyData( final String hostId )
    {
        String key = securityManager.getKeyManager().getPublicKeyDataAsASCII( hostId);

        if ( Strings.isNullOrEmpty( key ) )
        {
            return Response.status( Response.Status.NOT_FOUND ).entity( "Object Not found" ).build();
        }
        else
        {
            return Response.ok( key).build();
        }
    }


    /* ******************************
     *
     */
    @Override
    public Response getPublicKeyId( final String hostId )
    {
        PGPPublicKey key = securityManager.getKeyManager().getPublicKey(hostId);

        if ( key == null )
        {
            return Response.status( Response.Status.NOT_FOUND ).entity( "Object Not found" ).build();
        }
        else
        {
            return Response.ok( key.getKeyID()).build();
        }
    }


    /* ******************************
     *
     */
    @Override
    public Response getPublicKeyFingerprint( final String hostId )
    {
        PGPPublicKey key = securityManager.getKeyManager().getPublicKey(hostId);

        if ( key == null )
        {
            return Response.status( Response.Status.NOT_FOUND ).entity( "Object Not found" ).build();
        }
        else
        {
            return Response.ok( key.getFingerprint()).build();
        }
    }
}
