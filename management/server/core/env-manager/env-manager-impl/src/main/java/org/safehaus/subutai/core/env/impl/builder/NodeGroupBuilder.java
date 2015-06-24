package org.safehaus.subutai.core.env.impl.builder;


import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.safehaus.subutai.common.environment.CreateContainerGroupRequest;
import org.safehaus.subutai.common.environment.NodeGroup;
import org.safehaus.subutai.common.network.Gateway;
import org.safehaus.subutai.common.network.Vni;
import org.safehaus.subutai.common.peer.HostInfoModel;
import org.safehaus.subutai.common.peer.Peer;
import org.safehaus.subutai.common.peer.PeerException;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.common.util.ExceptionUtil;
import org.safehaus.subutai.core.env.impl.entity.EnvironmentContainerImpl;
import org.safehaus.subutai.core.env.impl.entity.EnvironmentImpl;
import org.safehaus.subutai.core.env.impl.exception.NodeGroupBuildException;
import org.safehaus.subutai.core.peer.api.LocalPeer;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;

import org.apache.commons.net.util.SubnetUtils;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;


/**
 * * A task that returns created node groups on a peer and may throw an exception.
 */
public class NodeGroupBuilder implements Callable<Set<NodeGroupBuildResult>>
{

    private final EnvironmentImpl environment;
    private final TemplateRegistry templateRegistry;
    private final PeerManager peerManager;
    private final Peer peer;
    private final Set<NodeGroup> nodeGroups;
    private final String defaultDomain;
    private final Set<Peer> allPeers;
    private final int ipAddressOffset;
    protected ExceptionUtil exceptionUtil = new ExceptionUtil();


    public NodeGroupBuilder( final EnvironmentImpl environment, final TemplateRegistry templateRegistry,
                             final PeerManager peerManager, final Peer peer, final Set<NodeGroup> nodeGroups,
                             final Set<Peer> allPeers, final String defaultDomain, final int ipAddressOffset )
    {
        Preconditions.checkNotNull( environment );
        Preconditions.checkNotNull( templateRegistry );
        Preconditions.checkNotNull( peerManager );
        Preconditions.checkNotNull( peer );
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( allPeers ) );
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( nodeGroups ) );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( defaultDomain ) );

        this.environment = environment;
        this.templateRegistry = templateRegistry;
        this.peerManager = peerManager;
        this.peer = peer;
        this.allPeers = allPeers;
        this.nodeGroups = nodeGroups;
        this.defaultDomain = defaultDomain;
        this.ipAddressOffset = ipAddressOffset;
    }


    /**
     * Before triggering container host creation process force to verify for all required templates existence
     *
     * @param sourcePeerId - initializer peer id
     * @param templateName - template name to fetch
     *
     * @return - list of templates with parent dependencies
     */
    public List<Template> fetchRequiredTemplates( UUID sourcePeerId, final String templateName )
            throws NodeGroupBuildException
    {
        List<Template> requiredTemplates = Lists.newArrayList();
        List<Template> templates = templateRegistry.getParentTemplates( templateName );

        Template installationTemplate = templateRegistry.getTemplate( templateName );
        if ( installationTemplate != null )
        {
            templates.add( installationTemplate );
        }
        else
        {
            throw new NodeGroupBuildException( String.format( "Template %s is not found in registry", templateName ),
                    null );
        }


        for ( Template t : templates )
        {
            requiredTemplates.add( t.getRemoteClone( sourcePeerId ) );
        }
        return requiredTemplates;
    }


    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     *
     * @throws NodeGroupBuildException if unable to compute a result
     */
    @Override
    public Set<NodeGroupBuildResult> call() throws NodeGroupBuildException
    {

        try
        {
            Set<NodeGroupBuildResult> results = Sets.newHashSet();
            LocalPeer localPeer = peerManager.getLocalPeer();

            //check if environment has reserved VNI
            Set<Vni> reservedVnis = getReservedVnis();

            validatePickedGateway( reservedVnis );

            Vni environmentVni = null;
            for ( Vni reservedVni : reservedVnis )
            {
                if ( reservedVni.getEnvironmentId().equals( environment.getId() ) )
                {
                    environmentVni = reservedVni;
                    break;
                }
            }

            if ( environmentVni == null )
            {

                //try to reserve VNI
                environmentVni = new Vni( environment.getVni(), environment.getId() );

                try
                {
                    peer.reserveVni( environmentVni );
                }
                catch ( PeerException e )
                {
                    throw new NodeGroupBuildException(
                            String.format( "Could not reserve VNI on peer %s", peer.getName() ), e );
                }
            }

            collectAndStartCreationProcess( localPeer, results );

            return results;
        }
        catch ( Exception e )
        {
            throw new NodeGroupBuildException( "Error building node group", exceptionUtil.getRootCause( e ) );
        }
    }


    private void collectAndStartCreationProcess( final LocalPeer localPeer, final Set<NodeGroupBuildResult> results )
    {
        int currentIpAddressOffset = 0;
        for ( NodeGroup nodeGroup : nodeGroups )
        {
            NodeGroupBuildException exception = null;
            Set<EnvironmentContainerImpl> containers = Sets.newHashSet();
            try
            {
                Set<String> peerIps = Sets.newHashSet();

                //add initiator peer mandatorily
                peerIps.add( localPeer.getManagementHost().getExternalIp() );

                combinePeers( localPeer, peerIps );

                Set<HostInfoModel> newHosts = peer.createContainerGroup(
                        new CreateContainerGroupRequest( peerIps, environment.getId(), localPeer.getId(),
                                localPeer.getOwnerId(), environment.getSubnetCidr(),
                                fetchRequiredTemplates( peer.getId(), nodeGroup.getTemplateName() ),
                                nodeGroup.getNumberOfContainers(),
                                nodeGroup.getContainerPlacementStrategy().getStrategyId(),
                                nodeGroup.getContainerPlacementStrategy().getCriteriaAsList(),
                                ipAddressOffset + currentIpAddressOffset ) );

                currentIpAddressOffset += nodeGroup.getNumberOfContainers();

                for ( HostInfoModel newHost : newHosts )
                {
                    containers.add( new EnvironmentContainerImpl( localPeer.getId(), peer, nodeGroup.getName(), newHost,
                            templateRegistry.getTemplate( nodeGroup.getTemplateName() ), nodeGroup.getSshGroupId(),
                            nodeGroup.getHostsGroupId(), defaultDomain ) );
                }

                if ( containers.size() < nodeGroup.getNumberOfContainers() )
                {
                    exception = new NodeGroupBuildException(
                            String.format( "Requested %d but created only %d containers",
                                    nodeGroup.getNumberOfContainers(), containers.size() ), null );
                }
            }
            catch ( Exception e )
            {
                exception = new NodeGroupBuildException(
                        String.format( "Error creating node group %s on peer %s", nodeGroup, peer.getName() ),
                        exceptionUtil.getRootCause( e ) );
            }
            results.add( new NodeGroupBuildResult( containers, exception ) );
        }
    }


    private void combinePeers( final LocalPeer localPeer, final Set<String> peerIps )
    {
        for ( Peer aPeer : allPeers )
        {
            if ( !aPeer.getId().equals( localPeer.getId() ) && !aPeer.getId().equals( peer.getId() ) )
            {
                peerIps.add( aPeer.getPeerInfo().getIp() );
            }
        }
    }


    private Set<Vni> getReservedVnis() throws NodeGroupBuildException
    {
        try
        {
            return peer.getReservedVnis();
        }
        catch ( PeerException e )
        {
            throw new NodeGroupBuildException(
                    String.format( "Error obtaining reserved vnis on peer %s", peer.getName() ), e );
        }
    }


    private void validatePickedGateway( final Set<Vni> reservedVnis ) throws NodeGroupBuildException
    {
        //check availability of subnet
        Set<Gateway> gateways;
        try
        {
            gateways = peer.getGateways();
        }
        catch ( PeerException e )
        {
            throw new NodeGroupBuildException( String.format( "Error obtaining gateways on peer %s", peer.getName() ),
                    e );
        }

        SubnetUtils subnetUtils = new SubnetUtils( environment.getSubnetCidr() );
        String environmentGatewayIp = subnetUtils.getInfo().getLowAddress();

        Gateway usedGateway = null;

        for ( Gateway gateway : gateways )
        {
            if ( gateway.getIp().equals( environmentGatewayIp ) )
            {
                usedGateway = gateway;
                break;
            }
        }

        if ( usedGateway != null )
        {
            validateEnvironmentSubnetUsage( reservedVnis, usedGateway, true );
        }
    }


    private void validateEnvironmentSubnetUsage( final Set<Vni> reservedVnis, final Gateway usedGateway,
                                                 boolean subnetIsUsed ) throws NodeGroupBuildException
    {
        //check if subnet is used for this environment
        for ( Vni reservedVni : reservedVnis )
        {
            if ( reservedVni.getEnvironmentId().equals( environment.getId() ) )
            {
                if ( reservedVni.getVlan() == usedGateway.getVlan() )
                {
                    //this subnet is used for this environment, all is ok
                    subnetIsUsed = false;
                }
                break;
            }
        }

        if ( subnetIsUsed )
        {
            throw new NodeGroupBuildException( String.format( "Subnet is already in use on peer %s", peer.getName() ),
                    null );
        }
    }
}
