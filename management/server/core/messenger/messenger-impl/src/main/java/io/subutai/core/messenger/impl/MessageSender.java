package io.subutai.core.messenger.impl;


import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

import io.subutai.common.peer.Peer;


/**
 * Background message sender
 */
public class MessageSender
{
    public static final int SLEEP_BETWEEN_ITERATIONS_SEC = 1;
    private final MessengerDao messengerDao;
    private final MessengerImpl messenger;

    protected static Logger LOG = LoggerFactory.getLogger( MessageSender.class.getName() );
    protected ScheduledExecutorService mainLoopExecutor = Executors.newSingleThreadScheduledExecutor();
    protected ExecutorService restExecutor = Executors.newCachedThreadPool();
    protected CompletionService<Boolean> completer = new ExecutorCompletionService<>( restExecutor );


    public MessageSender( final MessengerDao messengerDao, final MessengerImpl messenger )
    {
        this.messengerDao = messengerDao;
        this.messenger = messenger;
    }


    public void init()
    {
        //main background thread
        mainLoopExecutor.scheduleWithFixedDelay( new Runnable()
        {
            @Override
            public void run()
            {

                try
                {
                    deliverMessages();
                    purgeExpiredMessages();
                }
                catch ( Exception e )
                {
                    LOG.error( "Error in MessageSender", e );
                }
            }
        }, 0, SLEEP_BETWEEN_ITERATIONS_SEC, TimeUnit.SECONDS );
    }


    public void dispose()
    {
        restExecutor.shutdown();
        mainLoopExecutor.shutdown();
    }


    protected void purgeExpiredMessages()
    {
        messengerDao.purgeExpiredMessages();
    }


    protected void deliverMessages()
    {
        //get next messages to send
        Set<Envelope> envelopes = messengerDao.getEnvelopes();

        Map<String, Set<Envelope>> peerEnvelopesMap = Maps.newHashMap();
        int maxTimeToLive = 0;
        //distribute envelops to peers
        for ( Envelope envelope : envelopes )
        {
            if ( envelope.getTimeToLive() > maxTimeToLive )
            {
                maxTimeToLive = envelope.getTimeToLive();
            }
            Set<Envelope> peerEnvelopes = peerEnvelopesMap.get( envelope.getTargetPeerId() );
            if ( peerEnvelopes == null )
            {
                //sort by createDate asc once more
                peerEnvelopes = new TreeSet<>( new Comparator<Envelope>()
                {
                    @Override
                    public int compare( final Envelope o1, final Envelope o2 )
                    {
                        return o1.getCreateDate().compareTo( o2.getCreateDate() );
                    }
                } );
                peerEnvelopesMap.put( envelope.getTargetPeerId(), peerEnvelopes );
            }

            peerEnvelopes.add( envelope );
        }


        //try to send messages in parallel - one thread per peer
        for ( Map.Entry<String, Set<Envelope>> envelopsPerPeer : peerEnvelopesMap.entrySet() )
        {
            Peer targetPeer = messenger.getPeerManager().getPeer( envelopsPerPeer.getKey() );
            if ( targetPeer.isLocal() )
            {
                completer.submit( new LocalPeerMessageSender( messenger, messengerDao, envelopsPerPeer.getValue() ) );
            }
            else
            {
                completer.submit( new RemotePeerMessageSender( messengerDao, targetPeer, envelopsPerPeer.getValue() ) );
            }
        }

        //wait for completion
        try
        {
            for ( int i = 0; i < peerEnvelopesMap.size(); i++ )
            {
                Future<Boolean> future = completer.take();
                future.get();
            }
        }
        catch ( InterruptedException | ExecutionException e )
        {
            LOG.warn( "ignore", e );
        }
    }
}
