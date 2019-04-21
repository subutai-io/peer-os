package io.subutai.core.bazaarmanager.impl.util;


import java.util.Random;

import org.apache.commons.lang3.RandomStringUtils;

import com.google.common.base.Preconditions;

import io.subutai.common.peer.ContainerHost;
import io.subutai.common.util.TaskUtil;


public class Utils
{
    private Utils()
    {
    }


    public static boolean waitTillConnects( ContainerHost containerHost, int maxTimeoutSec )
    {
        int waited = 0;
        while ( !containerHost.isConnected() && waited < maxTimeoutSec )
        {
            waited++;
            TaskUtil.sleep( 1000 );
        }

        return containerHost.isConnected();
    }


    /**
     * Generate random password. If no digits and no special characters included, letters will be used by default.
     *
     * @param minLength password minimum length
     * @param maxLength password maximum length
     * @param digits include digits
     * @param letters include ASCII letters(both lower and upper case)
     * @param specialChars include following characters: "!@#$%^&*()-=[]{};:.,"
     */
    public static String generatePassword( int minLength, int maxLength, boolean digits, boolean letters,
                                           boolean specialChars )
    {
        Preconditions.checkArgument( minLength > 0, "Invalid min length" );
        Preconditions.checkArgument( maxLength > 0 && maxLength >= minLength, "Invalid max length" );

        StringBuilder chars = new StringBuilder();

        if ( letters || !digits && !specialChars )
        {
            chars.append( "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ" );
        }

        if ( digits )
        {
            chars.append( "0123456789" );
        }

        if ( specialChars )
        {
            chars.append( "!@#$%^&*()-=[]{};:.," );
        }

        int length = minLength + new Random().nextInt( maxLength - minLength );

        return RandomStringUtils.random( length, chars.toString() );
    }
}
