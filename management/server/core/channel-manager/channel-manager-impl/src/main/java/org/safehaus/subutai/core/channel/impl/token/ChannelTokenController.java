package org.safehaus.subutai.core.channel.impl.token;


/**
 * Created by nisakov on 3/3/15.
 */
public class ChannelTokenController implements Runnable
{
    @Override
    public void run()
    {
        while (true)
        {
            try
            {
                ChannelTokenManagerImpl.setTokenValidity();

                Thread.sleep( 2 * 1000 );
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }
}

