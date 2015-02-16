package org.safehaus.subutai.core.template.wizard.ui;


import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.CommandStatus;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.peer.api.HostNotFoundException;
import org.safehaus.subutai.core.peer.api.LocalPeer;
import org.safehaus.subutai.core.peer.api.ResourceHost;
import org.safehaus.subutai.core.peer.api.ResourceHostException;
import org.safehaus.subutai.server.ui.component.ProgressWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import com.vaadin.data.Validator;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.validator.AbstractStringValidator;
import com.vaadin.event.FieldEvents;
import com.vaadin.server.Sizeable;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;


/**
 * Created by talas on 2/10/15.
 */
public class TemplateWizardComponent extends CustomComponent
{
    private static final Logger LOGGER = LoggerFactory.getLogger( TemplateWizardComponent.class );

    private LocalPeer localPeer;
    private ComboBox parentTemplateComboBox;
    private ComboBox resourceHostComboBox;
    private TextField templateName;
    private TextArea commandsCollection;
    private Button createTemplateButton;
    private TemplateWizardPortalModule portalModule;


    public TemplateWizardComponent( TemplateWizardPortalModule portalModule )
    {
        setHeight( 100, Sizeable.Unit.PERCENTAGE );

        localPeer = portalModule.getPeerManager().getLocalPeer();
        this.portalModule = portalModule;

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing( true );
        verticalLayout.setMargin( new MarginInfo( 10 ) );
        //        verticalLayout.setSizeFull();

        BeanContainer<String, Template> templateBeanContainer = new BeanContainer<>( Template.class );
        templateBeanContainer.setBeanIdProperty( "md5sum" );
        templateBeanContainer.addAll( portalModule.getTemplateRegistry().getAllTemplates() );

        parentTemplateComboBox = new ComboBox( "Parent template", templateBeanContainer );
        parentTemplateComboBox.setItemCaptionMode( AbstractSelect.ItemCaptionMode.PROPERTY );
        parentTemplateComboBox.setItemCaptionPropertyId( "templateName" );
        parentTemplateComboBox.setNullSelectionAllowed( false );
        parentTemplateComboBox.setImmediate( true );
        parentTemplateComboBox.setTextInputAllowed( false );
        parentTemplateComboBox.setRequired( true );

        final BeanContainer<String, ResourceHost> resourceHostBeanContainer = new BeanContainer<>( ResourceHost.class );
        resourceHostBeanContainer.setBeanIdProperty( "hostname" );
        resourceHostBeanContainer.addAll( localPeer.getResourceHosts() );

        resourceHostComboBox = new ComboBox( "Parent template", resourceHostBeanContainer );
        resourceHostComboBox.setItemCaptionMode( AbstractSelect.ItemCaptionMode.PROPERTY );
        resourceHostComboBox.setItemCaptionPropertyId( "hostname" );
        resourceHostComboBox.setNullSelectionAllowed( false );
        resourceHostComboBox.setImmediate( true );
        resourceHostComboBox.setTextInputAllowed( false );
        resourceHostComboBox.setRequired( true );

        templateName = new TextField( "New template name" );
        templateName.setRequired( true );
        templateName.addValidator(
                new AbstractStringValidator( "New template name must be unique and at least 4 chars long." )
                {
                    @Override
                    protected boolean isValidValue( final String value )
                    {
                        try
                        {
                            if ( localPeer.getContainerHostByName( value ) != null )
                            {
                                return false;
                            }
                            if ( TemplateWizardComponent.this.portalModule.getTemplateRegistry().getTemplate( value )
                                    != null )
                            {
                                return false;
                            }
                        }
                        catch ( HostNotFoundException e )
                        {
                            LOGGER.debug( Marker.ANY_MARKER, "Couldn't get container host by name", e );
                        }
                        return true;
                    }
                } );

        commandsCollection = new TextArea( "Input commands for template configuration." );
        commandsCollection.setWidth( "40%" );

        createTemplateButton = new Button( "Create template", createTemplateListener );

        verticalLayout.addComponent( parentTemplateComboBox );
        verticalLayout.addComponent( resourceHostComboBox );
        verticalLayout.addComponent( templateName );
        verticalLayout.addComponent( commandsCollection );
        verticalLayout.addComponent( createTemplateButton );

        setCompositionRoot( verticalLayout );
    }


    private RequestBuilder cloneTemplateRequestBuilder( String templateName, String containerName )
    {
        return new RequestBuilder( String.format( "subutai clone %s %s", templateName, containerName ) )
                .withTimeout( 90 );
    }


    private RequestBuilder promoteRequestBuilder( String containerName )
    {
        return new RequestBuilder( String.format( "subutai promote %s", containerName ) ).withTimeout( 90 );
    }


    private RequestBuilder exportRequestBuilder( String templateName )
    {
        return new RequestBuilder( String.format( "subutai export %s", templateName ) ).withTimeout( 90 );
    }


    private RequestBuilder registerRequestBuilder( String templateName )
    {
        return new RequestBuilder( String.format( "subutai register %s", templateName ) ).withTimeout( 90 );
    }


    private void initTemplateCreationProcess( final TrackerOperation trackerOperation, final ResourceHost resourceHost )
    {
        BeanItem beanItem = ( BeanItem ) parentTemplateComboBox.getItem( parentTemplateComboBox.getValue() );
        Template template = ( Template ) beanItem.getBean();

        ContainerHost templateHost = null;
        try
        {
            templateHost = resourceHost.createContainer( template.getTemplateName(), templateName.getValue(), 90 );
        }
        catch ( ResourceHostException e )
        {
            LOGGER.error( "Error creating container.", e );
        }

        if ( templateHost == null )
        {
            try
            {
                resourceHost.importTemplate( template );
            }
            catch ( ResourceHostException e )
            {
                LOGGER.error( "Error importing template", e );
            }
        }

        try
        {
            templateHost = resourceHost.createContainer( template.getTemplateName(), templateName.getValue(), 90 );
        }
        catch ( ResourceHostException e )
        {
            LOGGER.error( "Error creating container.", e );
        }

        if ( templateHost == null )
        {
            return;
        }

        String commands[] = commandsCollection.getValue().split( "\n" );
        for ( final String command : commands )
        {

            try
            {
                CommandResult commandResult = templateHost.execute( new RequestBuilder( command ) );
                trackerOperation.addLog( commandResult.getStdOut() );
            }
            catch ( CommandException e )
            {
                LOGGER.error( "Error executing command: " + command, e );
                trackerOperation.addLogFailed( "Error executing command." );
            }
        }

        try
        {
            CommandResult commandResult = resourceHost.execute( promoteRequestBuilder( templateName.getValue() ) );
            if ( commandResult.getStatus() == CommandStatus.FAILED
                    || commandResult.getStatus() == CommandStatus.TIMEOUT )
            {
                trackerOperation.addLogFailed( "Error promoting container." );
                return;
            }
            else
            {
                trackerOperation.addLog( "Successfully promoted container host." );
            }
        }
        catch ( CommandException e )
        {
            LOGGER.error( "Error promoting command.", e );
            trackerOperation.addLogFailed( "Error promoting command." );
        }

        try
        {
            CommandResult exportCommandResult = resourceHost.execute( exportRequestBuilder( templateName.getValue() ) );

            if ( ( exportCommandResult.getStatus() == CommandStatus.TIMEOUT
                    || exportCommandResult.getStatus() == CommandStatus.FAILED ) )
            {
                trackerOperation.addLogFailed( "Error exporting container." );
                return;
            }
            else
            {
                trackerOperation.addLog( "Successfully exported template." );
            }
        }
        catch ( CommandException e )
        {
            LOGGER.error( "Error exporting command.", e );
            trackerOperation.addLogFailed( "Error exporting command." );
        }

        try
        {
            CommandResult registerCommandResult =
                    resourceHost.execute( registerRequestBuilder( templateName.getValue() ) );
            if ( registerCommandResult.getStatus() == CommandStatus.FAILED
                    || registerCommandResult.getStatus() == CommandStatus.TIMEOUT )
            {
                trackerOperation.addLogFailed( "Error registering container." );
                return;
            }
            else
            {
                trackerOperation.addLog( "Successfully registered template." );
            }
        }
        catch ( CommandException e )
        {
            LOGGER.error( "Error registering command.", e );
            trackerOperation.addLogFailed( "Error registering command." );
        }
        trackerOperation.addLogDone( "Successfully created template." );
    }


    private Button.ClickListener createTemplateListener = new Button.ClickListener()
    {
        @Override
        public void buttonClick( final Button.ClickEvent event )
        {
            if ( parentTemplateComboBox.getValue() == null )
            {
                Notification.show( "Please select parent template." );
                return;
            }

            if ( resourceHostComboBox.getValue() == null )
            {
                Notification.show( "Please select resource host." );
                return;
            }

            try
            {
                templateName.validate();
            }
            catch ( Validator.InvalidValueException e )
            {
                Notification.show( e.getMessage() );
                return;
            }

            BeanItem resourceHostItem = ( BeanItem ) resourceHostComboBox.getItem( resourceHostComboBox.getValue() );
            final ResourceHost resourceHost = ( ResourceHost ) resourceHostItem.getBean();
            if ( resourceHost != null )
            {
                final TrackerOperation trackerOperation = portalModule.getTracker()
                                                                      .createTrackerOperation( "TemplateWizard",
                                                                              "Started template creation." );

                ProgressWindow progressWindow =
                        new ProgressWindow( portalModule.getExecutor(), portalModule.getTracker(),
                                trackerOperation.getId(), "TemplateWizard" );
                progressWindow.getWindow().addCloseListener( new Window.CloseListener()
                {
                    @Override
                    public void windowClose( final Window.CloseEvent e )
                    {

                        parentTemplateComboBox.select( null );
                        resourceHostComboBox.select( null );
                        templateName.setValue( "" );
                        commandsCollection.setValue( "" );
                    }
                } );

                progressWindow.getWindow().addFocusListener( new FieldEvents.FocusListener()
                {
                    @Override
                    public void focus( final FieldEvents.FocusEvent event )
                    {
                        initTemplateCreationProcess( trackerOperation, resourceHost );
                    }
                } );
                getUI().addWindow( progressWindow.getWindow() );
                progressWindow.getWindow().focus();
            }
        }
    };
}