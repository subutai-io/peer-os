package io.subutai.core.identity.impl;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.AccessControlContext;
import java.security.AccessControlException;
import java.security.AccessController;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.servlet.http.HttpServletRequest;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.cxf.message.Message;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import io.subutai.common.dao.DaoManager;
import io.subutai.common.security.crypto.pgp.KeyPair;
import io.subutai.common.security.exception.IdentityExpiredException;
import io.subutai.common.security.exception.InvalidLoginException;
import io.subutai.common.security.exception.SystemSecurityException;
import io.subutai.common.security.objects.KeyTrustLevel;
import io.subutai.common.security.objects.Ownership;
import io.subutai.common.security.objects.PermissionObject;
import io.subutai.common.security.objects.PermissionOperation;
import io.subutai.common.security.objects.PermissionScope;
import io.subutai.common.security.objects.SecurityKeyType;
import io.subutai.common.security.objects.TokenType;
import io.subutai.common.security.objects.UserStatus;
import io.subutai.common.security.objects.UserType;
import io.subutai.common.security.relation.RelationManager;
import io.subutai.common.security.relation.RelationVerificationException;
import io.subutai.common.security.relation.model.Relation;
import io.subutai.common.security.relation.model.RelationInfoMeta;
import io.subutai.common.security.relation.model.RelationMeta;
import io.subutai.common.security.token.TokenUtil;
import io.subutai.common.util.JsonUtil;
import io.subutai.common.util.ServiceLocator;
import io.subutai.common.util.StringUtil;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.SecurityController;
import io.subutai.core.identity.api.SessionManager;
import io.subutai.core.identity.api.TokenHelper;
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
import io.subutai.core.identity.impl.dao.IdentityDataServiceImpl;
import io.subutai.core.identity.impl.model.PermissionEntity;
import io.subutai.core.identity.impl.model.RoleEntity;
import io.subutai.core.identity.impl.model.SessionEntity;
import io.subutai.core.identity.impl.model.UserDelegateEntity;
import io.subutai.core.identity.impl.model.UserEntity;
import io.subutai.core.identity.impl.model.UserTokenEntity;
import io.subutai.core.identity.impl.utils.SecurityUtil;
import io.subutai.core.identity.impl.utils.TokenHelperImpl;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.core.security.api.crypto.EncryptionTool;
import io.subutai.core.security.api.crypto.KeyManager;
import io.subutai.core.security.api.model.SecurityKey;
import io.subutai.core.template.api.TemplateManager;


/**
 * Overall Subutai Identity Management
 */
@PermitAll
public class IdentityManagerImpl implements IdentityManager
{

    private static final Logger LOGGER = LoggerFactory.getLogger( IdentityManagerImpl.class.getName() );

    private static final int IDENTITY_LIFETIME = 240; //days
    private static final String SYSTEM_USER_FULL_NAME = "System User";
    private static final String ADMIN_EMAIL = "admin@subutai.io";
    private static final String SYSTEM_USER_EMAIL = "system@subutai.io";
    private static final String ADMIN_USER_FULL_NAME = "Admin User";
    private static final String ADMIN_ROLE = "Administrator";
    private static final String SYSTEM_ROLE = "Internal-System";
    private static final long SIGN_TOKEN_TTL_SEC = 30;

    private IdentityDataService identityDataService = null;
    private SecurityController securityController = null;
    private DaoManager daoManager = null;
    private SecurityManager securityManager = null;
    private SessionManager sessionManager = null;

    private Cache<String, Boolean> signTokensCache =
            CacheBuilder.newBuilder().expireAfterWrite( SIGN_TOKEN_TTL_SEC, TimeUnit.SECONDS ).build();
    private Cache<String, String> jwtTokenCache;


    /* *************************************************
     */
    public IdentityManagerImpl()
    {

    }


    //*****************************************
    @Override
    public void init()
    {
        identityDataService = new IdentityDataServiceImpl( daoManager );
        sessionManager = new SessionManagerImpl( identityDataService );
        securityController = new SecurityControllerImpl();
        sessionManager.startSessionController();
        jwtTokenCache =
                CacheBuilder.newBuilder().expireAfterWrite( JWT_TOKEN_EXPIRATION_TIME, TimeUnit.SECONDS ).build();


        try
        {
            createDefaultUsers();
        }
        catch ( Exception e )
        {
            LOGGER.error( "***** Error! Error creating users:" + e.toString(), e );
        }
    }


    //*****************************************
    @Override
    public void destroy()
    {
        jwtTokenCache.invalidateAll();
        sessionManager.stopSessionController();
    }


    @Override
    public TokenHelper buildTokenHelper( final String token ) throws TokenParseException
    {
        return new TokenHelperImpl( token );
    }


    //*****************************************************
    private void createDefaultUsers() throws SystemSecurityException, UserExistsException
    {
        if ( identityDataService.getAllUsers().isEmpty() )
        {
            PermissionObject permsp[] = PermissionObject.values();
            Role role;
            Permission per;

            //---------------- internal system user
            // create system user
            User internal =
                    createUser( SYSTEM_USERNAME, "", SYSTEM_USER_FULL_NAME, SYSTEM_USER_EMAIL, UserType.SYSTEM.getId(),
                            KeyTrustLevel.FULL.getId(), false, false );

            // Create System Role for internal user
            role = createRole( SYSTEM_ROLE, UserType.SYSTEM.getId() );
            assignUserRole( internal, role );

            //assign permission for system role
            for ( final PermissionObject aPermsp : permsp )
            {
                if ( aPermsp != PermissionObject.IDENTITY_MANAGEMENT
                        && aPermsp != PermissionObject.KARAF_SERVER_ADMINISTRATION
                        && aPermsp != PermissionObject.TENANT_MANAGEMENT )
                {
                    per = createPermission( aPermsp.getId(), PermissionScope.ALL_SCOPE.getId(), true, true, true,
                            true );
                    assignRolePermission( role, per );
                }
            }

            // create system token for internal user
            createUserToken( internal, "", "", "", TokenType.PERMANENT.getId(), null );


            //---------------- admin user
            // create admin user
            User admin = createUser( ADMIN_USERNAME, ADMIN_DEFAULT_PWD, ADMIN_USER_FULL_NAME, ADMIN_EMAIL,
                    UserType.REGULAR.getId(), KeyTrustLevel.FULL.getId(), true, true );


            // create admin role
            role = createRole( ADMIN_ROLE, UserType.SYSTEM.getId() );

            // assign to admin user
            assignUserRole( admin, role );

            for ( final PermissionObject aPermsp : permsp )
            {
                per = createPermission( aPermsp.getId(), PermissionScope.ALL_SCOPE.getId(), true, true, true, true );
                assignRolePermission( role, per );
            }

            // Create Env Mgr Role (system for bazaar users only)
            role = createRole( ENV_MANAGER_ROLE, UserType.SYSTEM.getId() );

            per = createPermission( PermissionObject.ENVIRONMENT_MANAGEMENT.getId(), PermissionScope.ALL_SCOPE.getId(),
                    true, true, true, true );

            assignRolePermission( role, per );

            // Create Template Mgr Role (system for bazaar users only)
            role = createRole( TEMPLATE_MANAGER_ROLE, UserType.SYSTEM.getId() );

            per = createPermission( PermissionObject.TEMPLATE_MANAGEMENT.getId(), PermissionScope.ALL_SCOPE.getId(),
                    true, true, true, true );

            assignRolePermission( role, per );

            // editable roles -----------------------------

            //            // pre-create env-owner role for regular users
            //            role = createRole( ENV_OWNER_ROLE, UserType.REGULAR.getId() );
            //
            //            per = createPermission( PermissionObject.ENVIRONMENT_MANAGEMENT.getId(), PermissionScope
            // .ALL_SCOPE.getId(),
            //                    true, true, true, true );
            //
            //            assignRolePermission( role, per );


            //***** setPeer Owner By Default ***************
            setPeerOwner( admin );
            //**********************************************
        }
        else
        {
            //todo do we need this else clause at all?
            User admin = identityDataService.getUserByUsername( ADMIN_USERNAME );
            //***** setPeer Owner By Default ***************
            setPeerOwner( admin );
            //**********************************************
        }
    }


    /* *************************************************
     */
    private CallbackHandler getCallbackHandler( final String userName, final String password )
    {

        return new CallbackHandler()
        {
            @Override
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
    }


    /* ***********************************
     *  Authenticate Internal User
     */
    @PermitAll
    @Override
    public Session loginSystemUser()
    {
        return login( TOKEN_ID, getSystemUserToken() );
    }


    @PermitAll
    @Override
    public Session login( HttpServletRequest request, Message message )
    {
        try
        {

            final String bearerToken = getBearerToken( request );
            if ( bearerToken == null )
            {
                return null;
            }

            final TokenHelperImpl token = new TokenHelperImpl( bearerToken );
            String subject = token.getSubject();
            if ( subject == null )
            {
                return null;
            }

            Map<String, List<String>> headers = ( Map<String, List<String>> ) message.get( Message.PROTOCOL_HEADERS );
            headers.put( "subutaiOrigin", Arrays.asList( token.getSubject() ) );

            message.put( Message.PROTOCOL_HEADERS, headers );

            return verifyJWTToken( bearerToken ) ? loginSystemUser() : null;
        }
        catch ( TokenParseException e )
        {
            return null;
        }
    }


    private String getBearerToken( HttpServletRequest request )
    {
        String authorization = request.getHeader( "Authorization" );
        String result = null;
        if ( authorization != null && authorization.startsWith( "Bearer" ) )
        {
            String[] splittedAuthString = authorization.split( "\\s" );
            result = splittedAuthString.length == 2 ? splittedAuthString[1] : null;
        }
        return result;
    }


    @Override
    public String issueJWTToken( String origin ) throws TokenCreateException

    {
        final String secret = UUID.randomUUID().toString();
        DateTime issueDate = DateTime.now();
        DateTime expireDate = issueDate.plusSeconds( JWT_TOKEN_EXPIRATION_TIME );
        String token =
                new TokenHelperImpl( TOKEN_ISSUER, origin, issueDate.toDate(), expireDate.toDate(), secret ).getToken();

        this.jwtTokenCache.put( origin, secret );
        return token;
    }


    @Override
    public boolean verifyJWTToken( String token ) throws TokenParseException

    {
        final TokenHelperImpl signedToken = new TokenHelperImpl( token );
        if ( signedToken.getExpirationTime().before( new Date() ) )
        {
            return false;
        }
        String secret = this.jwtTokenCache.getIfPresent( signedToken.getSubject() );
        return secret != null && signedToken.verify( secret );
    }


    /* *************************************************
     */
    @PermitAll
    @Override
    public Session login( String userName, String password )
    {
        try
        {
            Session userSession = null;

            CallbackHandler ch = getCallbackHandler( userName, password );
            Subject subject = new Subject();
            LoginContext loginContext = new LoginContext( "karaf", subject, ch );
            loginContext.login();

            while ( subject.getPrivateCredentials().iterator().hasNext() )
            {
                Object obj = subject.getPrivateCredentials().iterator().next();

                if ( obj instanceof SessionEntity )
                {
                    userSession = ( Session ) obj;
                    userSession.setSubject( subject );
                    break;
                }
            }

            return userSession;
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
    public SessionManager getSessionManager()
    {
        return sessionManager;
    }


    /**
     * *********************************************************************************** Authenticates user and
     * returns Session
     *
     * @param login Login name  or "token" keyword
     * @param password Password or JWT
     *
     * @return Session object
     */
    @PermitAll
    @Override
    public Session authenticateSession( String login, String password )
    {
        String sessionId;
        Session session;
        User user = null;

        //-------------------------------------
        if ( TOKEN_ID.equalsIgnoreCase( login ) )
        {
            sessionId = password;
        }
        else
        {
            sessionId = UUID.randomUUID() + "-" + System.currentTimeMillis();
        }
        //-------------------------------------

        session = sessionManager.getValidSession( sessionId );

        if ( session == null )
        {
            user = authenticateUser( login, password );

            if ( user == null )
            {
                return null;
            }
        }

        session = sessionManager.startSession( sessionId, session, user );

        return session;
    }


    /**
     * *********************************************************************************** Create JSON Web Token and
     * save in DB
     *
     * @param user input String
     *
     * @return JSON Token
     */
    @RolesAllowed( "Identity-Management|Write" )
    @Override
    public UserToken createUserToken( User user, String token, String secret, String issuer, int tokenType,
                                      Date validDate )
    {
        try
        {
            UserToken userToken = new UserTokenEntity();

            if ( Strings.isNullOrEmpty( token ) )
            {
                token = UUID.randomUUID().toString();
            }
            if ( Strings.isNullOrEmpty( issuer ) )
            {
                issuer = "io.subutai";
            }
            if ( Strings.isNullOrEmpty( secret ) )
            {
                secret = UUID.randomUUID().toString();
            }
            if ( validDate == null )
            {
                validDate = DateUtils
                        .addMinutes( new Date( System.currentTimeMillis() ), sessionManager.getSessionTimeout() );
            }

            userToken.setTokenId( token );
            userToken.setHashAlgorithm( "HS256" );
            userToken.setIssuer( issuer );
            userToken.setSecret( secret );
            userToken.setUserId( user.getId() );
            userToken.setType( tokenType );
            userToken.setValidDate( validDate );

            identityDataService.persistUserToken( userToken );

            return userToken;
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
        //reserved for future
    }


    /**
     * *********************************************************************************** Checks username and password
     * (authenticates), on success returns full token
     *
     * @param userName Login name
     * @param password Password
     *
     * @return Full JWT
     */
    @PermitAll
    @Override
    public String getUserToken( String userName, String password )
    {
        String token = "";

        User user = authenticateUser( userName, password );

        if ( user != null )
        {
            UserToken userToken = getUserToken( user.getId() );

            if ( userToken == null )
            {
                userToken = createUserToken( user, "", "", "", TokenType.SESSION.getId(), null );
            }
            else
            {
                if ( userToken.getType() == TokenType.SESSION.getId() )
                {
                    removeUserToken( userToken.getTokenId() );

                    userToken = createUserToken( user, "", "", "", TokenType.SESSION.getId(), null );
                }
            }

            token = userToken.getFullToken();
        }

        return token;
    }


    @Override
    public UserToken getUserToken( long userId )
    {
        return identityDataService.getUserToken( userId );
    }


    @Override
    public UserToken updateTokenAndSession( long userId )
    {
        User user = identityDataService.getUser( userId );
        UserToken userToken = createUserToken( user, null, null, null, TokenType.SESSION.getId(), null );

        String sessionId = UUID.randomUUID() + "-" + System.currentTimeMillis();
        sessionManager.startSession( sessionId, null, user );
        return userToken;
    }


    @PermitAll
    @Override
    public String getSystemUserToken()
    {
        User user = identityDataService.getUserByUsername( SYSTEM_USERNAME );

        UserToken userToken = getUserToken( user.getId() );

        return userToken != null ? userToken.getFullToken() : null;
    }


    /**
     * *********************************************************************************** Update (renew) Authorization
     * ID of the User (Which is used by RSA keys to authenticate)
     *
     * @param user User
     * @param authId Authorization ID
     *
     * @return Newly assigned Authorization ID (random string, if authId param is NULL)
     */
    @PermitAll
    @Override
    public String updateUserAuthId( User user, String authId ) throws SystemSecurityException
    {
        if ( user != null )
        {
            if ( Strings.isNullOrEmpty( authId ) )
            {
                authId = UUID.randomUUID().toString();
            }

            if ( authId.length() < 4 )
            {
                throw new IllegalArgumentException( "Password cannot be shorter than 4 characters" );
            }

            if ( user.getAuthId().equals( authId ) )
            {
                throw new IllegalArgumentException( "NewPassword cannot be the same as old one." );
            }


            user.setAuthId( authId );
            user.setValidDate( DateUtils.addDays( new Date( System.currentTimeMillis() ), IDENTITY_LIFETIME ) );
            identityDataService.updateUser( user );

            return authId;
        }

        return "";
    }


    /**
     * *********************************************************************************** Encrypt with user's PGP
     * private key and return encrypted Authorization id
     *
     * @param user User
     *
     * @return Encrypted authorization id
     */
    @PermitAll
    @Override
    public String getEncryptedUserAuthId( User user ) throws SystemSecurityException
    {
        try
        {
            if ( user != null )
            {
                String authId = user.getAuthId();
                PGPPublicKey pubKey = securityManager.getKeyManager().getPublicKey( user.getSecurityKeyId() );

                if ( pubKey != null )
                {
                    byte enc[] = securityManager.getEncryptionTool().encrypt( authId.getBytes(), pubKey, true );

                    return new String( enc );
                }
                else
                {
                    throw new InvalidLoginException( "User Public Key not found." );
                }
            }
            else
            {
                throw new InvalidLoginException( "User not found." );
            }
        }
        catch ( Exception e )
        {
            LOGGER.error( " **** Error creating encrypted userAuth message ****", e );
        }

        return "";
    }


    /**
     * *********************************************************************************** Authenticate user by
     * Authorization id
     *
     * @param fingerprint fingerprint of the key
     * @param signedAuth Signed Authorization id (signedMessage)
     *
     * @return authenticated user
     */
    @PermitAll
    @Override
    public User authenticateByAuthSignature( final String fingerprint, final String signedAuth )
            throws SystemSecurityException
    {
        KeyManager keyManager = securityManager.getKeyManager();
        EncryptionTool encryptionTool = securityManager.getEncryptionTool();

        PGPPublicKeyRing publicKeyRing = keyManager.getPublicKeyRingByFingerprint( fingerprint.toUpperCase() );

        try
        {
            if ( Strings.isNullOrEmpty( signedAuth ) || !encryptionTool
                    .verifyClearSign( signedAuth.trim().getBytes(), publicKeyRing ) )
            {
                throw new InvalidLoginException( "Signed Auth verification failed." );
            }

            User user = getUserByFingerprint( fingerprint );

            if ( user == null )
            {
                throw new InvalidLoginException( "User not found associated with fingerprint: " + fingerprint );
            }

            String authId = new String( encryptionTool.extractClearSignContent( signedAuth.getBytes() ) );

            if ( !user.isIdentityValid() || !user.getAuthId().equals( authId.trim() ) )
            {
                throw new IdentityExpiredException( "User Credentials are expired" );
            }

            return user;
        }
        catch ( Exception e )
        {
            LOGGER.error( " **** Error authenticating user by signed Message ****", e );
            throw new SystemSecurityException( e.getMessage() );
        }
    }


    /**
     * *********************************************************************************** Authenticate user by JWT
     *
     * @param token Token to be checked
     *
     * @return authenticated user
     */
    @PermitAll
    @Override
    public User authenticateByToken( String token ) throws SystemSecurityException
    {
        String subject = TokenUtil.getSubject( token );

        UserToken userToken = identityDataService.getValidUserToken( subject );

        if ( userToken != null && TokenUtil.verifySignature( token, userToken.getSecret() ) )
        {
            return getUser( userToken.getUserId() );
        }
        else
        {
            throw new InvalidLoginException();
        }
    }


    /**
     * *********************************************************************************** Authenticate user with
     * Username and password
     *
     * @param userName Username
     * @param password Password
     *
     * @return authenticated user
     */
    @PermitAll
    @Override
    public User authenticateUser( String userName, String password ) throws SystemSecurityException
    {
        User user;

        if ( TOKEN_ID.equalsIgnoreCase( userName ) )
        {
            user = authenticateByToken( password );
        }
        else if ( userName.length() == 40 )
        {
            user = authenticateByAuthSignature( userName, password.trim() );
        }
        else
        {
            user = identityDataService.getUserByUsername( userName );

            if ( user != null && ( user.getTrustLevel() > 1 ) )
            {
                String pswHash = SecurityUtil.generateSecurePassword( password, user.getSalt() );

                if ( !pswHash.equals( user.getPassword() ) || user.getStatus() == UserStatus.DISABLED.getId() )
                {
                    throw new InvalidLoginException( "***** Invalid Login for user:" + userName );
                }
                else
                {
                    if ( !user.isIdentityValid() )
                    {
                        throw new IdentityExpiredException( "***** Identity Expired for user:" + userName );
                    }
                }
            }
            else
            {
                return null;
            }
        }

        return user;
    }


    /**
     * *********************************************************************************** Sets the Owner of the Peer
     *
     * @param user User that will be set as an owner
     */
    @PermitAll
    @Override
    public void setPeerOwner( User user )
    {
        securityManager.getKeyManager().setPeerOwnerId( user.getSecurityKeyId() );
    }


    /**
     * *********************************************************************************** Sets the Owner of the Peer
     *
     * @return Id of the PeerOwner
     */
    @PermitAll
    @Override
    public String getPeerOwnerId()
    {
        return securityManager.getKeyManager().getPeerOwnerId();
    }


    /* *************************************************
     */
    @PermitAll
    @Override
    public User getUserByKeyId( String keyId )
    {
        return identityDataService.getUserByKeyId( keyId );
    }


    /* *************************************************
     */
    @PermitAll
    @Override
    public User getUserByFingerprint( String fingerprint )
    {
        try
        {
            SecurityKey key = securityManager.getKeyManager().getKeyDataByFingerprint( fingerprint );

            if ( key != null )
            {
                return identityDataService.getUserByKeyId( key.getIdentityId() );
            }
            else
            {
                LOGGER.info( "******* User not found with fingerprint:" + fingerprint );
                return null;
            }
        }
        catch ( Exception ex )
        {
            LOGGER.error( "******* Error !! Error getting User by fingerprint", ex );
            return null;
        }
    }


    /* *************************************************
     */
    @PermitAll
    @Override
    public UserDelegate getUserDelegate( long userId )
    {
        return identityDataService.getUserDelegateByUserId( userId );
    }


    /* *************************************************
     */
    @PermitAll
    @Override
    public UserDelegate getUserDelegate( User user )
    {
        if ( user == null )
        {
            return null;
        }
        else
        {
            return identityDataService.getUserDelegateByUserId( user.getId() );
        }
    }


    /* *************************************************
     */
    @PermitAll
    @Override
    public UserDelegate getUserDelegate( String id )
    {
        return identityDataService.getUserDelegate( id );
    }


    /* *************************************************
     */
    @PermitAll
    @Override
    public void setUserPublicKey( long userId, String publicKeyASCII )
    {

        Session session = getActiveSession();
        if ( session != null )
        {
            TemplateManager templateManager = ServiceLocator.getServiceOrNull( TemplateManager.class );
            if ( templateManager != null )
            {
                templateManager.resetTemplateCache();
            }

            session.setCdnToken( null );
        }

        User user = identityDataService.getUser( userId );

        if ( user != null )
        {
            String secId = user.getSecurityKeyId();

            if ( Strings.isNullOrEmpty( secId ) )
            {
                secId = userId + "-" + UUID.randomUUID();
                user.setSecurityKeyId( secId );
            }
            publicKeyASCII = publicKeyASCII.trim();
            securityManager.getKeyManager()
                           .savePublicKeyRing( secId, SecurityKeyType.USER_KEY.getId(), publicKeyASCII );
            user.setFingerprint( securityManager.getKeyManager().getFingerprint( secId ) );
            identityDataService.updateUser( user );

            //update cached user
            Session activeSession = getActiveSession();
            if ( activeSession != null )
            {
                User activeUser = activeSession.getUser();

                if ( user.getId().equals( activeUser.getId() ) )
                {
                    activeUser.setFingerprint( user.getFingerprint() );
                    activeUser.setSecurityKeyId( user.getSecurityKeyId() );
                }
            }
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
    @RolesAllowed( "Identity-Management|Write" )
    @Override
    public void assignUserRole( long userId, Role role )
    {
        identityDataService.assignUserRole( userId, role );
    }


    /* *************************************************
     */
    @RolesAllowed( "Identity-Management|Write" )
    @Override
    public void assignUserRole( User user, Role role )
    {
        identityDataService.assignUserRole( user, role );
    }


    /* *************************************************
     */
    @PermitAll
    @Override
    public User getUser( long userId )
    {
        return identityDataService.getUser( userId );
    }


    /* *************************************************
     */
    @PermitAll
    @Override
    public User getSystemUser()
    {
        return getUserByUsername( SYSTEM_USERNAME );
    }


    /* *************************************************
     */
    @PermitAll
    @Override
    public User getActiveUser()
    {
        Session session = getActiveSession();

        if ( session != null )
        {
            return session.getUser();
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
    public Session getActiveSession()
    {
        Session session = null;

        try
        {
            Subject subject = getActiveSubject();

            if ( subject != null )
            {
                while ( subject.getPrivateCredentials().iterator().hasNext() )
                {
                    Object obj = subject.getPrivateCredentials().iterator().next();

                    if ( obj instanceof SessionEntity )
                    {
                        session = ( Session ) obj;
                        break;
                    }
                }
            }
        }
        catch ( Exception ex )
        {
            LOGGER.warn( "*** Cannot find active User (no session): {}", ex.getMessage() );
        }

        return session;
    }


    /* *************************************************
     */
    private Subject getActiveSubject() throws AccessControlException
    {

        Subject subject;

        AccessControlContext acc = AccessController.getContext();

        if ( acc == null )
        {
            throw new AccessControlException( "AccessControlContext is null" );
        }

        subject = Subject.getSubject( acc );

        if ( subject == null )
        {
            throw new AccessControlException( "Subject is null" );
        }

        return subject;
    }


    /* *************************************************
     */
    @PermitAll
    @Override
    public void runAs( Session userSession, final Callable action )
    {
        if ( userSession != null )
        {
            Subject.doAs( userSession.getSubject(), new PrivilegedAction<Void>()
            {
                @Override
                public Void run()
                {
                    try
                    {
                        action.call();
                    }
                    catch ( Exception ex )
                    {
                        LOGGER.error( "**** Error!! Error running privileged action.", ex );
                    }
                    return null;
                }
            } );
        }
    }


    /* *************************************************
     */
    @PermitAll
    @Override
    public void runAs( Session userSession, final Runnable action )
    {
        if ( userSession != null )
        {
            Subject.doAs( userSession.getSubject(), new PrivilegedAction<Void>()
            {
                @Override
                public Void run()
                {
                    try
                    {
                        action.run();
                    }
                    catch ( Exception ex )
                    {
                        LOGGER.error( "**** Error!! Error running privileged action.", ex );
                    }
                    return null;
                }
            } );
        }
    }


    /* *************************************************
     */
    @RolesAllowed( "Identity-Management|Write" )
    @Override
    public User createTempUser( String userName, String password, String fullName, String email, int type )
    {
        String salt;
        User user = null;

        try
        {
            //***************Cannot use TOKEN keyword *******
            if ( TOKEN_ID.equalsIgnoreCase( userName ) )
            {
                throw new IllegalArgumentException( "Cannot use TOKEN keyword." );
            }
            //***********************************************

            if ( Strings.isNullOrEmpty( password ) )
            {
                password = Integer.toString( ( new Random() ).nextInt() );
            }

            salt = SecurityUtil.generateSecureRandom();
            password = SecurityUtil.generateSecurePassword( password, salt );

            user = new UserEntity();
            user.setUserName( userName );
            user.setPassword( password );
            user.setSalt( salt );
            user.setEmail( email );
            user.setFullName( fullName );
            user.setType( type );
            user.setAuthId( UUID.randomUUID().toString() );
        }
        catch ( NoSuchAlgorithmException | NoSuchProviderException e )
        {
            LOGGER.warn( "Error in #createTempUser", e );
        }

        return user;
    }


    //**************************************************************
    @RolesAllowed( { "Identity-Management|Write", "Identity-Management|Update" } )
    @Override
    public void setTrustLevel( User user, int trustLevel )
    {
        //TODO Check Public Key for Trust

        user.setTrustLevel( trustLevel );

        identityDataService.updateUser( user );
    }


    /* *************************************************
     */
    @RolesAllowed( { "Identity-Management|Write", "Identity-Management|Update" } )
    @Override
    public UserDelegate createUserDelegate( User user, String delegateUserId, boolean genKeyPair )
    {
        String id = "";

        if ( Strings.isNullOrEmpty( delegateUserId ) )
        {
            id = user.getId() + "-" + UUID.randomUUID();
        }

        UserDelegate userDelegate = new UserDelegateEntity();
        userDelegate.setId( id );
        userDelegate.setUserId( user.getId() );
        identityDataService.persistUserDelegate( userDelegate );


        if ( genKeyPair )
        {
            generateKeyPair( id, SecurityKeyType.USER_KEY.getId() );
        }

        return userDelegate;
    }


    /* *************************************************
     */
    @Override
    public void approveDelegatedUser( final String trustMessage )
    {
        try
        {
            RelationManager relationManager = ServiceLocator.lookup( RelationManager.class );
            User activeUser = getActiveUser();
            UserDelegate delegatedUser = getUserDelegate( activeUser.getId() );
            relationManager.processTrustMessage( trustMessage, delegatedUser.getId() );
        }
        catch ( RelationVerificationException e )
        {
            LOGGER.error( "Message verification failed", e );
        }
    }


    /* *************************************************
     */
    @Override
    public void createIdentityDelegationDocument()
    {
        try
        {
            RelationManager relationManager = ServiceLocator.lookup( RelationManager.class );
            KeyManager keyManager = securityManager.getKeyManager();
            EncryptionTool encryptionTool = securityManager.getEncryptionTool();

            User activeUser = getActiveUser();
            UserDelegate delegatedUser = getUserDelegate( activeUser.getId() );
            if ( delegatedUser == null )
            {
                delegatedUser = createUserDelegate( activeUser, null, true );
            }

            if ( keyManager.getPrivateKey( delegatedUser.getId() ) == null )
            {
                generateKeyPair( delegatedUser.getId(), SecurityKeyType.USER_KEY.getId() );
            }

            assert relationManager != null;
            RelationInfoMeta relationInfoMeta =
                    new RelationInfoMeta( true, true, true, true, Ownership.USER.getLevel() );

            Map<String, String> traits = relationInfoMeta.getRelationTraits();
            traits.put( "read", "true" );
            traits.put( "write", "true" );
            traits.put( "update", "true" );
            traits.put( "delete", "true" );
            traits.put( "ownership", Ownership.USER.getName() );

            RelationMeta relationMeta =
                    new RelationMeta( activeUser, delegatedUser, delegatedUser, activeUser.getSecurityKeyId() );
            Relation relation = relationManager.buildRelation( relationInfoMeta, relationMeta );

            String relationJson = JsonUtil.toJson( relation );

            PGPPublicKey publicKey = keyManager.getPublicKey( delegatedUser.getId() );
            byte[] relationEncrypted = encryptionTool.encrypt( relationJson.getBytes(), publicKey, true );

            String encryptedMessage = "\n" + new String( relationEncrypted, StandardCharsets.UTF_8 );
            delegatedUser.setRelationDocument( encryptedMessage );
            identityDataService.updateUserDelegate( delegatedUser );
            LOGGER.debug( encryptedMessage );
            LOGGER.debug( delegatedUser.getId() );
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error in #createIdentityDelegationDocument", e );
        }
    }


    /* *************************************************
     */
    private void generateKeyPair( String securityKeyId, int type )
    {
        KeyPair kp = securityManager.getKeyManager().generateKeyPair( securityKeyId, false );
        securityManager.getKeyManager().saveKeyPair( securityKeyId, type, kp );
    }


    /* *************************************************
     */
    @RolesAllowed( { "Identity-Management|Write", "Identity-Management|Update" } )
    @Override
    public User createUser( String userName, String password, String fullName, String email, int type, int trustLevel,
                            boolean generateKeyPair, boolean createUserDelegate )
            throws SystemSecurityException, UserExistsException
    {
        User user = new UserEntity();

        //*********************************
        if ( Strings.isNullOrEmpty( userName ) )
        {
            userName = UUID.randomUUID().toString();
        }
        if ( Strings.isNullOrEmpty( password ) )
        {
            password = UUID.randomUUID().toString();
        }

        //*********************************
        // Remove XSS vulnerability code
        userName = validateInput( userName, true );
        fullName = validateInput( fullName, false );
        //*********************************

        isValidUserName( userName );
        isValidPassword( userName, password );
        isValidEmail( email );

        if ( identityDataService.getUserByUsername( userName ) != null )
        {
            throw new UserExistsException( String.format( "User with name %s already exists", userName ) );
        }

        try
        {
            String salt = SecurityUtil.generateSecureRandom();
            password = SecurityUtil.generateSecurePassword( password, salt );


            user.setUserName( userName );
            user.setPassword( password );
            user.setSalt( salt );
            user.setEmail( email );
            user.setFullName( fullName );
            user.setType( type );
            user.setTrustLevel( trustLevel );
            user.setAuthId( userName );
            user.setValidDate( new Date() );

            identityDataService.persistUser( user );

            //***************************************
            if ( generateKeyPair )
            {
                String securityKeyId = user.getId() + "-" + UUID.randomUUID();
                LOGGER.debug( "generating keypair for user {}: {}", user.getId(), user.getUserName() );
                generateKeyPair( securityKeyId, SecurityKeyType.USER_KEY.getId() );
                user.setSecurityKeyId( securityKeyId );
                identityDataService.updateUser( user );
            }
            //***************************************

            //***************************************
            if ( createUserDelegate )
            {
                LOGGER.debug( "generating delegate for user {}: {}", user.getId(), user.getUserName() );
                createUserDelegate( user, null, true );
            }

            LOGGER.debug( "User {} created", userName );
        }
        catch ( Exception e )
        {
            throw new SystemSecurityException( "Internal error", e );
        }

        return user;
    }


    /* *************************************************
     */
    @PermitAll
    @Override
    public User getUserByUsername( String userName )
    {
        return identityDataService.getUserByUsername( userName );
    }


    /* *************************************************
     */
    @RolesAllowed( { "Identity-Management|Write", "Identity-Management|Update" } )
    @Override
    public User modifyUser( User user, String password ) throws SystemSecurityException
    {
        //******Cannot update Internal User *************
        if ( user.getType() == UserType.SYSTEM.getId() )
        {
            throw new AccessControlException( "Internal User cannot be updated" );
        }

        try
        {
            //*********************************
            // Remove XSS vulnerability code
            user.setUserName( validateInput( user.getUserName(), true ) );
            user.setFullName( validateInput( user.getFullName(), false ) );

            //*********************************
            //**************************************
            isValidUserName( user.getUserName() );
            isValidEmail( user.getEmail() );
            //**************************************

            if ( !Strings.isNullOrEmpty( password ) )
            {
                isValidPassword( user.getUserName(), password );
                String salt = user.getSalt();
                password = SecurityUtil.generateSecurePassword( password, salt );

                user.setPassword( password );
            }

            identityDataService.updateUser( user );
        }
        catch ( IllegalArgumentException e )
        {
            throw e;
        }
        catch ( Exception e )
        {
            LOGGER.error( "modify user exception", e );
            throw new SystemSecurityException( "Internal error" );
        }

        return user;
    }


    /* *************************************************
     */
    @RolesAllowed( "Identity-Management|Delete" )
    @Override
    public void removeUserRole( long userId, Role role )
    {
        identityDataService.removeUserRole( userId, role );
    }


    /* *************************************************
     */
    @RolesAllowed( "Identity-Management|Delete" )
    @Override
    public void removeUserRole( User user, Role role )
    {
        identityDataService.removeUserRole( user, role );
    }


    /* *************************************************
     */
    @PermitAll
    @Override
    public boolean changeUserPassword( String userName, String oldPassword, String newPassword )
            throws SystemSecurityException
    {
        User user = identityDataService.getUserByUsername( userName );
        return changeUserPassword( user, oldPassword, newPassword );
    }


    /* *************************************************
     */
    @PermitAll
    @Override
    public boolean changeUserPassword( long userId, String oldPassword, String newPassword )
            throws SystemSecurityException
    {
        User user = identityDataService.getUser( userId );
        return changeUserPassword( user, oldPassword, newPassword );
    }


    /* *************************************************
     */
    @PermitAll
    @Override
    public boolean changeUserPassword( User user, String oldPassword, String newPassword )
            throws SystemSecurityException
    {
        String salt;

        if ( oldPassword.equals( newPassword ) )
        {
            throw new IllegalArgumentException( "NewPassword cannot be the same as old one." );
        }

        //******Cannot update Internal User *************
        if ( user.getType() == UserType.SYSTEM.getId() )
        {
            throw new AccessControlException( "Internal User cannot be updated" );
        }

        String pswHash = SecurityUtil.generateSecurePassword( oldPassword, user.getSalt() );

        if ( !pswHash.equals( user.getPassword() ) )
        {
            throw new InvalidLoginException( "Invalid old password specified" );
        }

        isValidPassword( user.getUserName(), newPassword );

        try
        {
            salt = SecurityUtil.generateSecureRandom();
            newPassword = SecurityUtil.generateSecurePassword( newPassword, salt );
            user.setSalt( salt );
            user.setPassword( newPassword );
            //user.setAuthId( UUID.randomUUID().toString() ); //Update AuthID also
            user.setValidDate( DateUtils.addDays( new Date( System.currentTimeMillis() ), IDENTITY_LIFETIME ) );
            identityDataService.updateUser( user );
        }
        catch ( NoSuchAlgorithmException | NoSuchProviderException e )
        {
            throw new SystemSecurityException( "Internal error" );
        }

        return true;
    }


    @Override
    public void resetPassword( String username, String newPassword, String sign ) throws SystemSecurityException
    {
        User user = getUserByUsername( username );

        if ( user == null )
        {
            throw new InvalidLoginException( "User not found" );
        }

        isValidPassword( user.getUserName(), newPassword );

        try
        {
            String signToken =
                    new String( securityManager.getEncryptionTool().extractClearSignContent( sign.getBytes() ) ).trim()
                                                                                                                .toLowerCase();

            if ( signTokensCache.getIfPresent( signToken ) == null )
            {
                throw new InvalidLoginException( "Sign token is invalid" );
            }

            if ( !securityManager.getEncryptionTool().verifyClearSign( sign.getBytes(),
                    securityManager.getKeyManager().getPublicKeyRingByFingerprint( user.getFingerprint() ) ) )
            {
                throw new InvalidLoginException( "Sign is invalid" );
            }

            //remove token from cache
            signTokensCache.invalidate( signToken );
        }
        catch ( PGPException e )
        {
            throw new SystemSecurityException( "Sign is invalid" );
        }

        try
        {
            String salt = SecurityUtil.generateSecureRandom();
            newPassword = SecurityUtil.generateSecurePassword( newPassword, salt );
            user.setSalt( salt );
            user.setPassword( newPassword );
            user.setValidDate( DateUtils.addDays( new Date( System.currentTimeMillis() ), IDENTITY_LIFETIME ) );
            identityDataService.updateUser( user );
        }
        catch ( NoSuchAlgorithmException | NoSuchProviderException e )
        {
            throw new SystemSecurityException( "Internal error" );
        }
    }


    @Override
    public String getSignToken()
    {
        String token = UUID.randomUUID().toString().toLowerCase();

        signTokensCache.put( token, true );

        return token;
    }


    /* *************************************************
     */
    @RolesAllowed( { "Identity-Management|Write", "Identity-Management|Update" } )
    @Override
    public void updateUser( User user )
    {
        //******Cannot update Internal User *************
        if ( user.getType() == UserType.SYSTEM.getId() )
        {
            throw new AccessControlException( "Internal User cannot be updated" );
        }
        //***********************************************

        identityDataService.updateUser( user );
    }


    /* *************************************************
     */
    @RolesAllowed( { "Identity-Management|Write", "Identity-Management|Update" } )
    @Override
    public void updateUser( User user, String publicKey )
    {
        //******Cannot update Internal User *************
        if ( user.getType() == UserType.SYSTEM.getId() )
        {
            throw new AccessControlException( "Internal User cannot be updated" );
        }
        //***********************************************

        identityDataService.updateUser( user );
    }


    /* *************************************************
     */
    @RolesAllowed( { "Identity-Management|Write", "Identity-Management|Delete" } )
    @Override
    public void removeUser( long userId )
    {
        //******Cannot remove Internal User *************
        User user = identityDataService.getUser( userId );

        if ( user == null )
        {
            return;
        }

        if ( user.getType() == UserType.SYSTEM.getId() )
        {
            throw new AccessControlException( "Internal User cannot be removed" );
        }

        identityDataService.removeUser( userId );
    }


    /* *************************************************
     */
    private void isValidUserName( String userName )
    {
        if ( Strings.isNullOrEmpty( userName ) || userName.length() < 4 )
        {
            throw new IllegalArgumentException( "User name cannot be shorter than 4 characters." );
        }

        if ( TOKEN_ID.equalsIgnoreCase( userName ) || "administrator".equalsIgnoreCase( userName ) || "authmessage"
                .equalsIgnoreCase( userName ) || "system".equalsIgnoreCase( userName ) )
        {
            throw new IllegalArgumentException( "User name is reserved by the system." );
        }
    }


    /* *************************************************
     */
    private void isValidPassword( String userName, String password )
    {
        Preconditions.checkArgument( !( Strings.isNullOrEmpty( userName ) || userName.trim().isEmpty() ),
                "Username can not be blank" );

        Preconditions.checkArgument( !( Strings.isNullOrEmpty( password ) || password.trim().length() < 4 ),
                "Password cannot be shorter than 4 characters" );


        Preconditions.checkArgument( !password.trim().equalsIgnoreCase( userName.trim() ),
                "Password can not be the same as username" );
    }


    /* *************************************************
     */
    private void isValidEmail( String email )
    {
        if ( !StringUtil.isValidEmail( email ) )
        {
            throw new IllegalArgumentException( "Invalid Email specified" );
        }
    }


    /* *************************************************
     */
    private String validateInput( String inputStr, boolean removeSpaces )
    {
        return StringUtil.removeHtmlAndSpecialChars( inputStr, removeSpaces );
    }


    @Override
    public boolean isTenantManager()
    {
        return isUserPermitted( getActiveUser(), PermissionObject.TENANT_MANAGEMENT, PermissionScope.ALL_SCOPE,
                PermissionOperation.READ );
    }


    @Override
    public boolean isSystemUser()
    {
        User user = getActiveUser();

        return user != null && SYSTEM_USERNAME.equalsIgnoreCase( user.getUserName() );
    }


    @Override
    public boolean isAdmin()
    {
        User user = getActiveUser();

        if ( user == null )
        {
            return false;
        }

        for ( Role role : user.getRoles() )
        {
            if ( ADMIN_ROLE.equalsIgnoreCase( role.getName() ) )
            {
                return true;
            }
        }

        return false;
    }


    /* *************************************************
     */
    @PermitAll
    @Override
    public boolean isUserPermitted( User user, PermissionObject permObj, PermissionScope permScope,
                                    PermissionOperation permOp )
    {
        if ( user == null )
        {
            return false;
        }

        List<Role> roles = user.getRoles();

        for ( Role role : roles )
        {
            for ( Permission permission : role.getPermissions() )
            {
                if ( permission.getObject() == permObj.getId() && permission.getScope() == permScope.getId() )
                {
                    switch ( permOp )
                    {
                        case READ:
                            return permission.isRead();
                        case WRITE:
                            return permission.isWrite();
                        case UPDATE:
                            return permission.isUpdate();
                        case DELETE:
                            return permission.isDelete();
                        default:
                            // no-op
                            break;
                    }
                }
            }
        }

        return false;
    }


    /* *************************************************
     */
    @RolesAllowed( { "Identity-Management|Write", "Identity-Management|Update" } )
    @Override
    public Role createRole( String roleName, int roleType )
    {
        LOGGER.debug( "Creating role {}", roleName );

        Preconditions.checkArgument( !Strings.isNullOrEmpty( roleName ), "Invalid role name" );

        if ( identityDataService.findRoleByName( roleName ) != null )
        {
            throw new IllegalArgumentException( String.format( "Role with name %s already exists", roleName ) );
        }

        //*********************************
        // Remove XSS vulnerability code
        roleName = validateInput( roleName, true );
        //*********************************

        Role role = new RoleEntity();
        role.setName( roleName );
        role.setType( roleType );

        identityDataService.persistRole( role );

        LOGGER.debug( "Role {} created", roleName );

        return role;
    }


    /* *************************************************
     */
    @RolesAllowed( { "Identity-Management|Read" } )
    @Override
    public Role findRoleByName( String roleName )
    {
        return identityDataService.findRoleByName( roleName );
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
    @PermitAll
    @Override
    public Role getRole( long roleId )
    {
        return identityDataService.getRole( roleId );
    }


    /* *************************************************
     */
    @RolesAllowed( { "Identity-Management|Write", "Identity-Management|Update" } )
    @Override
    public void updateRole( Role role )
    {
        //******Cannot update Internal Role *************
        if ( role.getType() == UserType.SYSTEM.getId() )
        {
            throw new AccessControlException( "Internal Role cannot be updated" );
        }
        //***********************************************

        //*********************************
        // Remove XSS vulnerability code
        role.setName( validateInput( role.getName(), true ) );
        //*********************************

        identityDataService.updateRole( role );
    }


    /* *************************************************
     */
    @RolesAllowed( "Identity-Management|Delete" )
    @Override
    public void removeRole( long roleId )
    {
        //******Cannot remove Internal Role *************
        Role role = identityDataService.getRole( roleId );

        if ( role.getType() == UserType.SYSTEM.getId() )
        {
            throw new AccessControlException( "Internal Role cannot be removed" );
        }
        //***********************************************

        identityDataService.removeRole( roleId );
    }


    /* *************************************************
     */
    @RolesAllowed( "Identity-Management|Write" )
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
    @RolesAllowed( "Identity-Management|Write" )
    @Override
    public void assignRolePermission( long roleId, Permission permission )
    {
        identityDataService.assignRolePermission( roleId, permission );
    }


    /* *************************************************
     */
    @RolesAllowed( "Identity-Management|Write" )
    @Override
    public void assignRolePermission( Role role, Permission permission )
    {
        identityDataService.assignRolePermission( role, permission );
    }


    /* *************************************************
     */
    @RolesAllowed( "Identity-Management|Write" )
    @Override
    public void removeAllRolePermissions( long roleId )
    {
        identityDataService.removeAllRolePermissions( roleId );
    }


    /* *************************************************
     */
    @RolesAllowed( "Identity-Management|Delete" )
    @Override
    public void removePermission( long permissionId )
    {
        identityDataService.removePermission( permissionId );
    }


    /* *************************************************
     */
    @RolesAllowed( "Identity-Management|Delete" )
    @Override
    public void removeRolePermission( long roleId, Permission permission )
    {
        identityDataService.removeRolePermission( roleId, permission );
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
    @RolesAllowed( "Identity-Management|Update" )
    @Override
    public void updatePermission( Permission permission )
    {
        identityDataService.updatePermission( permission );
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
    @PermitAll
    @Override
    public List<UserToken> getAllUserTokens()
    {
        return identityDataService.getAllUserTokens();
    }


    /* *************************************************
     */
    @PermitAll
    @Override
    public void extendTokenTime( UserToken token, int minutes )
    {
        if ( minutes == 0 )
        {
            minutes = sessionManager.getSessionTimeout();
        }
        token.setValidDate( DateUtils.addMinutes( new Date( System.currentTimeMillis() ), minutes ) );
        identityDataService.updateUserToken( token );
    }


    /* *************************************************
     */
    @PermitAll
    @Override
    public void updateUserToken( UserToken token )
    {
        identityDataService.updateUserToken( token );
    }


    /* *************************************************
     */
    @RolesAllowed( { "Identity-Management|Write", "Identity-Management|Update" } )
    @Override
    public void updateUserToken( String tokenId, User user, String token, String secret, String issuer, int tokenType,
                                 Date validDate )
    {
        identityDataService.removeUserToken( tokenId );
        createUserToken( user, token, secret, issuer, tokenType, validDate );
    }


    /* *************************************************
     */
    @RolesAllowed( { "Identity-Management|Write", "Identity-Management|Delete" } )
    @Override
    public void removeUserToken( String tokenId )
    {
        identityDataService.removeUserToken( tokenId );
    }


    /* *************************************************
     */
    public void setDaoManager( DaoManager daoManager )
    {
        this.daoManager = daoManager;
    }


    /* *************************************************
     */
    public void setSecurityManager( final SecurityManager securityManager )
    {
        this.securityManager = securityManager;
    }


    /* *************************************************
     */
    @Override
    public SecurityController getSecurityController()
    {
        return securityController;
    }
}
