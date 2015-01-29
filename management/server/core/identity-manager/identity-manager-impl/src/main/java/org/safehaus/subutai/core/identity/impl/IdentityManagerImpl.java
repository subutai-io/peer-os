package org.safehaus.subutai.core.identity.impl;


import java.io.Serializable;

import org.osgi.framework.BundleContext;
import org.safehaus.subutai.common.dao.DaoManager;
import org.safehaus.subutai.core.identity.api.IdentityManager;
import org.safehaus.subutai.core.identity.api.User;
import org.safehaus.subutai.core.identity.impl.dao.RoleDataService;
import org.safehaus.subutai.core.identity.impl.dao.UserDataService;
import org.safehaus.subutai.core.identity.impl.entity.RoleEntity;
import org.safehaus.subutai.core.identity.impl.entity.UserEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.SimpleByteSource;


/**
 * Implementation of Network Manager
 */
public class IdentityManagerImpl implements IdentityManager
{
    private static final Logger LOG =
            LoggerFactory.getLogger( org.safehaus.subutai.core.identity.impl.IdentityManagerImpl.class.getName() );

    static final String HEXES = "0123456789abcdef";

    private DaoManager daoManager;
    private BundleContext bundleContext;
    private SecurityManager securityManager;
    //    private Subject subject = null;
    //    private Serializable sessionId = null;
    //
    //
    //    public IdentityManagerImpl()
    //    {
    //        //        LOG.info( "Initializing security manager..." );
    //        //        IniSecurityManagerFactory factory = new IniSecurityManagerFactory( "subutai-shiro.ini" );
    //        //        org.apache.shiro.mgt.SecurityManager securityManager = factory.getInstance();
    //        //        SecurityUtils.setSecurityManager( securityManager );
    //        //        LOG.info( String.format( "Security manager initialized: %s", securityManager ) );
    //    }


    public void setSecurityManager( final SecurityManager securityManager )
    {
        this.securityManager = securityManager;
    }


    public void setBundleContext( final BundleContext bundleContext )
    {
        this.bundleContext = bundleContext;
    }


    private String getSalt( String username )
    {
        return "random_salt_value_" + username;
    }


    private String simpleSaltedHash( String password, byte[] salt )
    {
        Sha256Hash sha256Hash = new Sha256Hash( password, salt );
        String result = sha256Hash.toHex();
        return result;
    }


    public void init()
    {
        LOG.info( "Initializing security manager..." );
        //        SubutaiJdbcRealm subutaiJdbcRealm = new SubutaiJdbcRealm();
        //        if ( subutaiJdbcRealm == null )
        //        {
        //            LOG.error( "Could not initialiaze SubutaiJdbcrealm." );
        //        }
        //        Bundle bundle = bundleContext.getBundle();
        //        URL url = bundle.getEntry( "subutai-shiro.ini" );
        //        org.apache.shiro.config.Ini ini = new Ini();
        //        try
        //        {
        //            ini.load( url.openStream() );
        //        }
        //        catch ( IOException e )
        //        {
        //            e.printStackTrace();
        //        }
        //        IniSecurityManagerFactory factory = new IniSecurityManagerFactory( ini );
        //        securityManager = factory.getInstance();
        //        RealmSecurityManager r = ( RealmSecurityManager ) securityManager;
        //        r.setRealm( subutaiJdbcRealm );

        checkDefaultUser( "karaf" );
        checkDefaultUser( "admin" );
        checkDefaultUser( "timur" );

        //        RoleDataService roleDataService = new RoleDataService( daoManager.getEntityManagerFactory() );
        //        roleDataService.persist( roleEntity );
        SecurityUtils.setSecurityManager( securityManager );

        LOG.info( String.format( "Security manager initialized: %s", securityManager ) );
    }


    private void checkDefaultUser( String username )
    {

        UserDataService userDataService = new UserDataService( daoManager.getEntityManagerFactory() );
        User user = userDataService.findByUsername( username );
        LOG.info( String.format( "User: [%s] [%s]", username, user ) );
        if ( user != null )
        {
            return;
        }
        LOG.info( String.format( "User not found. Adding new user: [%s] ", username ) );
        RoleDataService roleDataService = new RoleDataService( daoManager.getEntityManagerFactory() );
        RoleEntity roleEntity = roleDataService.find( "admin" );
        if ( roleEntity == null )
        {
            roleEntity = new RoleEntity();
            roleEntity.setName( "admin" );
        }


        String password = "secret";
        String salt = getSalt( username );
        user = new UserEntity();
        user.setUsername( username );
        user.setPassword( simpleSaltedHash( password, new SimpleByteSource( salt ).getBytes() ) );
        user.setSalt( salt );
        //        user.addRole( roleEntity );
        userDataService.persist( ( UserEntity ) user );
        LOG.info( String.format( "User: %s", user.getId() ) );
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


    @Override
    public Subject login( final AuthenticationToken token )
    {
        LOG.debug( String.format( "Login. Thread ID: %d %s", Thread.currentThread().getId(),
                Thread.currentThread().getName() ) );
        SecurityUtils.setSecurityManager( securityManager );
        Subject subject = SecurityUtils.getSubject();
        subject.login( token );

        return subject;

        //        SecurityUtils.getSecurityManager().getSession( sessionId );
        //
        //        Subject s = new Subject.Builder( securityManager ).sessionId( sessionId ).buildSubject();
        //        LOG.info( String.format( "Principal: %s. Is authenticated?: %s", token.getPrincipal().toString(),
        //                subject.isAuthenticated() ) );
        //        ThreadContext.bind( subject );
        //        UserIdMdcHelper.set( subject );
    }


    @Override
    public Subject getSubject( Serializable sessionId )
    {
        Subject subject = new Subject.Builder( securityManager ).sessionId( sessionId ).buildSubject();
        return subject;
    }


    @Override
    public void logout( Serializable sessionId )
    {
        LOG.debug( String.format( "Logout. Thread ID: %d", Thread.currentThread().getId() ) );
        Subject subject = this.getSubject( sessionId );
        subject.logout();
    }
}
