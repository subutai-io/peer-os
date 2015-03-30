package org.safehaus.subutai.core.identity.ui.tabs.subviews;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.safehaus.subutai.core.identity.api.CliCommand;
import org.safehaus.subutai.core.identity.api.Permission;
import org.safehaus.subutai.core.identity.api.PortalModuleScope;
import org.safehaus.subutai.core.identity.api.RestEndpointScope;
import org.safehaus.subutai.core.identity.api.Role;
import org.safehaus.subutai.core.identity.ui.tabs.TabCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.TwinColSelect;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;


public class RoleForm extends VerticalLayout
{

    TabCallback<BeanItem<Role>> callback;

    private static final Logger LOGGER = LoggerFactory.getLogger( RoleForm.class );

    private BeanFieldGroup<Role> permissionFieldGroup = new BeanFieldGroup<>( Role.class );

    private boolean newValue;
    private TextField name = new TextField()
    {
        {
            setInputPrompt( "Role name" );
            setEnabled( false );
            setRequired( true );
        }
    };

    Button removeButton = new Button( "Remove role", new Button.ClickListener()
    {
        @Override
        public void buttonClick( final Button.ClickEvent event )
        {
            permissionFieldGroup.discard();
            if ( callback != null )
            {
                callback.removeOperation( permissionFieldGroup.getItemDataSource(), newValue );
            }
        }
    } );


    private TwinColSelect restEndpointsSelector = new TwinColSelect( "Accessible rest endpoints" )
    {
        {
            setItemCaptionMode( ItemCaptionMode.PROPERTY );
            setItemCaptionPropertyId( "restEndpoint" );
            setWidth( 600, Unit.PIXELS );
            setImmediate( true );
            setSpacing( true );
            setRequired( true );
            setNullSelectionAllowed( false );
        }
    };


    private TwinColSelect permissionsSelector = new TwinColSelect( "System Permissions" )
    {
        {
            setItemCaptionMode( ItemCaptionMode.PROPERTY );
            setItemCaptionPropertyId( "name" );
            setWidth( 400, Unit.PIXELS );
            setImmediate( true );
            setSpacing( true );
            setRequired( true );
            setVisible( false );
            setNullSelectionAllowed( false );
        }
    };


    private TwinColSelect modulesSelector = new TwinColSelect( "Accessible modules" )
    {
        {
            setItemCaptionMode( ItemCaptionMode.PROPERTY );
            setItemCaptionPropertyId( "moduleName" );
            setWidth( 400, Unit.PIXELS );
            setImmediate( true );
            setSpacing( true );
            setRequired( false );
            setNullSelectionAllowed( true );
        }
    };


    private TwinColSelect commandsSelector = new TwinColSelect( "Accessible cli commands" )
    {
        {
            setItemCaptionMode( ItemCaptionMode.PROPERTY );
            setItemCaptionPropertyId( "command" );
            setWidth( 500, Unit.PIXELS );
            setImmediate( true );
            setSpacing( true );
            setRequired( false );
            setNullSelectionAllowed( true );
        }
    };


    public RoleForm( TabCallback<BeanItem<Role>> callback, Set<Permission> permissions,
                     final Set<PortalModuleScope> allPortalModules, final Set<RestEndpointScope> allRestEndpoints,
                     final List<CliCommand> allCliCommands )
    {
        init();
        BeanContainer<String, Permission> permissionsContainer = new BeanContainer<>( Permission.class );
        permissionsContainer.setBeanIdProperty( "name" );
        permissionsContainer.addAll( permissions );
        permissionsSelector.setContainerDataSource( permissionsContainer );
        permissionsSelector.setItemCaptionPropertyId( "name" );

        BeanContainer<String, PortalModuleScope> modulesContainer = new BeanContainer<>( PortalModuleScope.class );
        modulesContainer.setBeanIdProperty( "moduleKey" );
        modulesContainer.addAll( allPortalModules );
        modulesSelector.setContainerDataSource( modulesContainer );
        modulesSelector.setItemCaptionPropertyId( "moduleName" );

        BeanContainer<String, RestEndpointScope> restEndpointsContainer =
                new BeanContainer<>( RestEndpointScope.class );
        restEndpointsContainer.setBeanIdProperty( "restEndpoint" );
        restEndpointsContainer.addAll( allRestEndpoints );
        restEndpointsSelector.setContainerDataSource( restEndpointsContainer );
        restEndpointsSelector.setItemCaptionPropertyId( "restEndpoint" );

        BeanContainer<String, CliCommand> cliCommandBeanContainer = new BeanContainer<>( CliCommand.class );
        cliCommandBeanContainer.setBeanIdProperty( "command" );
        cliCommandBeanContainer.addAll( allCliCommands );
        commandsSelector.setContainerDataSource( cliCommandBeanContainer );
        commandsSelector.setItemCaptionPropertyId( "command" );

        this.callback = callback;
    }


    private void init()
    {
        final Button saveButton = new Button( "Save role", saveListener );
        final Button cancelButton = new Button( "Cancel", cancelListener );
        saveButton.setStyleName( Reindeer.BUTTON_DEFAULT );

        HorizontalLayout buttons = new HorizontalLayout( saveButton, cancelButton, removeButton );
        buttons.setSpacing( true );

        HorizontalLayout selectorsGroupA = new HorizontalLayout( permissionsSelector, modulesSelector );
        selectorsGroupA.setSpacing( true );

        HorizontalLayout selectorsGroupB = new HorizontalLayout( restEndpointsSelector, commandsSelector );
        selectorsGroupB.setSpacing( true );

        final FormLayout form = new FormLayout();
        form.addComponents( name, selectorsGroupA, selectorsGroupB );

        addComponents( form, buttons );

        setSpacing( true );
    }


    public void setRole( final BeanItem<Role> role, boolean newValue )
    {
        this.newValue = newValue;
        if ( role != null )
        {
            permissionFieldGroup.setItemDataSource( role );

            permissionFieldGroup.bind( name, "name" );

            // Pre-select role permissions
            Role roleBean = role.getBean();
            Set<String> permissionNames = new HashSet<>();

            for ( final Permission permission : roleBean.getPermissions() )
            {
                permissionNames.add( permission.getName() );
            }
            permissionsSelector.setValue( permissionNames );

            Set<String> modules = new HashSet<>();
            for ( final PortalModuleScope portalModuleScope : roleBean.getAccessibleModules() )
            {
                modules.add( portalModuleScope.getModuleName() );
            }
            modulesSelector.setValue( modules );


            List<String> endpoints = new ArrayList<>();
            for ( final RestEndpointScope endpointScope : roleBean.getAccessibleRestEndpoints() )
            {
                endpoints.add( endpointScope.getRestEndpoint() );
            }
            restEndpointsSelector.setValue( endpoints );

            Set<String> cliCommands = new HashSet<>();
            for ( final CliCommand cliCommand : roleBean.getCliCommands() )
            {
                cliCommands.add( cliCommand.getCommand() );
            }
            commandsSelector.setValue( cliCommands );

            if ( !newValue )
            {
                permissionFieldGroup.setReadOnly( true );
                removeButton.setVisible( true );
            }
            else
            {
                permissionFieldGroup.setReadOnly( false );
                removeButton.setVisible( false );
            }
        }
    }


    // When OK button is clicked, commit the form to the bean
    private Button.ClickListener saveListener = new Button.ClickListener()
    {
        @Override
        public void buttonClick( final Button.ClickEvent event )
        {
            // New items have to be added to the container
            // Commit the addition
            try
            {
                permissionFieldGroup.commit();
                if ( callback != null )
                {
                    // Set selected permissions for role
                    Collection<String> selectedPermissions = ( Collection<String> ) permissionsSelector.getValue();
                    Role role = permissionFieldGroup.getItemDataSource().getBean();
                    for ( final String permissionId : selectedPermissions )
                    {
                        BeanItem beanItem = ( BeanItem ) permissionsSelector.getItem( permissionId );
                        role.addPermission( ( Permission ) beanItem.getBean() );
                    }

                    role.clearPortalModules();
                    Collection<String> selectedModuleNames = ( Collection<String> ) modulesSelector.getValue();
                    for ( final String moduleName : selectedModuleNames )
                    {
                        BeanItem beanItem = ( BeanItem ) modulesSelector.getItem( moduleName );
                        role.addPortalModule( ( PortalModuleScope ) beanItem.getBean() );
                    }


                    role.clearRestEndpointScopes();
                    Collection<String> selectedRestEndpoints = ( Collection<String> ) restEndpointsSelector.getValue();
                    for ( final String moduleName : selectedRestEndpoints )
                    {
                        BeanItem beanItem = ( BeanItem ) restEndpointsSelector.getItem( moduleName );
                        role.addRestEndpointScope( ( RestEndpointScope ) beanItem.getBean() );
                    }

                    role.setCliCommands( Collections.<CliCommand>emptyList() );
                    Collection<String> selectedCommands = ( Collection<String> ) commandsSelector.getValue();
                    for ( final String command : selectedCommands )
                    {
                        BeanItem beanItem = ( BeanItem ) commandsSelector.getItem( command );
                        role.addCliCommand( ( CliCommand ) beanItem.getBean() );
                    }

                    callback.saveOperation( permissionFieldGroup.getItemDataSource(), newValue );
                    Notification.show( "Successfully saved." );
                }
            }
            catch ( FieldGroup.CommitException e )
            {
                LOGGER.error( "Error commit role fieldGroup changes", e );
                Notification.show( "Verify for fields correctness", Notification.Type.WARNING_MESSAGE );
            }
        }
    };


    private Button.ClickListener cancelListener = new Button.ClickListener()
    {
        @Override
        public void buttonClick( final Button.ClickEvent event )
        {
            permissionFieldGroup.discard();
            RoleForm.this.setVisible( false );
            if ( callback != null )
            {
                callback.cancelOperation();
            }
        }
    };
}
