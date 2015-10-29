package io.subutai.core.identity.impl;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.subutai.common.dao.DaoManager;
import io.subutai.core.identity.api.token.UserTokenManager;
import io.subutai.core.identity.impl.dao.IdentityDataServiceImpl;
import io.subutai.core.identity.impl.model.PermissionEntity;
import io.subutai.core.identity.impl.model.RoleEntity;
import io.subutai.core.identity.impl.model.UserEntity;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.dao.IdentityDataService;
import io.subutai.core.identity.api.model.Permission;
import io.subutai.core.identity.api.model.Role;
import io.subutai.core.identity.api.model.User;
import io.subutai.common.security.objects.PermissionObject;
import io.subutai.common.security.objects.RoleType;


/**
 *
 *
 */
@PermitAll
public class IdentityManagerImpl implements IdentityManager
{

    private static final Logger LOGGER = LoggerFactory.getLogger( IdentityManagerImpl.class.getName() );

    private IdentityDataService identityDataService = null;
    private DaoManager daoManager = null;
    private UserTokenManager tokenManager;

    public IdentityManagerImpl()
    {
    }


    public void init()
    {
        identityDataService = new IdentityDataServiceImpl( daoManager );

        if ( identityDataService.getAllUsers().size() < 1 )
        {

            User user1 = createUser( "karaf", "karaf", "Karaf Full Name", "karaf@karaf.com" );
            User user2 = createUser( "admin", "admin", "Admin Full Name", "admin@karaf.com" );

            RoleType roleTypes[] = RoleType.values();
            PermissionObject permsp[] = PermissionObject.values();

            Role role = null;
            for ( int x = 0; x < roleTypes.length; x++ )
            {
                role = createRole( roleTypes[x].getName(), ( short ) roleTypes[x].getId() );

                assignUserRole( user1.getId(), role );
                assignUserRole( user2.getId(), role );
            }

            for ( int x = 0; x < permsp.length; x++ )
            {
                Permission per = createPermission( permsp[x].getId(), 1, true, true, true, true );

                assignRolePermission( role.getId(), per );
            }
        }
    }


    /* *************************************************
     */
    public void destroy()
    {

    }


    /* *************************************************
     */
    private CallbackHandler getCalbackHandler( final String userName, final String password )
    {
        CallbackHandler callbackHandler = new CallbackHandler()
        {
            public void handle( Callback[] callbacks ) throws IOException, UnsupportedCallbackException
            {
                for ( Callback callback : callbacks )
                {
                    if ( callback instanceof NameCallback )
                    {
                        ( ( NameCallback ) callback ).setName( userName );
                    }
                    else if ( callback instanceof PasswordCallback )
                    {
                        ( ( PasswordCallback ) callback ).setPassword( password.toCharArray() );
                    }
                    else
                    {
                        throw new UnsupportedCallbackException( callback );
                    }
                }
            }
        };

        return callbackHandler;
    }


    /* *************************************************
     */
    @PermitAll
    @Override
    public Subject login( String userName, String password )
    {
        try
        {
            CallbackHandler ch = getCalbackHandler( userName, password );
            Subject subject = new Subject();
            LoginContext loginContext = new LoginContext( "karaf", subject, ch );
            loginContext.login();

            return subject;
        }
        catch ( Exception ex )
        {
            return null;
        }
    }


    /* *************************************************
     */
    @PermitAll
    @Override
    public void logout()
    {
        // TODO Auto-generated method stub
    }


    /* *************************************************
     */
    @PermitAll
    @Override
    public User authenticateUser( String userName, String password )
    {
        User user = identityDataService.getUserByUsername( userName );

        if ( user != null )
        {
            if ( user.getPassword().equals( password ) )
            {
                return user;
            }
            else
            {
                return null;
            }
        }
        else
        {
            return null;
        }
    }


    /* *************************************************
     */
    @PermitAll
    @Override
    public List<User> getAllUsers()
    {
        List<User> result = new ArrayList<>();
        result.addAll( identityDataService.getAllUsers() );
        return result;
    }


    /* *************************************************
     */
    @RolesAllowed( "Identity-Management|A|Write" )
    @Override
    public void assignUserRole( long userId, Role role )
    {
        identityDataService.assignUserRole( userId, role );
    }


    /* *************************************************
     */
    @RolesAllowed( "Identity-Management|A|Write" )
    @Override
    public User createUser( String userName, String password, String fullName, String email )
    {
        User user = new UserEntity();
        user.setUserName( userName );
        user.setPassword( password );
        user.setEmail( email );
        user.setFullName( fullName );

        identityDataService.persistUser( user );

        return user;
    }


    /* *************************************************
     */
    @RolesAllowed( "Identity-Management|A|Delete" )
    @Override
    public void removeUser( long userId )
    {
        identityDataService.removeUser( userId );
    }


    /* *************************************************
     */
    @RolesAllowed( "Identity-Management|A|Write" )
    @Override
    public Role createRole( String roleName, short roleType )
    {
        Role role = new RoleEntity();
        role.setName( roleName );
        role.setType( roleType );

        identityDataService.persistRole( role );

        return role;
    }


    /* *************************************************
     */
    @RolesAllowed( "Identity-Management|A|Delete" )
    @Override
    public void removeRole( long roleId )
    {
        identityDataService.removeRole( roleId );
    }


    /* *************************************************
     */
    @RolesAllowed( "Identity-Management|A|Write" )
    @Override
    public Permission createPermission( int objectId, int scope, boolean read, boolean write, boolean update,
                                        boolean delete )
    {
        Permission permission = new PermissionEntity();
        permission.setObject( objectId );
        permission.setScope( scope );
        permission.setRead( read );
        permission.setWrite( write );
        permission.setUpdate( update );
        permission.setDelete( delete );

        identityDataService.persistPermission( permission );

        return permission;
    }


    /* *************************************************
     */
    @RolesAllowed( "Identity-Management|A|Write" )
    @Override
    public void assignRolePermission( long roleId, Permission permission )
    {
        identityDataService.assignRolePermission( roleId, permission );
    }


    /* *************************************************
     */
    @RolesAllowed( "Identity-Management|A|Delete" )
    @Override
    public void removePermission( long permissionId )
    {
        identityDataService.removePermission( permissionId );
    }


    /* *************************************************
     */
    @Override
    public IdentityDataService getIdentityDataService()
    {
        return identityDataService;
    }

    /* *************************************************
     */
    public DaoManager getDaoManager()
    {
        return daoManager;
    }


    /* *************************************************
     */
    public void setDaoManager( DaoManager daoManager )
    {
        this.daoManager = daoManager;
    }


    /* *************************************************
     */
    public UserTokenManager getTokenManager()
    {
        return tokenManager;
    }


    /* *************************************************
    */
    public void setTokenManager( final UserTokenManager tokenManager )
    {
        this.tokenManager = tokenManager;
    }
}
