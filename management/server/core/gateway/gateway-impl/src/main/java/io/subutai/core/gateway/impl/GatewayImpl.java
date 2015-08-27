package io.subutai.core.gateway.impl;


import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.DirectEncrypter;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import io.subutai.core.gateway.api.Gateway;
import io.subutai.core.identity.api.IdentityManager;


/**
 * Subutai Gateway Implementation
 */
public class GatewayImpl implements Gateway
{
    private final static String VERSION = "0.0.1";
    private static final String CONTENT = "content";
    Logger log = LoggerFactory.getLogger( GatewayImpl.class );

    IdentityManager identityManager;
    private JWSSigner signer;
    private SecretKey secretKey;


    public GatewayImpl() throws NoSuchAlgorithmException
    {
        log.debug( "Generating key materials..." );
        // Generate 256-bit AES key for HMAC as well as encryption
        KeyGenerator keyGen = KeyGenerator.getInstance( "AES" );
        keyGen.init( 256 );
        secretKey = keyGen.generateKey();

        // Create HMAC signer
        signer = new MACSigner( "012345678901234567890123456789--" );
    }


    public void setIdentityManager( final IdentityManager identityManager )
    {
        this.identityManager = identityManager;
    }


    @Override
    public String login( final String username, final String password )
    {
        log.debug( "Login invoked..." );
        final Serializable token = identityManager.login( username, password );

        log.debug( String.format( "Shiro Token: %s", token != null ? token.toString() : "NULL" ) );

        String result = "FAIL";
        try
        {
            JWEObject jweObject = new JWEObject(
                    new JWEHeader.Builder( JWEAlgorithm.DIR, EncryptionMethod.A256GCM ).contentType( "JWT" ).build(),
                    new Payload( token.toString() ) );

            // Perform encryption
            jweObject.encrypt( new DirectEncrypter( secretKey.getEncoded() ) );

            // Serialise to JWE compact form
            String jweString = jweObject.serialize();

            // Prepare JWT with claims set
            JWTClaimsSet claimsSet = new JWTClaimsSet();
            claimsSet.setSubject( username );
            claimsSet.setIssueTime( new Date() );
            claimsSet.setIssuer( "https://management.subutai.io" );

            claimsSet.setCustomClaim( CONTENT, jweString );

            JWSHeader header = new JWSHeader( JWSAlgorithm.HS256 );
            SignedJWT signedJWT = new SignedJWT( header, claimsSet );

            // Apply the HMAC
            signedJWT.sign( signer );
            result = signedJWT.serialize();
        }
        catch ( JOSEException e )
        {
            log.error( e.toString(), e );
        }
        return result;
    }


    @Override
    public void logout( final Serializable token )
    {
        log.debug( "Logout invoked..." );
        identityManager.logout( token );
    }


    @Override
    public String getVersion()
    {
        return VERSION;
    }


    class Payload1
    {
        private String shiroToken;


        public String getShiroToken()
        {
            return shiroToken;
        }


        public void setShiroToken( final String shiroToken )
        {
            this.shiroToken = shiroToken;
        }
    }
}
