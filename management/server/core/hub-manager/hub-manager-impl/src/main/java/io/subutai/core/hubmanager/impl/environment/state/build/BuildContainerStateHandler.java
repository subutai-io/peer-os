package io.subutai.core.hubmanager.impl.environment.state.build;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import com.google.common.base.Strings;

import io.subutai.common.command.CommandUtil;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.environment.CreateEnvironmentContainersRequest;
import io.subutai.common.environment.CreateEnvironmentContainersResponse;
import io.subutai.common.environment.Node;
import io.subutai.common.environment.PrepareTemplatesRequest;
import io.subutai.common.environment.PrepareTemplatesResponse;
import io.subutai.common.host.HostArchitecture;
import io.subutai.common.peer.ContainerSize;
import io.subutai.common.peer.Host;
import io.subutai.common.settings.Common;
import io.subutai.common.task.CloneRequest;
import io.subutai.common.task.CloneResponse;
import io.subutai.core.hubmanager.api.HubPluginException;
import io.subutai.core.hubmanager.impl.environment.state.Context;
import io.subutai.core.hubmanager.impl.environment.state.StateHandler;
import io.subutai.core.hubmanager.impl.http.RestResult;
import io.subutai.core.hubmanager.impl.util.AsyncUtil;
import io.subutai.hub.share.dto.environment.ContainerStateDto;
import io.subutai.hub.share.dto.environment.EnvironmentNodeDto;
import io.subutai.hub.share.dto.environment.EnvironmentNodesDto;
import io.subutai.hub.share.dto.environment.EnvironmentPeerDto;


public class BuildContainerStateHandler extends StateHandler
{
    public BuildContainerStateHandler( Context ctx )
    {
        super( ctx );
    }


    @Override
    protected Object doHandle( EnvironmentPeerDto peerDto ) throws Exception
    {
        String path = String.format( "/rest/v1/environments/%s/containers", peerDto.getEnvironmentInfo().getId() );

        EnvironmentNodesDto nodesDto = getEnvironmentContainers( path );

        prepareTemplates( peerDto, nodesDto );

        return cloneContainers( peerDto, nodesDto );
    }


    @Override
    protected void post( EnvironmentPeerDto peerDto, Object body )
    {
        String path = String.format( "/rest/v1/environments/%s/containers", peerDto.getEnvironmentInfo().getId() );

        ctx.restClient.post( path, body );
    }


    private EnvironmentNodesDto getEnvironmentContainers( String path ) throws HubPluginException
    {
        RestResult<EnvironmentNodesDto> restResult = ctx.restClient.get( path, EnvironmentNodesDto.class );

        if ( !restResult.isSuccess() )
        {
            throw new HubPluginException( restResult.getError() );
        }

        return restResult.getEntity();
    }


    public void prepareTemplates( final EnvironmentPeerDto peerDto, EnvironmentNodesDto nodesDto ) throws Exception
    {
        //        LocalPeer localPeer = peerManager.getLocalPeer();

        final Set<Node> nodes = new HashSet<>();

        for ( EnvironmentNodeDto nodeDto : nodesDto.getNodes() )
        {
            if ( nodeDto.getState().equals( ContainerStateDto.BUILDING ) )
            {
                ContainerSize contSize = ContainerSize.valueOf( ContainerSize.class, nodeDto.getContainerSize() );

                Node node =
                        new Node( nodeDto.getHostName(), nodeDto.getContainerName(), nodeDto.getTemplateName(), contSize, 0, 0, peerDto.getPeerId(),
                                nodeDto.getHostId() );

                nodes.add( node );
            }
        }

        //        ExecutorService taskExecutor = Executors.newSingleThreadExecutor();
        //
        //        CompletionService<PrepareTemplatesResponse> taskCompletionService = getCompletionService( taskExecutor );
        //
        //        taskCompletionService.submit( new CreatePeerTemplatePrepareTask( environmentId, localPeer, nodes ) );
        //
        //        taskExecutor.shutdown();

        PrepareTemplatesResponse prepareTemplatesResponse = AsyncUtil.execute( new Callable<PrepareTemplatesResponse>()
        {
            public PrepareTemplatesResponse call() throws Exception
            {
                Map<String, Set<String>> rhTemplates = new HashMap<>();

                for ( Node node : nodes )
                {
                    Set<String> templates = rhTemplates.get( node.getHostId() );
                    if ( templates == null )
                    {
                        templates = new HashSet<>();
                        rhTemplates.put( node.getHostId(), templates );
                    }
                    templates.add( node.getTemplateName() );
                }

                return ctx.peerManager.getLocalPeer()
                                      .prepareTemplates( new PrepareTemplatesRequest( peerDto.getEnvironmentInfo().getId(), rhTemplates ) );
            }
        } );

        //        Future<PrepareTemplatesResponse> futures = taskCompletionService.take();
        //
        //        PrepareTemplatesResponse prepareTemplatesResponse = futures.get();

        if ( !prepareTemplatesResponse.hasSucceeded() )
        {
            log.error( "Error to prepare templates" );
        }
    }


    public EnvironmentNodesDto cloneContainers( EnvironmentPeerDto peerDto, EnvironmentNodesDto envNodes ) throws Exception
    {
        CreateEnvironmentContainersRequest containerGroupRequest =
                new CreateEnvironmentContainersRequest( peerDto.getEnvironmentInfo().getId(), peerDto.getPeerId(),
                        peerDto.getOwnerId() );

        Set<EnvironmentNodeDto> failedNodes = new HashSet<>();

        for ( EnvironmentNodeDto nodeDto : envNodes.getNodes() )
        {
            if ( nodeDto.getState().equals( ContainerStateDto.BUILDING ) )
            {
                failedNodes.add( nodeDto );
                ContainerSize contSize = ContainerSize.valueOf( ContainerSize.class, nodeDto.getContainerSize() );

                nodeDto.setState( ContainerStateDto.UNKNOWN );
                CloneRequest cloneRequest =
                        new CloneRequest( nodeDto.getHostId(), nodeDto.getHostName(), nodeDto.getContainerName(),
                                nodeDto.getIp(), nodeDto.getTemplateName(), HostArchitecture.AMD64, contSize );

                containerGroupRequest.addRequest( cloneRequest );
            }
        }

        final CreateEnvironmentContainersResponse containerCollector;

        containerCollector = ctx.peerManager.getLocalPeer().createEnvironmentContainers( containerGroupRequest );

        Set<CloneResponse> cloneResponseList = containerCollector.getResponses();

        for ( CloneResponse cloneResponse : cloneResponseList )
        {
            for ( EnvironmentNodeDto nodeDto : envNodes.getNodes() )
            {
                if ( cloneResponse.getHostname().equals( nodeDto.getHostName() ) )
                {
                    failedNodes.remove( nodeDto );

                    nodeDto.setIp( cloneResponse.getIp() );
                    nodeDto.setTemplateArch( cloneResponse.getTemplateArch().name() );
                    nodeDto.setContainerId( cloneResponse.getContainerId() );
                    nodeDto.setElapsedTime( cloneResponse.getElapsedTime() );
                    nodeDto.setHostName( cloneResponse.getHostname() );
                    nodeDto.setState( ContainerStateDto.RUNNING );

                    Set<Host> hosts = new HashSet<>();
                    Host host = ctx.peerManager.getLocalPeer().getContainerHostById( nodeDto.getContainerId() );
                    hosts.add( host );

                    String sshKey = createSshKey( hosts, peerDto.getEnvironmentInfo().getId() );
                    nodeDto.addSshKey( sshKey );
                }
            }
        }

        return envNodes;
    }


    public String createSshKey( Set<Host> hosts, String environmentId )
    {
        CommandUtil commandUtil = new CommandUtil();

        CommandUtil.HostCommandResults results =
                commandUtil.execute( getCreateNReadSSHCommand(), hosts, environmentId );

        for ( CommandUtil.HostCommandResult result : results.getCommandResults() )
        {
            if ( result.hasSucceeded() && !Strings.isNullOrEmpty( result.getCommandResult().getStdOut() ) )
            {
                return result.getCommandResult().getStdOut();
            }
            else
            {
                log.debug( String.format( "Error: %s, Exit Code %d", result.getCommandResult().getStdErr(),
                        result.getCommandResult().getExitCode() ) );
            }
        }
        return null;
    }


    public RequestBuilder getCreateNReadSSHCommand()
    {
        return new RequestBuilder( String.format( "rm -rf %1$s && " +
                        "mkdir -p %1$s && " +
                        "chmod 700 %1$s && " +
                        "ssh-keygen -t dsa -P '' -f %1$s/id_dsa -q && " + "cat %1$s/id_dsa.pub",
                Common.CONTAINER_SSH_FOLDER ) );
    }


//        try
//        {
//
////                for ( String templateResponse : prepareTemplatesResponse.getMessages() )
////                {
////                    String msg =
////                            "Error during preparation template: " + templateResponse + " Peer ID: " + localPeer.getId();
////                    sendLogToHub( peerDto, msg, null, EnvironmentPeerLogDto.LogEvent.SUBUTAI,
////                            EnvironmentPeerLogDto.LogType.ERROR, null );
////                    log.error( msg );
////                    throw new EnvironmentCreationException( msg );
////                }
//            }
//        }
//        catch ( Exception e )
//        {
//            String msg = "There were errors during preparation templates. Unexpected error.";
//            sendLogToHub( peerDto, msg, e.getMessage(), EnvironmentPeerLogDto.LogEvent.SUBUTAI,
//                    EnvironmentPeerLogDto.LogType.ERROR, null );
//            log.error( msg, e.getMessage() );
//            throw new EnvironmentCreationException( msg );
//        }
//    }



//    private void buildContainers( EnvironmentPeerDto peerDto )
//    {
//        String path = String.format( "/rest/v1/environments/%s/container-build-workflow", peerDto.getEnvironmentInfo().getId() );
//
//        try
//        {
//            WebClient client = configManager.getTrustedWebClientWithAuth( containerDataURL, configManager.getHubIp() );
//            Response r = client.get();
//            byte[] encryptedContent = configManager.readContent( r );
//            byte[] plainContent = configManager.getMessenger().consume( encryptedContent );
//            EnvironmentNodesDto envNodes = JsonUtil.fromCbor( plainContent, EnvironmentNodesDto.class );
//
//            hubEnvManager.prepareTemplates( peerDto, envNodes, peerDto.getEnvironmentInfo().getId() );
//
//            EnvironmentNodesDto updatedNodes = hubEnvManager.cloneContainers( peerDto, envNodes );
//
//            byte[] cborData = JsonUtil.toCbor( updatedNodes );
//            byte[] encryptedData = configManager.getMessenger().produce( cborData );
//            Response response = client.put( encryptedData );
//            client.close();
//
//            log.debug( "response.status: {}", response.getStatus() );
//
//            if ( response.getStatus() == HttpStatus.SC_NO_CONTENT )
//            {
//                log.debug( "env_via_hub: Environment successfully build!!!" );
//            }
//        }
//        catch ( Exception e )
//        {
//            String mgs = "Could not get container creation data from Hub.";
//
//            hubEnvManager.sendLogToHub( peerDto, mgs, e.getMessage(), EnvironmentPeerLogDto.LogEvent.REQUEST_TO_HUB, EnvironmentPeerLogDto.LogType.ERROR, null );
//
//            log.error( mgs, e );
//        }
//    }


//    public void prepareTemplates( EnvironmentPeerDto peerDto, EnvironmentNodesDto nodesDto, String environmentId )
//            throws EnvironmentCreationException
//    {
//        LocalPeer localPeer = peerManager.getLocalPeer();
//        Set<Node> nodes = new HashSet<>();
//        for ( EnvironmentNodeDto nodeDto : nodesDto.getNodes() )
//        {
//            if ( nodeDto.getState().equals( ContainerStateDto.BUILDING ) )
//            {
//                ContainerSize contSize = ContainerSize.valueOf( ContainerSize.class, nodeDto.getContainerSize() );
//                Node node = new Node( nodeDto.getHostName(), nodeDto.getContainerName(), nodeDto.getTemplateName(),
//                        contSize, 0, 0, peerDto.getPeerId(), nodeDto.getHostId() );
//                nodes.add( node );
//            }
//        }
//
//        ExecutorService taskExecutor = Executors.newSingleThreadExecutor();
//        CompletionService<PrepareTemplatesResponse> taskCompletionService = getCompletionService( taskExecutor );
//
//        taskCompletionService.submit( new CreatePeerTemplatePrepareTask( environmentId, localPeer, nodes ) );
//        taskExecutor.shutdown();
//
//        try
//        {
//            Future<PrepareTemplatesResponse> futures = taskCompletionService.take();
//            final PrepareTemplatesResponse prepareTemplatesResponse = futures.get();
//
//            if ( !prepareTemplatesResponse.hasSucceeded() )
//            {
//                for ( String templateResponse : prepareTemplatesResponse.getMessages() )
//                {
//                    String msg =
//                            "Error during preparation template: " + templateResponse + " Peer ID: " + localPeer.getId();
//                    sendLogToHub( peerDto, msg, null, EnvironmentPeerLogDto.LogEvent.SUBUTAI,
//                            EnvironmentPeerLogDto.LogType.ERROR, null );
//                    log.error( msg );
//                    throw new EnvironmentCreationException( msg );
//                }
//            }
//        }
//        catch ( Exception e )
//        {
//            String msg = "There were errors during preparation templates. Unexpected error.";
//            sendLogToHub( peerDto, msg, e.getMessage(), EnvironmentPeerLogDto.LogEvent.SUBUTAI,
//                    EnvironmentPeerLogDto.LogType.ERROR, null );
//            log.error( msg, e.getMessage() );
//            throw new EnvironmentCreationException( msg );
//        }
//    }
//
//
//    public EnvironmentNodesDto cloneContainers( EnvironmentPeerDto peerDto, EnvironmentNodesDto envNodes )
//            throws EnvironmentCreationException
//    {
//        CreateEnvironmentContainersRequest containerGroupRequest =
//                new CreateEnvironmentContainersRequest( peerDto.getEnvironmentInfo().getId(), peerDto.getPeerId(),
//                        peerDto.getOwnerId() );
//
//        Set<EnvironmentNodeDto> failedNodes = new HashSet<>();
//
//        for ( EnvironmentNodeDto nodeDto : envNodes.getNodes() )
//        {
//            if ( nodeDto.getState().equals( ContainerStateDto.BUILDING ) )
//            {
//                failedNodes.add( nodeDto );
//                ContainerSize contSize = ContainerSize.valueOf( ContainerSize.class, nodeDto.getContainerSize() );
//
//                nodeDto.setState( ContainerStateDto.UNKNOWN );
//                CloneRequest cloneRequest =
//                        new CloneRequest( nodeDto.getHostId(), nodeDto.getHostName(), nodeDto.getContainerName(),
//                                nodeDto.getIp(), nodeDto.getTemplateName(), HostArchitecture.AMD64, contSize );
//
//                containerGroupRequest.addRequest( cloneRequest );
//            }
//        }
//
//        final CreateEnvironmentContainersResponse containerCollector;
//
//        try
//        {
//            containerCollector = peerManager.getLocalPeer().createEnvironmentContainers( containerGroupRequest );
//
//            Set<CloneResponse> cloneResponseList = containerCollector.getResponses();
//
//            for ( CloneResponse cloneResponse : cloneResponseList )
//            {
//                for ( EnvironmentNodeDto nodeDto : envNodes.getNodes() )
//                {
//                    if ( cloneResponse.getHostname().equals( nodeDto.getHostName() ) )
//                    {
//                        failedNodes.remove( nodeDto );
//
//                        nodeDto.setIp( cloneResponse.getIp() );
//                        nodeDto.setTemplateArch( cloneResponse.getTemplateArch().name() );
//                        nodeDto.setContainerId( cloneResponse.getContainerId() );
//                        nodeDto.setElapsedTime( cloneResponse.getElapsedTime() );
//                        nodeDto.setHostName( cloneResponse.getHostname() );
//                        nodeDto.setState( ContainerStateDto.RUNNING );
//
//                        Set<Host> hosts = new HashSet<>();
//                        Host host = peerManager.getLocalPeer().getContainerHostById( nodeDto.getContainerId() );
//                        hosts.add( host );
//
//                        String sshKey = createSshKey( hosts, peerDto.getEnvironmentInfo().getId() );
//                        nodeDto.addSshKey( sshKey );
//                    }
//                }
//            }
//        }
//        catch ( Exception e )
//        {
//            String msg = "Failed on cloning container: ";
//
//            for ( EnvironmentNodeDto nodeDto : failedNodes )
//            {
//                msg += nodeDto.getContainerId();
//                sendLogToHub( peerDto, msg, e.getMessage(), EnvironmentPeerLogDto.LogEvent.CONTAINER,
//                        EnvironmentPeerLogDto.LogType.ERROR, nodeDto.getContainerId() );
//                log.error( msg, e.getMessage() );
//            }
//            throw new EnvironmentCreationException( msg );
//        }
//
//        if ( failedNodes.size() != 0 )
//        {
//            String msg = "Failed on cloning container: ";
//
//            for ( EnvironmentNodeDto nodeDto : failedNodes )
//            {
//                sendLogToHub( peerDto, msg + nodeDto.getContainerId(), null, EnvironmentPeerLogDto.LogEvent.CONTAINER,
//                        EnvironmentPeerLogDto.LogType.ERROR, nodeDto.getContainerId() );
//
//                log.error( msg + nodeDto.getContainerId() );
//            }
//
//            throw new EnvironmentCreationException( msg );
//        }
//
//        return envNodes;
//    }
}