package org.safehaus.subutai.common.helper;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import com.google.common.base.Strings;

import static com.google.common.base.Preconditions.checkNotNull;


public class UserIdMdcHelper
{
    private static final Logger log = LoggerFactory.getLogger( UserIdMdcHelper.class );

    public static final String KEY = "userId";

    public static final String UNKNOWN = "*UNKNOWN*";


    public static boolean isSet()
    {
        String userId = MDC.get( KEY );
        return !( Strings.isNullOrEmpty( userId ) || UNKNOWN.equals( userId ) );
    }


    public static void setIfNeeded()
    {
        if ( !isSet() )
        {
            set();
        }
    }


    public static void set( final Subject subject )
    {
        checkNotNull( subject );
        String userId = userId( subject );
        log.trace( "Set: {}", userId );
        MDC.put( KEY, userId );
    }


    static String userId( final Subject subject )
    {
        if ( subject != null )
        {
            Object principal = subject.getPrincipal();
            if ( principal != null )
            {
                return principal.toString();
            }
        }
        return UNKNOWN;
    }


    public static void set()
    {
        Subject subject = SecurityUtils.getSubject();
        if ( subject == null )
        {
            MDC.put( KEY, UNKNOWN );
        }
        else
        {
            set( subject );
        }
    }


    public static String get()
    {
        return isSet() ? MDC.get( KEY ) : UNKNOWN;
    }


    public static void unset()
    {
        MDC.remove( KEY );
    }
}


