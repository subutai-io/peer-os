package org.safehaus.subutai.core.metric.ui;


import java.util.concurrent.ExecutorService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.environment.Environment;
import org.safehaus.subutai.common.metric.ResourceHostMetric;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.core.env.api.EnvironmentManager;
import org.safehaus.subutai.core.hostregistry.api.HostRegistry;
import org.safehaus.subutai.core.metric.api.ContainerHostMetric;
import org.safehaus.subutai.core.metric.api.Monitor;
import org.safehaus.subutai.core.metric.impl.ContainerHostMetricImpl;
import org.safehaus.subutai.core.metric.impl.ResourceHostMetricImpl;

import com.google.common.collect.Sets;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Table;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class MonitorFormTest
{

    private static final String OUTPUT = "output";
    private static final String ERR = "Error";
    @Mock
    ServiceLocator serviceLocator;
    @Mock
    Monitor monitor;
    @Mock
    EnvironmentManager environmentManager;
    @Mock
    Table metricsTable;
    @Mock
    ExecutorService executorService;

    @Mock
    HostRegistry hostRegistry;

    private static final String METRIC = " {\"host\":\"py991745969\", \"totalRam\":\"16501141504\", "
            + "\"availableRam\":\"15651282944\", \"usedRam\":\"849711104\",\n"
            + "\"usedCpu\":\"22220161270753\", \"availableDiskRootfs\":\"293251227648\", "
            + "\"availableDiskVar\":\"1175086145536\",\n"
            + "\"availableDiskHome\":\"1175086145536\", \"availableDiskOpt\":\"293251227648\", "
            + "\"usedDiskRootfs\":\"584933376\",\n"
            + "\"usedDiskVar\":\"258498560\", \"usedDiskHome\":\"258498560\", \"usedDiskOpt\":\"584933376\", "
            + "\"totalDiskRootfs\":\"298500227072\",\n"
            + "\"totalDiskVar\":\"1194000908288\", \"totalDiskHome\":\"1194000908288\", "
            + "\"totalDiskOpt\":\"298500227072\"}";


    ContainerHostMetric containerHostMetric;
    ResourceHostMetric resourceHostMetric;
    MonitorForm monitorForm;


    @Before
    public void setUp() throws Exception
    {
        when( serviceLocator.getService( Monitor.class ) ).thenReturn( monitor );
        when( serviceLocator.getService( EnvironmentManager.class ) ).thenReturn( environmentManager );
        when( environmentManager.getEnvironments() ).thenReturn( Sets.<Environment>newHashSet() );

        monitorForm = new MonitorForm( serviceLocator, hostRegistry );
        monitorForm.metricTable = metricsTable;
        monitorForm.executorService = executorService;
        containerHostMetric = JsonUtil.fromJson( METRIC, ContainerHostMetricImpl.class );
        resourceHostMetric = JsonUtil.fromJson( METRIC, ResourceHostMetricImpl.class );
    }


    @Test
    public void testGetContainerHostsButton() throws Exception
    {
        assertTrue( monitorForm.getContainerHostsButton() instanceof Button );
    }


    @Test
    public void testGetResourceHostsButton() throws Exception
    {
        assertTrue( monitorForm.getResourceHostsButton() instanceof Button );
    }


    @Test
    public void testGetEnvironmentComboBox() throws Exception
    {
        assertTrue( monitorForm.getEnvironmentComboBox() instanceof ComboBox );
    }


    @Test
    public void testPrintResourceHostMetrics() throws Exception
    {
        when( monitor.getResourceHostsMetrics() ).thenReturn( Sets.newHashSet( resourceHostMetric ) );

        monitorForm.printResourceHostMetrics();

        //        verify( metricsTable ).addItem( any( Object[].class ), anyObject() );

        verify( executorService ).submit( any( Runnable.class ) );

//        MonitorException exception = mock( MonitorException.class );
//        when( exception.getMessage() ).thenReturn( ERR );
//        doThrow( exception ).when( monitor ).getResourceHostsMetrics();
//
//        try
//        {
//            monitorForm.printResourceHostMetrics();
//        }
//        catch ( NullPointerException e )
//        {
//        }
//
//        verify( exception ).printStackTrace( any( PrintStream.class ) );
    }


    @Test
    public void testPrintContainerHostMetrics() throws Exception
    {
        Environment environment = mock( Environment.class );
        when( monitor.getContainerHostsMetrics( environment ) ).thenReturn( Sets.newHashSet( containerHostMetric ) );


        monitorForm.printContainerMetrics( environment );

//        verify( metricsTable ).addItem( any( Object[].class ), anyObject() );

        verify( executorService ).submit( any( Runnable.class ) );

//        MonitorException exception = mock( MonitorException.class );
//        doThrow( exception ).when( monitor ).getContainerHostsMetrics( environment );
//        when( exception.getMessage() ).thenReturn( ERR );
//        try
//        {
//            monitorForm.printContainerMetrics( environment );
//        }
//        catch ( NullPointerException e )
//        {
//        }
//        verify( exception ).printStackTrace( any( PrintStream.class ) );
    }
}
