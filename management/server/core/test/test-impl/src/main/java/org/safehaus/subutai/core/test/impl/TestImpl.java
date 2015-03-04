package org.safehaus.subutai.core.test.impl;


import java.io.Serializable;

import org.safehaus.subutai.core.identity.api.IdentityManager;
import org.safehaus.subutai.core.identity.api.User;
import org.safehaus.subutai.core.test.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TestImpl implements Test
{
    private static Logger LOG = LoggerFactory.getLogger( TestImpl.class.getName() );

    private final IdentityManager identityManager;


    public TestImpl( final IdentityManager identityManager )
    {
        this.identityManager = identityManager;
    }


    @Override
    public void logUsername()
    {
        User user = identityManager.getUser();
        LOG.error( "USER >>>> " + user.getUsername() );
    }


    @Override
    public String getUserName()
    {
        return identityManager.getUser().getUsername();
    }


    @Override
    public Serializable loginWithToken( String tokenId, String ip )
    {
        return identityManager.loginWithToken( tokenId, ip );
    }
}
