package io.subutai.core.identity.impl.utils;


import java.text.ParseException;
import java.util.Date;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import io.subutai.core.identity.api.TokenHelper;
import io.subutai.core.identity.api.exception.TokenCreateException;
import io.subutai.core.identity.api.exception.TokenParseException;


public class TokenHelperImpl implements TokenHelper
{
    private SignedJWT signedJWT;
    private String token;


    public TokenHelperImpl( String issuer, String subject, Date issueTime, Date expireTime, String secret )
            throws TokenCreateException
    {
        try
        {
            this.token = generate( issuer, subject, issueTime, expireTime, secret );
        }
        catch ( JOSEException e )
        {
            throw new TokenCreateException( e.getMessage() );
        }
    }


    public TokenHelperImpl( String token ) throws TokenParseException
    {
        try
        {
            this.signedJWT = SignedJWT.parse( token );
            this.token = token;
        }
        catch ( ParseException e )
        {
            throw new TokenParseException( e.getMessage() );
        }
    }


    protected String generate( final String issuer, final String subject, final Date issueTime, final Date expireTime,
                               final String secret ) throws JOSEException
    {
        JWSHeader jwtHeader = new JWSHeader( JWSAlgorithm.HS256 );
        JWTClaimsSet claimset =
                new JWTClaimsSet.Builder().expirationTime( expireTime ).issuer( issuer ).issueTime( issueTime )
                                          .subject( subject ).build();
        SignedJWT jwt = new SignedJWT( jwtHeader, claimset );

        JWSSigner signer = new MACSigner( secret );
        jwt.sign( signer );
        return jwt.serialize();
    }


    @Override
    public String getToken()
    {
        return token;
    }


    @Override
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


    @Override
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


    @Override
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
