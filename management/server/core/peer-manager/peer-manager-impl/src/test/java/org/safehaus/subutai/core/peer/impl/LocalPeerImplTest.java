package org.safehaus.subutai.core.peer.impl;


import java.util.UUID;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.core.executor.api.CommandExecutor;
import org.safehaus.subutai.core.hostregistry.api.HostRegistry;
import org.safehaus.subutai.core.lxc.quota.api.QuotaManager;
import org.safehaus.subutai.core.messenger.api.Messenger;
import org.safehaus.subutai.core.peer.api.PeerException;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.peer.impl.dao.PeerDAO;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;
import org.safehaus.subutai.core.strategy.api.StrategyManager;


/**
 * Created by timur on 11/5/14.
 */
@Ignore
@RunWith( MockitoJUnitRunner.class )
public class LocalPeerImplTest
{
    @Mock
    PeerManager peerManager;
    @Mock
    DataSource dataSource;
    @Mock
    Messenger messenger;

    @Mock
    TemplateRegistry templateRegistry;

    @Mock
    PeerDAO peerDAO;

    @Mock
    CommandExecutor commandExecutor;

    @Mock
    QuotaManager quotaManager;

    @Mock
    StrategyManager strategyManager;

    @Mock
    HostRegistry hostRegistry;


    @Before
    public void setup()
    {
        peerManager = new PeerManagerImpl( dataSource, messenger );
        //        peerManager.init();
    }


    @Test( expected = PeerException.class )
    public void testBindHostShouldFailOnNotExistenceHost() throws PeerException
    {
        LocalPeerImpl localPeer =
                new LocalPeerImpl( peerManager, templateRegistry, peerDAO, quotaManager, strategyManager, null,
                        commandExecutor, hostRegistry );

        localPeer.bindHost( UUID.randomUUID().toString() );
    }
}
