package io.subutai.common.peer;


import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.bouncycastle.openpgp.PGPPublicKeyRing;

import io.subutai.common.command.CommandCallback;
import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.environment.Containers;
import io.subutai.common.environment.CreateEnvironmentContainerGroupRequest;
import io.subutai.common.environment.CreateEnvironmentContainerResponseCollector;
import io.subutai.common.environment.HostAddresses;
import io.subutai.common.environment.PrepareTemplatesRequest;
import io.subutai.common.environment.PrepareTemplatesResponseCollector;
import io.subutai.common.environment.SshPublicKeys;
import io.subutai.common.host.ContainerHostState;
import io.subutai.common.host.HostId;
import io.subutai.common.host.HostInfo;
import io.subutai.common.host.HostInterfaces;
import io.subutai.common.metric.ProcessResourceUsage;
import io.subutai.common.metric.ResourceHostMetrics;
import io.subutai.common.network.Gateways;
import io.subutai.common.network.NetworkResource;
import io.subutai.common.network.UsedNetworkResources;
import io.subutai.common.network.Vni;
import io.subutai.common.network.Vnis;
import io.subutai.common.protocol.P2PConfig;
import io.subutai.common.protocol.P2PConnections;
import io.subutai.common.protocol.P2PCredentials;
import io.subutai.common.protocol.P2pIps;
import io.subutai.common.protocol.PingDistances;
import io.subutai.common.protocol.TemplateKurjun;
import io.subutai.common.quota.ContainerQuota;
import io.subutai.common.resource.HistoricalMetrics;
import io.subutai.common.resource.PeerResources;
import io.subutai.common.security.PublicKeyContainer;


/**
 * Peer interface
 */
public interface Peer
{

    /**
     * Returns id of peer
     */
    public String getId();

    /**
     * Returns name of peer
     */
    public String getName();

    /**
     * Returns owner id of peer
     */
    public String getOwnerId();

    /**
     * Returns metadata object of peer
     */
    public PeerInfo getPeerInfo() throws PeerException;

    /**
     * Creates environment container group on the peer
     *
     * @param request - container creation request
     */
    public CreateEnvironmentContainerResponseCollector createEnvironmentContainerGroup(
            final CreateEnvironmentContainerGroupRequest request ) throws PeerException;


    /**
     * Start container on the peer
     */
    public void startContainer( ContainerId containerId ) throws PeerException;

    /**
     * Stops container on the peer
     */
    public void stopContainer( ContainerId containerId ) throws PeerException;

    /**
     * Destroys container on the peer
     */
    public void destroyContainer( ContainerId containerId ) throws PeerException;

    /**
     * Sets default gateway for the container
     */
    public void setDefaultGateway( ContainerGateway containerGateway ) throws PeerException;


    /**
     * Returns true of the host is connected, false otherwise
     */
    public boolean isConnected( HostId hostId );

    /**
     * Executes command on the container
     *
     * @param requestBuilder - command
     * @param host - target host
     */
    public CommandResult execute( RequestBuilder requestBuilder, Host host ) throws CommandException;

    /**
     * Executes command on the container
     *
     * @param requestBuilder - command
     * @param host - target host
     * @param callback - callback to trigger on each response chunk to the command
     */
    public CommandResult execute( RequestBuilder requestBuilder, Host host, CommandCallback callback )
            throws CommandException;

    /**
     * Executes command on the container asynchronously
     *
     * @param requestBuilder - command
     * @param host - target host
     * @param callback - callback to trigger on each response chunk to the command
     */
    public void executeAsync( final RequestBuilder requestBuilder, final Host host, final CommandCallback callback )
            throws CommandException;

    /**
     * Executes command on the container asynchronously
     *
     * @param requestBuilder - command
     * @param host - target host
     */
    public void executeAsync( final RequestBuilder requestBuilder, final Host host ) throws CommandException;

    /**
     * Returns true if this a local peer, false otherwise
     */
    public boolean isLocal();


    /**
     * Returns template by name
     */
    public TemplateKurjun getTemplate( String templateName ) throws PeerException;

    /**
     * Returns true of the peer is reachable online, false otherwise
     */
    public boolean isOnline();

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
    public <T, V> V sendRequest( T request, String recipient, int requestTimeout, Class<V> responseType,
                                 int responseTimeout, Map<String, String> headers ) throws PeerException;

    /**
     * Sends message to the peer
     *
     * @param request - message
     * @param recipient - recipient
     * @param requestTimeout - message timeout
     * @param headers - map of http headers to pass with message
     */
    public <T> void sendRequest( T request, String recipient, int requestTimeout, Map<String, String> headers )
            throws PeerException;

    /**
     * Returns state of container
     */
    public ContainerHostState getContainerState( ContainerId containerId ) throws PeerException;

    /**
     * Returns set of container information of the environment
     */
    public Containers getEnvironmentContainers( EnvironmentId environmentId ) throws PeerException;

    //******** Quota functions ***********

    /**
     * Returns resource usage of process on container by its PID
     *
     * @param containerId - target container
     * @param pid - pid of process
     */
    public ProcessResourceUsage getProcessResourceUsage( final ContainerId containerId, int pid ) throws PeerException;

    /**
     * Returns allowed cpus/cores ids on container
     *
     * @param host - container
     *
     * @return - allowed cpu set
     */
    public Set<Integer> getCpuSet( ContainerHost host ) throws PeerException;

    /**
     * Sets allowed cpus/cores on container
     *
     * @param host - container
     * @param cpuSet - allowed cpu set
     */
    public void setCpuSet( ContainerHost host, Set<Integer> cpuSet ) throws PeerException;


    //networking

    UsedNetworkResources getUsedNetworkResources() throws PeerException;

    void reserveNetworkResource( NetworkResource networkResource ) throws PeerException;


    /**
     * Sets up tunnels on the local peer to the specified remote peers todo use EnvironmentId instead of string
     */
    public void setupTunnels( P2pIps p2pIps, String environmentId ) throws PeerException;


    /* ************************************************
     * Returns all existing gateways of the peer
     */
    public Gateways getGateways() throws PeerException;


    /* ************************************************
     * Reserves VNI on the peer
     */
    public Vni reserveVni( Vni vni ) throws PeerException;


    /* ************************************************
     * Returns all reserved vnis on the peer
     */
    @Deprecated
    public Vnis getReservedVnis() throws PeerException;

    /**
     * Gets containerHost by Id specified
     *
     * @return - containerHost
     *
     * todo use ContainerId instead of string
     */
    public HostInfo getContainerHostInfoById( String containerHostId ) throws PeerException;


    /* **************************************************************
     *
     */
    public PublicKeyContainer createPeerEnvironmentKeyPair( EnvironmentId environmentId ) throws PeerException;

    void updatePeerEnvironmentPubKey( EnvironmentId environmentId, PGPPublicKeyRing publicKeyRing )
            throws PeerException;


    /**
     * Gets network interfaces
     */

    HostInterfaces getInterfaces() throws PeerException;


    /**
     * Returns p2p IP of the specified p2p swarm on the specified RH
     *
     * @param resourceHostId - id of RH
     * @param swarmHash - hash of p2p swarm
     */
    String getP2PIP( String resourceHostId, String swarmHash ) throws PeerException;

    /**
     * Resets a secret key for a given P2P network on all RHs
     *
     * @param p2PCredentials - P2P network credentials
     */
    void resetP2PSecretKey( P2PCredentials p2PCredentials ) throws PeerException;


    /**
     * Sets up p2p connection on each RH.
     *
     * The p2p swarm must exists and have at least one participant already with explicit IP because this method will use
     * dynamic IP acquisition for RHs. If P2P connection already exists on RH, its secret key gets reset with new secret
     * key and ttl from  @param config. To setup initial p2p connection with explicit IP, use
     * Peer#setupInitialP2PConnection
     *
     * @return - P2P IP of RH with MH
     */
    P2PConnections setupP2PConnection( P2PConfig config ) throws PeerException;


    /**
     * Sets up initial P2P connection in swarm on MH. P2P IP must be present!
     *
     * This method throws PeerException if initial P2P connection with the specified hash is already setup.
     */
    public void setupInitialP2PConnection( final P2PConfig config ) throws PeerException;

    /**
     * Removes p2p connection by hash from all RHs
     *
     * @param p2pHash - hash of p2p swarm
     */
    void removeP2PConnection( String p2pHash ) throws PeerException;

    void cleanupEnvironment( final EnvironmentId environmentId ) throws PeerException;

    void removePeerEnvironmentKeyPair( EnvironmentId environmentId ) throws PeerException;

    ResourceHostMetrics getResourceHostMetrics() throws PeerException;

    //todo use PeerId instead of string
    PeerResources getResourceLimits( String peerId ) throws PeerException;

    ContainerQuota getAvailableQuota( ContainerId containerId ) throws PeerException;

    ContainerQuota getQuota( ContainerId containerId ) throws PeerException;

    void setQuota( ContainerId containerId, ContainerQuota quota ) throws PeerException;

    void alert( AlertEvent alert ) throws PeerException;

    HistoricalMetrics getHistoricalMetrics( String hostName, Date startTime, Date endTime ) throws PeerException;

    PingDistances getP2PSwarmDistances( String p2pHash, Integer maxAddress ) throws PeerException;

    void addPeerEnvironmentPubKey( String keyId, PGPPublicKeyRing pek ) throws PeerException;

    HostId getResourceHostIdByContainerId( ContainerId id ) throws PeerException;

    PrepareTemplatesResponseCollector prepareTemplates( final PrepareTemplatesRequest request ) throws PeerException;

    SshPublicKeys generateSshKeyForEnvironment( EnvironmentId environmentId ) throws PeerException;

    void configureSshInEnvironment( EnvironmentId environmentId, SshPublicKeys sshPublicKeys ) throws PeerException;

    void configureHostsInEnvironment( EnvironmentId environmentId, HostAddresses hostAddresses ) throws PeerException;
}
