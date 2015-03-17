package org.safehaus.subutai.core.identity.impl;


import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;

import org.safehaus.subutai.common.security.ShiroPrincipal;
import org.safehaus.subutai.common.security.SubutaiLoginContext;
import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.core.identity.api.IdentityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.karaf.jaas.boot.principal.RolePrincipal;
import org.apache.karaf.jaas.boot.principal.UserPrincipal;
import org.apache.karaf.jaas.config.JaasRealm;
import org.apache.karaf.jaas.modules.AbstractKarafLoginModule;


public class ShiroLoginModule extends AbstractKarafLoginModule
{
    private static final Logger LOGGER = LoggerFactory.getLogger( ShiroLoginModule.class.getName() );

    private Serializable sessionId;


    public void initialize( Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState,
                            Map<String, ?> options )
    {
        super.initialize( subject, callbackHandler, options );

        LOGGER.info( "Initializing shiro login module." );


        // just for importing JaasRealm
        Object o = subject;
        if ( o instanceof JaasRealm )
        {
            // empty
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
        principals = new HashSet<>();

        try
        {
            IdentityManager identityManager = ServiceLocator.getServiceNoCache( IdentityManager.class );
            sessionId = identityManager.login( user, password );
            LOGGER.debug( "Login success." );

            principals.add( new UserPrincipal( user ) );
            SubutaiLoginContext loginContext = new SubutaiLoginContext( sessionId.toString(), user, "127.0.0.1" );
            principals.add( new ShiroPrincipal( loginContext ) );
            Set<String> roles = identityManager.getRoles( sessionId );
            for ( String roleName : roles )
            {
                principals.add( new RolePrincipal( roleName ) );
            }
        }
        catch ( Exception e )
        {
            LOGGER.error( e.toString() );
            return false;
        }

        subject.getPrincipals().addAll( principals );
        LOGGER.debug( "Finish login." );
        return true;
    }


    @Override
    public boolean commit() throws LoginException
    {
        LOGGER.debug( "Invoking commit." );
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
        LOGGER.debug( "Invoking logout." );
        try
        {
            IdentityManager identityManager = ServiceLocator.getServiceNoCache( IdentityManager.class );
            identityManager.logout( sessionId );
        }
        catch ( NamingException e )
        {
            LOGGER.error( e.toString(), e );
        }
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
