package io.subutai.core.identity.impl;


import java.io.IOException;
import java.security.AccessControlContext;
import java.security.AccessControlException;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import io.subutai.common.dao.DaoManager;
import io.subutai.common.security.objects.PermissionOperation;
import io.subutai.common.security.objects.PermissionScope;
import io.subutai.common.security.objects.TokenType;
import io.subutai.common.security.objects.UserStatus;
import io.subutai.common.security.objects.UserType;
import io.subutai.common.security.token.TokenUtil;
import io.subutai.core.identity.api.model.Session;
import io.subutai.core.identity.api.model.UserToken;
import io.subutai.core.identity.impl.dao.IdentityDataServiceImpl;
import io.subutai.core.identity.impl.model.PermissionEntity;
import io.subutai.core.identity.impl.model.RoleEntity;
import io.subutai.core.identity.impl.model.SessionEntity;
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

import org.apache.commons.lang.time.DateUtils;

import com.google.common.base.Strings;

import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.dao.IdentityDataService;
import io.subutai.core.identity.api.model.Permission;
import io.subutai.core.identity.api.model.Role;
import io.subutai.core.identity.api.model.User;
import io.subutai.common.security.objects.PermissionObject;
import io.subutai.core.identity.impl.model.UserTokenEntity;


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


    /* *************************************************
     */
    public IdentityManagerImpl()
    {

    }

    public void init()
    {
        createDefaultUsers();
    }

    public void destroy()
    {

    }
    //*****************************************************



    //*****************************************************
    private void createDefaultUsers()
    {
        identityDataService = new IdentityDataServiceImpl( daoManager );

        if ( identityDataService.getAllUsers().size() < 1 )
        {
            PermissionObject permsp[] = PermissionObject.values();
            Role role = null;
            Permission per = null;


            //***Create User ********************************************
            User internal = createUser( "internal", "internal", "System User", "internal@subutai.io" ,1);
            User admin    = createUser( "admin", "secret", "Administrator", "admin@subutai.io" ,2);
            User manager  = createUser( "manager","manager", "Manager", "manager@subutai.io" ,2);
            User karaf    = createUser( "karaf", "karaf", "Karaf Manager", "karaf@subutai.io" ,2);
            //***********************************************************

            //***Create Token *******************************************
            createUserToken( internal ,"" ,"" ,"", TokenType.Permanent.getId(),null);
            //***********************************************************


            //****Create Roles ******************************************
            role = createRole( "Karaf-Manager", UserType.Regular.getId() );
            assignUserRole( karaf.getId(), role );
            assignUserRole( admin.getId(), role );

            per = createPermission( PermissionObject.KarafServerAdministration.getId() , 1, true, true, true, true );
            assignRolePermission( role.getId(), per );

            per = createPermission( PermissionObject.KarafServerManagement.getId() , 1, true, true, true, true );
            assignRolePermission( role.getId(), per );
            //*********************************************

            //*********************************************
            role = createRole( "Administrator", UserType.Regular.getId() );
            assignUserRole( admin.getId(), role );

            for ( int a = 0; a < permsp.length; a++ )
            {
                per = createPermission( permsp[a].getId(), 1, true, true, true, true );
                assignRolePermission( role.getId(), per );
            }
            //*********************************************

            //*********************************************
            role = createRole( "Manager", UserType.Regular.getId() );
            assignUserRole( manager.getId(), role );

            //*********************************************
            for ( int a = 0; a < permsp.length; a++ )
            {
                if(permsp[a] != PermissionObject.IdentityManagement &&
                        permsp[a] != PermissionObject.KarafServerAdministration &&
                        permsp[a] != PermissionObject.KarafServerManagement    &&
                        permsp[a] != PermissionObject.PeerManagement &&
                        permsp[a] != PermissionObject.ResourceManagement     )
                {
                    per = createPermission( permsp[a].getId(), 3, true, true, true, true );
                    assignRolePermission( role.getId(), per );
                }
            }
            //*********************************************

            //*********************************************
            role = createRole( "Internal-System", UserType.System.getId() );
            assignUserRole( internal.getId(), role );

            //*********************************************
            for ( int a = 0; a < permsp.length; a++ )
            {
                if(permsp[a] != PermissionObject.IdentityManagement &&
                        permsp[a] != PermissionObject.KarafServerAdministration &&
                        permsp[a] != PermissionObject.KarafServerManagement     )
                {
                    per = createPermission( permsp[a].getId(), 1, true, true, true, true );
                    assignRolePermission( role.getId(), per );
                }
            }
            //*********************************************
        }
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
    public User login( String userName, String password )
    {
        try
        {
            User user = null;

            CallbackHandler ch = getCalbackHandler( userName, password );
            Subject subject = new Subject();
            LoginContext loginContext = new LoginContext( "karaf", subject, ch );
            loginContext.login();

            while(subject.getPrivateCredentials().iterator().hasNext())
            {
                Object obj = subject.getPrivateCredentials().iterator().next();

                if(obj instanceof UserEntity)
                {
                    user = (User)obj;
                    user.setSubject( subject );
                    break;
                }
            }

            return user;
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
    public Session startSession( User user )
    {
        try
        {
            Session userSession = new SessionEntity();
            userSession.setUser( user );

            identityDataService.persistSession( userSession );

            return userSession;
        }
        catch ( Exception ex )
        {
            return null;
        }
    }


    /* *************************************************
     */
    @RolesAllowed( "Identity-Management|A|Write" )
    @Override
    public UserToken createUserToken( User user, String token, String secret, String issuer,int tokenType ,Date validDate)
    {
        try
        {
            UserToken userToken = new UserTokenEntity();

            if( Strings.isNullOrEmpty(token))
                token =  UUID.randomUUID().toString();
            if( Strings.isNullOrEmpty(issuer))
                issuer =  "io.subutai";
            if( Strings.isNullOrEmpty(secret))
                secret =  UUID.randomUUID().toString();
            if(validDate == null)
                validDate = DateUtils.addHours( new Date(System.currentTimeMillis()),1);

            userToken.setToken(token );
            userToken.setHashAlgorithm( "HS256" );
            userToken.setIssuer( issuer );
            userToken.setSecret(secret);
            userToken.setUser( user );
            userToken.setType( tokenType );
            userToken.setValidDate( validDate );

            identityDataService.persistUserToken( userToken );

            return userToken;
        }
        catch( Exception ex )
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
        try
        {
            //loginContext.logout();
        }
        catch ( Exception e )
        {
        }
    }


    /* *************************************************
     */
    @PermitAll
    @Override
    public String getUserToken( String userName, String password )
    {
        String token = "";

        User user = authenticateUser( userName, password );

        if(user!=null)
        {
            UserToken uToken = identityDataService.getUserToken( user.getId() );

            if(uToken == null)
            {
                uToken = createUserToken( user, "", "", "" ,TokenType.Session.getId(),null);
            }

            token = uToken.getFullToken();
        }

        return token;

    }

    /* *************************************************
     */
    @PermitAll
    @Override
    public User authenticateByToken( String token )
    {
        String subject = TokenUtil.getSubject( token);

        UserToken userToken = identityDataService.getValidUserToken( subject );

        if(userToken!=null)
        {
            if ( !TokenUtil.verifySignature( token, userToken.getSecret() ) )
            {
                return null;
            }
            else
            {
                return userToken.getUser();
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
    public User authenticateUser( String userName, String password )
    {
        User user = null;

        if(userName.equalsIgnoreCase( "token" ))
        {
            user = authenticateByToken(password);
        }
        else
        {
            user = identityDataService.getUserByUsername( userName );

            if ( user != null )
            {
                if ( !user.getPassword().equals( password )
                        || user.getStatus() == UserStatus.Disabled.getId() )
                {
                    return null;
                }
            }
            else
            {
                return null;
            }
        }

        return user;
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
    @PermitAll
    @Override
    public User getUser(long userId) { return identityDataService.getUser(userId ); }



    /* *************************************************
     */
    @PermitAll
    @Override
    public User getLoggedUser()
    {
        User user = null;
        try
        {
            AccessControlContext acc =  AccessController.getContext();
            if  (acc == null)
            {
                throw  new  RuntimeException("access control context is null");
            }

            Subject subject = Subject.getSubject(acc);
            if  (subject == null)
            {
                throw  new  RuntimeException("subject is null");
            }

            while(subject.getPrivateCredentials().iterator().hasNext())
            {
                Object obj = subject.getPrivateCredentials().iterator().next();

                if(obj instanceof UserEntity)
                {
                    user = (User)obj;
                    user.setSubject( subject );
                    break;
                }
            }

            return user;
        }
        catch(RuntimeException ex)
        {
            return null;
        }
        catch(Exception ex)
        {
            return null;
        }
    }


    /* *************************************************
     */
    @RolesAllowed( "Identity-Management|A|Write" )
    @Override
    public User createUser( String userName, String password, String fullName, String email, int type )
    {
        //***************Cannot use TOKEN keyword *******
        if(userName.equalsIgnoreCase( "token" ))
        {
            throw new IllegalArgumentException("Cannot use TOKEN keyword.");
        }
        //***********************************************

        if(Strings.isNullOrEmpty( password ))
        {
            password = Integer.toString( ( new Random() ).nextInt());
        }

        User user = new UserEntity();
        user.setUserName( userName );
        user.setPassword( password );
        user.setEmail( email );
        user.setFullName( fullName );
        user.setType( type );

        identityDataService.persistUser( user );

        return user;
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
    @RolesAllowed( "Identity-Management|A|Delete" )
    @Override
    public void removeUserRole( long userId, Role role )
    {
        identityDataService.removeUserRole( userId, role );
    }


    /* *************************************************
     */
    @RolesAllowed( "Identity-Management|A|Delete" )
    @Override
    public void removeUserAllRoles( long userId )
    {
        identityDataService.removeUserAllRoles( userId );
    }


    /* *************************************************
     */
    @PermitAll
    @Override
    public boolean changeUserPassword( long userId, String oldPassword , String newPassword )
    {
        User user = identityDataService.getUser( userId );

        //******Cannot update Internal User *************
        if(user.getType() == UserType.System.getId() )
        {
            throw new AccessControlException("Internal User cannot be updated");
        }

        //***********************************************
        if(!user.getPassword().equals( oldPassword ))
        {
            throw new AccessControlException("Invalid old password specified");
        }
        else
        {
            user.setPassword( newPassword );
            identityDataService.updateUser( user );
            return true;
        }
        //***********************************************
    }


    /* *************************************************
     */
    @RolesAllowed( "Identity-Management|A|Update" )
    @Override
    public void updateUser( User user )
    {
        //******Cannot update Internal User *************
        if(user.getType() == UserType.System.getId() )
        {
            throw new AccessControlException("Internal User cannot be updated");
        }
        //***********************************************

        identityDataService.updateUser( user );
    }


    /* *************************************************
     */
    @RolesAllowed( "Identity-Management|A|Delete" )
    @Override
    public void removeUser( long userId )
    {
        //******Cannot remove Internal User *************
        User user = identityDataService.getUser( userId );
        if(user.getType() == UserType.System.getId() )
        {
            throw new AccessControlException("Internal User cannot be removed");
        }
        //***********************************************

        identityDataService.removeUser( userId );
    }


    /* *************************************************
     */
    @PermitAll
    @Override
    public boolean isUserPermitted( User user ,PermissionObject permObj, PermissionScope permScope, PermissionOperation permOp )
    {
        boolean isPermitted = false;

        List<Role> roles = user.getRoles();

        for(Role role:roles)
        {
            for(Permission permission:role.getPermissions())
            {
                if(permission.getId() == permObj.getId() && permission.getScope() == permScope.getId())
                {
                    switch ( permOp )
                    {
                        case Read:
                            return permission.isRead();
                        case Write:
                            return permission.isWrite() ;
                        case Update:
                            return permission.isUpdate();
                        case Delete:
                            return permission.isDelete();
                    }
                }
            }
        }

        return isPermitted;
    }


    /* *************************************************
     */
    @RolesAllowed( "Identity-Management|A|Write" )
    @Override
    public Role createRole( String roleName, int roleType )
    {
        Role role = new RoleEntity();
        role.setName( roleName );
        role.setType( roleType );

        identityDataService.persistRole( role );

        return role;
    }



    /* *************************************************
     */
    @PermitAll
    @Override
    public List<Role> getAllRoles()
    {
        return identityDataService.getAllRoles();
    }



    /* *************************************************
     */
    @Override
    public Role getRole(long roleId) { return identityDataService.getRole(roleId); }



    /* *************************************************
     */
    @RolesAllowed( "Identity-Management|A|Update" )
    @Override
    public void updateRole( Role role )
    {
        //******Cannot update Internal Role *************
        if(role.getType() == UserType.System.getId() )
        {
            throw new AccessControlException("Internal Role cannot be updated");
        }
        //***********************************************

        identityDataService.updateRole( role );
    }


    /* *************************************************
     */
    @RolesAllowed( "Identity-Management|A|Delete" )
    @Override
    public void removeRole( long roleId )
    {
        //******Cannot remove Internal Role *************
        Role role = identityDataService.getRole( roleId );

        if(role.getType() == UserType.System.getId() )
        {
            throw new AccessControlException("Internal Role cannot be removed");
        }
        //***********************************************

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
    @RolesAllowed( "Identity-Management|A|Write" )
    @Override
    public void removeAllRolePermissions( long roleId )
    {
        identityDataService.removeAllRolePermissions( roleId );
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
    @PermitAll
    @Override
    public List<Permission> getAllPermissions()
    {
        return identityDataService.getAllPermissions();
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

}
