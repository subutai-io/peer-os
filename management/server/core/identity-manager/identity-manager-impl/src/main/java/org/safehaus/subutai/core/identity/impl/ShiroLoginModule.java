package org.safehaus.subutai.core.identity.impl;


import java.io.IOException;
import java.security.Principal;
import java.util.HashSet;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;

import org.safehaus.subutai.common.helper.UserIdMdcHelper;
import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.core.identity.api.IdentityManager;
import org.safehaus.subutai.core.identity.api.ShiroPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.karaf.jaas.boot.principal.GroupPrincipal;
import org.apache.karaf.jaas.boot.principal.RolePrincipal;
import org.apache.karaf.jaas.boot.principal.UserPrincipal;
import org.apache.karaf.jaas.config.JaasRealm;
import org.apache.karaf.jaas.modules.AbstractKarafLoginModule;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.session.Session;


/**
 * Created by timur on 1/22/15.
 */
public class ShiroLoginModule extends AbstractKarafLoginModule
{
    private static final Logger LOGGER = LoggerFactory.getLogger( ShiroLoginModule.class.getName() );
    //
    //    public final static String ADDRESS = "address";
    //    public final static String ADMIN_USER = "admin.user"; // for the backing engine
    //    public final static String ADMIN_PASSWORD = "admin.password"; // for the backing engine
    //    private String address;

    //    protected Set<Principal> principals = new HashSet();
    //    protected Subject subject;
    //    protected String user;
    //    protected CallbackHandler callbackHandler;
    //    protected boolean debug;
    //    protected Map<String, ?> options;
    //    protected String rolePolicy;
    //    protected String roleDiscriminator;
    //    protected boolean detailedLoginExcepion;
    //    protected BundleContext bundleContext;
    //    private EncryptionSupport encryptionSupport;
    org.apache.shiro.subject.Subject shiroSubject;


    private IdentityManager getIdentityManager()
    {
        try
        {
            InitialContext ic = new InitialContext();
            return ( IdentityManager ) ic.lookup( "osgi:service/identityManager" );
        }
        catch ( NamingException e )
        {
            LOGGER.error( "JNDI error while retrieving osgi:service/identityManager", e );
            throw new AuthorizationException( e );
        }
    }


    public void initialize( Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState,
                            Map<String, ?> options )
    {
        super.initialize( subject, callbackHandler, options );
        //        this.subject = subject;
        //        this.callbackHandler = callbackHandler;

        LOGGER.info( "Initializing shiro login module." );
        //        address = ( String ) options.get( ADDRESS );
        //        LOGGER.info( String.format( "size: %d", subject.getPrincipals().size() ) );
        //        for ( Principal p : subject.getPrincipals() )
        //        {
        //            LOGGER.info( String.format( "principal: %s %s", p.getName(), p.toString() ) );
        //        }

        Object o = subject;
        if ( o instanceof JaasRealm )
        {
            LOGGER.debug( "NOT" );
        }
        else
        {
            LOGGER.debug( "OK" );
        }
    }


    public boolean login() throws LoginException
    {
        LOGGER.debug( "Invoking login." );

        Callback[] callbacks = new Callback[2];
        callbacks[0] = new NameCallback( "Username: " );
        callbacks[1] = new PasswordCallback( "Password: ", false );
        try
        {
            callbackHandler.handle( callbacks );
        }
        catch ( IOException ioException )
        {
            throw new LoginException( ioException.getMessage() );
        }
        catch ( UnsupportedCallbackException unsupportedCallbackException )
        {
            throw new LoginException(
                    unsupportedCallbackException.getMessage() + " not available to obtain information from user." );
        }
        user = ( ( NameCallback ) callbacks[0] ).getName();
        char[] tmpPassword = ( ( PasswordCallback ) callbacks[1] ).getPassword();
        if ( tmpPassword == null )
        {
            tmpPassword = new char[0];
        }
        String password = new String( tmpPassword );
        principals = new HashSet<Principal>();

        LOGGER.debug( String.format( "Thread ID: %d", Thread.currentThread().getId() ) );
        LOGGER.debug( "************************" + user + " " + password );
        try
        {
            UsernamePasswordToken token = new UsernamePasswordToken( user, password );

            LOGGER.info( "Trying authc..." );

            IdentityManager identityManager = ServiceLocator.getServiceNoCache( IdentityManager.class );
            LOGGER.info( identityManager.toString() );
            shiroSubject = identityManager.login( token );
            Session session = shiroSubject.getSession();
            LOGGER.info( "Login success." );

            principals.add( new UserPrincipal( user ) );
//            principals.add( new RolePrincipal( "session:" + session.getId().toString() ) );
            principals.add( new ShiroPrincipal( shiroSubject ) );
            principals.add( new RolePrincipal( "admin" ) );
            principals.add( new RolePrincipal( "manager" ) );
            principals.add( new RolePrincipal( "viewer" ) );
            principals.add( new RolePrincipal( "group" ) );
            principals.add( new GroupPrincipal( "admingroup" ) );


            //            for ( Principal role : subject.getPrincipals())
            //            {
            //                principals.add( new RolePrincipal( role.getName() ) );
            //            }
        }
        catch ( Exception e )
        {
            LOGGER.error( e.toString() );
            return false;
        }


        subject.getPrincipals().addAll( principals );
        LOGGER.debug( "Finish login." );
        UserIdMdcHelper.set( shiroSubject );
        return true;
    }


    @Override
    public boolean commit() throws LoginException
    {
        LOGGER.info( "Invoking commit." );
        //        boolean result = super.commit();
        //        LOGGER.info( "commit: " + result );
        return super.commit();
    }


    @Override
    public boolean abort()
    {
        LOGGER.debug( "Invoking abort." );
        return true;
    }


    @Override
    public boolean logout() throws LoginException
    {
        System.out.println( "**********************************************" );
        LOGGER.debug( "Invoking logout." );
        try
        {
            IdentityManager identityManager = ServiceLocator.getServiceNoCache( IdentityManager.class );
            identityManager.logout( shiroSubject.getSession().getId() );
        }
        catch ( NamingException e )
        {
            e.printStackTrace();
        }
        //        UserIdMdcHelper.unset();
        subject.getPrincipals().removeAll( principals );
        principals.clear();
        return true;
    }


    public String getEncryptedPassword( final String password )
    {
        LOGGER.debug( "Invoking getEncryptedPassword." );
        return super.getEncryptedPassword( password );
    }


    public boolean checkPassword( final String plain, final String encrypted )
    {
        LOGGER.debug( "Invoking checkPassword." );
        return super.checkPassword( plain, encrypted );
    }
}
