package org.safehaus.subutai.core.channel.impl.token;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by nisakov on 3/3/15.
 */
public class ChannelTokenController implements Runnable
{
    private final static Logger LOG = LoggerFactory.getLogger( ChannelTokenController.class );

    @Override
    public void run()
    {
        while (true)
        {
            try
            {
                //ChannelTokenManagerImpl.setTokenValidity();

                LOG.info( "******** Channel Token Controller invoked *********" );


                Thread.sleep( 60 * 1000 );
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }
}

