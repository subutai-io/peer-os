package org.safehaus.subutai.core.monitor.ui;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.core.monitor.api.Metric;


class Chart
{

    private static final String CHART_TEMPLATE = FileUtil.getContent( "js/chart.js", Chart.class );
    private final DateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
    private final int maxSize;


    Chart( int maxSize )
    {
        this.maxSize = maxSize;
        loadScripts();
    }


    private void loadScripts()
    {
        com.vaadin.ui.JavaScript.getCurrent().execute( "js/jquery.min.js" );
        com.vaadin.ui.JavaScript.getCurrent().execute( "js/jquery.flot.min.js" );
        com.vaadin.ui.JavaScript.getCurrent().execute( "js/jquery.flot.time.min.js" );
    }


    void load( String host, Metric metric, Map<Date, Double> values )
    {

        String data = toPoints( values );
        String label = String.format( "%s for %s", metric.toString(), host );

        String chart = CHART_TEMPLATE.replace( "$label", label ).replace( "$yTitle", metric.getUnit() )
                                     .replace( "$data", data );

        com.vaadin.ui.JavaScript.getCurrent().execute( chart );
    }


    private String toPoints( Map<Date, Double> values )
    {

        StringBuilder str = new StringBuilder();
        int i = 0;

        for ( Map.Entry<Date, Double> entry : values.entrySet() )
        {
            if ( str.length() > 0 )
            {
                str.append( ", " );
            }

            str.append(
                    String.format( "[Date.parse('%s'), %s ]", dateFormat.format( entry.getKey() ), entry.getValue() ) );
            i++;

            if ( i > maxSize )
            {
                break;
            }
        }

        return String.format( "[%s]", str );
    }
}
