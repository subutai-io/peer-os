package io.subutai.core.environment.impl;


import java.util.UUID;

import io.subutai.common.environment.EnvironmentNotFoundException;
import io.subutai.common.environment.Topology;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.environment.impl.workflow.EnvironmentCreationWorkflow;


public class EnvironmentManagerImpl implements EnvironmentManager
{
    @Override
    public String createEnvironment( final String name, final Topology topology, final String subnetCidr,
                                     final String sshKey )
    {
        //generate environment id or obtain it as input parameter
        String environmentId = UUID.randomUUID().toString();

        //launch environment creation workflow
        EnvironmentCreationWorkflow environmentCreationWorkflow = new EnvironmentCreationWorkflow( subnetCidr );

        environmentCreationWorkflow.start();

        //this call makes the method synchronous, otherwise we can just return the environment id
        environmentCreationWorkflow.join();


        return environmentId;
    }


    @Override
    public void growEnvironment( final String environmentId, final Topology topology )
            throws EnvironmentNotFoundException
    {
        //todo
    }
}
