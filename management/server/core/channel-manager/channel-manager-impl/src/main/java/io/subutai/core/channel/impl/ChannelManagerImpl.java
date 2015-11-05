package io.subutai.core.channel.impl;

import io.subutai.core.channel.api.ChannelManager;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.security.api.SecurityManager;


/**
 * Manages all CXF channels (tunnel)
 */
public class ChannelManagerImpl implements ChannelManager
{
    private IdentityManager identityManager = null;
    private SecurityManager securityManager = null;
    private boolean encryptionEnabled;

    public void init()
    {
    }


    public void destroy()
    {
     }



    public void setEncryptionEnabled( final boolean encryptionEnabled )
    {
        this.encryptionEnabled = encryptionEnabled;
    }

    @Override
    public boolean isEncryptionEnabled()
    {
        return encryptionEnabled;
    }

    public IdentityManager getIdentityManager()
    {
        return identityManager;
    }

    public void setIdentityManager( final IdentityManager identityManager )
    {
        this.identityManager = identityManager;
    }


    public SecurityManager getSecurityManager()
    {
        return securityManager;
    }


    public void setSecurityManager( final SecurityManager securityManager )
    {
        this.securityManager = securityManager;
    }


}

