package org.safehaus.subutai.core.monitor.impl;


import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.common.util.RestUtil;


/**
 * Test for MonitoringImpl
 */
public class MonitoringImplTest
{
    private static final String HOST = "host";
    private static final int PORT = 9200;
    private static final String RESPONSE =
            FileUtil.getContent( "elasticsearch/test_response.json", MonitoringImpl.class );
    private MonitoringImplExt monitoring;


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
    }


    @Before
    public void setUp()
    {
        monitoring = new MonitoringImplExt( HOST, PORT );
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
    public void testQuery() throws Exception
    {


        //        Calendar calendar = Calendar.getInstance();
        //        calendar.set( Calendar.YEAR, 2014 );
        //        calendar.set( Calendar.MONTH, Calendar.OCTOBER );
        //        calendar.set( Calendar.DAY_OF_MONTH, 15 );
        //
        //        Date startDate = calendar.getTime();
        //        calendar.set( Calendar.DAY_OF_MONTH, 20 );
        //        Date endDate = calendar.getTime();
        //
        //
        //        List<Metric> metrics = monitoring
        //                .getMetrics( Sets.newHashSet( "py627967291", "py420202276", "cassandra", "master1" ),
        //                        Sets.newHashSet(  MetricType.DISK_OPS ), startDate, endDate, 10 );
        //
        //        for ( Metric metric : metrics )
        //        {
        //            System.out.println( metric );
        //        }
    }
}
