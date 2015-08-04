package io.subutai.common.callback;


import java.io.Console;
import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;



/**
 * A CallbackHandler that reads the password from the console
 * or returns the password given to it's constructor.
 *
 */

class ConsoleCallbackHandler implements CallbackHandler
{

    private char[] password = null;


    public ConsoleCallbackHandler( String password )
    {
        if ( password != null )
        {
            this.password = password.toCharArray();
        }
    }


    /**
     * Handles the callbacks.
     *
     * @param callbacks The callbacks to handle
     *
     * @throws UnsupportedCallbackException If the console is not available or other than PasswordCallback is supplied
     */
    @Override
    public void handle( Callback[] callbacks ) throws IOException, UnsupportedCallbackException
    {
        Console cons = System.console();
        if ( cons == null && password == null )
        {
            throw new UnsupportedCallbackException( callbacks[0], "Console is not available" );
        }
        for ( int i = 0; i < callbacks.length; i++ )
        {
            if ( callbacks[i] instanceof PasswordCallback )
            {
                if ( password == null )
                {
                    //It is used instead of cons.readPassword(prompt), because the prompt may contain '%' characters
                    ( ( PasswordCallback ) callbacks[i] ).setPassword(
                            cons.readPassword( "%s", ( ( PasswordCallback ) callbacks[i] ).getPrompt() ) );
                }
                else
                {
                    ( ( PasswordCallback ) callbacks[i] ).setPassword( password );
                }
            }
            else
            {
                throw new UnsupportedCallbackException( callbacks[i] );
            }
        }
    }
}