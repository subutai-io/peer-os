package org.safehaus.subutai.core.identity.api;


import org.apache.shiro.mgt.SecurityManager;


public interface IdentityManager
{
    public SecurityManager getSecurityManager();
}

