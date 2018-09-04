package io.subutai.common.security.token;

import java.util.Date;

import org.junit.Test;
import org.apache.commons.lang3.time.DateUtils;
import static junit.framework.Assert.assertTrue;


/**
 *
 */
public class TokenUtilTest
{
    private String tokenSecret = "secret1-secret2-secret3-secret4-secret5";

    //***********************************
    private String getHeader()
    {
        return "{\"typ\":\"JWT\",\"alg\":\"HS256\"}";
    }


    //***********************************
    private String getClaims(String token, Date validDate)
    {
        String str = "";

        str += "{\"iss\":\"ISSUER\",";

        if(validDate == null)
            str += "\"exp\":0,";
        else
            str += "\"exp\":" + validDate.getTime() + ",";

        str += "\"sub\":\"" + token + "\"}";

        return str;
    }


    private String createToken()
    {
        Date validDate = DateUtils.addMinutes( new Date( System.currentTimeMillis() ), 3 );

        return TokenUtil.createToken(  getHeader(), getClaims("TestToken",validDate ) ,tokenSecret);
    }

    private String createToken(Date validDate)
    {
        return TokenUtil.createToken(  getHeader(), getClaims("TestToken",validDate ) ,tokenSecret);
    }


    private boolean verifyTokenSignature(String fullToken)
    {
        return TokenUtil.verifySignature( fullToken , tokenSecret );
    }


    @Test
    public void testVerifyToken()
    {
        String token = createToken();

        assertTrue( verifyTokenSignature(token) );
    }



    @Test
    public void testVerifyTokenDate()
    {
        Date testDate1 = DateUtils.setMinutes ( new Date( System.currentTimeMillis() ), 3 );
        Date testDate2 = DateUtils.addMinutes (  new Date( System.currentTimeMillis() ), 2 );

        String token1 = createToken(testDate1);
        String token2 = createToken(testDate2);

        //long expDate = TokenUtils.getDate( token2 );

//        assertFalse( TokenUtil.verifyToken( token1, tokenSecret ) );
        assertTrue( TokenUtil.verifyToken( token2, tokenSecret ) );
    }


}

