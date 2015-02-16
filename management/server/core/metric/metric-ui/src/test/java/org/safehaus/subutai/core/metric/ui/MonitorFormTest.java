package org.safehaus.subutai.core.metric.ui;


import java.io.PrintStream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.environment.Environment;
import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.core.env.api.EnvironmentManager;
import org.safehaus.subutai.core.metric.api.ContainerHostMetric;
import org.safehaus.subutai.core.metric.api.Monitor;
import org.safehaus.subutai.core.metric.api.MonitorException;
import org.safehaus.subutai.common.metric.ResourceHostMetric;

import com.google.common.collect.Sets;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.TextArea;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class MonitorFormTest
{

    private static final String OUTPUT = "output";
    @Mock
    ServiceLocator serviceLocator;
    @Mock
    Monitor monitor;
    @Mock
    EnvironmentManager environmentManager;
    @Mock
    TextArea outputTextArea;


    MonitorForm monitorForm;


    @Before
    public void setUp() throws Exception
    {
        when( serviceLocator.getService( Monitor.class ) ).thenReturn( monitor );
        when( serviceLocator.getService( EnvironmentManager.class ) ).thenReturn( environmentManager );
        when( environmentManager.getEnvironments() ).thenReturn( Sets.<Environment>newHashSet() );

        monitorForm = new MonitorForm( serviceLocator );
        monitorForm.outputTxtArea = outputTextArea;
        when( outputTextArea.getValue() ).thenReturn( OUTPUT );
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
    public void testGetOutputArea() throws Exception
    {
        assertTrue( monitorForm.getOutputArea() instanceof TextArea );
    }


    @Test
    public void testGetEnvironmentComboBox() throws Exception
    {
        assertTrue( monitorForm.getEnvironmentComboBox() instanceof ComboBox );
    }


    @Test
    public void testAddOutput() throws Exception
    {

        monitorForm.addOutput( OUTPUT );


        verify( outputTextArea ).setValue( anyString() );
    }


    @Test
    public void testPrintResourceHostMetrics() throws Exception
    {
        ResourceHostMetric resourceHostMetric = mock( ResourceHostMetric.class );
        when( monitor.getResourceHostsMetrics() ).thenReturn( Sets.newHashSet( resourceHostMetric ) );

        monitorForm.printResourceHostMetrics();

        verify( outputTextArea ).setValue( anyString() );


        MonitorException exception = mock( MonitorException.class );
        doThrow( exception ).when( monitor ).getResourceHostsMetrics();

        monitorForm.printResourceHostMetrics();

        verify( exception ).printStackTrace( any( PrintStream.class ) );
    }


    @Test
    public void testPrintContainerHostMetrics() throws Exception
    {
        ContainerHostMetric containerHostMetric = mock( ContainerHostMetric.class );
        Environment environment = mock( Environment.class );
        when( monitor.getContainerHostsMetrics( environment ) ).thenReturn( Sets.newHashSet( containerHostMetric ) );


        monitorForm.printContainerMetrics( environment );

        verify( outputTextArea ).setValue( anyString() );

        MonitorException exception = mock( MonitorException.class );
        doThrow( exception ).when( monitor ).getContainerHostsMetrics( environment );

        monitorForm.printContainerMetrics( environment );

        verify( exception ).printStackTrace( any( PrintStream.class ) );
    }
}
