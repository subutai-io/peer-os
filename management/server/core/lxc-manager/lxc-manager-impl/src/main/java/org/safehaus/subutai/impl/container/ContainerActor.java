package org.safehaus.subutai.impl.container;


import java.util.concurrent.Callable;

import org.safehaus.subutai.api.container.ContainerManager;
import org.safehaus.subutai.api.lxcmanager.LxcDestroyException;


/**
 * Created by dilshat on 7/23/14.
 */
public class ContainerActor implements Callable<ContainerInfo> {
    private final ContainerInfo containerInfo;
    private final ContainerManager containerManager;
    private final ContainerAction containerAction;


    public ContainerActor( final ContainerInfo containerInfo, final ContainerManager containerManager,
                           final ContainerAction containerAction ) {
        this.containerInfo = containerInfo;
        this.containerManager = containerManager;
        this.containerAction = containerAction;
    }


    @Override
    public ContainerInfo call() {
        if ( containerAction == ContainerAction.CREATE ) {

            //to be implemented
        }
        else {
            try {
                containerManager
                        .cloneDestroy( containerInfo.getPhysicalAgent().getHostname(), containerInfo.getLxcHostname() );
                containerInfo.setResult( true );
            }
            catch ( LxcDestroyException e ) {

            }
        }
        return containerInfo;
    }
}
