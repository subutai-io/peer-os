package org.safehaus.subutai.common.util;


/**
 * Created by dilshat on 9/23/14.
 */
public class NumUtil
{

    private NumUtil()
    {
    }


    public static boolean isNumBetween( int num, int from, int to )
    {
        return num >= from && num <= to;
    }
}
