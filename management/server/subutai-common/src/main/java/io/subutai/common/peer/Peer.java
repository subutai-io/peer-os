package io.subutai.common.peer;


import java.util.Map;
import java.util.Set;

import org.bouncycastle.openpgp.PGPPublicKeyRing;

import io.subutai.common.command.CommandCallback;
import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.environment.CreateEnvironmentContainerGroupRequest;
import io.subutai.common.host.ContainerHostInfoModel;
import io.subutai.common.host.ContainerHostState;
import io.subutai.common.host.HostId;
import io.subutai.common.host.HostInfo;
import io.subutai.common.host.HostInterfaces;
import io.subutai.common.metric.ProcessResourceUsage;
import io.subutai.common.metric.ResourceHostMetrics;
import io.subutai.common.network.Gateway;
import io.subutai.common.network.Vni;
import io.subutai.common.protocol.N2NConfig;
import io.subutai.common.protocol.Template;
import io.subutai.common.resource.ResourceType;
import io.subutai.common.resource.ResourceValue;
import io.subutai.common.security.PublicKeyContainer;
import io.subutai.common.protocol.TemplateKurjun;


/**
 * Peer interface
 */
public interface Peer extends PeerSpecific, EnvironmentSpecific
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
    public PeerInfo getPeerInfo();

    /**
     * Creates environment container group on the peer
     *
     * @param request - container creation request
     *
     * @return - set of metadaobjects of created containers
     */
    public Set<ContainerHostInfoModel> createEnvironmentContainerGroup(
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
     * Cleans up environment networking settings. This method is called when an environment is being destroyed to clean
     * up its settings on the local peer.
     */
    void cleanupEnvironmentNetworkSettings( final EnvironmentId environmentId ) throws PeerException;

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
    public boolean isOnline() throws PeerException;

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
    public ContainerHostState getContainerState( ContainerId containerId );

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


    /**
     * Destroys container group
     *
     * @param environmentId - id fo environment
     *
     * @return {@code ContainersDestructionResult}
     */
    public ContainersDestructionResult destroyContainersByEnvironment( final String environmentId )
            throws PeerException;

    //networking


    /**
     * Sets up tunnels on the local peer to the specified remote peers
     */
    public int setupTunnels( Map<String, String> peerIps, String environmentId ) throws PeerException;

    /* ************************************************
     * Returns all existing gateways of the peer
     */
    public Set<Gateway> getGateways() throws PeerException;


    /* ************************************************
     * Reserves VNI on the peer
     */
    public Vni reserveVni( Vni vni ) throws PeerException;


    /* ************************************************
     * Returns all reserved vnis on the peer
     */
    public Set<Vni> getReservedVnis() throws PeerException;

    /**
     * Gets containerHost by Id specified
     *
     * @return - containerHost
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

    void setupN2NConnection( N2NConfig config ) throws PeerException;

    void removeN2NConnection( EnvironmentId environmentId ) throws PeerException;

    void createGateway( Gateway gateway ) throws PeerException;

    void removePeerEnvironmentKeyPair( EnvironmentId environmentId ) throws PeerException;

    ResourceHostMetrics getResourceHostMetrics() throws PeerException;

    ResourceValue getAvailableQuota( ContainerHost containerHost, ResourceType resourceType ) throws PeerException;

    ResourceValue getQuota( ContainerHost containerHost, ResourceType resourceType ) throws PeerException;

    void setQuota( ContainerHost containerHost, ResourceType resourceType, ResourceValue resourceValue )
            throws PeerException;


    ResourceValue getAvailableQuota( ContainerId containerId, ResourceType resourceType ) throws PeerException;

    ResourceValue getQuota( ContainerId containerId, ResourceType resourceType ) throws PeerException;

    void setQuota( ContainerId containerId, ResourceType resourceType, ResourceValue resourceValue )
            throws PeerException;

    void putAlert( AlertPack alert ) throws PeerException;
}
