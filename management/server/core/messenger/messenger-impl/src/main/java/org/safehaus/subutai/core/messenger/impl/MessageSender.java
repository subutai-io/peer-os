package org.safehaus.subutai.core.messenger.impl;


import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.safehaus.subutai.common.util.RestUtil;
import org.safehaus.subutai.core.peer.api.Peer;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;


/**
 * Background message sender
 */
public class MessageSender
{
    private static final Logger LOG = LoggerFactory.getLogger( MessageSender.class.getName() );
    private static final int SLEEP_BETWEEN_ITERATIONS_SEC = 1;

    private final ScheduledExecutorService mainLoopExecutor = Executors.newSingleThreadScheduledExecutor();
    private final PeerManager peerManager;
    private final MessengerDao messengerDao;
    private final MessengerImpl messenger;
    private ExecutorService restExecutor = Executors.newCachedThreadPool();
    protected RestUtil restUtil;


    public MessageSender( final PeerManager peerManager, final MessengerDao messengerDao,
                          final MessengerImpl messenger )
    {
        this.peerManager = peerManager;
        this.messengerDao = messengerDao;
        this.messenger = messenger;
        this.restUtil = new RestUtil();
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


    private void purgeExpiredMessages()
    {
        messengerDao.purgeExpiredMessages();
    }


    protected void deliverMessages()
    {
        //get next messages to send
        Set<Envelope> envelopes = messengerDao.getEnvelopes();

        Map<UUID, Set<Envelope>> peerEnvelopesMap = Maps.newHashMap();
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

        CompletionService<Boolean> completer = new ExecutorCompletionService<>( restExecutor );

        //try to send messages in parallel - one thread per peer
        for ( Map.Entry<UUID, Set<Envelope>> envelopsPerPeer : peerEnvelopesMap.entrySet() )
        {
            Peer targetPeer = peerManager.getPeer( envelopsPerPeer.getKey() );
            if ( peerManager.getLocalPeer().getId().compareTo( targetPeer.getId() ) == 0 )
            {
                completer.submit( new LocalPeerMessageSender( messenger, messengerDao, envelopsPerPeer.getValue() ) );
            }
            else
            {
                completer.submit(
                        new RemotePeerMessageSender( restUtil, messengerDao, targetPeer, envelopsPerPeer.getValue() ) );
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
        catch ( InterruptedException | ExecutionException ignore )
        {
            LOG.warn( ignore.getMessage() );
        }
    }
}
