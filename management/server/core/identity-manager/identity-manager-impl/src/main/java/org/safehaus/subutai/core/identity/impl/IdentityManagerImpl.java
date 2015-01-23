package org.safehaus.subutai.core.identity.impl;


import java.io.IOException;
import java.net.URL;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.safehaus.subutai.common.dao.DaoManager;
import org.safehaus.subutai.core.identity.api.IdentityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.config.Ini;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.SecurityManager;


/**
 * Implementation of Network Manager
 */
public class IdentityManagerImpl implements IdentityManager
{
    private static final Logger LOG =
            LoggerFactory.getLogger( org.safehaus.subutai.core.identity.impl.IdentityManagerImpl.class.getName() );

    private DaoManager daoManager;
    private BundleContext bundleContext;
    private SecurityManager securityManager;


    public IdentityManagerImpl()
    {
        //        LOG.info( "Initializing security manager..." );
        //        IniSecurityManagerFactory factory = new IniSecurityManagerFactory( "subutai-shiro.ini" );
        //        org.apache.shiro.mgt.SecurityManager securityManager = factory.getInstance();
        //        SecurityUtils.setSecurityManager( securityManager );
        //        LOG.info( String.format( "Security manager initialized: %s", securityManager ) );
    }


    public void setBundleContext( final BundleContext bundleContext )
    {
        this.bundleContext = bundleContext;
    }


    public void init()
    {
        LOG.info( "Initializing security manager..." );
        Bundle bundle = bundleContext.getBundle();
        URL url = bundle.getEntry( "subutai-shiro.ini" );
        org.apache.shiro.config.Ini ini = new Ini();
        try
        {
            ini.load( url.openStream() );
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
        IniSecurityManagerFactory factory = new IniSecurityManagerFactory( ini );
        securityManager = factory.getInstance();

        SecurityUtils.setSecurityManager( securityManager );

        LOG.info( String.format( "Security manager initialized: %s", securityManager ) );
    }


    public void setDaoManager( final DaoManager daoManager )
    {
        this.daoManager = daoManager;
    }


    @Override
    public SecurityManager getSecurityManager()
    {
        return securityManager;
    }
}
