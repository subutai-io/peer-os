package org.safehaus.subutai.core.container.impl;


import java.util.concurrent.Callable;

import org.safehaus.subutai.core.container.api.ContainerCreateException;
import org.safehaus.subutai.core.container.api.ContainerDestroyException;
import org.safehaus.subutai.core.container.api.ContainerManager;


/**
 * Handles parallel container creation/destruction
 */
public class ContainerActor implements Callable<ContainerInfo>
{
    private final ContainerInfo containerInfo;
    private final ContainerManager containerManager;


    public ContainerActor( final ContainerInfo containerInfo, final ContainerManager containerManager )
    {
        this.containerInfo = containerInfo;
        this.containerManager = containerManager;
    }


    @Override
    public ContainerInfo call()
    {
        if ( containerInfo.getContainerAction() == ContainerAction.CREATE )
        {

            try
            {
                containerManager.clone( containerInfo.getEnvId(), containerInfo.getPhysicalAgent().getHostname(),
                        containerInfo.getTemplateName(), containerInfo.getCloneName() );
                containerInfo.success();
            }
            catch ( ContainerCreateException ignore )
            {
                containerInfo.fail();
            }
        }
        else
        {
            try
            {
                containerManager
                        .destroy( containerInfo.getPhysicalAgent().getHostname(), containerInfo.getCloneName() );
                containerInfo.success();
            }
            catch ( ContainerDestroyException ignore )
            {
                containerInfo.fail();
            }
        }
        return containerInfo;
    }
}
