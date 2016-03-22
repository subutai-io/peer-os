package io.subutai.common.util;


import java.time.Instant;


public class DateUtil
{

    public static long getUnixTimestamp()
    {
        return Instant.now().getEpochSecond();
    }
}
