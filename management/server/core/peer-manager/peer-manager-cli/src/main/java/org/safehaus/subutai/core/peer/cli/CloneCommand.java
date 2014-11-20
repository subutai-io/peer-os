package org.safehaus.subutai.core.peer.cli;


import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.peer.api.LocalPeer;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


@Command( scope = "peer", name = "clone" )
public class CloneCommand extends OsgiCommandSupport
{

    private PeerManager peerManager;
    private TemplateRegistry templateRegistry;


    public PeerManager getPeerManager()
    {
        return peerManager;
    }


    public void setPeerManager( final PeerManager peerManager )
    {
        this.peerManager = peerManager;
    }


    public void setTemplateRegistry( final TemplateRegistry templateRegistry )
    {
        this.templateRegistry = templateRegistry;
    }


    @Argument( index = 0, name = "envId", multiValued = false, description = "Environment UUID" )
    private String envId;

    @Argument( index = 1, name = "templateName", multiValued = false, description = "Remote template name" )
    private String templateName;

    @Argument( index = 2, name = "quantity", multiValued = false, description = "Number of containers to clone" )
    private int quantity;

    @Argument( index = 3, name = "strategyId", multiValued = false, description = "Container placement strategy" )
    private String strategyId;

    @Argument( index = 4, name = "nodeGroupName", multiValued = false, description = "Node group name" )
    private String nodeGroupName;


    @Override
    protected Object doExecute() throws Exception
    {

        LocalPeer localPeer = peerManager.getLocalPeer();


        UUID environmentId = UUID.fromString( envId );
        Template template = templateRegistry.getTemplate( templateName );
        List<Template> templates = templateRegistry.getParentTemplates( templateName );
        templates.add( template );
        Set<ContainerHost> containers = localPeer
                .createContainers( peerManager.getLocalPeer().getId(), environmentId, templates, quantity, strategyId,
                        null, nodeGroupName );

        System.out.println(
                String.format( "Containers successfully created.\nList of new %d containers:\n", containers.size() ) );
        System.out.println( String.format( "Hostname\tParent hostname\tAgent ID\tEnv ID" ) );
        for ( ContainerHost containerHost : containers )
        {
            System.out.println(
                    String.format( "%s\t%s\t%s\t%s", containerHost.getHostname(), containerHost.getParentHostname(),
                            containerHost.getId(), containerHost.getEnvironmentId() ) );
        }
        return null;
    }
}
