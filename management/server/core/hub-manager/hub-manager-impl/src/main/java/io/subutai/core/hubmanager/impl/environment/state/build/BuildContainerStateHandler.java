package io.subutai.core.hubmanager.impl.environment.state.build;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bouncycastle.openpgp.PGPException;

import org.apache.commons.lang3.EnumUtils;

import io.subutai.common.command.CommandResult;
import io.subutai.common.command.CommandUtil;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.environment.CreateEnvironmentContainersRequest;
import io.subutai.common.environment.CreateEnvironmentContainersResponse;
import io.subutai.common.environment.Environment;
import io.subutai.common.environment.Node;
import io.subutai.common.environment.PrepareTemplatesRequest;
import io.subutai.common.host.HostArchitecture;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.ContainerSize;
import io.subutai.common.peer.Host;
import io.subutai.common.peer.PeerException;
import io.subutai.common.security.objects.PermissionObject;
import io.subutai.common.security.relation.RelationLinkDto;
import io.subutai.common.settings.Common;
import io.subutai.common.task.CloneRequest;
import io.subutai.core.hubmanager.impl.environment.state.Context;
import io.subutai.core.hubmanager.impl.environment.state.StateHandler;
import io.subutai.core.hubmanager.impl.http.RestResult;
import io.subutai.hub.share.dto.environment.ContainerStateDto;
import io.subutai.hub.share.dto.environment.EnvironmentNodeDto;
import io.subutai.hub.share.dto.environment.EnvironmentNodesDto;
import io.subutai.hub.share.dto.environment.EnvironmentPeerDto;

import static io.subutai.hub.share.dto.environment.ContainerStateDto.BUILDING;


public class BuildContainerStateHandler extends StateHandler
{
    private static final String PATH = "/rest/v1/environments/%s/containers";

    private final CommandUtil commandUtil = new CommandUtil();


    public BuildContainerStateHandler( Context ctx )
    {
        super( ctx, "Building containers" );
    }


    @Override
    protected Object doHandle( EnvironmentPeerDto peerDto ) throws Exception
    {
        logStart();

        EnvironmentNodesDto nodesDto = ctx.restClient.getStrict( path( PATH, peerDto ), EnvironmentNodesDto.class );

        prepareTemplates( peerDto, nodesDto );

        setupPeerEnvironmentKey( peerDto );

        Object result = cloneContainers( peerDto, nodesDto );

        logEnd();

        return result;
    }


    /**
     * TODO. Identify for future do we need envKeyId (or do we need keyId for {@link RelationLinkDto})
     */
    private void setupPeerEnvironmentKey( EnvironmentPeerDto peerDto ) throws PeerException, PGPException
    {
        RelationLinkDto envLink = new RelationLinkDto( peerDto.getEnvironmentInfo().getId(), Environment.class.getSimpleName(),
                PermissionObject.EnvironmentManagement.getName(), peerDto.getEnvironmentInfo().getId() );

        ctx.localPeer.createPeerEnvironmentKeyPair( envLink );
    }


    @Override
    protected RestResult<Object> post( EnvironmentPeerDto peerDto, Object body )
    {
        return ctx.restClient.post( path( PATH, peerDto ), body );
    }


    private void prepareTemplates( final EnvironmentPeerDto peerDto, EnvironmentNodesDto nodesDto ) throws Exception
    {
        Set<Node> nodes = new HashSet<>();

        log.info( "Prepare templates:" );

        for ( EnvironmentNodeDto nodeDto : nodesDto.getNodes() )
        {
            ContainerSize contSize = ContainerSize.valueOf( nodeDto.getContainerSize() );

            log.info( "- noteDto: containerId={}, containerName={}, hostname={}, state={}",
                    nodeDto.getContainerId(), nodeDto.getContainerName(), nodeDto.getHostName(), nodeDto.getState() );

            Node node = new Node( nodeDto.getHostName(), nodeDto.getContainerName(), nodeDto.getTemplateName(), contSize, 0, 0,
                    peerDto.getPeerId(), nodeDto.getHostId() );

            nodes.add( node );
        }

        // <hostId, templates>
        Map<String, Set<String>> rhTemplates = new HashMap<>();

        for ( Node node : nodes )
        {
            Set<String> templates = rhTemplates.getOrDefault( node.getHostId(), new HashSet<String>() );

            if ( templates.isEmpty() )
            {
                rhTemplates.put( node.getHostId(), templates );
            }

            templates.add( node.getTemplateName() );
        }

        ctx.localPeer.prepareTemplates( new PrepareTemplatesRequest( peerDto.getEnvironmentInfo().getId(), rhTemplates ) );
    }


    private EnvironmentNodesDto cloneContainers( EnvironmentPeerDto peerDto, EnvironmentNodesDto envNodes ) throws Exception
    {
        CreateEnvironmentContainersRequest cloneRequests = createCloneRequests( peerDto, envNodes );

        // Clone requests may be empty if all containers already exists. For example, in case of duplicated requests.
        CreateEnvironmentContainersResponse cloneResponses = cloneRequests.getRequests().isEmpty()
                                                             ? null
                                                             : ctx.localPeer.createEnvironmentContainers( cloneRequests );

        if ( cloneResponses != null && !cloneResponses.hasSucceeded() )
        {
            throw new Exception( cloneResponses.getMessages().toString() );
        }

        populateResultNodes( peerDto, envNodes );

        return envNodes;
    }


    private void populateResultNodes( EnvironmentPeerDto peerDto, EnvironmentNodesDto envNodes ) throws Exception
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


    private void updateNodeDto( EnvironmentNodeDto nodeDto, Set<ContainerHost> envContainers ) throws Exception
    {
        ContainerHost ch = findContainerByHostname( envContainers, nodeDto.getHostName() );

        String contId = ch.getContainerId().getId();

        nodeDto.addSshKey( createSshKey( contId ) );

        nodeDto.setContainerId( contId );

        nodeDto.setState( EnumUtils.getEnum( ContainerStateDto.class, ch.getState().toString() ) );
    }


    private CreateEnvironmentContainersRequest createCloneRequests( EnvironmentPeerDto peerDto, EnvironmentNodesDto envNodes )
    {
        String envId = peerDto.getEnvironmentInfo().getId();

        Set<ContainerHost> envContainers = ctx.localPeer.findContainersByEnvironmentId( envId );

        CreateEnvironmentContainersRequest createRequests = new CreateEnvironmentContainersRequest( envId, peerDto.getPeerId(), peerDto.getOwnerId() );

        log.info( "Clone requests:" );

        for ( EnvironmentNodeDto nodeDto : envNodes.getNodes() )
        {
            log.info( "- noteDto: containerId={}, containerName={}, hostname={}, state={}",
                    nodeDto.getContainerId(), nodeDto.getContainerName(), nodeDto.getHostName(), nodeDto.getState() );

            // Exclude existing containers. This may happen as a result of duplicated requests or adding a new container to existing peer in env.
            if ( !containerExists( nodeDto.getHostName(), envContainers ) && nodeDto.getState() == BUILDING )
            {
                createRequests.addRequest( createCloneRequest( nodeDto ) );
            }
        }

        return createRequests;
    }


    private CloneRequest createCloneRequest( EnvironmentNodeDto nodeDto )
    {
        ContainerSize contSize = ContainerSize.valueOf( nodeDto.getContainerSize() );

        return new CloneRequest( nodeDto.getHostId(), nodeDto.getHostName(), nodeDto.getContainerName(),
                nodeDto.getIp(), nodeDto.getTemplateName(), HostArchitecture.AMD64, contSize );
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


    private String createSshKey( String containerId ) throws Exception
    {
        Host host = ctx.localPeer.getContainerHostById( containerId );

        RequestBuilder rb = new RequestBuilder( String.format( "rm -rf %1$s && " +
                        "mkdir -p %1$s && " +
                        "chmod 700 %1$s && " +
                        "ssh-keygen -t rsa -P '' -f %1$s/id_rsa -q && " + "cat %1$s/id_rsa.pub",
                Common.CONTAINER_SSH_FOLDER ) );

        CommandResult result = commandUtil.execute( rb, host );

        if ( !result.hasSucceeded() )
        {
            throw new Exception( "Failed to create SSH key: " + result.getStdErr() );
        }

        return result.getStdOut();
    }
}