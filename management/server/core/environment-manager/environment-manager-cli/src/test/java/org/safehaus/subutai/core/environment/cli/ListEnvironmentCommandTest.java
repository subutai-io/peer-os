package org.safehaus.subutai.core.environment.cli;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;


/**
 * Created by bahadyr on 9/25/14.
 */
@RunWith( MockitoJUnitRunner.class )
public class ListEnvironmentCommandTest
{
    private static final String NAME = "name";
    ListEnvironmentsCommand command;
    @Mock
    EnvironmentManager manager;


    @Before
    public void setUp() throws Exception
    {
        command = new ListEnvironmentsCommand();
        command.setEnvironmentManager( manager );
    }


    @Test
    public void test() throws Exception
    {
        //        List<Environment> l = new ArrayList<>();
        //        l.add( new Environment( NAME ) );
        //        when( manager.getEnvironments() ).thenReturn( l );
        //        command.doExecute();
    }
}
