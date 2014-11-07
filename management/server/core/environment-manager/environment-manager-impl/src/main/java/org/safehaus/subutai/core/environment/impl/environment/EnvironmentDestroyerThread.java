package org.safehaus.subutai.core.environment.impl.environment;


import java.util.Observable;

import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.peer.api.PeerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by bahadyr on 11/7/14.
 */
public class EnvironmentDestroyerThread extends Observable implements Runnable
{
    private static final Logger LOG = LoggerFactory.getLogger( EnvironmentDestroyerThread.class.getName() );
    private Environment environment;


    public EnvironmentDestroyerThread( Environment environment )
    {
        this.environment = environment;
    }


    @Override
    public void run()
    {
        for ( ContainerHost container : environment.getContainers() )
        {
            try
            {
                container.dispose();
                setChanged();
                notifyObservers( container );
            }
            catch ( PeerException e )
            {
                LOG.error( String.format( "Could not destroy container %s on %s: %s", container.getHostname(),
                        e.toString() ) );
            }
        }
    }
}
