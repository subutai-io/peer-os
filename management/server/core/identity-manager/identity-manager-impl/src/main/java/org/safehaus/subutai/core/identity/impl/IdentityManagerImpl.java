package org.safehaus.subutai.core.identity.impl;


import org.osgi.framework.BundleContext;
import org.safehaus.subutai.common.dao.DaoManager;
import org.safehaus.subutai.common.helper.UserIdMdcHelper;
import org.safehaus.subutai.core.identity.api.IdentityManager;
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
import org.apache.shiro.util.ThreadContext;


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


    public IdentityManagerImpl()
    {
        //        LOG.info( "Initializing security manager..." );
        //        IniSecurityManagerFactory factory = new IniSecurityManagerFactory( "subutai-shiro.ini" );
        //        org.apache.shiro.mgt.SecurityManager securityManager = factory.getInstance();
        //        SecurityUtils.setSecurityManager( securityManager );
        //        LOG.info( String.format( "Security manager initialized: %s", securityManager ) );
    }


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

        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setName( "admin" );

        String username = "karaf";
        String password = "secret";
        String salt = getSalt( username );
        UserDataService userDataService = new UserDataService( daoManager.getEntityManagerFactory() );
        UserEntity user = new UserEntity();
        user.setUsername( username );
        user.setPassword( simpleSaltedHash( password, new SimpleByteSource( salt ).getBytes() ) );
        user.setSalt( salt );
        user.addRole( roleEntity );
        userDataService.persist( user );
        LOG.info( String.format( "User: %s", user.getId() ) );

        //        RoleDataService roleDataService = new RoleDataService( daoManager.getEntityManagerFactory() );
        //        roleDataService.persist( roleEntity );
        SecurityUtils.setSecurityManager( securityManager );

        LOG.info( String.format( "Security manager initialized: %s", securityManager ) );
    }


    //    public String getHex( byte[] raw )
    //    {
    //        if ( raw == null )
    //        {
    //            return null;
    //        }
    //        final StringBuilder hex = new StringBuilder( 2 * raw.length );
    //        for ( final byte b : raw )
    //        {
    //            hex.append( HEXES.charAt( ( b & 0xF0 ) >> 4 ) ).append( HEXES.charAt( ( b & 0x0F ) ) );
    //        }
    //        return hex.toString();
    //    }


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
    public void login( final AuthenticationToken token )
    {
        Subject subject = this.getSubject();
        subject.login( token );
        LOG.info( String.format( "Principal: %s. Is authenticated?: %s", token.getPrincipal().toString(),
                subject.isAuthenticated() ) );
        ThreadContext.bind( subject );
        //        UserIdMdcHelper.set( subject );
    }


    @Override
    public Subject getSubject()
    {
        return SecurityUtils.getSubject();
    }


    @Override
    public void logout()
    {
        UserIdMdcHelper.unset();
        Subject subject = SecurityUtils.getSubject();
        subject.logout();
    }
}
