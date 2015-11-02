package io.subutai.common.security.token;


import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import net.minidev.json.JSONObject;

/**
 *
 */
public class TokenUtil
{

    public static String createToken(String headerJson, String claimJson,String sharedKey)
    {
        try
        {
            JWSHeader header = JWSHeader.parse(headerJson) ;
            JWSSigner signer = new MACSigner( sharedKey.getBytes() );
            JWTClaimsSet claimsSet = JWTClaimsSet.parse( claimJson ) ;

            SignedJWT signedJWT = new SignedJWT(header, claimsSet);
            signedJWT.sign( signer );

            return signedJWT.serialize();
        }
        catch ( Exception ex )
        {
            return "";
        }
    }


    public static boolean verifySignature(String token, String sharedKey)
    {
        boolean verifiedSignature = false;

        try
        {
            JWSObject jwsObject  = JWSObject.parse( token );
            JWSVerifier verifier = new MACVerifier( sharedKey.getBytes() );
            verifiedSignature = jwsObject.verify( verifier );
        }
        catch ( Exception ex )
        {
            return false;
        }

        return verifiedSignature;
    }


    public static Payload parseToken(String token)
    {
        Payload payload = null;
        try
        {
            JWSObject jwsObject = JWSObject.parse( token );
            payload = jwsObject.getPayload();
        }
        catch ( Exception ex )
        {
            return null;
        }

        return payload;
    }


    public static String getSubject(String token)
    {
        try
        {
            Payload payload   = parseToken(token);
            JSONObject obj = payload.toJSONObject();
            return obj.get( "sub" ).toString();
        }
        catch ( Exception ex )
        {
            return null;
        }
    }


}
