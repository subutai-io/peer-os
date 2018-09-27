package io.subutai.common.peer;


import java.util.Date;
import java.util.Map;

import org.bouncycastle.openpgp.PGPPublicKeyRing;

import io.subutai.common.command.CommandCallback;
import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.environment.Containers;
import io.subutai.common.environment.CreateEnvironmentContainersRequest;
import io.subutai.common.environment.CreateEnvironmentContainersResponse;
import io.subutai.common.environment.HostAddresses;
import io.subutai.common.environment.Nodes;
import io.subutai.common.environment.PeerTemplatesDownloadProgress;
import io.subutai.common.environment.PrepareTemplatesRequest;
import io.subutai.common.environment.PrepareTemplatesResponse;
import io.subutai.common.host.ContainerHostState;
import io.subutai.common.host.HostId;
import io.subutai.common.host.Quota;
import io.subutai.common.metric.HistoricalMetrics;
import io.subutai.common.metric.ResourceHostMetrics;
import io.subutai.common.network.NetworkResourceImpl;
import io.subutai.common.network.UsedNetworkResources;
import io.subutai.common.protocol.CustomProxyConfig;
import io.subutai.common.protocol.P2PConfig;
import io.subutai.common.protocol.P2PCredentials;
import io.subutai.common.protocol.P2pIps;
import io.subutai.common.security.PublicKeyContainer;
import io.subutai.common.security.SshEncryptionType;
import io.subutai.common.security.SshKey;
import io.subutai.common.security.SshKeys;
import io.subutai.common.security.relation.RelationLink;
import io.subutai.common.security.relation.RelationLinkDto;
import io.subutai.bazaar.share.quota.ContainerQuota;
import io.subutai.bazaar.share.resource.PeerResources;


/**
 * Peer interface
 */
public interface Peer extends RelationLink
{

    /**
     * Returns id of peer
     */
    String getId();

    /**
     * Returns name of peer
     */
    String getName();

    /**
     * Returns owner id of peer
     */
    String getOwnerId();

    /**
     * Returns metadata object of peer
     */
    PeerInfo getPeerInfo();

    /**
     * Checks if peer can accommodate the requested container group
     *
     * @param nodes requested nodes (containers)
     *
     * @return true - can accommodate, false - otherwise
     */
    boolean canAccommodate( Nodes nodes ) throws PeerException;


    /**
     * Creates environment container group on the peer
     *
     * @param request container creation request
     */
    CreateEnvironmentContainersResponse createEnvironmentContainers( CreateEnvironmentContainersRequest request )
            throws PeerException;


    /**
     * Start container on the peer
     */
    void startContainer( ContainerId containerId ) throws PeerException;

    /**
     * Stops container on the peer
     */
    void stopContainer( ContainerId containerId ) throws PeerException;

    /**
     * Destroys container on the peer
     */
    void destroyContainer( ContainerId containerId ) throws PeerException;


    /**
     * Returns true of the host is connected (AND running, in case it is a container host), false otherwise
     */
    boolean isConnected( HostId hostId );

    /**
     * Executes command on the container
     *
     * @param requestBuilder - command
     * @param host - target host
     */
    CommandResult execute( RequestBuilder requestBuilder, Host host ) throws CommandException;

    /**
     * Executes command on the container
     *
     * @param requestBuilder - command
     * @param host - target host
     * @param callback - callback to trigger on each response chunk to the command
     */
    CommandResult execute( RequestBuilder requestBuilder, Host host, CommandCallback callback ) throws CommandException;

    /**
     * Executes command on the container asynchronously
     *
     * @param requestBuilder - command
     * @param host - target host
     * @param callback - callback to trigger on each response chunk to the command
     */
    void executeAsync( final RequestBuilder requestBuilder, final Host host, final CommandCallback callback )
            throws CommandException;

    /**
     * Executes command on the container asynchronously
     *
     * @param requestBuilder - command
     * @param host - target host
     */
    void executeAsync( final RequestBuilder requestBuilder, final Host host ) throws CommandException;

    /**
     * Returns true if this a local peer, false otherwise
     */
    boolean isLocal();


    /**
     * Returns true of the peer is reachable online, false otherwise
     */
    boolean isOnline();

    /**
     * Sends message to the peer
     *
     * @param request - message
     * @param recipient - recipient
     * @param requestTimeout - message timeout
     * @param responseType -  type of response to return
     * @param responseTimeout - response timeout
     * @param headers - map of http headers to pass with message
     *
     * @return - response from the recipient
     */
    <T, V> V sendRequest( T request, String recipient, int requestTimeout, Class<V> responseType, int responseTimeout,
                          Map<String, String> headers ) throws PeerException;

    /**
     * Sends message to the peer
     *
     * @param request - message
     * @param recipient - recipient
     * @param requestTimeout - message timeout
     * @param headers - map of http headers to pass with message
     */
    <T> void sendRequest( T request, String recipient, int requestTimeout, Map<String, String> headers )
            throws PeerException;

    /**
     * Returns state of container
     */
    ContainerHostState getContainerState( ContainerId containerId ) throws PeerException;

    Quota getRawQuota( ContainerId containerId ) throws PeerException;

    /**
     * Returns set of container information of the environment
     */
    Containers getEnvironmentContainers( EnvironmentId environmentId ) throws PeerException;


    //networking

    UsedNetworkResources getUsedNetworkResources() throws PeerException;

    Integer reserveNetworkResource( NetworkResourceImpl networkResource ) throws PeerException;

    void updateEtcHostsWithNewContainerHostname( EnvironmentId environmentId, String oldHostname, String newHostname )
            throws PeerException;

    void updateAuthorizedKeysWithNewContainerHostname( EnvironmentId environmentId, String oldHostname,
                                                       String newHostname, SshEncryptionType sshEncryptionType )
            throws PeerException;


    /**
     * Sets up tunnels on the local peer to the specified remote peers
     */
    void setupTunnels( P2pIps p2pIps, EnvironmentId environmentId ) throws PeerException;


    /* **************************************************************
     *
     */
    PublicKeyContainer createPeerEnvironmentKeyPair( RelationLinkDto linkDto ) throws PeerException;

    void updatePeerEnvironmentPubKey( EnvironmentId environmentId, PGPPublicKeyRing publicKeyRing )
            throws PeerException;


    /**
     * Resets a secret key for a given P2P network on all RHs
     *
     * @param p2PCredentials - P2P network credentials
     */
    void resetSwarmSecretKey( P2PCredentials p2PCredentials ) throws PeerException;


    /**
     * Sets up p2p connections on specified RHs.
     */
    void joinP2PSwarm( P2PConfig config ) throws PeerException;

    void joinOrUpdateP2PSwarm( P2PConfig config ) throws PeerException;


    void cleanupEnvironment( final EnvironmentId environmentId ) throws PeerException;


    ResourceHostMetrics getResourceHostMetrics() throws PeerException;


    /**
     * Returns limits for requested peer
     *
     * @param peerId peer ID
     */
    PeerResources getResourceLimits( PeerId peerId ) throws PeerException;

    ContainerQuota getQuota( ContainerId containerId ) throws PeerException;

    void setQuota( ContainerId containerId, ContainerQuota quota ) throws PeerException;

    void alert( AlertEvent alert ) throws PeerException;

    String getHistoricalMetrics( HostId hostId, Date startTime, Date endTime ) throws PeerException;

    HistoricalMetrics getMetricsSeries( HostId hostId, Date startTime, Date endTime ) throws PeerException;

    void addPeerEnvironmentPubKey( String keyId, PGPPublicKeyRing pek ) throws PeerException;

    HostId getResourceHostIdByContainerId( ContainerId id ) throws PeerException;

    PrepareTemplatesResponse prepareTemplates( final PrepareTemplatesRequest request ) throws PeerException;

    SshKeys readOrCreateSshKeysForEnvironment( EnvironmentId environmentId, SshEncryptionType sshKeyType )
            throws PeerException;

    void configureSshInEnvironment( EnvironmentId environmentId, SshKeys sshKeys ) throws PeerException;

    void removeFromAuthorizedKeys( EnvironmentId environmentId, String sshPublicKey ) throws PeerException;

    void addToAuthorizedKeys( EnvironmentId environmentId, String sshPublicKey ) throws PeerException;

    void configureHostsInEnvironment( EnvironmentId environmentId, HostAddresses hostAddresses ) throws PeerException;

    void addCustomProxy( CustomProxyConfig proxyConfig ) throws PeerException;

    void removeCustomProxy( CustomProxyConfig proxyConfig ) throws PeerException;

    SshKeys getSshKeys( EnvironmentId environmentId, SshEncryptionType sshEncryptionType ) throws PeerException;

    SshKey createSshKey( EnvironmentId environmentId, ContainerId containerId, SshEncryptionType encType )
            throws PeerException;

    SshKeys getContainerAuthorizedKeys( ContainerId containerId ) throws PeerException;

    void setContainerHostname( ContainerId containerId, String hostname ) throws PeerException;

    RegistrationStatus getStatus();

    PeerTemplatesDownloadProgress getTemplateDownloadProgress( EnvironmentId environmentId ) throws PeerException;
}
