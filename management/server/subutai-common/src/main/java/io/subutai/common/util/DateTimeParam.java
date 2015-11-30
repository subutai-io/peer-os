package io.subutai.common.util;


import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.common.base.Preconditions;


/**
 * Date & time parameter class
 */
public class DateTimeParam
{
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String TIME_FORMAT = "HH:MM:SS";
    public static final String DATE_TIME_FORMAT = DATE_FORMAT + " " + TIME_FORMAT;
    private final Date date;
    private static final DateFormat dateFormat = new SimpleDateFormat( DATE_FORMAT );
    private static final DateFormat timeFormat = new SimpleDateFormat( TIME_FORMAT );
    private static final DateFormat dateTimeFormat = new SimpleDateFormat( DATE_TIME_FORMAT );


    public DateTimeParam( String str ) throws ParseException
    {
        Preconditions.checkNotNull( str );
        Preconditions.checkArgument( str.length() == 10 );

        this.date = dateTimeFormat.parse( str );
    }


    public DateTimeParam( final Date date )
    {
        this.date = date;
    }


    public Date getDate()
    {
        return date;
    }


    public String getDateString()
    {
        return timeFormat.format( date );
    }


    public String getTimeString()
    {
        return timeFormat.format( date );
    }


    @Override
    public String toString()
    {
        return dateTimeFormat.format( date );
    }
}
