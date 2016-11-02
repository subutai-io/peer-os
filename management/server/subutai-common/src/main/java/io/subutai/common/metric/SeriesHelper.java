package io.subutai.common.metric;


import java.util.List;


public class SeriesHelper
{

    private SeriesHelper()
    {
        throw new IllegalAccessError( "Utility class" );
    }


    public static double getAvg( final List<Series> series, final Tag... tags )
    {
        double result = 0;

        int count = 0;
        for ( Series s : series )
        {
            boolean match = true;
            for ( int i = 0; i < tags.length && match; i++ )
            {
                String k = s.getTags().get( tags[i].getName() );
                if ( k == null || !k.equalsIgnoreCase( tags[i].getValue() ) )
                {
                    match = false;
                }
            }

            if ( match )
            {
                for ( Double[] v : s.getValues() )
                {
                    result += v[1];
                    count++;
                }
                break;
            }
        }

        return count > 0 ? result / count : result;
    }


    public static Double getLast( final List<Series> series, final Tag... tags )
    {
        Double result = null;

        for ( Series s : series )
        {
            boolean match = true;
            for ( int i = 0; i < tags.length && match; i++ )
            {
                String k = s.getTags().get( tags[i].getName() );
                if ( k == null || !k.equalsIgnoreCase( tags[i].getValue() ) )
                {
                    match = false;
                }
            }

            if ( match )
            {
                result = s.getValues().get( s.getValues().size() - 1 )[1];
                break;
            }
        }

        return result;
    }
}
