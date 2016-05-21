package io.subutai.core.hubmanager.impl.environment.state.build;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bouncycastle.openpgp.PGPException;

import io.subutai.common.command.CommandResult;
import io.subutai.common.command.CommandUtil;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.environment.CreateEnvironmentContainersRequest;
import io.subutai.common.environment.CreateEnvironmentContainersResponse;
import io.subutai.common.environment.Environment;
import io.subutai.common.environment.Node;
import io.subutai.common.environment.PrepareTemplatesRequest;
import io.subutai.common.host.HostArchitecture;
import io.subutai.common.peer.ContainerSize;
import io.subutai.common.peer.Host;
import io.subutai.common.peer.PeerException;
import io.subutai.common.security.objects.PermissionObject;
import io.subutai.common.security.relation.RelationLinkDto;
import io.subutai.common.settings.Common;
import io.subutai.common.task.CloneRequest;
import io.subutai.common.task.CloneResponse;
import io.subutai.core.hubmanager.impl.environment.state.Context;
import io.subutai.core.hubmanager.impl.environment.state.StateHandler;
import io.subutai.hub.share.dto.environment.ContainerStateDto;
import io.subutai.hub.share.dto.environment.EnvironmentNodeDto;
import io.subutai.hub.share.dto.environment.EnvironmentNodesDto;
import io.subutai.hub.share.dto.environment.EnvironmentPeerDto;


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
                PermissionObject.EnvironmentManagement.getName(), "" );

        ctx.localPeer.createPeerEnvironmentKeyPair( envLink );
    }


    @Override
    protected void post( EnvironmentPeerDto peerDto, Object body )
    {
        ctx.restClient.post( path( PATH, peerDto ), body );
    }


    private void prepareTemplates( final EnvironmentPeerDto peerDto, EnvironmentNodesDto nodesDto ) throws Exception
    {
        Set<Node> nodes = new HashSet<>();

        for ( EnvironmentNodeDto nodeDto : nodesDto.getNodes() )
        {
            ContainerSize contSize = ContainerSize.valueOf( nodeDto.getContainerSize() );

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
        String envId = peerDto.getEnvironmentInfo().getId();

        CreateEnvironmentContainersRequest createRequests = new CreateEnvironmentContainersRequest( envId, peerDto.getPeerId(), peerDto.getOwnerId() );

        for ( EnvironmentNodeDto nodeDto : envNodes.getNodes() )
        {
            ContainerSize contSize = ContainerSize.valueOf( nodeDto.getContainerSize() );

            CloneRequest cloneRequest = new CloneRequest( nodeDto.getHostId(), nodeDto.getHostName(), nodeDto.getContainerName(),
                            nodeDto.getIp(), nodeDto.getTemplateName(), HostArchitecture.AMD64, contSize );

            createRequests.addRequest( cloneRequest );
        }

        CreateEnvironmentContainersResponse createResponse = ctx.localPeer.createEnvironmentContainers( createRequests );

        for ( CloneResponse cloneResponse : createResponse.getResponses() )
        {
            for ( EnvironmentNodeDto nodeDto : envNodes.getNodes() )
            {
                if ( cloneResponse.getHostname().equals( nodeDto.getHostName() ) )
                {
                    nodeDto.addSshKey( createSshKey( cloneResponse.getContainerId() ) );

                    nodeDto.setContainerId( cloneResponse.getContainerId() );

                    nodeDto.setHostName( cloneResponse.getHostname() );

                    nodeDto.setState( ContainerStateDto.RUNNING );
                }
            }
        }

        return envNodes;
    }


    private String createSshKey( String containerId ) throws Exception
    {
        Host host = ctx.localPeer.getContainerHostById( containerId );

        RequestBuilder rb = new RequestBuilder( String.format( "rm -rf %1$s && " +
                        "mkdir -p %1$s && " +
                        "chmod 700 %1$s && " +
                        "ssh-keygen -t dsa -P '' -f %1$s/id_dsa -q && " + "cat %1$s/id_dsa.pub",
                Common.CONTAINER_SSH_FOLDER ) );

        CommandResult result = commandUtil.execute( rb, host );

        if ( !result.hasSucceeded() )
        {
            throw new Exception( "Failed to create SSH key: " + result.getStdErr() );
        }

        return result.getStdOut();
    }
}