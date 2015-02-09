package org.safehaus.subutai.core.peer.impl.container;


import java.util.concurrent.Callable;

import org.safehaus.subutai.common.command.CommandUtil;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.core.peer.api.ContainerCreationException;
import org.safehaus.subutai.core.peer.api.ResourceHost;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;


public class CreateContainerTask implements Callable<ContainerHost>
{
    private final ResourceHost resourceHost;
    private final String hostname;
    private final String templateName;
    private final int timeoutSec;
    private CommandUtil commandUtil = new CommandUtil();


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
        commandUtil.execute(
                new RequestBuilder( "subutai clone" ).withCmdArgs( Lists.newArrayList( templateName, hostname ) )
                                                     .withTimeout( 1 ).daemon(), resourceHost );

        long start = System.currentTimeMillis();

        ContainerHost containerHost = null;
        while ( System.currentTimeMillis() - start < timeoutSec * 1000 && containerHost == null )
        {
            Thread.sleep( 100 );
            containerHost = resourceHost.getContainerHostByName( hostname );
        }

        if ( containerHost == null )
        {
            throw new ContainerCreationException(
                    String.format( "Container %s did not connect within timeout", hostname ) );
        }

        return containerHost;
    }
}
