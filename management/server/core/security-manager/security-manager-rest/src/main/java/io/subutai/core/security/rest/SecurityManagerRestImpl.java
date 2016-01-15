package io.subutai.core.security.rest;


import javax.naming.NamingException;
import javax.ws.rs.core.Response;

import org.bouncycastle.openpgp.PGPPublicKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import io.subutai.common.security.crypto.pgp.PGPKeyUtil;
import io.subutai.common.security.objects.KeyTrustLevel;
import io.subutai.common.util.JsonUtil;
import io.subutai.common.util.ServiceLocator;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.core.security.api.crypto.KeyManager;
import io.subutai.core.security.api.model.SecurityKey;
import io.subutai.core.security.rest.model.SecurityKeyData;


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
    public Response addPublicKeyRing( final String identityId, final String keyText )
    {
        securityManager.getKeyManager().savePublicKeyRing( identityId, ( short ) 3, keyText );

        return Response.ok().build();
    }


    /* ******************************
     *
     */
    @Override
    public Response removePublicKeyRing( final String identityId )
    {
        securityManager.getKeyManager().removePublicKeyRing( identityId );

        return Response.ok().build();
    }


    /* ******************************
     *
     */
    @Override
    public Response getPublicKeyRing( final String identityId )
    {
        String key = securityManager.getKeyManager().getPublicKeyRingAsASCII( identityId );

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
    public Response getPublicKeyId( final String identityId )
    {
        PGPPublicKey key = securityManager.getKeyManager().getPublicKeyRing( identityId ).getPublicKey();

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
    public Response getPublicKeyFingerprint( final String identityId )
    {
        String fingerprint = securityManager.getKeyManager().getFingerprint( identityId );

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
    public Response getKeyTrustTree( String identityId )
    {
        try
        {
            logger.debug( "Received identityId: " + identityId );

            KeyManager keyManager = securityManager.getKeyManager();
            SecurityKey securityKey = keyManager.getKeyTrustTree( identityId );

            return Response.ok( JsonUtil.toJson( securityKey ) ).build();
        }
        catch ( Exception ex )
        {
            return Response.serverError().build();
        }
    }


    /* ***********************************************************
     *
     */
    @Override
    public Response revokeKey( String sourceFingerprint, String targetFingerprint )
    {
        try
        {
            KeyManager keyManager = securityManager.getKeyManager();
            keyManager.setKeyTrust( sourceFingerprint, targetFingerprint, KeyTrustLevel.Never.getId() );
        }
        catch ( Exception e )
        {
            return Response.serverError().build();
        }
        return Response.ok().build();
    }



    /* ***********************************************************
     *
     */
    @Override
    public Response setTrust( String source, String target, int level )
    {
        try
        {
            KeyManager keyManager = securityManager.getKeyManager();
            keyManager.setKeyTrust( source, target, level );
        }
        catch ( Exception e )
        {
            return Response.serverError().build();
        }
        return Response.ok().build();
    }


    /* ***********************************************************
     *
     */
    @Override
    public Response approveKey( String source, String target )
    {
        try
        {
            KeyManager keyManager = securityManager.getKeyManager();
            keyManager.setKeyTrust( source, target, KeyTrustLevel.Full.getId() );
        }
        catch ( Exception e )
        {
            return Response.serverError().build();
        }
        return Response.ok().build();
    }


    /* ***********************************************************
     *
     */
    @Override
    public Response signKey( SecurityKeyData keyData )
    {
        String key = securityManager.getKeyManager()
                                    .signPublicKey( keyData.getSourceKeyIdentityId(), keyData.getKeyText(),
                                            keyData.getTrustlevel() );

        if ( Strings.isNullOrEmpty( key ) )
        {
            return Response.status( Response.Status.NOT_FOUND ).entity( "Object Not found" ).build();
        }
        else
        {
            return Response.ok( key ).build();
        }
    }


    /* ***********************************************************
     *
     */
    @Override
    public Response verifySignature( String sourceFingerprint, String targetFingerprint )
    {
        KeyManager keyManager = securityManager.getKeyManager();

        boolean certfied = keyManager.verifySignature( sourceFingerprint, targetFingerprint  );

        return Response.ok( certfied ).build();
    }


    /* ***********************************************************
     *
     */
    @Override
    public Response verifyTrust( String sourceFingerprint, String targetFingerprint )
    {
        return null;
    }

}
