package org.safehaus.subutai.core.environment.impl.environment;


import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.safehaus.subutai.common.protocol.CloneContainersMessage;
import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.protocol.NodeGroup;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.environment.api.helper.EnvironmentBuildProcess;
import org.safehaus.subutai.core.environment.impl.EnvironmentManagerImpl;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by bahadyr on 11/5/14.
 */
public class EnvironmentBuilderImpl implements EnvironmentBuilder, Observer
{

    private static final Logger LOG = LoggerFactory.getLogger( EnvironmentBuilderImpl.class.getName() );
    ExecutorService executorService;
    EnvironmentManagerImpl manager;
    Environment environment;


    public EnvironmentBuilderImpl( EnvironmentManagerImpl manager )
    {
        this.manager = manager;
    }


    @Override
    public Environment build( final EnvironmentBlueprint blueprint, final EnvironmentBuildProcess process )
            throws BuildException
    {
        this.environment = new Environment( blueprint.getName() );

        int messageSize = process.getMessageMap().size();
        int containersAmount = 0;
        this.executorService = Executors.newFixedThreadPool( messageSize );

        for ( String key : process.getMessageMap().keySet() )
        {
            CloneContainersMessage message = process.getMessageMap().get( key );
            ContainerCreatorThread creatorThread =
                    new ContainerCreatorThread( this, message, manager.getPeerManager() );
            containersAmount = containersAmount + message.getNumberOfNodes();
            executorService.execute( creatorThread );
        }

        executorService.shutdown();

        try
        {
            int timeout = containersAmount * 30;
            executorService.awaitTermination( timeout, TimeUnit.SECONDS );
        }
        catch ( InterruptedException e )
        {
            LOG.error( e.getMessage(), e );
            throw new BuildException( e.getMessage() );
        }

        return environment;
    }


    @Override
    public void addNodeGroup( final UUID environmentId, final NodeGroup nodeGroup ) throws BuildException
    {

    }


    @Override
    public void addContainerToNodeGroup( final CloneContainersMessage message )
    {

    }


    @Override
    public void removeContainer( final ContainerHost containerHost )
    {

    }


    @Override
    public void update( final Observable o, final Object arg )
    {
        if ( arg instanceof Set )
        {
            for ( ContainerHost containerHost : ( Set<ContainerHost> ) arg )
            {
                environment.addContainer( containerHost );
            }
        }
        else if ( arg instanceof Exception )
        {
            executorService.shutdownNow();
        }
    }
}
