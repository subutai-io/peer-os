package org.safehaus.subutai.core.security.impl;


import java.io.IOException;
import java.security.Principal;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.karaf.jaas.boot.principal.GroupPrincipal;
import org.apache.karaf.jaas.boot.principal.RolePrincipal;
import org.apache.karaf.jaas.boot.principal.UserPrincipal;
import org.apache.karaf.jaas.config.JaasRealm;
import org.apache.karaf.jaas.modules.encryption.EncryptionSupport;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.session.Session;


/**
 * Created by timur on 1/22/15.
 */
public class ShiroLoginModule implements LoginModule
{
    private static final Logger LOGGER = LoggerFactory.getLogger( ShiroLoginModule.class.getName() );

    public final static String ADDRESS = "address";
    public final static String ADMIN_USER = "admin.user"; // for the backing engine
    public final static String ADMIN_PASSWORD = "admin.password"; // for the backing engine
    private String address;

    protected Set<Principal> principals = new HashSet();
    protected Subject subject;
    protected String user;
    protected CallbackHandler callbackHandler;
    protected boolean debug;
    protected Map<String, ?> options;
    protected String rolePolicy;
    protected String roleDiscriminator;
    protected boolean detailedLoginExcepion;
    protected BundleContext bundleContext;
    private EncryptionSupport encryptionSupport;


    public void initialize( Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState,
                            Map<String, ?> options )
    {
        //        super.initialize( subject, callbackHandler, options );
        this.subject = subject;
        this.callbackHandler = callbackHandler;

        LOGGER.info( "Initializing shiro login module." );
        //        address = ( String ) options.get( ADDRESS );
        LOGGER.info( String.format( "size: %d", subject.getPrincipals().size() ) );
        for ( Principal p : subject.getPrincipals() )
        {
            LOGGER.info( String.format( "principal: %s %s", p.getName(), p.toString() ) );
        }

        Object o = subject;
        if ( o instanceof JaasRealm )
        {
            LOGGER.info( "NOT" );
        }
        else
        {
            LOGGER.info( "OK" );
        }
    }


    public boolean login() throws LoginException
    {
        LOGGER.info( "Invoking login." );

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


        org.apache.shiro.subject.Subject s = SecurityUtils.getSubject();
        UsernamePasswordToken token = new UsernamePasswordToken( user, password );

        LOGGER.info( "************************" + user + " " + password );
        try
        {
            LOGGER.info( "Trying authc..." );
            s.login( token );
            Session session = s.getSession();
            LOGGER.info( "Login success." );

            principals.add( new UserPrincipal( user ) );
            principals.add( new RolePrincipal( "session:" + session.getId().toString() ) );
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
        LOGGER.info( "Finish login." );
        return true;
    }


    @Override
    public boolean commit() throws LoginException
    {
        LOGGER.info( "Invoking commit." );
        //        boolean result = super.commit();
        //        LOGGER.info( "commit: " + result );
        return true;
    }


    public boolean abort()
    {
        LOGGER.info( "Invoking abort." );
        return true;
    }


    public boolean logout() throws LoginException
    {
        LOGGER.info( "Invoking logout." );

        subject.getPrincipals().removeAll( principals );
        principals.clear();
        return true;
    }


    public String getEncryptedPassword( final String password )
    {
        LOGGER.info( "Invoking getEncryptedPassword." );
        return password;
        //        return super.getEncryptedPassword( password );
    }


    public boolean checkPassword( final String plain, final String encrypted )
    {
        LOGGER.info( "Invoking checkPassword." );
        return true;
        //        return super.checkPassword( plain, encrypted );
    }
}
