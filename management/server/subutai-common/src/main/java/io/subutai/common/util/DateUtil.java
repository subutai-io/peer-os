package io.subutai.common.util;


import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.TimeZone;


public class DateUtil
{

    public static long getUnixTimestamp()
    {
        return Instant.now().getEpochSecond();
    }

    public static String convertMillisToHHMMSS( long millis )
    {
        TimeZone tz = TimeZone.getTimeZone( "UTC" );
        SimpleDateFormat df = new SimpleDateFormat( "HH:mm:ss" );
        df.setTimeZone( tz );
        return df.format( new Date( millis ) );
    }
}
