package io.subutai.common.security.token;


import com.google.gson.JsonObject;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.subutai.common.util.JsonUtil;


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


    public static String parseToken(String token)
    {
        String payloadJSON = "";
        try
        {
            JWSObject jwsObject = JWSObject.parse( token );
            Payload payload = jwsObject.getPayload();
            payloadJSON  = payload.toString();
        }
        catch ( Exception ex )
        {
            return null;
        }

        return payloadJSON;
    }


    public static String getSubject(String token)
    {
        try
        {
            String claim   = parseToken(token);
            JsonObject obj = JsonUtil.fromJson(claim,JsonObject.class);
            return obj.get( "sub" ).toString();
        }
        catch ( Exception ex )
        {
            return null;
        }
    }


}
