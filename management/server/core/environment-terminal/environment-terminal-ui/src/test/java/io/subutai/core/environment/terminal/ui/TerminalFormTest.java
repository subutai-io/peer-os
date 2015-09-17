package io.subutai.core.environment.terminal.ui;


import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.vaadin.ui.TextArea;
import com.vaadin.ui.UI;

import io.subutai.core.environment.api.EnvironmentManager;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class TerminalFormTest
{
    private static final String OUTPUT = "output";

    @Mock
    EnvironmentManager environmentManager;

    @Mock
    TextArea commandOutputTxtArea;

    @Mock
    UI ui;

    TerminalForm terminalForm;


    @Before
    public void setUp()
    {
        terminalForm = new TerminalForm( environmentManager, new Date() );
    }


    @Test
    public void testProperties()
    {
        assertNotNull( terminalForm.daemonChk );
        assertNotNull( terminalForm.environmentTree );
        assertNotNull( terminalForm.indicator );
        assertNotNull( terminalForm.programTxtFld );
        assertNotNull( terminalForm.taskCount );
        assertNotNull( terminalForm.workDirTxtFld );
    }


    @Test
    public void testAddOutput() throws Exception
    {
        when( commandOutputTxtArea.getValue() ).thenReturn( OUTPUT );
        terminalForm.commandOutputTxtArea = commandOutputTxtArea;

        terminalForm.addOutput( OUTPUT );
    }
}
