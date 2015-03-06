package org.safehaus.subutai.core.peer.impl.container;


import java.util.concurrent.Callable;

import org.safehaus.subutai.common.command.CommandUtil;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.core.peer.api.ResourceHost;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;


public class DestroyContainerTask implements Callable
{
    //    private static final String CONTAINER_DOES_NOT_EXIST = "Container \"%s\" does NOT exist";
    //    private static final String CONTAINER_DESTROYED = "Destruction of \"%s\" completed successfully";
    private static final int DESTROY_TIMEOUT = 180;

    private final ResourceHost resourceHost;
    private final String hostname;
    private CommandUtil commandUtil = new CommandUtil();


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
        //        final Semaphore semaphore = new Semaphore( 0 );

//        final StringBuilder out = new StringBuilder();

        RequestBuilder destroyCommand =
                new RequestBuilder( "subutai destroy" ).withCmdArgs( Lists.newArrayList( hostname ) )
                                                       .withTimeout( DESTROY_TIMEOUT );

        commandUtil.execute( destroyCommand, resourceHost );

        //        commandUtil.executeAsync( destroyCommand, resourceHost, new CommandUtil.StoppableCallback()
        //        {
        //            @Override
        //            public void onResponse( final Response response, final CommandResult commandResult )
        //            {
        //                out.append( commandResult.getStdOut() );
        //                if ( commandResult.getStdOut().contains( String.format( CONTAINER_DESTROYED, hostname ) )
        //                        || commandResult.getStdOut().contains( String.format( CONTAINER_DOES_NOT_EXIST,
        // hostname ) ) )
        //                {
        //                    semaphore.release();
        //                    stop();
        //                }
        //            }
        //        } );


        //        if ( !semaphore.tryAcquire( DESTROY_TIMEOUT + 3, TimeUnit.SECONDS ) )
        //        {
        //            throw new ContainerDestructionException(
        //                    String.format( "Unexpected command result while destroying container: %s", out ) );
        //        }

        return null;
    }
}
