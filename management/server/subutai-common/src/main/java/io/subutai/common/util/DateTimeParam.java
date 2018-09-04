package io.subutai.common.util;


import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.base.Preconditions;


/**
 * Date & time parameter class
 */
public class DateTimeParam
{
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String TIME_FORMAT = "HH:mm:ss";
    public static final String DATE_TIME_FORMAT = DATE_FORMAT + " " + TIME_FORMAT;
    private final Date date;
    private static final DateFormat dateFormat = new SimpleDateFormat( DATE_FORMAT );
    private static final DateFormat timeFormat = new SimpleDateFormat( TIME_FORMAT );
    private static final DateFormat dateTimeFormat = new SimpleDateFormat( DATE_TIME_FORMAT );


    @JsonCreator
    public DateTimeParam( String str ) throws ParseException, UnsupportedEncodingException
    {
        Preconditions.checkNotNull( str );
        Preconditions.checkArgument( str.length() >= 10 );

        String decoded = URLDecoder.decode( str, StandardCharsets.UTF_8.name() );
        this.date = dateTimeFormat.parse( decoded );
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
        return dateFormat.format( date );
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
