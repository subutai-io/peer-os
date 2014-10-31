package org.safehaus.subutai.core.metric.impl;


import org.safehaus.subutai.core.messenger.api.Message;
import org.safehaus.subutai.core.messenger.api.MessageListener;


/**
 * Listens to alerts from remote peers
 */
public class RemoteAlertListener extends MessageListener
{
    private static final String ALERT_RECIPIENT = "alert";


    protected RemoteAlertListener()
    {
        super( ALERT_RECIPIENT );
    }


    @Override
    public void onMessage( final Message message )
    {

    }
}
