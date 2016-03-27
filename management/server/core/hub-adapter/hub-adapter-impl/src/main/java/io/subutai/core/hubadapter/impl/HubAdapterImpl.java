package io.subutai.core.hubadapter.impl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.dao.DaoManager;
import io.subutai.core.hubadapter.api.HubAdapter;
import io.subutai.core.hubadapter.impl.dao.DaoHelper;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.security.api.SecurityManager;


public class HubAdapterImpl implements HubAdapter
{
    private final Logger log = LoggerFactory.getLogger( getClass() );

    private final DaoManager daoManager;

    private final SecurityManager securityManager;

    private final PeerManager peerManager;

    private ConfigManager configManager;

    private final DaoHelper daoHelper;


    public HubAdapterImpl( DaoManager daoManager, SecurityManager securityManager, PeerManager peerManager )
    {
        this.daoManager = daoManager;
        this.securityManager = securityManager;
        this.peerManager = peerManager;

        daoHelper = new DaoHelper( daoManager );

        try
        {
            configManager = new ConfigManager( securityManager, peerManager );
        }
        catch ( Exception e )
        {
            log.error( "Error to init: ", e );
        }
    }


    @Override
    public String sayHello()
    {
        String peerId = peerManager.getLocalPeer().getId();

        boolean registered = daoHelper.isPeerRegisteredToHub( peerId );

        if ( registered )
        {
            String ownerId = daoHelper.getPeerOwnerId( peerId );

            log.debug( "Registered to Hub. Owner id: {}", ownerId );
        }
        else
        {
            log.debug( "Not registered to Hub" );
        }

        return "Hello";
    }


//    @Override
//    public String sayHello()
//    {
//        try
//        {
//            WebClient client = configManager.getTrustedWebClientWithAuth( "/rest/v1.1/marketplace/products", "hub.subut.ai" );
//
//            Response r = client.get();
//
//            log.debug( "status: {}", r.getStatus() );
//        }
//        catch ( Exception e )
//        {
//            log.error( "Error to test: ", e );
//        }
//
//        return "done";
//    }
}