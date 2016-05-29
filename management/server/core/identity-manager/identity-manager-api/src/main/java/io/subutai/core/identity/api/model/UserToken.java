package io.subutai.core.identity.api.model;


import java.util.Date;


/**
 *
 */
public interface UserToken
{
    //***********************************
    String getHeader();

    //***********************************
    String getClaims();


    //***********************************
    String getFullToken();


    //***********************************
    String getTokenId();


    //***********************************
    void setTokenId( String tokenId );


    //***********************************
    String getSecret();


    //***********************************
    void setSecret( String secret );


    //***********************************
    int getType();


    //***********************************
    void setType( int type );


    //***********************************
    String getHashAlgorithm();


    //***********************************
    void setHashAlgorithm( String hashAlgorithm );


    //***********************************
    String getIssuer();


    //***********************************
    void setIssuer( String issuer );


    //***********************************
    Date getValidDate();


    //***********************************
    void setValidDate( Date validDate );


    String getTypeName();


    public long getUserId();


    public void setUserId( long userId );
}
