package org.safehaus.subutai.core.monitor.impl;


import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.safehaus.subutai.common.exception.HTTPException;
import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.common.util.NumUtil;
import org.safehaus.subutai.common.util.RestUtil;
import org.safehaus.subutai.common.util.StringUtil;
import org.safehaus.subutai.core.monitor.api.Metric;
import org.safehaus.subutai.core.monitor.api.MetricType;
import org.safehaus.subutai.core.monitor.api.MonitorException;
import org.safehaus.subutai.core.monitor.api.Monitoring;
import org.safehaus.subutai.core.monitor.api.PluginType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


public class MonitoringImpl implements Monitoring
{

    private static final Logger LOG = LoggerFactory.getLogger( MonitoringImpl.class );
    private static final String DEFAULT_QUERY =
            FileUtil.getContent( "elasticsearch/query_default.json", MonitoringImpl.class );
    private static final String DISK_QUERY =
            FileUtil.getContent( "elasticsearch/query_disk.json", MonitoringImpl.class );
    private final DateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
    private final String esHost;
    private final int esPort;
    protected ObjectMapper objectMapper;
    protected RestUtil restUtil;


    public MonitoringImpl( final String esHost, final int esPort )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( esHost ), "ES host is missing" );
        Preconditions.checkArgument( esPort >= 1024 && esPort <= 65536, "Port must be n range 1024 and 65536" );

        this.esHost = esHost;
        this.esPort = esPort;
        objectMapper = new ObjectMapper();
        this.restUtil = new RestUtil();
    }


    @Override
    public List<Metric> getMetrics( Set<String> hosts, Set<MetricType> metricTypes, Date startDate, Date endDate,
                                    int limit ) throws MonitorException
    {

        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( hosts ), "Hosts is empty" );
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( metricTypes ), "Metrics is empty" );
        Preconditions.checkNotNull( startDate, "Start date is null" );
        Preconditions.checkNotNull( endDate, "End date is null" );
        Preconditions.checkArgument( NumUtil.isIntBetween( limit, 1, 10000 ), "Limit must be between 1 and 1000" );


        Set<String> defaultMetricsTypes = new HashSet<>();
        Set<String> hddMetricsTypes = new HashSet<>();
        for ( MetricType metricType : metricTypes )
        {
            if ( metricType.getPluginType() == PluginType.DEFAULT )
            {
                defaultMetricsTypes.add( metricType.name().toLowerCase() );
            }
            else if ( metricType.getPluginType() == PluginType.DISK )
            {
                hddMetricsTypes.add( metricType.name().toLowerCase() );
            }
        }

        List<Metric> result = new ArrayList<>();
        if ( !defaultMetricsTypes.isEmpty() )
        {
            result.addAll( getDefaultMetrics(
                    executeQuery( PluginType.DEFAULT, hosts, defaultMetricsTypes, startDate, endDate, limit ) ) );
        }
        if ( !hddMetricsTypes.isEmpty() )
        {
            result.addAll( getHddMetrics(
                    executeQuery( PluginType.DISK, hosts, hddMetricsTypes, startDate, endDate, limit ) ) );
        }

        return result;
    }


    private String executeQuery( PluginType pluginType, Set<String> hosts, Set<String> metricNames, Date startDate,
                                 Date endDate, int limit ) throws MonitorException
    {
        String query;
        switch ( pluginType )
        {
            case DISK:
                query = DISK_QUERY;
                break;
            default:
                query = DEFAULT_QUERY;
        }
        query = query.replace( "$startDate", dateToStr( startDate ) ).replace( "$endDate", dateToStr( endDate ) )
                     .replace( "$hosts", "[" + StringUtil.joinStrings( hosts, ',', true ) + "]" )
                     .replace( "$names", "[" + StringUtil.joinStrings( metricNames, ',', true ) + "]" )
                     .replace( "$limit", String.valueOf( limit ) );
        Map<String, String> params = new HashMap<>();
        params.put( "source", query );
        try
        {

            return restUtil.request( RestUtil.RequestType.GET,
                    String.format( "http://%s:%d/_all/logs/_search", esHost, esPort ), params );
        }
        catch ( HTTPException e )
        {
            LOG.error( "Error in getDefaultMetrics", e );
            throw new MonitorException( e );
        }
    }


    private List<Metric> getHddMetrics( final String response ) throws MonitorException
    {
        try
        {
            List<JsonNode> nodes = toNodes( response );
            List<Metric> metrics = new ArrayList<>();

            for ( JsonNode node : nodes )
            {
                Date date = strToDate( node.get( "@timestamp" ).asText() );
                String host = node.get( "host" ).asText();
                String name = node.get( "collectd_type" ).asText();
                Long read = node.get( "read" ).getLongValue();
                Long write = node.get( "write" ).getLongValue();

                Metric metric = new Metric( MetricType.valueOf( name.toUpperCase() ), read, write, host, date );
                metrics.add( metric );
            }

            return metrics;
        }
        catch ( ParseException | IOException e )
        {
            LOG.error( "Error in getHddMetrics", e );
            throw new MonitorException( e );
        }
    }


    private List<Metric> getDefaultMetrics( String response ) throws MonitorException
    {
        try
        {
            List<JsonNode> nodes = toNodes( response );
            List<Metric> metrics = new ArrayList<>();

            for ( JsonNode node : nodes )
            {
                Date date = strToDate( node.get( "@timestamp" ).asText() );
                String host = node.get( "log_host" ).asText();
                String name = node.get( "name" ).asText();
                Double value = node.get( "val" ).asDouble();
                String units = node.get( "units" ).asText();

                Metric metric = new Metric( MetricType.valueOf( name.toUpperCase() ), value, units, host, date );
                metrics.add( metric );
            }

            return metrics;
        }
        catch ( ParseException | IOException e )
        {
            LOG.error( "Error in getDefaultMetrics", e );
            throw new MonitorException( e );
        }
    }


    protected List<JsonNode> toNodes( String response ) throws IOException
    {
        JsonNode json = objectMapper.readTree( response );
        JsonNode hits = json.get( "hits" ).get( "hits" );

        List<JsonNode> nodes = new ArrayList<>();

        for ( int i = 0; i < hits.size(); i++ )
        {
            JsonNode node = hits.get( i ).get( "fields" );
            nodes.add( node );
        }

        return nodes;
    }


    protected Date strToDate( String dateStr ) throws ParseException
    {

        String target = dateStr.replace( "T", " " ).replace( "Z", "" );

        return dateFormat.parse( target );
    }


    protected String dateToStr( Date date )
    {
        return dateFormat.format( date ).replace( " ", "T" );
    }
}
