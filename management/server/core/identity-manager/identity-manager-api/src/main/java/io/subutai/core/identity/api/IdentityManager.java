package io.subutai.core.identity.api;


import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import javax.annotation.security.PermitAll;
import javax.servlet.http.HttpServletRequest;

import org.apache.cxf.message.Message;

import io.subutai.common.security.exception.SystemSecurityException;
import io.subutai.common.security.objects.PermissionObject;
import io.subutai.common.security.objects.PermissionOperation;
import io.subutai.common.security.objects.PermissionScope;
import io.subutai.core.identity.api.dao.IdentityDataService;
import io.subutai.core.identity.api.exception.TokenCreateException;
import io.subutai.core.identity.api.exception.TokenParseException;
import io.subutai.core.identity.api.exception.UserExistsException;
import io.subutai.core.identity.api.model.Permission;
import io.subutai.core.identity.api.model.Role;
import io.subutai.core.identity.api.model.Session;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.identity.api.model.UserDelegate;
import io.subutai.core.identity.api.model.UserToken;


/**
 *
 */
public interface IdentityManager
{
    int JWT_TOKEN_EXPIRATION_TIME = 3600;
    String TOKEN_ISSUER = "Subutai Peer OS";
    String SYSTEM_USERNAME = "internal";
    String ADMIN_USERNAME = "admin";
    String TOKEN_ID = "token";


    String ADMIN_DEFAULT_PWD = "secret";

    String ENV_MANAGER_ROLE = "Environment-Manager";
    String TEMPLATE_MANAGER_ROLE = "Template-Manager";
    //    String ENV_OWNER_ROLE = "Environment-Owner";


    void init();

    void destroy();

    TokenHelper buildTokenHelper( String token ) throws TokenParseException;

    /**
     * Token auth login
     *
     * @param request HttpServletRequest
     *
     * @return Session @see Session
     */
    Session login( HttpServletRequest request, Message message );

    /* *************************************************
     */
    void removeRolePermission( long roleId, Permission permission );

    /* *************************************************
     */
    List<Permission> getAllPermissions();


    /* *************************************************
     */
    void updatePermission( Permission permission );


    /* *************************************************
     *
     */
    public IdentityDataService getIdentityDataService();


    /* *************************************************
     */
    SessionManager getSessionManager();


    /* *************************************************
     */
    void logout();


    /* *************************************************
     */
    String getUserToken( String userName, String password );


    /* *************************************************
     */
    UserToken getUserToken( long userId );


    /* *************************************************
     */
    String getSystemUserToken();


    /* *************************************************
     */
    UserToken updateTokenAndSession( long userId );


    /* *************************************************
     */
    @PermitAll
    String updateUserAuthId( User user, String authId ) throws SystemSecurityException;

    /* *************************************************
     */
    String getEncryptedUserAuthId( User user ) throws SystemSecurityException;


    /* *************************************************
     */
    User authenticateByAuthSignature( String fingerprint, String signedAuth );


    /* *************************************************
     */
    User authenticateByToken( String token ) throws SystemSecurityException;


    /* *************************************************
     */
    User authenticateUser( String userName, String password );


    /* *************************************************
     */
    void setPeerOwner( User user );


    /* *************************************************
     */
    String getPeerOwnerId();


    /* *************************************************
     */
    User getUserByKeyId( String keyId );


    /* *************************************************
     */
    User getUserByFingerprint( String fingerprint );


    /* *************************************************
     */
    UserDelegate getUserDelegate( long userId );


    /* *************************************************
     */
    UserDelegate getUserDelegate( User user );


    /* *************************************************
     */
    UserDelegate getUserDelegate( String id );


    /* *************************************************
     */
    void setUserPublicKey( long userId, String publicKeyASCII );


    /* *************************************************
     */
    List<User> getAllUsers();


    /* *************************************************
     */
    void assignUserRole( long userId, Role role );


    /* *************************************************
     */
    void assignUserRole( User user, Role role );


    /* *************************************************
     */
    User getUser( long userId );


    /* *************************************************
     */
    User getSystemUser();


    /* *************************************************
     */
    User getActiveUser();


    /* *************************************************
     */
    Session getActiveSession();


    /* *************************************************
     */
    void runAs( Session userSession, Callable action );


    /* *************************************************
     */
    void runAs( Session userSession, Runnable action );


    /* *************************************************
     */
    User createTempUser( String userName, String password, String fullName, String email, int type );


    //**************************************************************
    void setTrustLevel( User user, int trustLevel );


    /* *************************************************
     */
    UserDelegate createUserDelegate( User user, String delegateUserId, boolean genKeyPair );


    /* *************************************************
     */
    User createUser( String userName, String password, String fullName, String email, int type, int trustLevel,
                     boolean generateKeyPair, boolean createUserDelegate )
            throws SystemSecurityException, UserExistsException;

    User modifyUser( User user, String password ) throws SystemSecurityException;


    void approveDelegatedUser( String trustMessage );


    void createIdentityDelegationDocument();


    User getUserByUsername( String userName );


    /* *************************************************
     */
    void removeUserRole( long userId, Role role );


    /* *************************************************
     */
    void removeUserRole( User user, Role role );


    /* *************************************************
     */
    @PermitAll
    boolean changeUserPassword( String userName, String oldPassword, String newPassword )
            throws SystemSecurityException;

    /* *************************************************
     */
    boolean changeUserPassword( long userId, String oldPassword, String newPassword ) throws SystemSecurityException;


    /* *************************************************
     */
    @PermitAll
    boolean changeUserPassword( User user, String oldPassword, String newPassword ) throws SystemSecurityException;

    void resetPassword( String username, String newPassword, String sign ) throws SystemSecurityException;

    /**
     * Issues a token to be signed and passed to
     * {@link IdentityManager#resetPassword(java.lang.String username, * java.lang.String newPassword, java.lang.String sign)}. TTL of token is 30 seconds
     *
     * @return token to be signed by user PGP key
     */
    String getSignToken();

    /* *************************************************
     */
    void updateUser( User user );

    /*
     * ************************************************
     */
    void updateUser( User user, String publicKey );


    /* *************************************************
     */
    void removeUser( long userId );


    /* *************************************************
     */
    boolean isUserPermitted( User user, PermissionObject permObj, PermissionScope permScope,
                             PermissionOperation permOp );


    /**
     * Returns true if user is tenant manager, false otherwise
     */
    boolean isTenantManager();

    /**
     * Returns true if user is internal system user, false otherwise
     */
    boolean isSystemUser();

    /**
     * Returns true if user has admin role, false otherwise
     */
    boolean isAdmin();

    /* *************************************************
     *
     */
    Role createRole( String roleName, int roleType );


    /* ***********************************
     *  Authenticate Internal User
     */
    Session loginSystemUser();


    String issueJWTToken( final String origin ) throws TokenCreateException;

    boolean verifyJWTToken( final String token ) throws TokenParseException;

    /* *************************************************
     *
     */
    Session login( String userName, String password );


    /* *************************************************
     */
    List<Role> getAllRoles();


    Role findRoleByName( String roleName );


    /* *************************************************
     */
    Role getRole( long roleId );


    /* *************************************************
     */
    void updateRole( Role role );


    /* *************************************************
     */
    void removeRole( long roleId );


    /* *************************************************
     */
    Permission createPermission( int objectId, int scope, boolean read, boolean write, boolean update, boolean delete );


    /* *************************************************
     */
    void assignRolePermission( long roleId, Permission permission );


    /* *************************************************
     */
    void assignRolePermission( Role role, Permission permission );


    /* *************************************************
     */
    void removeAllRolePermissions( long roleId );


    /* *************************************************
     */
    void removePermission( long permissionId );


    /* *************************************************
     */
    Session authenticateSession( String login, String password );


    /* *************************************************
     */
    UserToken createUserToken( User user, String token, String secret, String issuer, int tokenType, Date validDate );


    /* *************************************************
     */
    List<UserToken> getAllUserTokens();


    /* *************************************************
     */
    void extendTokenTime( UserToken token, int minutes );


    /* *************************************************
     */
    void updateUserToken( UserToken token );


    /* *************************************************
     */
    public void updateUserToken( String tokenId, User user, String token, String secret, String issuer, int tokenType,
                                 Date validDate );


    /* *************************************************
     */
    void removeUserToken( String tokenId );


    /* *************************************************
     */
    SecurityController getSecurityController();
}
