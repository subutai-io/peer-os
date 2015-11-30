package io.subutai.common.util;


import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.TestCase.assertEquals;


@RunWith( MockitoJUnitRunner.class )
public class DateTimeParamTest
{
    public static final String EXAMPLE_DATE = "2015-11-30";
    public static final String EXAMPLE_TIME = "23:48:21";
    public static final String EXAMPLE_DATE_TIME = EXAMPLE_DATE + " " + EXAMPLE_TIME;


    @Before
    public void setUp()
    {

    }


    @Test
    public void testStringParam() throws UnsupportedEncodingException, ParseException
    {
        DateTimeParam dateTimeParam = new DateTimeParam( EXAMPLE_DATE_TIME );
        assertEquals( EXAMPLE_DATE, dateTimeParam.getDateString() );
        assertEquals( EXAMPLE_TIME, dateTimeParam.getTimeString() );

        Calendar result = GregorianCalendar.getInstance();
        result.setTime( dateTimeParam.getDate() );
        assertEquals( 2015, result.get( Calendar.YEAR ) );
        assertEquals( 10, result.get( Calendar.MONTH ));
        assertEquals( 30, result.get( Calendar.DAY_OF_MONTH ) );
        assertEquals( 23, result.get( Calendar.HOUR_OF_DAY ) );
        assertEquals( 48, result.get( Calendar.MINUTE ) );
        assertEquals( 21, result.get( Calendar.SECOND ) );
    }
}
