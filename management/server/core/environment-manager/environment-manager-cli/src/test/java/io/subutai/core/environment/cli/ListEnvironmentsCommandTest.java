package io.subutai.core.environment.cli;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import io.subutai.common.environment.Environment;
import io.subutai.common.test.SystemOutRedirectTest;
import io.subutai.core.environment.api.EnvironmentManager;

import com.google.common.collect.Sets;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class ListEnvironmentsCommandTest extends SystemOutRedirectTest
{
    @Mock
    EnvironmentManager environmentManager;
    @Mock
    Environment environment;

    ListEnvironmentsCommand command;


    @Before
    public void setUp() throws Exception
    {

        command = new ListEnvironmentsCommand( environmentManager );

        when( environmentManager.getEnvironments() ).thenReturn( Sets.newHashSet( environment ) );

        when( environment.getId() ).thenReturn( TestUtil.ENV_ID );
    }


    @Test
    public void testDoExecute() throws Exception
    {
        command.doExecute();

        assertThat( getSysOut(), containsString( TestUtil.ENV_ID.toString() ) );
    }
}
