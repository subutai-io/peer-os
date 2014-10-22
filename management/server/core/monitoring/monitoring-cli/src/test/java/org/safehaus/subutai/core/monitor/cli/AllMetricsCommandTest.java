package org.safehaus.subutai.core.monitor.cli;


import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.core.monitor.api.Metric;
import org.safehaus.subutai.core.monitor.api.MonitorException;
import org.safehaus.subutai.core.monitor.api.Monitoring;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anySet;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Test for AllMetricsCommand
 */
public class AllMetricsCommandTest
{
    private static final String SOUT = "METRIC";
    private static final String ERR_MSG = "ERR";
    private static final Throwable ERR = new Throwable( ERR_MSG );
    private Monitoring monitoring;
    private AllMetricsCommand allMetricsCommand;
    private ByteArrayOutputStream myOut;


    @Before
    public void setUp()
    {
        monitoring = mock( Monitoring.class );
        allMetricsCommand = new AllMetricsCommand( monitoring );
        myOut = new ByteArrayOutputStream();
        System.setOut( new PrintStream( myOut ) );
    }


    @After
    public void tearDown()
    {
        System.setOut( System.out );
    }


    private String getSysOut()
    {
        return myOut.toString().trim();
    }


    @Test( expected = NullPointerException.class )
    public void shouldThrowNullPointerExceptionOnSetMonitoring()
    {

        new AllMetricsCommand( null );
    }


    @Test
    public void testDoExecute() throws MonitorException
    {
        List<Metric> metrics = new ArrayList<>();
        Metric metric = mock( Metric.class );
        when( metric.toString() ).thenReturn( SOUT );
        metrics.add( metric );
        when( monitoring.getMetrics( anySet(), anySet(), any( Date.class ), any( Date.class ), anyInt() ) )
                .thenReturn( metrics );

        allMetricsCommand.doExecute();

        assertEquals( SOUT, getSysOut() );
    }


    @Test
    public void testException() throws Exception
    {

        doThrow( new MonitorException( ERR ) ).when( monitoring )
                                              .getMetrics( anySet(), anySet(), any( Date.class ), any( Date.class ),
                                                      anyInt() );

        allMetricsCommand.doExecute();

        assertThat( getSysOut(), containsString( ERR_MSG ) );
    }
}
