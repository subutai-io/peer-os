package org.safehaus.subutai.core.template.wizard.ui;


import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.peer.api.HostNotFoundException;
import org.safehaus.subutai.core.peer.api.LocalPeer;
import org.safehaus.subutai.core.peer.api.ResourceHost;
import org.safehaus.subutai.server.ui.component.ProgressWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
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


public class TemplateWizardComponent extends CustomComponent
{
    private static final Logger LOGGER = LoggerFactory.getLogger( TemplateWizardComponent.class );

    private LocalPeer localPeer;
    private ComboBox parentTemplateComboBox;
    private ComboBox resourceHostComboBox;
    private TextField newTemplateName;
    private TextArea commandsCollection;
    private Button createTemplateButton;
    private TemplateWizardPortalModule portalModule;


    public TemplateWizardComponent( final TemplateWizardPortalModule portalModule )
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

        newTemplateName = new TextField( "New template name" );
        newTemplateName.setRequired( true );
        newTemplateName.addValidator(
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
                        catch ( HostNotFoundException ignore )
                        {
                            //                            LOGGER.debug( Marker.ANY_MARKER, "Couldn't get container
                            // host by name", e );
                        }
                        return true;
                    }
                } );

        commandsCollection = new TextArea( "Input commands for template configuration." );
        commandsCollection.setWidth( "40%" );

        createTemplateButton = new Button( "Create template", createTemplateListener );

        Button createContainerHost = new Button( "New container host", new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent event )
            {
                BeanItem beanItem = ( BeanItem ) parentTemplateComboBox.getItem( parentTemplateComboBox.getValue() );
                Template template = ( Template ) beanItem.getBean();
                portalModule.getTemplateWizard()
                            .createContainerHost( newTemplateName.getValue(), template.getTemplateName() );
            }
        } );

        Button installProducts = new Button( "Install Products", new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent event )
            {
                String products[] = commandsCollection.getValue().split( "\n" );
                portalModule.getTemplateWizard().installProducts( Lists.newArrayList( products ) );
            }
        } );

        verticalLayout.addComponent( parentTemplateComboBox );
        verticalLayout.addComponent( resourceHostComboBox );
        verticalLayout.addComponent( newTemplateName );
        verticalLayout.addComponent( commandsCollection );
        verticalLayout.addComponent( createTemplateButton );
        verticalLayout.addComponent( createContainerHost );

        setCompositionRoot( verticalLayout );
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
                newTemplateName.validate();
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

                final ProgressWindow progressWindow =
                        new ProgressWindow( portalModule.getExecutor(), portalModule.getTracker(),
                                trackerOperation.getId(), "TemplateWizard" );
                progressWindow.getWindow().addCloseListener( new Window.CloseListener()
                {
                    @Override
                    public void windowClose( final Window.CloseEvent e )
                    {

                        parentTemplateComboBox.select( null );
                        resourceHostComboBox.select( null );
                        newTemplateName.setValue( "" );
                        commandsCollection.setValue( "" );
                    }
                } );

                final FieldEvents.FocusListener focusListener = new FieldEvents.FocusListener()
                {
                    @Override
                    public void focus( final FieldEvents.FocusEvent event )
                    {
                        progressWindow.getWindow().removeFocusListener( this );
                        BeanItem beanItem =
                                ( BeanItem ) parentTemplateComboBox.getItem( parentTemplateComboBox.getValue() );
                        Template template = ( Template ) beanItem.getBean();
                        String products[] = commandsCollection.getValue().split( "\n" );
                        portalModule.getTemplateWizard()
                                    .createTemplate( newTemplateName.getValue(), template.getTemplateName(),
                                            Lists.newArrayList( "" ), Lists.newArrayList( products ),
                                            Lists.newArrayList( "" ), resourceHost.getId(), trackerOperation );
                        //                        initTemplateCreationProcess( trackerOperation, resourceHost );
                    }
                };
                progressWindow.getWindow().addFocusListener( focusListener );
                getUI().addWindow( progressWindow.getWindow() );
                progressWindow.getWindow().focus();
            }
        }
    };
}