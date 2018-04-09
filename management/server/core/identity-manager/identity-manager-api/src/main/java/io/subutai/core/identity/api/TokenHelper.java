package io.subutai.core.identity.api;


import java.util.Date;

import io.subutai.core.identity.api.exception.TokenParseException;


public interface TokenHelper
{
    String getToken();

    boolean verify( String secret );

    String getSubject() throws TokenParseException;

    Date getExpirationTime() throws TokenParseException;
}
