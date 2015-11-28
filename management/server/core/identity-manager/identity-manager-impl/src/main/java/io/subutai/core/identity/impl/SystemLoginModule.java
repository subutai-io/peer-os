package io.subutai.core.identity.impl;


import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.karaf.jaas.boot.principal.RolePrincipal;
import org.apache.karaf.jaas.boot.principal.UserPrincipal;
import org.apache.karaf.jaas.config.JaasRealm;
import org.apache.karaf.jaas.modules.AbstractKarafLoginModule;

import io.subutai.common.util.ServiceLocator;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.Permission;
import io.subutai.core.identity.api.model.Role;
import io.subutai.core.identity.api.model.Session;
import io.subutai.core.identity.api.model.User;


public class SystemLoginModule extends AbstractKarafLoginModule
{
    private static final Logger LOGGER = LoggerFactory.getLogger( SystemLoginModule.class.getName() );
    private IdentityManager identityManager = null;


    @Override
    public void initialize( Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState,
                            Map<String, ?> options )
    {
        super.initialize( subject, callbackHandler, options );

        try
        {
            LOGGER.info( "Initializing Karaf login module." );
            Class.forName( "org.apache.karaf.jaas.config.JaasRealm", true, JaasRealm.class.getClassLoader() );
            identityManager = ServiceLocator.getServiceNoCache( IdentityManager.class );
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error loading class JaasRealm", e );
        }
    }


    @Override
    public boolean login() throws LoginException
    {
        LOGGER.debug( "Invoking login." );

        // **************************************
        Callback[] callbacks = new Callback[2];
        callbacks[0] = new NameCallback( "Username: " );
        callbacks[1] = new PasswordCallback( "Password: ", false );
        // **************************************

        try
        {
            // **************************************
            callbackHandler.handle( callbacks );
            user = ( ( NameCallback ) callbacks[0] ).getName();

            char[] tmpPassword = ( ( PasswordCallback ) callbacks[1] ).getPassword();
            if ( tmpPassword == null )
            {
                tmpPassword = new char[0];
            }
            String password = new String( tmpPassword );
            // **************************************

            Session userSession = identityManager.authenticateSession( user, password );

            if(userSession != null)
            {

                User loggedUser = userSession.getUser();
                //******************************************
                principals = new HashSet<>();
                principals.add( new UserPrincipal( user ) );
                principals.add( new RolePrincipal( "webconsole" ) );
                //******************************************

                if ( userSession.getSubject() != null ) //restore
                {
                    principals.addAll( userSession.getSubject().getPrincipals() );
                    LOGGER.debug( "Session restored" );
                }
                else //create new subject
                {

                    //******************************************
                    List<Role> roles = loggedUser.getRoles();
                    for ( Role role : roles )
                    {
                        List<Permission> permissions = role.getPermissions();
                        for ( Permission permission : permissions )
                        {
                            List<String> perms = permission.asString();

                            for ( String perm : perms )
                            {
                                principals.add( new RolePrincipal( perm ) );
                            }
                        }
                    }
                    //******************************************
                    LOGGER.debug( "Successful login." );
                }

                //******************************************
                subject.getPrincipals().clear();
                subject.getPrincipals().addAll( principals );
                subject.getPrivateCredentials().add( userSession );
                //******************************************
            }
            else
            {
                throw new LoginException( "Invalid Login" );
            }
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
        catch ( Exception e )
        {
            LOGGER.error( e.toString() );
            return false;
        }

        return true;
    }


    @Override
    public boolean commit() throws LoginException
    {
        return super.commit();
    }


    @Override
    public boolean abort()
    {
        return true;
    }


    @Override
    public boolean logout() throws LoginException
    {
        LOGGER.debug( "Invoking logout." );
        try
        {
            identityManager.logout();
        }
        catch ( Exception e )
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
