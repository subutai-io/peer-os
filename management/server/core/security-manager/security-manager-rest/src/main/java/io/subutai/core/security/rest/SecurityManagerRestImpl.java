package io.subutai.core.security.rest;


import javax.naming.NamingException;
import javax.ws.rs.FormParam;
import javax.ws.rs.core.Response;

import org.bouncycastle.openpgp.PGPPublicKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import io.subutai.common.security.crypto.pgp.PGPKeyUtil;
import io.subutai.common.util.JsonUtil;
import io.subutai.common.util.ServiceLocator;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.core.security.api.crypto.KeyManager;
import io.subutai.core.security.api.model.SecurityKeyIdentity;


/**
 * Implementation of Key Server Rest
 */
public class SecurityManagerRestImpl implements SecurityManagerRest
{

    private static final Logger logger = LoggerFactory.getLogger( SecurityManagerRestImpl.class );

    // SecurityManager service
    private SecurityManager securityManager;


    /* ******************************
     *
     */
    public SecurityManagerRestImpl( SecurityManager securityManager )
    {
        this.securityManager = securityManager;
    }


    /* ******************************
     *
     */
    @Override
    public Response addPublicKeyRing( final String hostId, final String keyText )
    {
        securityManager.getKeyManager().savePublicKeyRing( hostId, ( short ) 3, keyText );

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
            return Response.ok( key ).build();
        }
    }


    /* ******************************
     *
     */
    @Override
    public Response getPublicKeyId( final String hostId )
    {
        PGPPublicKey key = securityManager.getKeyManager().getPublicKeyRing( hostId ).getPublicKey();

        if ( key == null )
        {
            return Response.status( Response.Status.NOT_FOUND ).entity( "Object Not found" ).build();
        }
        else
        {
            return Response.ok( PGPKeyUtil.encodeNumericKeyId( key.getKeyID() ) ).build();
        }
    }


    /* ***********************************************************
     *
     */
    @Override
    public Response getUserKeyTrustTree()
    {
        try
        {
            IdentityManager identityManager = ServiceLocator.getServiceNoCache( IdentityManager.class );
            User user = null;
            if ( identityManager != null )
            {
                user = identityManager.getActiveUser();
                return getKeyTrustTree( user.getSecurityKeyId() );
            }
        }
        catch ( NamingException e )
        {
            logger.error( "Error getting identity manager.", e );
        }
        return null;
    }


    /* ******************************
     *
     */
    @Override
    public Response getPublicKeyFingerprint( final String hostId )
    {
        String fingerprint = securityManager.getKeyManager().getFingerprint( hostId );

        if ( fingerprint == null )
        {
            return Response.status( Response.Status.NOT_FOUND ).entity( "Object Not found" ).build();
        }
        else
        {
            return Response.ok( fingerprint ).build();
        }
    }


    /* ***********************************************************
     *
     */
    @Override
    public Response getKeyTrustTree( String hostId )
    {
        logger.debug( "Received hostId: " + hostId );
        KeyManager keyManager = securityManager.getKeyManager();
        SecurityKeyIdentity keyIdentity = keyManager.getKeyTrustTree( hostId );
        return Response.ok( JsonUtil.toJson( keyIdentity ) ).build();
    }


    /* ***********************************************************
     *
     */
    @Override
    public Response revokeKey( String hostId )
    {
        return null;
    }


    /* ***********************************************************
     *
     */
    @Override
    public Response signKey( final String sourceHostId, final String keyText, final int trustLevel )
    {
        String key = securityManager.getKeyManager().signPublicKey( sourceHostId, keyText, trustLevel );

        if ( Strings.isNullOrEmpty( key ) )
        {
            return Response.status( Response.Status.NOT_FOUND ).entity( "Object Not found" ).build();
        }
        else
        {
            return Response.ok( key ).build();
        }
    }
}
