package org.safehaus.subutai.common.util;


/**
 * Provides number utility functions
 */
public class NumUtil
{

    private NumUtil()
    {
    }


    public static boolean isIntBetween( int num, int from, int to )
    {
        return num >= from && num <= to;
    }
}
