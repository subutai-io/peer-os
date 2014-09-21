package org.safehaus.subutai.core.monitor.impl;


import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.safehaus.subutai.core.monitor.api.Metric;
import org.safehaus.subutai.core.monitor.api.Monitor;


public class MonitorImpl implements Monitor
{

    private static final Logger LOG = Logger.getLogger( MonitorImpl.class.getName() );

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String QUERY = FileUtil.getContent( "elasticsearch/query.json" );
    private final DateFormat DATE_FORMAT = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );


    /**
     * The method uses the simplest way to get the data for all the metrics. In terms of performance this is not the
     * best way but the speed should not be an issue for near future. Further optimization should be done with rewriting
     * the query to elasticsearch. See: query.json.
     */
    @Override
    public Map<Metric, Map<Date, Double>> getDataForAllMetrics( String host, Date startDate, Date endDate )
    {

        HashMap<Metric, Map<Date, Double>> data = new HashMap<>();

        for ( Metric metric : Metric.values() )
        {
            Map<Date, Double> metricData = getData( host, metric, startDate, endDate );
            data.put( metric, metricData );
        }

        return data;
    }


    @Override
    public Map<Date, Double> getData( String host, Metric metric, Date startDate, Date endDate )
    {
        LOG.log( Level.INFO,
                String.format( "host: {%s}, metric: {%s}, startDate: {%s}, endDate: {%s}", host, metric, startDate,
                        endDate ) );

        Map<Date, Double> data = Collections.emptyMap();

        try
        {
            data = execute( host, metric, startDate, endDate );
        }
        catch ( Exception e )
        {
            LOG.log( Level.SEVERE, "Error while executing query: ", e );
        }

        return data;
    }


    private Map<Date, Double> execute( String host, Metric metric, Date startDate, Date endDate ) throws Exception
    {

        String query = QUERY.replace( "$host", host ).replace( "$metricName", metric.name().toLowerCase() )
                            .replace( "$startDate", dateToStr( startDate ) )
                            .replace( "$endDate", dateToStr( endDate ) );

        LOG.log( Level.CONFIG, String.format( "query: {%s}", query ) );

        String response = HttpPost.execute( query );
        List<JsonNode> nodes = toNodes( response );

        LOG.log( Level.INFO, String.format( "nodes count: {%d}", nodes.size() ) );

        // Reversing the list b/c the query returns the data in desc order (to get the latest values first).
        Collections.reverse( nodes );

        return toMap( nodes );
    }


    private static List<JsonNode> toNodes( String response ) throws IOException
    {

        JsonNode json = OBJECT_MAPPER.readTree( response );
        JsonNode hits = json.get( "hits" ).get( "hits" );

        ArrayList<JsonNode> nodes = new ArrayList<>();

        for ( int i = 0; i < hits.size(); i++ )
        {
            JsonNode node = hits.get( i ).get( "_source" );
            nodes.add( node );

            LOG.log( Level.CONFIG, String.format( "node: {%s}", node ) );
        }

        return nodes;
    }


    private Map<Date, Double> toMap( List<JsonNode> nodes )
    {

        Map<Date, Double> values = new TreeMap<>();

        for ( JsonNode node : nodes )
        {
            Date date = strToDate( node.get( "@timestamp" ).asText() );
            double value = node.get( "val" ).asDouble();
            values.put( date, value );
        }

        return values;
    }


    private Date strToDate( String dateStr )
    {

        String target = dateStr.replace( "T", " " ).replace( "Z", "" );
        Date date = null;

        try
        {
            date = DATE_FORMAT.parse( target );
        }
        catch ( ParseException e )
        {
            LOG.log( Level.SEVERE, "Error while parsing time: ", e );
        }

        return date;
    }


    private String dateToStr( Date date )
    {
        return DATE_FORMAT.format( date ).replace( " ", "T" );
    }
}
