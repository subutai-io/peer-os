package io.subutai.core.identity.impl.utils;


import java.text.ParseException;
import java.util.Date;
import java.util.List;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import io.subutai.core.identity.api.exception.TokenCreateException;
import io.subutai.core.identity.api.exception.TokenParseException;


public class TokenHelper
{
    private SignedJWT signedJWT;
    private String token;


    public TokenHelper( String issuer, String environmentId, String containerId, Date issueTime, Date expireTime,
                        String secret ) throws TokenCreateException
    {
        try
        {
            this.token = generate( issuer, environmentId, containerId, issueTime, expireTime, secret );
        }
        catch ( JOSEException e )
        {
            throw new TokenCreateException( e.getMessage() );
        }
    }


    public TokenHelper( String token ) throws TokenParseException
    {
        this.token = token;
        try
        {
            this.signedJWT = SignedJWT.parse( token );
        }
        catch ( ParseException e )
        {
            throw new TokenParseException( e.getMessage() );
        }
    }


    protected String generate( String issuer, String environmentId, String containerId, Date issueTime, Date expireTime,
                               String secret ) throws JOSEException
    {
        JWSHeader jwtHeader = new JWSHeader( JWSAlgorithm.HS256 );
        JWTClaimsSet claimset =
                new JWTClaimsSet.Builder().expirationTime( expireTime ).issuer( issuer ).issueTime( issueTime )
                                          .subject( containerId ).audience( environmentId ).build();
        SignedJWT jwt = new SignedJWT( jwtHeader, claimset );

        JWSSigner signer = new MACSigner( secret );
        jwt.sign( signer );
        return jwt.serialize();
    }


    public String getToken()
    {
        return token;
    }


    public boolean verify( String secret )
    {
        try
        {
            JWSVerifier verifier = new MACVerifier( secret );
            return this.signedJWT.verify( verifier );
        }
        catch ( JOSEException e )
        {
            return false;
        }
    }


    public String getSubject() throws TokenParseException
    {
        try
        {
            return this.signedJWT.getJWTClaimsSet().getSubject();
        }
        catch ( ParseException e )
        {
            throw new TokenParseException( e.getMessage() );
        }
    }


    public String getAudience() throws TokenParseException
    {
        try
        {
            List<String> audience = this.signedJWT.getJWTClaimsSet().getAudience();
            if ( audience == null || audience.isEmpty() )
            {
                return null;
            }
            return audience.get( 0 );
        }
        catch ( ParseException e )
        {
            throw new TokenParseException( e.getMessage() );
        }
    }


    public Date getExpirationTime() throws TokenParseException
    {
        try
        {
            return this.signedJWT.getJWTClaimsSet().getExpirationTime();
        }
        catch ( ParseException e )
        {
            throw new TokenParseException( e.getMessage() );
        }
    }
}
