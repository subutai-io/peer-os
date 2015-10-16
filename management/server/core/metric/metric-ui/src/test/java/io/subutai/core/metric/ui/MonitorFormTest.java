package io.subutai.core.metric.ui;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Sets;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Table;

import io.subutai.common.environment.Environment;
import io.subutai.common.metric.ContainerHostMetric;
import io.subutai.common.metric.ResourceHostMetric;
import io.subutai.common.util.JsonUtil;
import io.subutai.common.util.ServiceLocator;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.hostregistry.api.HostRegistry;
import io.subutai.core.metric.api.Monitor;
import io.subutai.core.metric.impl.ContainerHostMetricImpl;

import static org.junit.Assert.assertTrue;
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
//    @Mock
//    ExecutorService executorService;

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
//        monitorForm.executorService = executorService;
        containerHostMetric = JsonUtil.fromJson( METRIC, ContainerHostMetricImpl.class );
        resourceHostMetric = JsonUtil.fromJson( METRIC, ResourceHostMetric.class );
    }


    @Test
    public void testGetEnvironmentComboBox() throws Exception
    {
        assertTrue( monitorForm.getEnvironmentComboBox() instanceof ComboBox );
    }

}
