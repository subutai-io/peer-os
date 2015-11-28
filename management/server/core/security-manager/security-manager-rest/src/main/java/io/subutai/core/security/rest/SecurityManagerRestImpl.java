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
import io.subutai.core.security.rest.model.KeyIdentityDTO;
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
        try
        {
            logger.debug( "Received hostId: " + hostId );
            KeyManager keyManager = securityManager.getKeyManager();

            KeyIdentityDTO keyIdentityDTO = new KeyIdentityDTO( keyManager.getKeyTrustTree( hostId ) );
            keyIdentityDTO.setChild( false );
            keyIdentityDTO.setTrustLevel( KeyTrustLevel.Ultimate.getId() );
            keyIdentityDTO.setParentId( keyIdentityDTO.getHostId() );
            keyIdentityDTO.setParentPublicKeyFingerprint( keyIdentityDTO.getParentPublicKeyFingerprint() );

            resetTrustLevels( keyIdentityDTO, keyManager );


            return Response.ok( JsonUtil.toJson( keyIdentityDTO ) ).build();
        }
        catch ( Exception ex )
        {
            return Response.serverError().build();
        }
    }


    private void resetTrustLevels( KeyIdentityDTO keyIdentityDTO, KeyManager keyManager )
    {
        for ( final KeyIdentityDTO identityDTO : keyIdentityDTO.getTrusts() )
        {
            identityDTO.setParentId( keyIdentityDTO.getHostId() );
            identityDTO.setParentPublicKeyFingerprint( keyIdentityDTO.getPublicKeyFingerprint() );
            identityDTO.setChild( true );
            identityDTO
                    .setTrustLevel( keyManager.getTrustLevel( keyIdentityDTO.getIdentityId(), identityDTO.getIdentityId() ) );
            resetTrustLevels( identityDTO, keyManager );
        }
    }


    /* ***********************************************************
     *
     */
    @Override
    public Response revokeKey( String source, String target )
    {
        try
        {
            KeyManager keyManager = securityManager.getKeyManager();
            keyManager.setKeyTrust( source, target, KeyTrustLevel.Never.getId() );
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
    public Response allowKey( String source, String target )
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
}
