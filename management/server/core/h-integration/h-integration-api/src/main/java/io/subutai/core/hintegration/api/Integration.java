package io.subutai.core.hintegration.api;


import java.util.Set;


public interface Integration
{
    void registerOwnerPubKey() throws HIntegrationException;

    void registerPeerPubKey() throws HIntegrationException;

    void sendTrustData() throws HIntegrationException;

    void register() throws HIntegrationException;

    Set<String> sendHeartbeat() throws HIntegrationException;

    void processStateLink( String link ) throws HIntegrationException;
}
