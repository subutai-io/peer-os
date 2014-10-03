package org.safehaus.subutai.core.configpointtracker.cli;


import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.core.configpointtracker.api.ConfigPointTracker;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Created by talas on 10/3/14.
 */
public class RemoveCommandTest
{

    private ConfigPointTracker configPointTracker;
    private RemoveCommand removeCommand;


    @Before
    public void setupClasses()
    {
        configPointTracker = mock( ConfigPointTracker.class );
        removeCommand = new RemoveCommand();
        removeCommand.setConfigPointTracker( configPointTracker );
    }


    @Test( expected = NullPointerException.class )
    public void shouldThrowNullPointExceptionOnSetConfigPointTracker()
    {
        removeCommand.setConfigPointTracker( null );
    }


    @Test
    public void shouldAccessConfigPointTrackerOnDoExecute()
    {
        final String templateName = "";
        final String configPath = "";
        when( configPointTracker.remove( templateName, configPath ) ).thenReturn( true );
        removeCommand.doExecute();
        verify( configPointTracker ).remove( templateName, configPath );
    }
}
