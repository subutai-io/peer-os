package org.safehaus.subutai.core.template.wizard.impl;


import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.peer.api.HostNotFoundException;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.peer.api.ResourceHost;
import org.safehaus.subutai.core.peer.api.ResourceHostException;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;
import org.safehaus.subutai.core.template.wizard.api.InstallationPhase;
import org.safehaus.subutai.core.template.wizard.api.TemplateWizardManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;


public class TemplateWizardManagerImpl implements TemplateWizardManager
{
    private static final Logger LOGGER = LoggerFactory.getLogger( TemplateWizardManagerImpl.class );


    private volatile InstallationPhase currentPhase;


    private volatile List<String> products = Lists.newArrayList();


    private String newTemplateName = "newTemplateName";


    private List<String> postInstallationScripts = Lists.newArrayList();


    private List<String> preInstallationScripts = Lists.newArrayList();


    private ResourceHost resourceHost;


    private Template template;


    private PeerManager peerManager;


    private TemplateRegistry templateRegistry;


    private TrackerOperation trackerOperation;

    private volatile ContainerHost containerHost;


    @Override
    public InstallationPhase getCurrentPhase()
    {
        return currentPhase;
    }


    @Override
    public void createTemplate( final String newTemplateName, final String templateName,
                                final List<String> postInstallationScripts, final List<String> products,
                                final List<String> preInstallationScripts, final UUID resourceHostId,
                                final TrackerOperation trackerOperation )
    {
        Preconditions.checkNotNull( newTemplateName, "Invalid parameter value for newTemplateName" );
        Preconditions.checkNotNull( templateName, "Invalid parameter value for templateName" );
        Preconditions.checkNotNull( postInstallationScripts, "Invalid parameter value for postInstallationScripts" );
        Preconditions.checkNotNull( products, "Invalid parameter value for products" );
        Preconditions.checkNotNull( preInstallationScripts, "Invalid parameter value for preInstallationScripts" );
        Preconditions.checkNotNull( resourceHostId, "Invalid parameter value for resourceHostId" );
        Preconditions.checkNotNull( trackerOperation, "Invalid parameter value for trackerOperation" );

        this.newTemplateName = newTemplateName;
        this.postInstallationScripts = postInstallationScripts;
        this.preInstallationScripts = preInstallationScripts;
        this.products = products;
        this.template = templateRegistry.getTemplate( templateName );
        this.trackerOperation = trackerOperation;

        try
        {
            this.resourceHost = peerManager.getLocalPeer().getResourceHostById( resourceHostId );
        }
        catch ( HostNotFoundException e )
        {
            LOGGER.error( "Error getting resourceHost", e );
        }

        triggerTemplateCreation();
    }


    @Override
    public void createContainerHost( final String newTemplateName, final String templateName )
    {
        Preconditions.checkNotNull( newTemplateName, "Invalid parameter value for newTemplateName" );
        Preconditions.checkNotNull( templateName, "Invalid parameter value for templateName" );

        this.newTemplateName = newTemplateName;
        this.template = templateRegistry.getTemplate( templateName );

        createContainerHost( newTemplateName );
    }


    @Override
    public void installProducts( final List<String> products )
    {
        this.products = products;
        installProducts( containerHost );
    }


    private void triggerTemplateCreation()
    {
        // #1 Create ContainerHost
        containerHost = createContainerHost( newTemplateName );

        if ( containerHost == null )
        {
            return;
        }

        // #2 Execute Pre-Products installation scripts by saving them into temporary executable file, execute them

        // #3 Install products
        installProducts( containerHost );

        // #4 Execute Post-Products installation scripts by saving them into temporary executable file, execute them


        // #5 Promote containerHost
        CommandResult result = promoteContainerHost( newTemplateName );

        // #6 Export template
        if ( result != null )
        {
            result = exportTemplate( newTemplateName );
        }
        // #7 Register template

        if ( result != null )
        {
            result = registerTemplate( newTemplateName );
        }
        // #8 Notify User completeness status
        if ( result == null )
        {
            //notify fail
        }
        else
        {
            //notify success
        }
    }


    private CommandResult registerTemplate( final String templateName )
    {
        try
        {
            trackerOperation.addLog( "Registering template" );
            String cmd = String.format( "subutai register %s", templateName );
            RequestBuilder requestBuilder = new RequestBuilder( cmd );
            requestBuilder.withTimeout( 180 );
            return resourceHost.execute( requestBuilder );
        }
        catch ( CommandException e )
        {
            LOGGER.error( "Error executing registration command.", e );
            trackerOperation.addLogFailed( "Error executing registration command." );
            return null;
        }
    }


    private CommandResult exportTemplate( final String templateName )
    {
        try
        {
            trackerOperation.addLog( "Exporting template" );
            String cmd = String.format( "subutai export %s", templateName );
            RequestBuilder requestBuilder = new RequestBuilder( cmd );
            requestBuilder.withTimeout( 180 );
            return resourceHost.execute( requestBuilder );
        }
        catch ( CommandException e )
        {
            LOGGER.error( "Error executing export command.", e );
            trackerOperation.addLogFailed( "Error executing export command." );
            return null;
        }
    }


    private ContainerHost createContainerHost( String containerName )
    {
        ContainerHost containerHost = null;
        try
        {
            trackerOperation.addLog( "Creating container host" );
            containerHost = resourceHost
                    .createContainer( template.getTemplateName(),/* Arrays.asList( template ),*/ containerName, 90 );
        }
        catch ( ResourceHostException e )
        {
            //            trackerOperation.addLogFailed( "Failed to create container host" );
            LOGGER.warn( "Error creating container.", e );
        }
        if ( containerHost == null )
        {
//            importTemplateTree( template );
            containerHost = createContainerHost( containerName );
        }
        return containerHost;
    }
//
//
//    private void importTemplateTree( Template template )
//    {
//        try
//        {
//            resourceHost.importTemplate( template );
//        }
//        catch ( ResourceHostException e )
//        {
//            Template parent = templateRegistry
//                    .getParentTemplate( template.getTemplateName(), template.getTemplateVersion(),
//                            template.getLxcArch() );
//            if ( parent == null )
//            {
//                throw new RuntimeException( "Couldn't construct template tree" );
//            }
//            importTemplateTree( parent );
//            importTemplateTree( template );
//        }
//    }


    private void installProducts( ContainerHost containerHost )
    {
        try
        {
            trackerOperation.addLog( "Installing products" );
            ProductsInstallationProcedure installationProcedure =
                    new ProductsInstallationProcedure( products, containerHost );

            installationProcedure.doStart();
            if ( installationProcedure.isStopped() )
            {
                trackerOperation.addLog( "Products successfully installed" );
                LOGGER.debug( "Successfully installed products on host" );
            }
        }
        catch ( Exception e )
        {
            trackerOperation.addLogFailed( "Error installing products." );
            LOGGER.error( "Error installing products.", e );
            throw new RuntimeException( "Error installing products.", e );
        }
    }


    private CommandResult promoteContainerHost( String containerHostName )
    {
        try
        {
            trackerOperation.addLog( "Promoting container host" );
            String cmd = String.format( "subutai promote %s", containerHostName );
            RequestBuilder requestBuilder = new RequestBuilder( cmd );
            requestBuilder.withTimeout( 90 );
            return resourceHost.execute( requestBuilder );
        }
        catch ( CommandException e )
        {
            LOGGER.error( "Error executing promotion command.", e );
            trackerOperation.addLogFailed( "Error executing promotion command." );
            return null;
        }
    }


    @Override
    public void preInstallationScripts( final List<String> scripts )
    {

    }


    @Override
    public void postInstallationScripts( final List<String> scripts )
    {

    }


    public PeerManager getPeerManager()
    {
        return peerManager;
    }


    public void setPeerManager( final PeerManager peerManager )
    {
        this.peerManager = peerManager;
    }


    public TemplateRegistry getTemplateRegistry()
    {
        return templateRegistry;
    }


    public void setTemplateRegistry( final TemplateRegistry templateRegistry )
    {
        this.templateRegistry = templateRegistry;
    }
}
