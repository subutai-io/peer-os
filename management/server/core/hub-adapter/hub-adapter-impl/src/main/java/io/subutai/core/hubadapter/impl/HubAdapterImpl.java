package io.subutai.core.hubadapter.impl;


import io.subutai.common.dao.DaoManager;
import io.subutai.core.hubadapter.api.HubAdapter;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.security.api.SecurityManager;

import static java.lang.String.format;

public class HubAdapterImpl implements HubAdapter
{
    private static final String USER_ENVIRONMENTS = "/rest/v1/adapter/users/%s/environments";

    private final DaoHelper daoHelper;

    private final HttpClient httpClient;

    private final String peerId;


    public HubAdapterImpl( DaoManager daoManager, SecurityManager securityManager, PeerManager peerManager ) throws Exception
    {
        daoHelper = new DaoHelper( daoManager );

        httpClient = new HttpClient( securityManager );

        peerId = peerManager.getLocalPeer().getId();
    }


    @Override
    public String sayHello()
    {
        return isRegistered()
               ? httpClient.doGet( format( USER_ENVIRONMENTS, getOwnerId() ) )
               : null;
    }


    private boolean isRegistered()
    {
        return daoHelper.isPeerRegisteredToHub( peerId );
    }


    private String getOwnerId()
    {
        return daoHelper.getPeerOwnerId( peerId );
    }
}