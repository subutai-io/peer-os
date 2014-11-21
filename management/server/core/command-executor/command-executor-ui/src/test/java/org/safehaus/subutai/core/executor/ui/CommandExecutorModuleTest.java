package org.safehaus.subutai.core.executor.ui;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.core.executor.api.CommandExecutor;
import org.safehaus.subutai.core.hostregistry.api.HostRegistry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


@RunWith( MockitoJUnitRunner.class )
public class CommandExecutorModuleTest
{
    @Mock
    HostRegistry hostRegistry;
    @Mock
    CommandExecutor commandExecutor;

    CommandExecutorModule commandExecutorModule;


    @Before
    public void setUp() throws Exception
    {
        commandExecutorModule = new CommandExecutorModule( commandExecutor, hostRegistry );
    }


    @Test
    public void testProperties() throws Exception
    {
        assertEquals( CommandExecutorModule.MODULE_NAME, commandExecutorModule.getName() );
        assertEquals( CommandExecutorModule.MODULE_NAME, commandExecutorModule.getId() );
        assertNotNull( commandExecutorModule.getImage() );
        assertTrue( commandExecutorModule.isCorePlugin() );
    }


    @Test
    public void testCreateComponent() throws Exception
    {
        assertTrue( commandExecutorModule.createComponent() instanceof TerminalForm );
    }
}
