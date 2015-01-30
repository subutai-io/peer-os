package org.safehaus.subutai.core.peer.impl.container;


import java.util.concurrent.Callable;

import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.core.peer.api.ResourceHost;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


public class CreateContainerTask implements Callable<ContainerHost>
{
    private final ResourceHost resourceHost;
    private final String hostname;
    private final String templateName;
    private final int timeoutSec;


    public CreateContainerTask( final ResourceHost resourceHost, final String templateName, final String hostname,
                                final int timeoutSec )
    {
        Preconditions.checkNotNull( resourceHost );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostname ) );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( templateName ) );
        Preconditions.checkArgument( timeoutSec > 0 );

        this.resourceHost = resourceHost;
        this.hostname = hostname;
        this.templateName = templateName;
        this.timeoutSec = timeoutSec;
    }


    @Override
    public ContainerHost call() throws Exception
    {

        //create container
        return resourceHost.createContainer( templateName, hostname, timeoutSec );
    }
}
