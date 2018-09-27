package io.subutai.core.bazaarmanager.api;


import java.util.Map;

import io.subutai.core.bazaarmanager.api.exception.BazaarManagerException;
import io.subutai.core.bazaarmanager.api.model.Config;
import io.subutai.core.identity.api.model.UserToken;


public interface BazaarManager
{
    String BAZAAR_EMAIL_SUFFIX = "@bazaar.subutai.io";

    void registerPeer( String email, String password, String peerName, String peerScope ) throws BazaarManagerException;

    void unregisterPeer() throws BazaarManagerException;

    String getPeerName();

    void sendHeartbeat() throws BazaarManagerException;

    void triggerHeartbeat();

    String getBazaarIp() throws BazaarManagerException;

    Map<String, String> getPeerInfo() throws BazaarManagerException;

    Config getBazaarConfiguration();

    String getCurrentUserEmail();

    boolean isRegisteredWithBazaar();

    boolean isBazaarReachable();

    boolean canWorkWithBazaar();

    boolean isPeerUpdating();

    RestClient getRestClient();

    boolean hasBazaarTasksInAction();

    void notifyBazaarThatPeerIsOffline();

    void sendPeersMertics() throws BazaarManagerException;

    void sendContainerMertics() throws BazaarManagerException;

    void schedulePeerMetrics();

    UserToken getUserToken( String envOwnerId, String peerId );
}
