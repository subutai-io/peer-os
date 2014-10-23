package org.safehaus.subutai.core.peer.impl;


import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.sql.DataSource;

import org.safehaus.subutai.common.exception.HTTPException;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.CloneContainersMessage;
import org.safehaus.subutai.common.protocol.ContainerState;
import org.safehaus.subutai.common.protocol.DestroyContainersMessage;
import org.safehaus.subutai.common.protocol.ExecuteCommandMessage;
import org.safehaus.subutai.common.protocol.PeerCommandMessage;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.common.util.RestUtil;
import org.safehaus.subutai.common.util.UUIDUtil;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.command.api.command.AgentResult;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.command.api.command.CommandException;
import org.safehaus.subutai.core.command.api.command.RequestBuilder;
import org.safehaus.subutai.core.container.api.ContainerCreateException;
import org.safehaus.subutai.core.container.api.ContainerDestroyException;
import org.safehaus.subutai.core.container.api.ContainerManager;
import org.safehaus.subutai.core.peer.api.Peer;
import org.safehaus.subutai.core.peer.api.PeerContainer;
import org.safehaus.subutai.core.peer.api.PeerException;
import org.safehaus.subutai.core.peer.api.PeerGroup;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.peer.api.message.Common;
import org.safehaus.subutai.core.peer.api.message.PeerMessageException;
import org.safehaus.subutai.core.peer.api.message.PeerMessageListener;
import org.safehaus.subutai.core.peer.impl.dao.PeerDAO;
import org.safehaus.subutai.core.registry.api.RegistryException;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;


/**
 * PeerManager implementation
 */
public class PeerManagerImpl implements PeerManager
{

    private static final Logger LOG = LoggerFactory.getLogger( PeerManagerImpl.class.getName() );
    private static final String SOURCE = "PEER_MANAGER";
    private static final String PEER_GROUP = "PEER_GROUP";
    private final Queue<PeerMessageListener> peerMessageListeners = new ConcurrentLinkedQueue<>();
    private AgentManager agentManager;
    private PeerDAO peerDAO;
    private ContainerManager containerManager;
    private CommandRunner commandRunner;
    private TemplateRegistry templateRegistry;
    private DataSource dataSource;
    private Set<PeerContainer> containers = new HashSet<>();


    public PeerManagerImpl( final DataSource dataSource )
    {
        Preconditions.checkNotNull( dataSource, "Data source is null" );
        this.dataSource = dataSource;
    }


    public void init()
    {
        try
        {
            this.peerDAO = new PeerDAO( dataSource );
        }
        catch ( SQLException e )
        {
            LOG.error( e.getMessage(), e );
        }
    }


    public void destroy()
    {
    }


    public void setAgentManager( final AgentManager agentManager )
    {
        this.agentManager = agentManager;
    }


    public void setCommandRunner( final CommandRunner commandRunner )
    {
        this.commandRunner = commandRunner;
    }


    public void setTemplateRegistry( final TemplateRegistry templateRegistry )
    {
        this.templateRegistry = templateRegistry;
    }


    public ContainerManager getContainerManager()
    {
        return containerManager;
    }


    public void setContainerManager( final ContainerManager containerManager )
    {
        this.containerManager = containerManager;
    }


    @Override
    public boolean register( final Peer peer )
    {
        Agent management = agentManager.getAgentByHostname( "management" );
        String cmd = String.format( "sed '/^path_map.*$/ s/$/ ; %s %s/' apt-cacher.conf > apt-cacher.conf"
                        + ".new && mv apt-cacher.conf.new apt-cacher.conf && /etc/init.d/apt-cacher reload",
                peer.getId().toString(),
                ( "http://" + peer.getIp() + "/ksks" ).replace( ".", "\\." ).replace( "/", "\\/" ) );

        LOG.info( cmd );
        RequestBuilder rb = new RequestBuilder( cmd );
        rb.withCwd( "/etc/apt-cacher/" );
        Command command = commandRunner.createCommand( rb, Sets.newHashSet( management ) );
        commandRunner.runCommand( command );
        boolean r = command.hasSucceeded();
        LOG.info( "Apt-cacher mapping result: " + r );
        return peerDAO.saveInfo( SOURCE, peer.getId().toString(), peer );
    }


    @Override
    public boolean update( final Peer peer )
    {
        return peerDAO.saveInfo( SOURCE, peer.getId().toString(), peer );
    }


    @Override
    public UUID getSiteId()
    {
        return UUIDUtil.generateMACBasedUUID();
    }


    @Override
    public List<Peer> peers()
    {
        return peerDAO.getInfo( SOURCE, Peer.class );
    }


    @Override
    public boolean unregister( final String uuid )
    {
        return peerDAO.deleteInfo( SOURCE, uuid );
    }


    @Override
    public Peer getPeerByUUID( UUID uuid )
    {
        if ( getSiteId().compareTo( uuid ) == 0 )
        {
            Peer peer = new Peer();
            peer.setId( uuid );
            peer.setIp( getLocalIp() );
            peer.setName( "Me" );
            return peer;
        }

        return peerDAO.getInfo( SOURCE, uuid.toString(), Peer.class );
    }


    /*@Override
    public String getRemoteId( final String baseUrl )
    {
        RemotePeerClient client = new RemotePeerClient();
        client.setBaseUrl( baseUrl );
        return client.callRemoteRest();
    }*/


    @Override
    public void addPeerMessageListener( PeerMessageListener listener )
    {
        try
        {
            if ( !peerMessageListeners.contains( listener ) )
            {
                peerMessageListeners.add( listener );
            }
        }
        catch ( Exception ex )
        {
            LOG.error( "Error in addPeerMessageListener", ex );
        }
    }


    @Override
    public void removePeerMessageListener( PeerMessageListener listener )
    {
        try
        {
            peerMessageListeners.remove( listener );
        }
        catch ( Exception ex )
        {
            LOG.error( "Error in removePeerMessageListener", ex );
        }
    }


    @Override
    public String sendPeerMessage( final Peer peer, String recipient, final String message ) throws PeerMessageException
    {
        if ( peer == null )
        {
            throw new PeerMessageException( "Peer is null" );
        }
        if ( Strings.isNullOrEmpty( recipient ) )
        {
            throw new PeerMessageException( "Recipient is null or empty" );
        }
        if ( Strings.isNullOrEmpty( message ) )
        {
            throw new PeerMessageException( "Message is null or empty" );
        }

        try
        {
            if ( isPeerReachable( peer ) )
            {

                Map<String, String> params = new HashMap<>();
                params.put( Common.RECIPIENT_PARAM_NAME, recipient );
                params.put( Common.PEER_ID_PARAM_NAME, getSiteId().toString() );
                params.put( Common.MESSAGE_PARAM_NAME, message );
                try
                {
                    return RestUtil.post( String.format( Common.MESSAGE_REQUEST_URL, peer.getIp() ), params );
                }
                catch ( HTTPException e )
                {
                    LOG.error( "Error in sendPeerMessage", e );
                    throw new PeerMessageException( e.getMessage() );
                }
            }
            else
            {
                String err = "Peer is not reachable";
                LOG.error( "Error in sendPeerMessage", err );
                throw new PeerMessageException( err );
            }
        }
        catch ( PeerException e )
        {
            LOG.error( "Error in sendPeerMessage", e );
            throw new PeerMessageException( e.getMessage() );
        }
    }


    @Override
    public String processPeerMessage( final String peerId, final String recipient, final String message )
            throws PeerMessageException
    {
        if ( Strings.isNullOrEmpty( peerId ) )
        {
            throw new PeerMessageException( "Peer id is null or empty" );
        }
        if ( Strings.isNullOrEmpty( recipient ) )
        {
            throw new PeerMessageException( "Recipient is null or empty" );
        }
        if ( Strings.isNullOrEmpty( message ) )
        {
            throw new PeerMessageException( "Message is null or empty" );
        }
        try
        {
            UUID peerUUID = UUID.fromString( peerId );
            Peer senderPeer = getPeerByUUID( peerUUID );
            if ( senderPeer != null )
            {
                try
                {
                    if ( isPeerReachable( senderPeer ) )
                    {
                        for ( PeerMessageListener listener : peerMessageListeners )
                        {
                            if ( listener.getName().equalsIgnoreCase( recipient ) )
                            {
                                try
                                {
                                    return listener.onMessage( senderPeer, message );
                                }
                                catch ( Exception e )
                                {
                                    LOG.error( "Error in processPeerMessage", e );
                                    throw new PeerMessageException( e.getMessage() );
                                }
                            }
                        }
                        String err = String.format( "Recipient %s not found", recipient );
                        LOG.error( "Error in processPeerMessage", err );
                        throw new PeerMessageException( err );
                    }
                    else
                    {
                        String err = String.format( "Peer is not reachable %s", senderPeer );
                        LOG.error( "Error in processPeerMessage", err );
                        throw new PeerMessageException( err );
                    }
                }
                catch ( PeerException e )
                {
                    LOG.error( "Error in processPeerMessage", e );
                    throw new PeerMessageException( e.getMessage() );
                }
            }
            else
            {
                String err = String.format( "Peer %s not found", peerId );
                LOG.error( "Error in processPeerMessage", err );
                throw new PeerMessageException( err );
            }
        }
        catch ( IllegalArgumentException e )
        {
            LOG.error( "Error in processPeerMessage", e );
            throw new PeerMessageException( e.getMessage() );
        }
    }


    @Override
    public boolean isPeerReachable( final Peer peer ) throws PeerException
    {
        if ( peer == null )
        {
            throw new PeerException( "Peer is null" );
        }
        if ( getPeerByUUID( peer.getId() ) != null )
        {

            if ( peer.getId().compareTo( getSiteId() ) == 0 )
            {
                return true;
            }
            try
            {
                RestUtil.get( String.format( Common.PING_URL, peer.getIp() ), null );
                return true;
            }
            catch ( HTTPException e )
            {
                return false;
            }
        }
        else
        {
            throw new PeerException( "Peer not found" );
        }
    }


    @Override
    public Set<Agent> getConnectedAgents( String environmentId ) throws PeerException
    {
        try
        {
            UUID envId = UUID.fromString( environmentId );
            return agentManager.getAgentsByEnvironmentId( envId );
        }
        catch ( IllegalArgumentException e )
        {
            LOG.error( "Error in getConnectedAgents", e );
            throw new PeerException( e.getMessage() );
        }
    }


    @Override
    public Set<Agent> getConnectedAgents( final Peer peer, final String environmentId ) throws PeerException
    {
        if ( isPeerReachable( peer ) )
        {
            try
            {
                Map<String, String> params = new HashMap<>();
                params.put( Common.ENV_ID_PARAM_NAME, environmentId );
                String response = RestUtil.get( String.format( Common.GET_AGENTS_URL, peer.getIp() ), params );
                return JsonUtil.fromJson( response, new TypeToken<Set<Agent>>()
                {
                }.getType() );
            }
            catch ( JsonSyntaxException | HTTPException e )
            {
                LOG.error( "Error in getConnectedAgents", e );
                throw new PeerException( e.getMessage() );
            }
        }
        else
        {
            String err = String.format( "Peer is not reachable %s", peer );
            LOG.error( "Error in getConnectedAgents", err );
            throw new PeerException( err );
        }
    }


    @Override
    public Set<Agent> createContainers( UUID envId, UUID peerId, String template, int numberOfNodes, String strategy )
            throws ContainerCreateException
    {
        if ( peerId == null )
        {
            throw new IllegalArgumentException( "Peer could not be null." );
        }

        return containerManager.clone( envId, template, numberOfNodes, strategy, null );
    }


    protected boolean isRemotePeer( final UUID peerId )
    {
        return getSiteId().equals( peerId );
    }


    private void tryToRegister( final Template template ) throws RegistryException
    {
        if ( templateRegistry.getTemplate( template.getTemplateName() ) == null )
        {
            templateRegistry.registerTemplate( template );
        }
    }

    //
    //    protected Template getRemoteTemplate( final String template,
    // UUID remotePeerId ) throws ContainerCreateException
    //    {
    //        PeerCommandMessage getTemplateCommand =
    //                new DefaultCommandMessage( PeerCommandType.GET_TEMPLATE, null, remotePeerId, null );
    //        getTemplateCommand.setInput( template );
    //        peerCommandDispatcher.invoke( getTemplateCommand );
    //        if ( getTemplateCommand.isSuccess() )
    //        {
    //            return JsonUtil.fromJson( getTemplateCommand.getResult().toString(), Template.class );
    //        }
    //        else
    //        {
    //            throw new ContainerCreateException( "Could not get remote template." );
    //        }
    //    }


    @Override
    public boolean startContainer( final PeerContainer container )
    {
        Agent parentAgent = agentManager.getAgentByUUID( container.getParentHostId() );
        if ( parentAgent == null )
        {
            return false;
        }
        return containerManager.startLxcOnHost( parentAgent, container.getHostname() );
    }


    @Override
    public boolean stopContainer( final PeerContainer container )
    {
        Agent parentAgent = agentManager.getAgentByUUID( container.getParentHostId() );
        if ( parentAgent == null )
        {
            return false;
        }
        return containerManager.stopLxcOnHost( parentAgent, container.getHostname() );
    }


    @Override
    public boolean isContainerConnected( final PeerContainer container )
    {
        return agentManager.getAgentByUUID( container.getAgentId() ) != null;
    }


    @Override
    public Set<PeerContainer> getContainers()
    {
        return containers;
    }


    @Override
    public void addContainer( final PeerContainer peerContainer )
    {
        if ( peerContainer == null )
        {
            throw new IllegalArgumentException( "Peer container could not be null." );
        }

        peerContainer.setPeerManager( this );
        containers.add( peerContainer );
    }


    @Override
    public void invoke( PeerCommandMessage peerCommandMessage )
    {

        if ( !getSiteId().equals( peerCommandMessage.getPeerId() ) )
        {
            LOG.warn( String.format( "Orphan command message: %s", peerCommandMessage ) );
            return;
        }
        PeerContainer peerContainer = containerLookup( peerCommandMessage );
        LOG.debug( String.format( "Before =================[%s]", peerCommandMessage ) );
        boolean result;
        Template template;
        switch ( peerCommandMessage.getType() )
        {
            case CLONE:
                if ( peerCommandMessage instanceof CloneContainersMessage )
                {

                    CloneContainersMessage ccm = ( CloneContainersMessage ) peerCommandMessage;
                    try
                    {
                        for ( Template t : ccm.getTemplates() )
                        {
                            if ( t.isRemote() )
                            {
                                tryToRegister( t );
                            }
                        }
                        Set<Agent> agents = createContainers( ccm.getEnvId(), ccm.getPeerId(), ccm.getTemplate(),
                                ccm.getNumberOfNodes(), ccm.getStrategy() );
                        ccm.setResult( agents );
                    }
                    catch ( Exception e )
                    {
                        peerCommandMessage.setExceptionMessage( e.toString() );
                    }
                }
                break;
            case GET_PEER_ID:
                UUID peerId = getSiteId();
                peerCommandMessage.setResult( peerId );
                break;
            case GET_CONNECTED_CONTAINERS:
                Set<Agent> agents = agentManager.getAgents();

                Set<PeerContainer> containers = new HashSet<>();
                for ( Agent agent : agents )
                {
                    PeerContainer pc = new PeerContainer();
                    pc.setAgentId( agent.getUuid() );
                    pc.setPeerId( agent.getSiteId() );
                    pc.setState( ContainerState.STARTED );
                    containers.add( pc );
                }
                String jsonObject = JsonUtil.toJson( containers );
                peerCommandMessage.setResult( jsonObject );
                break;
            case START:
                result = startContainer( peerContainer );
                if ( result )
                {
                    peerCommandMessage.setResult( "true" );
                }
                else
                {
                    peerCommandMessage.setExceptionMessage( "Could not start container." );
                }
                break;
            case STOP:
                result = stopContainer( peerContainer );
                if ( result )
                {
                    peerCommandMessage.setResult( "true" );
                }
                else
                {
                    peerCommandMessage.setExceptionMessage( "Could not stop container." );
                }
                break;
            case IS_CONNECTED:

                result = isContainerConnected( peerContainer );
                if ( result )
                {
                    peerCommandMessage.setResult( "true" );
                }
                else
                {
                    //                    peerCommandMessage.setSuccess( result );
                    peerCommandMessage.setExceptionMessage( "Container is not connected." );
                }
                break;
            case EXECUTE:
                if ( peerCommandMessage instanceof ExecuteCommandMessage )
                {
                    ExecuteCommandMessage ecm = ( ExecuteCommandMessage ) peerCommandMessage;
                    executeCommand( peerContainer, ecm );
                }
                else
                {
                    peerCommandMessage.setExceptionMessage( "Unknown execute command." );
                }
                break;
            case REGISTER_TEMPLATE:
                template = JsonUtil.fromJson( peerCommandMessage.getInput().toString(), Template.class );

                try
                {
                    templateRegistry.registerTemplate( template );
                    peerCommandMessage.setResult( "true" );
                }
                catch ( RegistryException e )
                {
                    peerCommandMessage.setExceptionMessage( e.toString() );
                }

                break;
            case GET_TEMPLATE:
                String templateName = peerCommandMessage.getInput().toString();
                template = templateRegistry.getTemplate( templateName );
                if ( template != null )
                {
                    peerCommandMessage.setResult( JsonUtil.toJson( template ) );
                }
                else
                {
                    peerCommandMessage.setExceptionMessage( "Template not found." );
                }
                break;
            case DESTROY:
                if ( peerCommandMessage instanceof DestroyContainersMessage )
                {
                    DestroyContainersMessage dcm = ( DestroyContainersMessage ) peerCommandMessage;
                    try
                    {
                        Agent agent = agentManager.getAgentByHostname( dcm.getHostname() );
                        Agent parentAgent = agentManager.getAgentByHostname( agent.getParentHostName() );
                        containerManager.destroy( parentAgent.getHostname(), agent.getHostname() );

                        //                        peerCommandMessage.setSuccess( true );
                    }
                    catch ( ContainerDestroyException e )
                    {
                        LOG.error( e.getMessage(), e );
                        //                        peerCommandMessage.setSuccess( false );
                    }
                }
                else
                {
                    //                    peerCommandMessage.setSuccess( false );
                }
                break;
            default:
                peerCommandMessage.setExceptionMessage( "Unknown command." );
                //                peerCommandMessage.setSuccess( false );
                break;
        }
        peerCommandMessage.setProccessed( true );

        LOG.debug( String.format( "After =================[%s]", peerCommandMessage ) );
    }


    @Override
    public List<PeerGroup> peersGroups()
    {
        List<PeerGroup> peerGroups = peerDAO.getInfo( PEER_GROUP, PeerGroup.class );
        /*Set<PeerGroup> peerGroups = new HashSet<>();
        for ( int i = 0; i < 10; i++ )
        {
            PeerGroup peerGroup = new PeerGroup();
            peerGroup.setName( "Group " + i );
            for ( int j = 0; j < 10; j++ )
            {
                peerGroup.addPeerUUID( UUID.randomUUID() );
            }
            peerGroups.add( peerGroup );
        }*/
        return peerGroups;
    }


    @Override
    public void deletePeerGroup( final PeerGroup group )
    {
        peerDAO.deleteInfo( PEER_GROUP, group.getUUID().toString() );
    }


    @Override
    public boolean savePeerGroup( final PeerGroup group )
    {
        return peerDAO.saveInfo( PEER_GROUP, group.getId().toString(), group );
    }


    private void executeCommand( final PeerContainer peerContainer, final ExecuteCommandMessage ecm )
    {
        Agent agent = agentManager.getAgentByUUID( ecm.getAgentId() );
        if ( agent == null )
        {
            ecm.setExceptionMessage( "Container is not available.\n" );
            //            ecm.setSuccess( false );
            return;
        }
        RequestBuilder requestBuilder = new RequestBuilder( ecm.getCommand() );


        if ( ecm.getCwd() != null && !Strings.isNullOrEmpty( ecm.getCwd() ) )
        {
            requestBuilder.withCwd( ecm.getCwd() );
        }

        long timeout = ecm.getTimeout();

        requestBuilder.withTimeout( ( int ) timeout );
        Command cmd = commandRunner.createCommand( "Remote command", requestBuilder, Sets.newHashSet( agent ) );
        try
        {
            cmd.execute();
            AgentResult result = cmd.getResults().get( ecm.getAgentId() );
            ExecuteCommandMessage.ExecutionResult executionResult =
                    ecm.createExecutionResult( result.getStdOut(), result.getStdErr(), result.getExitCode() );
            ecm.setResult( executionResult );
            //            ecm.setStdOut( result.getStdOut() );
            //            ecm.setStdErr( result.getStdErr() );
            //            ecm.setExitCode( result.getExitCode() );
            //            ecm.setSuccess( true );
        }
        catch ( CommandException e )
        {
            //            ecm.setSuccess( false );
            ecm.setExceptionMessage( e.toString() );
        }
    }


    private PeerContainer containerLookup( PeerCommandMessage peerCommand )
    {
        if ( peerCommand.getAgentId() == null )
        {
            return null;
        }
        UUID agentId = peerCommand.getAgentId();
        PeerContainer container = findPeerContainer( agentId );
        return container;
    }


    private PeerContainer findPeerContainer( final UUID agentId )
    {
        PeerContainer result = null;
        Iterator iterator = containers.iterator();
        while ( result == null && iterator.hasNext() )
        {
            PeerContainer c = ( PeerContainer ) iterator.next();
            if ( c.getAgentId().equals( agentId ) )
            {
                result = c;
            }
        }
        return result;
    }


    private String getLocalIp()
    {
        Enumeration<NetworkInterface> n;
        try
        {
            n = NetworkInterface.getNetworkInterfaces();
            for (; n.hasMoreElements(); )
            {
                NetworkInterface e = n.nextElement();

                Enumeration<InetAddress> a = e.getInetAddresses();
                for (; a.hasMoreElements(); )
                {
                    InetAddress addr = a.nextElement();
                    if ( !addr.getHostAddress().startsWith( "10" ) && addr.isSiteLocalAddress() )
                    {
                        return ( addr.getHostName() );
                    }
                }
            }
        }
        catch ( SocketException e )
        {
            System.out.println( e.getMessage() );
        }


        return "127.0.0.1";
    }


    public Collection<PeerMessageListener> getPeerMessageListeners()
    {
        return Collections.unmodifiableCollection( peerMessageListeners );
    }
}
