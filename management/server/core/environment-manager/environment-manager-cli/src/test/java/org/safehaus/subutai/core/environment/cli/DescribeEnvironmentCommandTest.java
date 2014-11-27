package org.safehaus.subutai.core.environment.cli;


import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.peer.api.LocalPeer;

import static org.mockito.Mockito.when;


/**
 * Created by bahadyr on 9/25/14.
 */
@RunWith( MockitoJUnitRunner.class )
public class DescribeEnvironmentCommandTest
{
    DescribeEnvironmentCommand describeEnvironmentCommand;
    @Mock
    EnvironmentManager manager;

    @Mock
    LocalPeer localPeer;

    @Before
    public void setUp() throws Exception
    {
        describeEnvironmentCommand = new DescribeEnvironmentCommand();
        describeEnvironmentCommand.setEnvironmentManager( manager );
    }


//    @Test
//    public void test() throws Exception
//    {
//        String name = "name";
//        Environment environment = new Environment( name, localPeer );
//        final Set<ContainerHost> set = new HashSet<>();
//        environment.setContainers( set );
//        describeEnvironmentCommand.setEnvironmentName( name );
//        when( manager.getEnvironment( name ) ).thenReturn( environment );
//        describeEnvironmentCommand.doExecute();
//    }
}
