package io.subutai.core.messenger.impl;


import java.util.Set;
import java.util.concurrent.Callable;


public class LocalPeerMessageSender implements Callable<Boolean>
{
    private MessengerImpl messenger;
    private MessengerDataService messengerDataService;
    private Set<Envelope> envelopes;


    public LocalPeerMessageSender( final MessengerImpl messenger, final MessengerDataService messengerDataService,
                                   final Set<Envelope> envelopes )
    {
        this.messenger = messenger;
        this.messengerDataService = messengerDataService;
        this.envelopes = envelopes;
    }


    @Override
    public Boolean call() throws Exception
    {
        for ( Envelope envelope : envelopes )
        {
            messenger.notifyListeners( envelope );
            messengerDataService.markAsSent( envelope );
        }
        return true;
    }
}
