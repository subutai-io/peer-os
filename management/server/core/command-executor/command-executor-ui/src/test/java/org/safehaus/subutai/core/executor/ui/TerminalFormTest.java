package org.safehaus.subutai.core.executor.ui;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.core.executor.api.CommandExecutor;
import org.safehaus.subutai.core.hostregistry.api.HostRegistry;
import org.safehaus.subutai.server.ui.component.HostTree;

import com.vaadin.ui.TextArea;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class TerminalFormTest
{
    private static final String OUTPUT = "output";
    @Mock
    CommandExecutor commandExecutor;
    @Mock
    HostRegistry hostRegistry;
    @Mock
    TextArea commandOutputTxtArea;
    @Mock
    HostTree hostTree;

    TerminalForm terminalForm;


    @Before
    public void setUp() throws Exception
    {
        terminalForm = new TerminalForm( commandExecutor, hostRegistry );
        when( commandOutputTxtArea.getValue() ).thenReturn( OUTPUT );
    }


    @Test
    public void testProperties() throws Exception
    {
        assertNotNull( terminalForm.getHostTree() );
        assertNotNull( terminalForm.getProgramTxtFld() );
        assertNotNull( terminalForm.getTimeoutTxtFld() );
        assertNotNull( terminalForm.getWorkDirTxtFld() );
        assertNotNull( terminalForm.getRunAsTxtFld() );
        assertNotNull( terminalForm.getRequestTypeCombo() );
        assertNotNull( terminalForm.getIndicator() );
        assertNotNull( terminalForm.getTaskCount() );
    }


    @Test
    public void testAddOutput() throws Exception
    {
        terminalForm.commandOutputTxtArea = commandOutputTxtArea;

        terminalForm.addOutput( OUTPUT );

        verify( commandOutputTxtArea ).setValue( anyString() );
    }


    @Test
    public void testDispose() throws Exception
    {
        terminalForm.hostTree = hostTree;

        terminalForm.dispose();

        verify( hostTree ).dispose();
    }
}
