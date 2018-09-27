package io.subutai.core.bazaarmanager.impl.environment.state.create;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bouncycastle.openpgp.PGPException;

import org.apache.commons.lang3.EnumUtils;

import com.google.common.base.Preconditions;

import io.subutai.common.command.CommandResult;
import io.subutai.common.command.CommandUtil;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.environment.CreateEnvironmentContainersRequest;
import io.subutai.common.environment.CreateEnvironmentContainersResponse;
import io.subutai.common.environment.Environment;
import io.subutai.common.environment.Node;
import io.subutai.common.environment.PrepareTemplatesRequest;
import io.subutai.common.host.ContainerHostInfo;
import io.subutai.common.host.HostArchitecture;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.Host;
import io.subutai.common.peer.PeerException;
import io.subutai.common.security.objects.PermissionObject;
import io.subutai.common.security.relation.RelationLinkDto;
import io.subutai.common.settings.Common;
import io.subutai.common.task.CloneRequest;
import io.subutai.core.bazaarmanager.api.RestResult;
import io.subutai.core.bazaarmanager.api.exception.BazaarManagerException;
import io.subutai.core.bazaarmanager.impl.environment.state.Context;
import io.subutai.core.bazaarmanager.impl.environment.state.StateHandler;
import io.subutai.bazaar.share.dto.environment.ContainerStateDto;
import io.subutai.bazaar.share.dto.environment.EnvironmentNodeDto;
import io.subutai.bazaar.share.dto.environment.EnvironmentNodesDto;
import io.subutai.bazaar.share.dto.environment.EnvironmentPeerDto;
import io.subutai.bazaar.share.quota.ContainerQuota;

import static io.subutai.bazaar.share.dto.environment.ContainerStateDto.BUILDING;


public class BuildContainerStateHandler extends StateHandler
{
    private static final String PATH = "/rest/v1/environments/%s/containers";

    private final CommandUtil commandUtil = new CommandUtil();


    public BuildContainerStateHandler( Context ctx )
    {
        super( ctx, "Building containers" );
    }


    @Override
    protected Object doHandle( EnvironmentPeerDto peerDto ) throws BazaarManagerException
    {
        try
        {
            logStart();

            EnvironmentNodesDto nodesDto = ctx.restClient.getStrict( path( PATH, peerDto ), EnvironmentNodesDto.class );

            prepareTemplates( peerDto, nodesDto );

            setupPeerEnvironmentKey( peerDto );

            Object result = cloneContainers( peerDto, nodesDto );

            logEnd();

            return result;
        }
        catch ( BazaarManagerException e )
        {
            throw e;
        }
        catch ( Exception e )
        {
            throw new BazaarManagerException( e );
        }
    }


    /**
     * TODO. Identify for future do we need envKeyId (or do we need keyId for {@link RelationLinkDto})
     */
    private void setupPeerEnvironmentKey( EnvironmentPeerDto peerDto ) throws PeerException, PGPException
    {
        RelationLinkDto envLink =
                new RelationLinkDto( peerDto.getEnvironmentInfo().getId(), Environment.class.getSimpleName(),
                        PermissionObject.ENVIRONMENT_MANAGEMENT.getName(), peerDto.getEnvironmentInfo().getId() );

        ctx.localPeer.createPeerEnvironmentKeyPair( envLink );
    }


    @Override
    protected RestResult<Object> post( EnvironmentPeerDto peerDto, Object body )
    {
        return ctx.restClient.post( path( PATH, peerDto ), body );
    }


    private void prepareTemplates( final EnvironmentPeerDto peerDto, EnvironmentNodesDto nodesDto )
            throws BazaarManagerException
    {
        Set<Node> nodes = new HashSet<>();

        log.info( "Prepare templates:" );

        for ( EnvironmentNodeDto nodeDto : nodesDto.getNodes() )
        {
            ContainerQuota quota = nodeDto.getContainerQuota();

            log.info( "- noteDto: containerId={}, containerName={}, hostname={}, state={}", nodeDto.getContainerId(),
                    nodeDto.getContainerName(), nodeDto.getHostName(), nodeDto.getState() );

            Node node = new Node( nodeDto.getHostName(), nodeDto.getContainerName(), quota, peerDto.getPeerId(),
                    nodeDto.getHostId(), nodeDto.getTemplateId() );

            nodes.add( node );
        }

        // <hostId, templates>
        Map<String, Set<String>> rhTemplates = new HashMap<>();

        for ( Node node : nodes )
        {
            Set<String> templates = rhTemplates.get( node.getHostId() );

            if ( templates == null )
            {
                templates = new HashSet<>();
                rhTemplates.put( node.getHostId(), templates );
            }

            templates.add( node.getTemplateId() );
        }

        try
        {
            ctx.localPeer.prepareTemplates(
                    new PrepareTemplatesRequest( peerDto.getEnvironmentInfo().getId(), peerDto.getKurjunToken(),
                            rhTemplates ) );
        }
        catch ( PeerException e )
        {
            throw new BazaarManagerException( e );
        }
    }


    private EnvironmentNodesDto cloneContainers( EnvironmentPeerDto peerDto, EnvironmentNodesDto envNodes )
            throws BazaarManagerException
    {
        CreateEnvironmentContainersRequest cloneRequests = createCloneRequests( peerDto, envNodes );

        // Clone requests may be empty if all containers already exists. For example, in case of duplicated requests.
        CreateEnvironmentContainersResponse cloneResponses;

        Set<CloneRequest> cloneRequestsSet = cloneRequests.getRequests();

        //check if container with such hostname already exists within not registered ones
        //destroy if so and then continue
        Set<ContainerHostInfo> containerHostInfos = ctx.localPeer.getNotRegisteredContainers();
        for ( CloneRequest cloneRequest : cloneRequestsSet )
        {
            for ( ContainerHostInfo containerHostInfo : containerHostInfos )
            {
                if ( cloneRequest.getHostname().equalsIgnoreCase( containerHostInfo.getHostname() ) )
                {
                    try
                    {
                        ctx.localPeer.destroyNotRegisteredContainer( containerHostInfo.getId() );
                    }
                    catch ( PeerException e )
                    {
                        log.warn( "Error destroying not registered duplicate container: {}", e.getMessage() );
                    }
                }
            }
        }

        try
        {
            cloneResponses =
                    cloneRequestsSet.isEmpty() ? null : ctx.localPeer.createEnvironmentContainers( cloneRequests );
        }
        catch ( PeerException e )
        {
            throw new BazaarManagerException( e );
        }

        if ( cloneResponses != null && !cloneResponses.hasSucceeded() )
        {
            throw new BazaarManagerException( cloneResponses.getMessages().toString() );
        }

        populateResultNodes( peerDto, envNodes );

        return envNodes;
    }


    private void populateResultNodes( EnvironmentPeerDto peerDto, EnvironmentNodesDto envNodes )
            throws BazaarManagerException
    {
        String envId = peerDto.getEnvironmentInfo().getId();

        Set<ContainerHost> envContainers = ctx.localPeer.findContainersByEnvironmentId( envId );

        for ( EnvironmentNodeDto nodeDto : envNodes.getNodes() )
        {
            // Update for just cloned containers only. Containers with RUNNING state were created in previous builds.
            if ( nodeDto.getState() == BUILDING )
            {
                updateNodeDto( nodeDto, envContainers );
            }
        }
    }


    private void updateNodeDto( EnvironmentNodeDto nodeDto, Set<ContainerHost> envContainers )
            throws BazaarManagerException
    {
        ContainerHost ch = findContainerByHostname( envContainers, nodeDto.getHostName() );

        Preconditions.checkNotNull( ch );

        String contId = ch.getContainerId().getId();

        nodeDto.addSshKey( createSshKey( contId ) );

        nodeDto.setContainerId( contId );

        nodeDto.setContainerName( ch.getContainerName() );

        nodeDto.setState( EnumUtils.getEnum( ContainerStateDto.class, ch.getState().toString() ) );
    }


    private CreateEnvironmentContainersRequest createCloneRequests( EnvironmentPeerDto peerDto,
                                                                    EnvironmentNodesDto envNodes )
            throws BazaarManagerException
    {
        String envId = peerDto.getEnvironmentInfo().getId();

        Set<ContainerHost> envContainers = ctx.localPeer.findContainersByEnvironmentId( envId );

        CreateEnvironmentContainersRequest createRequests =
                new CreateEnvironmentContainersRequest( envId, Common.BAZAAR_ID,
                        peerDto.getEnvironmentInfo().getOwnerId() );

        log.info( "Clone requests:" );

        for ( EnvironmentNodeDto nodeDto : envNodes.getNodes() )
        {
            log.info( "- noteDto: containerId={}, containerName={}, hostname={}, state={}", nodeDto.getContainerId(),
                    nodeDto.getContainerName(), nodeDto.getContainerName(), nodeDto.getState() );

            // Exclude existing containers. This may happen as a result of duplicated requests or adding a new
            // container to existing peer in env.
            if ( !containerExists( nodeDto.getHostName(), envContainers ) && nodeDto.getState() == BUILDING )
            {
                createRequests.addRequest( createCloneRequest( nodeDto ) );
            }
        }

        return createRequests;
    }


    private CloneRequest createCloneRequest( EnvironmentNodeDto nodeDto ) throws BazaarManagerException
    {
        return new CloneRequest( nodeDto.getHostId(), nodeDto.getHostName(), nodeDto.getContainerName(),
                nodeDto.getIp(), nodeDto.getTemplateId(), HostArchitecture.AMD64, nodeDto.getContainerQuota() );
    }


    private boolean containerExists( String hostname, Set<ContainerHost> envContainers )
    {
        ContainerHost ch = findContainerByHostname( envContainers, hostname );

        if ( ch != null )
        {
            log.info( "Container already exists: id={}, hostname={}", ch.getContainerId(), ch.getHostname() );

            return true;
        }

        return false;
    }


    private ContainerHost findContainerByHostname( Set<ContainerHost> envContainers, String hostname )
    {
        for ( ContainerHost ch : envContainers )
        {
            if ( ch.getHostname().equals( hostname ) )
            {
                return ch;
            }
        }

        return null;
    }


    private String createSshKey( String containerId ) throws BazaarManagerException
    {
        CommandResult result;

        try
        {
            Host host = ctx.localPeer.getContainerHostById( containerId );

            RequestBuilder rb = new RequestBuilder( String.format(
                    "if [ -f %1$s/id_rsa.pub ]; " + "then cat %1$s/id_rsa.pub ;" + "else rm -rf %1$s ; "
                            + "mkdir -p %1$s && " + "chmod 700 %1$s && "
                            + "ssh-keygen -t rsa -P '' -f %1$s/id_rsa -q && " + "cat %1$s/id_rsa.pub; fi",
                    Common.CONTAINER_SSH_FOLDER ) ).withTimeout( Common.CREATE_SSH_KEY_TIMEOUT_SEC );

            result = commandUtil.execute( rb, host );
        }
        catch ( Exception e )
        {
            throw new BazaarManagerException( e );
        }

        if ( !result.hasSucceeded() )
        {
            throw new BazaarManagerException( "Failed to create SSH key: " + result.getStdErr() );
        }

        return result.getStdOut();
    }
}