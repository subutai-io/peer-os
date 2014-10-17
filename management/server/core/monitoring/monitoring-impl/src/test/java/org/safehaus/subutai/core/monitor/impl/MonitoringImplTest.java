package org.safehaus.subutai.core.monitor.impl;


import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.core.monitor.api.Metric;
import org.safehaus.subutai.core.monitor.api.MetricType;

import com.google.common.collect.Sets;


/**
 * Test for MonitoringImpl
 */
public class MonitoringImplTest
{


    private MonitoringImpl monitoring;


    @Before
    public void setUp()
    {
        monitoring = new MonitoringImpl( "172.16.131.203", 9200 );
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
//                        Sets.newHashSet( MetricType.DISK_OPS , MetricType.CPU_USER), startDate, endDate, 10 );
//
//        for ( Metric metric : metrics )
//        {
//            System.out.println( metric );
//        }
    }
}
