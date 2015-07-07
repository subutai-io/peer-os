package io.subutai.core.messenger.impl;


import java.util.Set;
import java.util.concurrent.Callable;


public class LocalPeerMessageSender implements Callable<Boolean>
{
    private MessengerImpl messenger;
    private MessengerDao messengerDao;
    private Set<Envelope> envelopes;


    public LocalPeerMessageSender( final MessengerImpl messenger, final MessengerDao messengerDao,
                                   final Set<Envelope> envelopes )
    {
        this.messenger = messenger;
        this.messengerDao = messengerDao;
        this.envelopes = envelopes;
    }


    @Override
    public Boolean call() throws Exception
    {
        for ( Envelope envelope : envelopes )
        {
            messenger.notifyListeners( envelope );
            messengerDao.markAsSent( envelope );
        }
        return true;
    }
}
