package org.safehaus.subutai.core.identity.impl;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.safehaus.subutai.common.dao.DaoManager;
import org.safehaus.subutai.common.peer.Host;
import org.safehaus.subutai.common.security.SubutaiLoginContext;
import org.safehaus.subutai.common.security.SubutaiThreadContext;
import org.safehaus.subutai.common.util.SecurityUtil;
import org.safehaus.subutai.core.identity.api.IdentityManager;
import org.safehaus.subutai.core.identity.api.Permission;
import org.safehaus.subutai.core.identity.api.PermissionGroup;
import org.safehaus.subutai.core.identity.api.Role;
import org.safehaus.subutai.core.identity.api.User;
import org.safehaus.subutai.core.identity.impl.dao.PermissionDataService;
import org.safehaus.subutai.core.identity.impl.dao.RoleDataService;
import org.safehaus.subutai.core.identity.impl.dao.UserDataService;
import org.safehaus.subutai.core.identity.impl.entity.PermissionEntity;
import org.safehaus.subutai.core.identity.impl.entity.PermissionPK;
import org.safehaus.subutai.core.identity.impl.entity.RoleEntity;
import org.safehaus.subutai.core.identity.impl.entity.UserEntity;
import org.safehaus.subutai.core.key.api.KeyInfo;
import org.safehaus.subutai.core.key.api.KeyManager;
import org.safehaus.subutai.core.key.api.KeyManagerException;
import org.safehaus.subutai.core.peer.api.HostNotFoundException;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.SimpleByteSource;

import com.google.common.collect.Lists;


/**
 * Implementation of Identity Manager
 */
public class IdentityManagerImpl implements IdentityManager
{
    private static final Logger LOG = LoggerFactory.getLogger( IdentityManagerImpl.class );

    private DaoManager daoManager;
    private KeyManager keyManager;
    private SecurityManager securityManager;

    private UserDataService userDataService;
    private PermissionDataService permissionDataService;
    private RoleDataService roleDataService;
    private PeerManager peerManager;


    public void setSecurityManager( final SecurityManager securityManager )
    {
        this.securityManager = securityManager;
    }


    public void setPeerManager( final PeerManager peerManager )
    {
        this.peerManager = peerManager;
    }


    public void setKeyManager( final KeyManager keyManager )
    {
        this.keyManager = keyManager;
    }


    private String getSimpleSalt( String username )
    {
        return "random_salt_value_" + username;
    }


    public void init()
    {
        LOG.info( "Initializing identity manager..." );

        userDataService = new UserDataService( daoManager.getEntityManagerFactory() );
        permissionDataService = new PermissionDataService( daoManager.getEntityManagerFactory() );
        roleDataService = new RoleDataService( daoManager.getEntityManagerFactory() );

        checkDefaultUser( "karaf" );
        checkDefaultUser( "admin" );

        SecurityUtils.setSecurityManager( securityManager );

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
        generateKey( user );
        userDataService.persist( user );
        LOG.debug( String.format( "User: %s", user.getId() ) );
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
    public User getUser( String username )
    {
        return userDataService.findByUsername( username );
    }


    @Override
    public User getUser()
    {
        SubutaiLoginContext loginContext = SubutaiThreadContext.get();
        LOG.debug( String.format( "Login context: [%s] ", loginContext ) );


        String username = null;
        if ( loginContext != null && isAuthenticated( loginContext.getSessionId() ) )
        {
            username = loginContext.getUsername();
        }
        else
        {
            Serializable sessionId = SecurityUtil.getSessionId();
            Subject subject = getSubject( sessionId );
            if ( subject != null )
            {
                username = subject.getPrincipal().toString();
            }
        }

        return username != null ? userDataService.findByUsername( username ) : null;
    }


    @Override
    public Subject login( final AuthenticationToken token )
    {

        SecurityUtils.setSecurityManager( securityManager );
        Subject subject = SecurityUtils.getSubject();
        subject.login( token );
        //        UserIdMdcHelper.set( subject.getSession().getId() );
        return subject;
    }


    @Override
    public Serializable login( final String username, final String password )
    {
        UsernamePasswordToken usernamePasswordToken = new UsernamePasswordToken( username, password );
        SecurityUtils.setSecurityManager( securityManager );
        Subject subject = SecurityUtils.getSubject();
        subject.login( usernamePasswordToken );

        return subject.getSession().getId();
    }


    @Override
    public boolean isAuthenticated( final Serializable sessionId )
    {

        Subject subject = getSubject( sessionId );

        return subject != null && subject.isAuthenticated();
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

        try
        {
            Host host = peerManager.getLocalPeer().getManagementHost();
            KeyInfo keyInfo = keyManager.generateKey( host, fullname, email );
            user.setKey( keyInfo.getPublicKeyId() );
            LOG.debug( String.format( "%s", keyInfo.toString() ) );
        }
        catch ( HostNotFoundException | KeyManagerException e )
        {
            LOG.error( e.toString(), e );
        }

        userDataService.persist( user );
        return user.getId() != null;
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

        if ( user.getId() == null )
        {
            generateKey( user );
        }

        userDataService.update( user );
        return true;
    }


    private void generateKey( final User user )
    {

        Host host = null;
        long threshold = System.currentTimeMillis() + 1000 * 60;
        do
        {
            try
            {
                host = peerManager.getLocalPeer().getManagementHost();
            }
            catch ( HostNotFoundException ignore )
            {
            }
            try
            {
                LOG.debug( String.format( "Waiting for management host...[%s]", host == null ? "OFF" : "ON" ) );
                Thread.sleep( 1000 );
            }
            catch ( InterruptedException ignore )
            {
            }
        }
        while ( host == null && threshold > System.currentTimeMillis() );

        if ( host != null )
        {
            KeyInfo keyInfo = null;
            try
            {
                keyInfo = keyManager.generateKey( host, user.getUsername(), user.getEmail() );
                user.setKey( keyInfo.getPublicKeyId() );
                LOG.debug( String.format( "%s", keyInfo.toString() ) );
            }
            catch ( KeyManagerException e )
            {
                LOG.error( e.toString(), e );
            }
        }
        else
        {
            LOG.warn( "Management host not available on generating keys." );
        }
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
        RoleEntity role = new RoleEntity( "" );
        return role;
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
        String result = sha256Hash.toHex();
        return result;
    }
}
