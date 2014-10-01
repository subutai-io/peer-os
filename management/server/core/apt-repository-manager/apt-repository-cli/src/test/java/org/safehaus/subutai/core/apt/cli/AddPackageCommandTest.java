package org.safehaus.subutai.core.apt.cli;


import org.junit.Test;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.apt.api.AptRepositoryManager;

import static org.mockito.Mockito.mock;


/**
 * Test for AddPackageCommand
 */
public class AddPackageCommandTest
{

    @Test( expected = NullPointerException.class )
    public void constructorShouldFailOnNullAptRepoManager()
    {
        new AddPackageCommand( null, mock( AgentManager.class ) );
    }


    @Test( expected = NullPointerException.class )
    public void constructorShouldFailOnNullAgentManager()
    {
        new AddPackageCommand(mock( AptRepositoryManager.class ), null );
    }
}
