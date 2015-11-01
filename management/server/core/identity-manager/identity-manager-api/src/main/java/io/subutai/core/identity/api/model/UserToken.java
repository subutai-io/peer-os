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
    String getToken();

    //***********************************
    void setToken( String token );

    //***********************************
    String getSecret();

    //***********************************
    void setSecret( String secret );

    //***********************************
    String getType();

    //***********************************
    void setType( String type );

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

    //***********************************
    User getUser();

    //***********************************
    void setUser( User user );
}
