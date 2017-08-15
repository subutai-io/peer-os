package io.subutai.common.util;


import java.lang.reflect.InvocationTargetException;

import org.apache.commons.lang3.exception.ExceptionUtils;


public class ExceptionUtil
{
    public Throwable getRootCause( Throwable throwable )
    {
        Throwable cause = ExceptionUtils.getRootCause( throwable );

        if ( cause instanceof InvocationTargetException )
        {
            cause = cause.getCause();
        }

        return cause == null ? throwable : cause;
    }
}
