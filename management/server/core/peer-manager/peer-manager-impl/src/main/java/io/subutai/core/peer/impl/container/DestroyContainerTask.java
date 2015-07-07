package io.subutai.core.peer.impl.container;


import java.util.concurrent.Callable;

import io.subutai.common.command.CommandUtil;
import io.subutai.common.command.RequestBuilder;
import io.subutai.core.peer.api.ResourceHost;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;


public class DestroyContainerTask implements Callable
{
    private static final int DESTROY_TIMEOUT = 180;

    private final ResourceHost resourceHost;
    private final String hostname;
    protected CommandUtil commandUtil = new CommandUtil();


    public DestroyContainerTask( final ResourceHost resourceHost, final String hostname )
    {
        Preconditions.checkNotNull( resourceHost );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostname ) );

        this.resourceHost = resourceHost;
        this.hostname = hostname;
    }


    @Override
    public Object call() throws Exception
    {

        RequestBuilder destroyCommand =
                new RequestBuilder( "subutai destroy" ).withCmdArgs( Lists.newArrayList( hostname ) )
                                                       .withTimeout( DESTROY_TIMEOUT );

        commandUtil.execute( destroyCommand, resourceHost );

        return null;
    }
}
