package org.safehaus.subutai.core.test.impl;


import java.io.Serializable;
import java.util.concurrent.ExecutorService;

import org.safehaus.subutai.common.mdc.SubutaiExecutors;
import org.safehaus.subutai.core.identity.api.IdentityManager;
import org.safehaus.subutai.core.identity.api.User;
import org.safehaus.subutai.core.test.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

//import org.safehaus.subutai.common.mdc.MDCAwareRunnable;


public class TestImpl implements Test
{
    private static Logger LOG = LoggerFactory.getLogger( TestImpl.class.getName() );

    private final IdentityManager identityManager;
    ExecutorService executorService = SubutaiExecutors.newCachedThreadPool();


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


    public void testExecutor()
    {
        executorService.submit( new Runnable()
        {
            @Override
            public void run()
            {
                LOG.error( "User >>>>>> " + identityManager.getUser().getUsername() );
            }
        } );
    }


    @Override
    public String getUserName()
    {
        return identityManager.getUser().getUsername();
    }


    @Override
    public Serializable loginWithToken( String username )
    {
        return identityManager.loginWithToken( username );
    }
}
