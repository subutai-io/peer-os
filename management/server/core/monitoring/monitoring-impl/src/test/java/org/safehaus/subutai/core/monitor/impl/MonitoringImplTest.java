package org.safehaus.subutai.core.monitor.impl;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.core.monitor.api.Metric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by talas on 10/6/14.
 */
public class MonitoringImplTest
{
    private static final Logger LOG = LoggerFactory.getLogger( MonitoringImpl.class );
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String QUERY = FileUtil.getContent( "elasticsearch/query.json", MonitoringImpl.class );
    private final DateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );


    private MonitoringImpl monitoring;


    @Before
    public void setupClasses()
    {
        monitoring = new MonitoringImpl();
    }


    @Test
    public void shouldReturnData()
    {
        monitoring.getDataForAllMetrics( "host", new Date(), new Date() );
    }


    @Test
    public void shouldTestGetData()
    {
        monitoring.getData( "host", Metric.CPU_IDLE, new Date(), new Date() );
    }
}
