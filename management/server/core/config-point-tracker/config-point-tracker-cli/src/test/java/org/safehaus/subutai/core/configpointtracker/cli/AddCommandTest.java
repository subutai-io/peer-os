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
public class AddCommandTest
{

    private ConfigPointTracker configPointTracker;
    private AddCommand addCommand;


    @Before
    public void setupClasses()
    {
        configPointTracker = mock( ConfigPointTracker.class );
        addCommand = new AddCommand();
        addCommand.setConfigPointTracker( configPointTracker );
    }


    @Test( expected = NullPointerException.class )
    public void shouldThrowNullPointExceptionOnSetConfigPointTracker()
    {
        addCommand.setConfigPointTracker( null );
    }


    @Test
    public void shouldAccessConfigPointTrackerOnDoExecute()
    {
        final String configPath = "";
        final String templateName = "";
        when( configPointTracker.add( templateName, configPath ) ).thenReturn( true );
        addCommand.doExecute();
        verify( configPointTracker ).add( templateName, configPath );
    }
}
