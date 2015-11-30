package io.subutai.common.util;


import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.common.base.Preconditions;


/**
 * Date parameter class
 */
public class DateParam
{
    public static final String FORMAT = "yyyy-MM-dd";
    private final Date date;
    public static final DateFormat dateFormat = new SimpleDateFormat( FORMAT );


    public DateParam( String str ) throws ParseException
    {
        Preconditions.checkNotNull( str );
        Preconditions.checkArgument( str.length() == 10 );

        this.date = dateFormat.parse( str );
    }


    public DateParam( final Date date )
    {
        this.date = date;
    }


    public Date getDate()
    {
        return date;
    }


    @Override
    public String toString()
    {
        return dateFormat.format( date );
    }
}
