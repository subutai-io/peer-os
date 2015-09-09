package io.subutai.core.hintegration.api;


import io.subutai.hub.common.dto.HeartbeatResponseDTO;


public interface Integration
{
    void registerOwnerPubKey() throws HIntegrationException;

    void registerPeerPubKey() throws HIntegrationException;

    void sendTrustData() throws HIntegrationException;

    void register() throws HIntegrationException;

    HeartbeatResponseDTO sendHeartbeat() throws HIntegrationException;
}
