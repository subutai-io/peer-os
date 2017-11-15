package io.subutai.common.util;


import org.apache.commons.lang3.exception.ExceptionUtils;


public class ExceptionUtil
{
    public Throwable getRootCause( Throwable throwable )
    {
        return getRootCauze( throwable );
    }


    public static Throwable getRootCauze( Throwable throwable )
    {
        Throwable cause = ExceptionUtils.getRootCause( throwable );

        return cause == null ? throwable : cause;
    }
}
