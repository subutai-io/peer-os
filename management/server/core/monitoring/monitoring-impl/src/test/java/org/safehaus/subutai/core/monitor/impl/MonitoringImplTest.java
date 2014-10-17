package org.safehaus.subutai.core.monitor.impl;


import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.safehaus.subutai.common.exception.HTTPException;
import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.common.util.RestUtil;
import org.safehaus.subutai.core.monitor.api.Metric;
import org.safehaus.subutai.core.monitor.api.MetricType;
import org.safehaus.subutai.core.monitor.api.MonitorException;

import com.google.common.collect.Sets;

import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Test for MonitoringImpl
 */
public class MonitoringImplTest
{
    private static final String HOST = "host";
    private static final int PORT = 9200;
    private static final String DEFAULT_RESPONSE =
            FileUtil.getContent( "elasticsearch/default_response.json", MonitoringImpl.class );
    private static final String DISK_RESPONSE =
            FileUtil.getContent( "elasticsearch/disk_response.json", MonitoringImpl.class );
    private static final Set<String> HOSTS = Sets.newHashSet( "host1", "host2" );
    private static final Set<MetricType> METRICS = Sets.newHashSet( MetricType.values() );
    private static final Date START_DATE = new Date();
    private static final Date END_DATE = new Date();
    private static final int LIMIT = 10;

    private MonitoringImplExt monitoring;
    private RestUtil restUtil = mock( RestUtil.class );
    private ObjectMapper objectMapper = mock( ObjectMapper.class );


    class MonitoringImplExt extends MonitoringImpl
    {
        MonitoringImplExt( final String esHost, final int esPort )
        {
            super( esHost, esPort );
        }


        public void setRestUtil( RestUtil restUtil )
        {
            this.restUtil = restUtil;
        }


        public void setObjectMapper( ObjectMapper objectMapper )
        {
            this.objectMapper = objectMapper;
        }
    }


    @Before
    public void setUp() throws HTTPException
    {
        monitoring = new MonitoringImplExt( HOST, PORT );
        monitoring.setRestUtil( restUtil );
        when( restUtil.request( any( RestUtil.RequestType.class ), anyString(), anyMap() ) )
                .thenAnswer( new Answer<String>()
                {
                    @Override
                    public String answer( final InvocationOnMock invocation ) throws Throwable
                    {
                        Map<String, String> params = ( Map<String, String> ) invocation.getArguments()[2];
                        if ( params.get( "source" ).contains( "collectd_type" ) )
                        {
                            return DISK_RESPONSE;
                        }
                        return DEFAULT_RESPONSE;
                    }
                } );
    }


    @Test( expected = IllegalArgumentException.class )
    public void testConstructorShouldFailOnNullHost() throws Exception
    {
        new MonitoringImpl( null, PORT );
    }


    @Test( expected = IllegalArgumentException.class )
    public void testConstructorShouldFailOnInvalidPort() throws Exception
    {
        new MonitoringImpl( HOST, -1 );
    }


    @Test
    public void testGetMetrics() throws Exception
    {

        List<Metric> metrics = monitoring.getMetrics( HOSTS, METRICS, START_DATE, END_DATE, LIMIT );

        assertFalse( metrics.isEmpty() );
    }


    @Test( expected = MonitorException.class )
    public void testGetMetricsException() throws Exception
    {
        doThrow( new HTTPException( "" ) ).when( restUtil )
                                          .request( any( RestUtil.RequestType.class ), anyString(), anyMap() );

        monitoring.getMetrics( HOSTS, METRICS, START_DATE, END_DATE, LIMIT );
    }


    @Test( expected = MonitorException.class )
    public void testExceptionInGetDefaultMetrics() throws Exception
    {
        monitoring.setObjectMapper( objectMapper );

        when( objectMapper.readTree( anyString() ) ).thenAnswer( new Answer<Object>()
        {
            @Override
            public Object answer( final InvocationOnMock invocation ) throws Throwable
            {
                String response = ( String ) invocation.getArguments()[0];
                if ( response.contains( "log_host" ) )
                {
                    throw new IOException( "" );
                }
                return null;
            }
        } );

        monitoring.getMetrics( HOSTS, Sets.newHashSet( MetricType.CPU_USER ), START_DATE, END_DATE, LIMIT );
    }


    @Test( expected = MonitorException.class )
    public void testExceptionInGetDiskMetrics() throws Exception
    {
        monitoring.setObjectMapper( objectMapper );

        when( objectMapper.readTree( anyString() ) ).thenAnswer( new Answer<Object>()
        {
            @Override
            public Object answer( final InvocationOnMock invocation ) throws Throwable
            {
                String response = ( String ) invocation.getArguments()[0];
                if ( response.contains( "collectd_type" ) )
                {
                    throw new IOException( "" );
                }
                return null;
            }
        } );

        monitoring.getMetrics( HOSTS, Sets.newHashSet( MetricType.DISK_OPS ), START_DATE, END_DATE, LIMIT );
    }
}
