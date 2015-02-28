package org.safehaus.subutai.common.helper;


import org.safehaus.subutai.common.security.SubutaiLoginContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

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

    //
    //    public static void setIfNeeded()
    //    {
    //        if ( !isSet() )
    //        {
    //            set();
    //        }
    //    }


    public static void set( final SubutaiLoginContext subject )
    {
        checkNotNull( subject );
        String userId = userId( subject );
        log.trace( "Set: {}", userId );
        MDC.put( KEY, userId );
    }


    static String userId( final SubutaiLoginContext subject )
    {
        return subject.getUsername();
    }


    //    public static void set()
    //    {
    //        Subject subject = SecurityUtils.getSubject();
    //        if ( subject == null )
    //        {
    //            MDC.put( KEY, UNKNOWN );
    //        }
    //        else
    //        {
    //            set( subject );
    //        }
    //    }


    public static String get()
    {
        return isSet() ? MDC.get( KEY ) : UNKNOWN;
    }


    public static void unset()
    {
        MDC.remove( KEY );
    }
}


