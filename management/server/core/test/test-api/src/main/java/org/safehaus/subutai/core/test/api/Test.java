package org.safehaus.subutai.core.test.api;


import java.io.Serializable;


public interface Test
{

    public void logUsername();

    public String getUserName();

    public Serializable loginWithToken( String tokenId, String ip );
}
