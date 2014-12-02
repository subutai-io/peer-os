package org.safehaus.subutai.core.environment.impl.environment;


import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.peer.api.PeerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by bahadyr on 11/7/14.
 */
public class EnvironmentDestroyerThread implements Runnable
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
        for ( ContainerHost container : environment.getContainerHosts() )
        {
            try
            {
                container.dispose();
                LOG.info( String.format( "Container %s destroyed.", container.getId() ) );
            }
            catch ( PeerException e )
            {
                LOG.error( e.getMessage(), e );
            }
        }
    }
}
