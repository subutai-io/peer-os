package org.safehaus.subutai.core.identity.impl;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.safehaus.subutai.common.dao.DaoManager;
import org.safehaus.subutai.common.security.NullSubutaiLoginContext;
import org.safehaus.subutai.common.security.SubutaiLoginContext;
import org.safehaus.subutai.common.security.SubutaiThreadContext;
import org.safehaus.subutai.common.util.SecurityUtil;
import org.safehaus.subutai.core.identity.api.IdentityManager;
import org.safehaus.subutai.core.identity.api.Permission;
import org.safehaus.subutai.core.identity.api.PermissionGroup;
import org.safehaus.subutai.core.identity.api.Role;
import org.safehaus.subutai.core.identity.api.Roles;
import org.safehaus.subutai.core.identity.api.User;
import org.safehaus.subutai.core.identity.api.UserPortalModule;
import org.safehaus.subutai.core.identity.impl.dao.PermissionDataService;
import org.safehaus.subutai.core.identity.impl.dao.RoleDataService;
import org.safehaus.subutai.core.identity.impl.dao.UserDataService;
import org.safehaus.subutai.core.identity.impl.dao.UserPortalModuleDataService;
import org.safehaus.subutai.core.identity.impl.entity.PermissionEntity;
import org.safehaus.subutai.core.identity.impl.entity.PermissionPK;
import org.safehaus.subutai.core.identity.impl.entity.RoleEntity;
import org.safehaus.subutai.core.identity.impl.entity.UserEntity;
import org.safehaus.subutai.core.identity.impl.entity.UserPortalModuleEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.felix.gogo.api.CommandSessionListener;
import org.apache.felix.service.command.CommandSession;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.SessionListener;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.DefaultSessionManager;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.DefaultSubjectContext;
import org.apache.shiro.util.SimpleByteSource;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;


/**
 * Implementation of Identity Manager
 */
public class IdentityManagerImpl implements IdentityManager, CommandSessionListener
{
    private static final Logger LOG = LoggerFactory.getLogger( IdentityManagerImpl.class );

    private final DaoManager daoManager;
    private final DataSource dataSource;
    private DefaultSecurityManager securityManager;

    private UserDataService userDataService;
    private PermissionDataService permissionDataService;
    private RoleDataService roleDataService;
    private UserPortalModuleDataService portalModuleDataService;


    private String getSimpleSalt( String username )
    {
        return "random_salt_value_" + username;
    }


    public IdentityManagerImpl( final DaoManager daoManager, final DataSource dataSource )
    {
        this.daoManager = daoManager;
        this.dataSource = dataSource;
    }


    public void init()
    {
        LOG.info( "Initializing identity manager..." );

        userDataService = new UserDataService( daoManager );
        permissionDataService = new PermissionDataService( daoManager.getEntityManagerFactory() );
        roleDataService = new RoleDataService( daoManager.getEntityManagerFactory() );
        portalModuleDataService = new UserPortalModuleDataService( daoManager.getEntityManagerFactory() );


        securityManager = new DefaultSecurityManager();

        DefaultSessionManager sessionManager = new DefaultSessionManager();
        sessionManager.setGlobalSessionTimeout( 3600000 );

        securityManager.setSessionManager( sessionManager );

        SubutaiJdbcRealm realm = new SubutaiJdbcRealm( dataSource );
        realm.setPermissionsLookupEnabled( false );
        realm.setAuthenticationQuery( "select password, salt from subutai_user where user_name = ?" );
        realm.setUserRolesQuery(
                "select r.role_name from subutai_user_role r inner join subutai_user u on u.user_id = r.user_id where"
                        + " u.user_name = ?" );
        realm.setPermissionsQuery( "select permission from subutai_roles_permissions where role_name = ?" );


        HashedCredentialsMatcher credentialsMatcher = new HashedCredentialsMatcher();
        credentialsMatcher.setHashAlgorithmName( "SHA-256" );
        credentialsMatcher.setHashIterations( 1 );

        realm.setCredentialsMatcher( credentialsMatcher );


        securityManager.setRealms( Sets.<Realm>newHashSet( realm, new TokenRealm( this ) ) );


        checkDefaultUser( "karaf" );
        checkDefaultUser( "admin" );

        List<SessionListener> sessionListeners = new ArrayList<>();
        sessionListeners.add( new SubutaiSessionListener() );

        DefaultSecurityManager defaultSecurityManager = securityManager;
        ( ( DefaultSessionManager ) defaultSecurityManager.getSessionManager() )
                .setSessionListeners( sessionListeners );

        LOG.info( String.format( "Identity manager initialized: %s", securityManager ) );
    }


    private void checkDefaultUser( String username )
    {

        User user = userDataService.findByUsername( username );
        LOG.info( String.format( "User: [%s] [%s]", username, user ) );
        if ( user != null )
        {
            return;
        }
        LOG.info( String.format( "User not found. Adding new user: [%s] ", username ) );
        RoleDataService roleDataService = new RoleDataService( daoManager.getEntityManagerFactory() );
        RoleEntity adminRole = roleDataService.find( "admin" );
        if ( adminRole == null )
        {
            adminRole = new RoleEntity();
            adminRole.setName( "admin" );
            roleDataService.persist( adminRole );
        }

        RoleEntity managerRole = roleDataService.find( "manager" );
        if ( managerRole == null )
        {
            managerRole = new RoleEntity();
            managerRole.setName( "manager" );
            roleDataService.persist( managerRole );
        }


        String password = "secret";
        String salt = getSimpleSalt( username );
        user = new UserEntity();
        user.setUsername( username );
        user.setFullname( "Full name of: " + username );
        user.setEmail( username + "@subutai.at" );
        user.setPassword( saltedHash( password, new SimpleByteSource( salt ).getBytes() ) );
        user.setSalt( salt );
        user.addRole( adminRole );
        user.addRole( managerRole );
        userDataService.persist( user );
        LOG.debug( String.format( "User: %s", user.getId() ) );
    }


    @Override
    public SecurityManager getSecurityManager()
    {
        return securityManager;
    }


    private void logActiveSessions()
    {
        LOG.debug( "Active sessions:" );
        DefaultSecurityManager defaultSecurityManager = securityManager;
        DefaultSessionManager sm = ( DefaultSessionManager ) defaultSecurityManager.getSessionManager();
        for ( Session session : sm.getSessionDAO().getActiveSessions() )
        {
            SimplePrincipalCollection p =
                    ( SimplePrincipalCollection ) session.getAttribute( DefaultSubjectContext.PRINCIPALS_SESSION_KEY );

            LOG.debug( String.format( "%s %s", session.getId(), p ) );
        }
    }


    @Override
    public User getUser()
    {
        //        logActiveSessions();

        SubutaiLoginContext loginContext = getSubutaiLoginContext();
        //        LOG.debug( String.format( "Login context: [%s] ", loginContext ) );

        if ( loginContext instanceof NullSubutaiLoginContext )
        {
            return null;
        }

        if ( isAuthenticated( loginContext.getSessionId() ) )
        {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader( this.getClass().getClassLoader() );
            try
            {
                return userDataService.findByUsername( loginContext.getUsername() );
            }
            finally
            {
                Thread.currentThread().setContextClassLoader( cl );
            }
        }
        else
        {
            return null;
        }
    }


    private SubutaiLoginContext getSubutaiLoginContext()
    {
        SubutaiLoginContext loginContext = SubutaiThreadContext.get();
        return loginContext instanceof NullSubutaiLoginContext ? SecurityUtil.getSubutaiLoginContext() : loginContext;
    }


    @Override
    public Serializable login( final String username, final String password )
    {
        UsernamePasswordToken usernamePasswordToken = new UsernamePasswordToken( username, password );

        SecurityUtils.setSecurityManager( securityManager );
        Subject subject = SecurityUtils.getSubject();

        try
        {
            subject.login( usernamePasswordToken );
            return subject.getSession().getId();
        }
        catch ( UnknownSessionException e )
        {
            subject = new Subject.Builder().buildSubject();
            subject.login( usernamePasswordToken );
            return subject.getSession( true ).getId();
        }
    }


    public Serializable loginWithToken( final String username )
    {
        UserToken userToken = new UserToken( username );

        SecurityUtils.setSecurityManager( securityManager );
        Subject subject = SecurityUtils.getSubject();

        Session session = null;
        try
        {
            subject.login( userToken );
            session = subject.getSession();
            return session.getId();
        }
        catch ( UnknownSessionException e )
        {
            subject = new Subject.Builder().buildSubject();
            subject.login( userToken );
            session = subject.getSession( true );
            return session.getId();
        }
        finally
        {
            if ( session != null )
            {
                SubutaiLoginContext loginContext =
                        new SubutaiLoginContext( session.getId().toString(), subject.getPrincipal().toString(), null );
                SubutaiThreadContext.set( loginContext );
            }
        }
    }


    private boolean isAuthenticated( final Serializable sessionId )
    {

        Subject subject = getSubject( sessionId );

        return subject != null && subject.isAuthenticated();
    }


    @Override
    public boolean isAuthenticated()
    {
        SubutaiLoginContext loginContext = getSubutaiLoginContext();

        return !( loginContext instanceof NullSubutaiLoginContext ) && isAuthenticated( loginContext.getSessionId() );
    }


    @Override
    public Set<String> getRoles( final Serializable shiroSessionId )
    {
        Set<String> result = new HashSet<>();
        Subject subject = getSubject( shiroSessionId );

        for ( Roles role : Roles.values() )
        {
            if ( subject.hasRole( role.getRoleName() ) )
            {
                result.add( role.getRoleName() );
            }
        }
        return result;
    }


    @Override
    public UserPortalModule createMockUserPortalModule( final String moduleKey, final String moduleName )
    {
        return new UserPortalModuleEntity( moduleKey, moduleName );
    }


    @Override
    public boolean updateUserPortalModule( final UserPortalModule userPortalModule )
    {
        LOG.debug( "Saving new portal module: ", userPortalModule.toString() );
        if ( !( userPortalModule instanceof UserPortalModuleEntity ) )
        {
            return false;
        }
        portalModuleDataService.update( ( UserPortalModuleEntity ) userPortalModule );
        return true;
    }


    @Override
    public Subject getSubject( Serializable sessionId )
    {
        return new Subject.Builder( securityManager ).sessionId( sessionId ).buildSubject();
    }


    @Override
    public void touch( Serializable sessionId )
    {
        Subject subject = getSubject( sessionId );
        if ( subject != null && subject.isAuthenticated() )
        {
            subject.getSession().touch();
        }
    }


    @Override
    public void logout( Serializable sessionId )
    {
        Subject subject = this.getSubject( sessionId );
        subject.logout();
    }


    @Override
    public List<User> getAllUsers()
    {
        List<User> result = new ArrayList<>();
        result.addAll( userDataService.getAll() );
        return result;
    }


    @Override
    public boolean addUser( final String username, final String fullname, final String password, final String email )
    {
        String salt = getSimpleSalt( username );
        UserEntity user = new UserEntity();
        user.setUsername( username );
        user.setEmail( email );
        user.setFullname( fullname );
        user.setSalt( salt );
        user.setPassword( saltedHash( password, salt.getBytes() ) );

        //        try
        //        {
        //            Host host = managementHost;
        //            KeyInfo keyInfo = keyManager.generateKey( host, fullname, email );
        //            user.setKey( keyInfo.getPublicKeyId() );
        //            LOG.debug( String.format( "%s", keyInfo.toString() ) );
        //        }
        //        catch ( KeyManagerException e )
        //        {
        //            LOG.error( e.toString(), e );
        //        }

        userDataService.persist( user );
        return user.getId() != null;
    }


    @Override
    public User getUser( final String username )
    {
        return userDataService.findByUsername( username );
    }


    @Override
    public String getUserKey( final String username )
    {
        User user = userDataService.findByUsername( username );
        return user.getKey();
    }


    @Override
    public User createMockUser( final String username, final String fullName, final String password,
                                final String email )
    {
        String salt = getSimpleSalt( username );
        UserEntity user = new UserEntity();
        user.setUsername( username );
        user.setEmail( email );
        user.setFullname( fullName );
        user.setSalt( salt );
        user.setPassword( saltedHash( password, salt.getBytes() ) );
        return user;
    }


    @Override
    public boolean updateUser( final User user )
    {
        //TODO check for right function operation...
        if ( !( user instanceof UserEntity ) )
        {
            return false;
        }

        if ( isPasswordChanged( user ) )
        {
            user.setSalt( getSimpleSalt( user.getUsername() ) );
            user.setPassword( saltedHash( user.getPassword(), ( ( UserEntity ) user ).getSalt().getBytes() ) );
        }

        //        if ( user.getId() == null )
        //        {
        //            generateKey( user );
        //        }

        userDataService.update( user );
        return true;
    }

    //
    //    private void generateKey( final User user )
    //    {
    //
    //        Host host = null;
    //        long threshold = System.currentTimeMillis() + 1000 * 60;
    //        do
    //        {
    //            try
    //            {
    //                host = managementHost;
    //            }
    //            catch ( HostNotFoundException ignore )
    //            {
    //            }
    //            try
    //            {
    //                LOG.debug( String.format( "Waiting for management host...[%s]", host == null ? "OFF" : "ON" ) );
    //                Thread.sleep( 1000 );
    //            }
    //            catch ( InterruptedException ignore )
    //            {
    //            }
    //        }
    //        while ( host == null && threshold > System.currentTimeMillis() );
    //
    //        if ( host != null )
    //        {
    //            KeyInfo keyInfo = null;
    //            try
    //            {
    //                keyInfo = keyManager.generateKey( host, user.getUsername(), user.getEmail() );
    //                user.setKey( keyInfo.getPublicKeyId() );
    //                LOG.debug( String.format( "%s", keyInfo.toString() ) );
    //            }
    //            catch ( KeyManagerException e )
    //            {
    //                LOG.error( e.toString(), e );
    //            }
    //        }
    //        else
    //        {
    //            LOG.warn( "Management host not available on generating keys." );
    //        }
    //    }


    private boolean isPasswordChanged( final User user )
    {
        if ( user.getId() != null )
        {
            User entity = userDataService.find( user.getId() );
            return !entity.getPassword().equals( user.getPassword() );
        }
        else
        {
            return true;
        }
    }


    @Override
    public User getUser( final Long id )
    {
        return userDataService.find( id );
    }


    @Override
    public boolean deleteUser( final User user )
    {
        if ( !( user instanceof UserEntity ) )
        {
            return false;
        }
        userDataService.remove( user.getId() );
        return true;
    }


    @Override
    public Set<UserPortalModule> getAllPortalModules()
    {
        Set<UserPortalModule> portalModules = Sets.newHashSet();
        portalModules.addAll( portalModuleDataService.getAll() );
        return portalModules;
    }


    @Override
    public List<Permission> getAllPermissions()
    {
        List<Permission> permissions = Lists.newArrayList();
        permissions.addAll( permissionDataService.getAll() );
        return permissions;
    }


    @Override
    public Permission createMockPermission( final String permissionName, final PermissionGroup permissionGroup,
                                            final String description )
    {
        return new PermissionEntity( permissionName, permissionGroup, description );
    }


    @Override
    public boolean updatePermission( final Permission permission )
    {
        if ( !( permission instanceof PermissionEntity ) )
        {
            return false;
        }
        permissionDataService.update( ( PermissionEntity ) permission );
        return true;
    }


    @Override
    public Permission getPermission( final String name, final PermissionGroup permissionGroup )
    {
        return permissionDataService.find( new PermissionPK( name, permissionGroup ) );
    }


    @Override
    public boolean deletePermission( final Permission permission )
    {
        if ( !( permission instanceof PermissionEntity ) )
        {
            return false;
        }
        permissionDataService.remove( new PermissionPK( permission.getName(), permission.getPermissionGroup() ) );
        return true;
    }


    @Override
    public List<Role> getAllRoles()
    {
        List<Role> roles = Lists.newArrayList();
        roles.addAll( roleDataService.getAll() );
        return roles;
    }


    @Override
    public Role createMockRole( final String permissionName, final PermissionGroup permissionGroup,
                                final String description )
    {
        return new RoleEntity( "" );
    }


    @Override
    public boolean updateRole( final Role role )
    {
        if ( !( role instanceof RoleEntity ) )
        {
            return false;
        }
        roleDataService.update( ( RoleEntity ) role );
        return true;
    }


    @Override
    public Role getRole( final String name )
    {
        return null;
    }


    @Override
    public boolean deleteRole( final Role role )
    {
        return false;
    }


    private String saltedHash( String password, byte[] salt )
    {
        Sha256Hash sha256Hash = new Sha256Hash( password, salt );
        return sha256Hash.toHex();
    }


    @Override
    public void beforeExecute( final CommandSession commandSession, final CharSequence charSequence )
    {
        SubutaiLoginContext loginContext = getSubutaiLoginContext();
        if ( !( loginContext instanceof NullSubutaiLoginContext ) && isAuthenticated( loginContext.getSessionId() ) )
        {
            touch( loginContext.getSessionId() );
        }
    }


    @Override
    public void afterExecute( final CommandSession commandSession, final CharSequence charSequence, final Exception e )
    {

    }


    @Override
    public void afterExecute( final CommandSession commandSession, final CharSequence charSequence, final Object o )
    {

    }
}
