package org.safehaus.subutai.core.security.impl;


import java.io.IOException;
import java.net.URL;
import java.util.Set;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.safehaus.subutai.common.dao.DaoManager;
import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.core.security.api.SecurityManager;
import org.safehaus.subutai.core.security.api.SecurityManagerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.config.Ini;
import org.apache.shiro.config.IniSecurityManagerFactory;


/**
 * Implementation of Network Manager
 */
public class SecurityManagerImpl implements SecurityManager
{
    private static final Logger LOG = LoggerFactory.getLogger( SecurityManagerImpl.class.getName() );

    private DaoManager daoManager;
    private BundleContext bundleContext;
    org.apache.shiro.mgt.SecurityManager securityManager;


    public SecurityManagerImpl()
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


    @Override
    public org.apache.shiro.mgt.SecurityManager getSecurityManager()
    {
        return securityManager;
    }


    public void setDaoManager( final DaoManager daoManager )
    {
        this.daoManager = daoManager;
    }


    @Override
    public void configSshOnAgents( Set<ContainerHost> containerHosts ) throws SecurityManagerException
    {
        //        EntityManager em = daoManager.getEntityManagerFactory().createEntityManager();
        try
        {
            new SshManager( containerHosts ).execute();
        }
        catch ( SSHManagerException e )
        {
            throw new SecurityManagerException( e );
        }
    }


    @Override
    public void addSshKeyToAuthorizedKeys( final String sshKey, Set<ContainerHost> containerHosts )
            throws SecurityManagerException
    {
        try
        {
            new SshManager( containerHosts ).append( sshKey );
        }
        catch ( SSHManagerException e )
        {
            throw new SecurityManagerException( e );
        }
    }


    @Override
    public void configSshOnAgents( Set<ContainerHost> containerHosts, ContainerHost containerHost )
            throws SecurityManagerException
    {
        try
        {
            new SshManager( containerHosts ).execute( containerHost );
        }
        catch ( SSHManagerException e )
        {
            throw new SecurityManagerException( e );
        }
    }


    @Override
    public void configHostsOnAgents( Set<ContainerHost> containerHosts, String domainName )
            throws SecurityManagerException
    {
        try
        {
            new HostManager( containerHosts, domainName ).execute();
        }
        catch ( HostManagerException e )
        {
            throw new SecurityManagerException( e );
        }
    }
}
