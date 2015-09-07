package io.subutai.core.hintegration.api;


public interface Integration
{
    void registerOwnerPubKey() throws HIntegrationException;

    void registerPeerPubKey() throws HIntegrationException;

    void sendTrustData() throws HIntegrationException;

    void register() throws HIntegrationException;
}
