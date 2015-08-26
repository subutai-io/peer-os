package io.subutai.core.identity.impl;


import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

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
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.SimpleByteSource;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import io.subutai.common.dao.DaoManager;
import io.subutai.common.security.NullSubutaiLoginContext;
import io.subutai.common.security.SubutaiLoginContext;
import io.subutai.common.security.SubutaiThreadContext;
import io.subutai.common.settings.CLISettings;
import io.subutai.common.settings.ChannelSettings;
import io.subutai.common.util.SecurityUtil;
import io.subutai.core.identity.api.CliCommand;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.Permission;
import io.subutai.core.identity.api.PermissionGroup;
import io.subutai.core.identity.api.PortalModuleScope;
import io.subutai.core.identity.api.RestEndpointScope;
import io.subutai.core.identity.api.Role;
import io.subutai.core.identity.api.Roles;
import io.subutai.core.identity.api.User;
import io.subutai.core.identity.impl.dao.CliCommandDataService;
import io.subutai.core.identity.impl.dao.PermissionDataService;
import io.subutai.core.identity.impl.dao.PortalModuleDataService;
import io.subutai.core.identity.impl.dao.RestEndpointDataService;
import io.subutai.core.identity.impl.dao.RoleDataService;
import io.subutai.core.identity.impl.dao.UserDataService;
import io.subutai.core.identity.impl.entity.CliCommandEntity;
import io.subutai.core.identity.impl.entity.PermissionEntity;
import io.subutai.core.identity.impl.entity.PermissionPK;
import io.subutai.core.identity.impl.entity.PortalModuleScopeEntity;
import io.subutai.core.identity.impl.entity.RestEndpointScopeEntity;
import io.subutai.core.identity.impl.entity.RoleEntity;
import io.subutai.core.identity.impl.entity.UserEntity;


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
    private CliCommandDataService cliCommandDataService;
    private PortalModuleDataService portalModuleDataService;
    private RestEndpointDataService restEndpointDataService;


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
        cliCommandDataService = new CliCommandDataService( daoManager.getEntityManagerFactory() );
        portalModuleDataService = new PortalModuleDataService( daoManager.getEntityManagerFactory() );
        restEndpointDataService = new RestEndpointDataService( daoManager.getEntityManagerFactory() );


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


    /**
     * Check if user has access to the Rest service
     *
     * @param user User
     * @param restURL String
     *
     * @return short
     */
    public short checkRestPermissions( User user, String restURL )
    {
        short status = 0;

        if ( user != null )
        {
            Set<Role> roles = user.getRoles();

            for ( Role role : roles )
            {
                Set<RestEndpointScope> restEndpointScopeList = role.getAccessibleRestEndpoints();
                status = checkRestEndpointScope( restURL, restEndpointScopeList, status );

                if ( status == 1 )
                {
                    break;
                }
            }
        }

        return status;
    }


    private short checkRestEndpointScope( final String restURL, final Set<RestEndpointScope> restEndpointScopeList,
                                          final short status )
    {
        if ( restEndpointScopeList != null )
        {
            for ( RestEndpointScope restEndpointScope : restEndpointScopeList )
            {
                if ( restEndpointScope.getRestEndpoint().contains( "{*}" ) )
                {
                    return 1;
                }
            }
        }
        return status;
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
        RoleEntity adminRole = roleDataService.find( "admin" );
        adminRole = initAdminRole( adminRole );

        RoleEntity managerRole = roleDataService.find( "manager" );
        if ( managerRole == null )
        {
            managerRole = new RoleEntity();
            managerRole.setName( "manager" );
            roleDataService.update( managerRole );
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
        userDataService.update( user );
        LOG.debug( String.format( "User: %s", user.getId() ) );
    }


    private RoleEntity initAdminRole( final RoleEntity adminRolePersisted )
    {
        if ( adminRolePersisted == null )
        {
            RoleEntity adminRole = new RoleEntity();
            adminRole.setName( "admin" );

            for ( final String uri : ChannelSettings.REST_URL )
            {
                if ( uri.length() == 0 )
                {
                    continue;
                }
                RestEndpointScopeEntity restEndpointScopeEntity = new RestEndpointScopeEntity( uri );
                restEndpointDataService.update( restEndpointScopeEntity );
                adminRole.addRestEndpointScope( restEndpointScopeEntity );
            }

            for ( final Map.Entry<String, Set<String>> scopes : CLISettings.CLI_CMD_MAP.entrySet() )
            {
                Set<String> names = scopes.getValue();
                for ( final String name : names )
                {
                    CliCommandEntity cliCommandEntity = new CliCommandEntity( scopes.getKey(), name );
                    cliCommandDataService.update( cliCommandEntity );
                    adminRole.addCliCommand( cliCommandEntity );
                }
            }

            roleDataService.update( adminRole );
            return adminRole;
        }
        return adminRolePersisted;
    }


    @Override
    public SecurityManager getSecurityManager()
    {
        return securityManager;
    }


    @Override
    public User getUser()
    {
        SubutaiLoginContext loginContext = getSubutaiLoginContext();

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
        return SubutaiThreadContext.get();
    }


    @Override
    public Serializable login( final String username, final String password )
    {
        UsernamePasswordToken usernamePasswordToken = new UsernamePasswordToken( username, password );

        SecurityUtils.setSecurityManager( securityManager );
        Subject subject = SecurityUtils.getSubject();

        Session session = null;
        try
        {
            subject.login( usernamePasswordToken );
            session = subject.getSession();
            return session.getId();
        }
        catch ( UnknownSessionException e )
        {
            LOG.warn( "Exception getting session #login", e );
            subject = new Subject.Builder().buildSubject();
            subject.login( usernamePasswordToken );
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
    public boolean updateUserPortalModule( String moduleKey, String moduleName )
    {
        ClassLoader cl = IdentityManagerImpl.class.getClassLoader();
        Thread.currentThread().setContextClassLoader( IdentityManagerImpl.class.getClassLoader() );
        try
        {
            LOG.debug( "Saving new portal module: ", moduleName );
            PortalModuleScopeEntity portalModuleScope = new PortalModuleScopeEntity( moduleKey, moduleName );
            portalModuleDataService.update( portalModuleScope );
            List<Role> roles = roleDataService.getAll();
            for ( Role roleEntity : roles )
            {
                if ( roleEntity.getName().equalsIgnoreCase( Roles.ADMIN.getRoleName() ) )
                {
                    roleEntity.addPortalModule( portalModuleScope );
                    roleDataService.update( roleEntity );
                }
            }
        }
        catch ( Exception e )
        {
            LOG.error( "Error updating portalModule for role", e );
        }
        finally
        {
            Thread.currentThread().setContextClassLoader( cl );
        }
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
        user.setPassword( saltedHash( password, salt.getBytes( Charset.forName( "UTF-8" ) ) ) );

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
            user.setPassword( saltedHash( user.getPassword(),
                    ( ( UserEntity ) user ).getSalt().getBytes( Charset.forName( "UTF-8" ) ) ) );
        }

        userDataService.update( user );
        return true;
    }


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
    public List<CliCommand> getAllCliCommands()
    {
        List<CliCommand> cliCommands = new ArrayList<>();
        cliCommands.addAll( cliCommandDataService.getAll() );
        return cliCommands;
    }


    @Override
    public CliCommand createCliCommand( final String scope, final String name )
    {
        return new CliCommandEntity( scope, name );
    }


    @Override
    public boolean updateCliCommand( final CliCommand cliCommand )
    {
        if ( cliCommand instanceof CliCommandEntity )
        {
            cliCommandDataService.update( ( CliCommandEntity ) cliCommand );
            return true;
        }
        return false;
    }


    @Override
    public Set<RestEndpointScope> getAllRestEndpoints()
    {
        Set<RestEndpointScope> restEndpointScopes = Sets.newHashSet();
        restEndpointScopes.addAll( restEndpointDataService.getAll() );
        return restEndpointScopes;
    }


    @Override
    public Set<PortalModuleScope> getAllPortalModules()
    {
        Set<PortalModuleScope> portalModules = Sets.newHashSet();
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
    public Permission createPermission( final String permissionName, final PermissionGroup permissionGroup,
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
    public Role createRole( final String roleName )
    {
        return new RoleEntity( roleName );
    }


    @Override
    public boolean updateRole( final Role role )
    {
        LOG.debug( String.format( "Updating role: %s", role.getName() ) );
        if ( !( role instanceof RoleEntity ) )
        {
            return false;
        }
        roleDataService.update( role );
        return true;
    }


    @Override
    public Role getRole( final String name )
    {
        return roleDataService.find( name );
    }


    @Override
    public void deleteRole( final Role role )
    {
        roleDataService.remove( role.getName() );
    }


    private String saltedHash( String password, byte[] salt )
    {
        Sha256Hash sha256Hash = new Sha256Hash( password, salt );
        return sha256Hash.toHex();
    }


    @Override
    public void beforeExecute( final CommandSession commandSession, final CharSequence charSequence )
    {
        SubutaiLoginContext loginContext = SecurityUtil.getSubutaiLoginContext();
        if ( !( loginContext instanceof NullSubutaiLoginContext ) && isAuthenticated( loginContext.getSessionId() ) )
        {
            SubutaiThreadContext.set( loginContext );
            touch( loginContext.getSessionId() );
        }
    }


    @Override
    public void afterExecute( final CommandSession commandSession, final CharSequence charSequence, final Exception e )
    {
        //ignore
    }


    @Override
    public void afterExecute( final CommandSession commandSession, final CharSequence charSequence, final Object o )
    {
        //ignore
    }
}
