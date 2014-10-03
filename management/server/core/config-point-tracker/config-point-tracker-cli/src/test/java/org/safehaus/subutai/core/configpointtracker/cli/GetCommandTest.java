package org.safehaus.subutai.core.configpointtracker.cli;


import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.core.configpointtracker.api.ConfigPointTracker;

import com.google.common.collect.Sets;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Created by talas on 10/3/14.
 */
public class GetCommandTest
{
    private ConfigPointTracker configPointTracker;
    private GetCommand getCommand;


    @Before
    public void setupClasses()
    {
        configPointTracker = mock( ConfigPointTracker.class );
        getCommand = new GetCommand();
        getCommand.setConfigPointTracker( configPointTracker );
    }


    @Test( expected = NullPointerException.class )
    public void shouldThrowNullPointerExceptionOnSetConfigPointTracker()
    {
        getCommand.setConfigPointTracker( null );
    }


    @Test
    public void shouldAccessConfigPointTrackerOnDoExecute()
    {
        final String templateName = "";
        when( configPointTracker.get( templateName ) ).thenReturn( Sets.newHashSet( templateName ) );
        getCommand.doExecute();
        verify( configPointTracker ).get( templateName );
    }
}
