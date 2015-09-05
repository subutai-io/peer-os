package io.subutai.core.security.rest;


import javax.ws.rs.core.Response;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.base.Strings;

import io.subutai.common.security.crypto.pgp.PGPKeyUtil;
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
    public Response addPublicKeyRing( final String hostId,final String keyText )
    {
        securityManager.getKeyManager().savePublicKeyRing( hostId, (short)3, keyText );

        return Response.ok().build();
    }


    /* ******************************
     *
     */
    @Override
    public Response removePublicKeyRing( final String hostId )
    {
        securityManager.getKeyManager().removePublicKeyRing( hostId );

        return Response.ok().build();
    }


    /* ******************************
     *
     */
    @Override
    public Response getPublicKeyRing( final String hostId )
    {
        String key = securityManager.getKeyManager().getPublicKeyRingAsASCII( hostId );

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
        PGPPublicKey key = securityManager.getKeyManager().getPublicKeyRing( hostId).getPublicKey();

        if ( key == null )
        {
            return Response.status( Response.Status.NOT_FOUND ).entity( "Object Not found" ).build();
        }
        else
        {
            return Response.ok( PGPKeyUtil.encodeNumericKeyId( key.getKeyID())).build();
        }
    }


    /* ******************************
     *
     */
    @Override
    public Response getPublicKeyFingerprint( final String hostId )
    {
        PGPPublicKey key = securityManager.getKeyManager().getPublicKeyRing( hostId).getPublicKey();

        if ( key == null )
        {
            return Response.status( Response.Status.NOT_FOUND ).entity( "Object Not found" ).build();
        }
        else
        {
            return Response.ok( PGPKeyUtil.getFingerprint( key.getFingerprint())).build();
        }
    }
}
