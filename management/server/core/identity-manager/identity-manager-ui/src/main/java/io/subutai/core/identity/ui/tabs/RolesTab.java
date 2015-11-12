package io.subutai.core.identity.ui.tabs;


import com.vaadin.server.Page;
import com.vaadin.ui.*;

import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.Role;
import io.subutai.core.identity.ui.tabs.subviews.RoleForm;


import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;


import java.util.List;


public class RolesTab extends CustomComponent implements TabCallback<BeanItem<Role>>
{
    private enum FormState
    {
        STATE_EXISTING_ENTITY_SELECTED,
        STATE_SAVE_EXISTING_ENTITY,
        STATE_SAVE_NEW_ENTITY,
        STATE_REMOVE_ENTITY,
        STATE_NEW_ENTITY,
        STATE_CANCEL
    }


    private IdentityManager identityManager;
    private Table rolesTable;
    private BeanItemContainer<Role> beans;
    private Button newBean;
    private RoleForm form;
    private BeanItem<Role> setRole;


    public RolesTab( final IdentityManager identityManager )
    {
        this.identityManager = identityManager;
        editorForm();
    }


    void editorForm()
    {
        VerticalLayout vlayout = new VerticalLayout();

        // Create a container for such beans
        // Add some beans to it
        beans = new BeanItemContainer<>( Role.class );

        beans.addAll( identityManager.getAllRoles() );
        //beans.addNestedContainerProperty( "permissionGroup.name" );


        // Bind a table to it
        rolesTable = new Table( "Permissions", beans );
        rolesTable.setVisibleColumns( new Object[] { "id", "name" } );
        rolesTable.setPageLength( 7 );
        rolesTable.setColumnHeader( "id", "id" );
        rolesTable.setColumnHeader( "name", "Name" );
        rolesTable.setBuffered( false );


        // When the user selects an item, show it in the form
        rolesTable.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                // Close the form if the item is deselected
                if ( event.getProperty().getValue() == null )
                {
                    form.setVisible( false );
                    return;
                }
                setRole = new BeanItem<>( beans.getItem( rolesTable.getValue() ).getBean() );
                refreshControls( FormState.STATE_EXISTING_ENTITY_SELECTED );
                //                rolesTable.select( null );
            }
        } );
        rolesTable.setSelectable( true );
        rolesTable.setImmediate( true );

        // Creates a new bean for editing in the form before adding
        // it to the table. Adding is handled after committing
        // the form.
        newBean = new Button( "New role" );
        newBean.addClickListener( new Button.ClickListener()
        {
            public void buttonClick( Button.ClickEvent event )
            {

                // Create a new item; this will create a new bean
                //BeanItem<Role> newPermission = new BeanItem<>( identityManager.createRole( "" ) );

                // The form was opened for editing a new item

                final Window subWindow = new Window( "New Role" );
                subWindow.setClosable( false );
                subWindow.center();
                VerticalLayout content = new VerticalLayout();
                content.setMargin( true );
                content.setSpacing( true );
                subWindow.setContent( content );

                final TextField newName = new TextField( "Name" );
                newName.setInputPrompt( "Enter new name" );
                final ComboBox newType = new ComboBox( "Type" );
                newType.setNullSelectionAllowed( false );
                newType.setTextInputAllowed( false );
                for ( int i = 0; i < 2; ++i )
                {
                    newType.addItem( i + 1 );
                    switch ( i )
                    {
                        case ( 0 ):
                        {
                            newType.setItemCaption( i + 1, "System" );
                            break;
                        }
                        case ( 1 ):
                        {
                            newType.setItemCaption( i + 1, "Regular" );
                        }
                    }
                }
                newType.setValue( 1 );
                HorizontalLayout fieldGrid = new HorizontalLayout();
                fieldGrid.setSpacing( true );
                fieldGrid.addComponent( newName );
                fieldGrid.addComponent( newType );
                content.addComponent( fieldGrid );

                Button close = new Button( "Close" );
                close.addClickListener( new Button.ClickListener()
                {
                    @Override
                    public void buttonClick( Button.ClickEvent clickEvent )
                    {
                        subWindow.close();
                    }
                } );
                Button create = new Button( "Create" );
                create.addClickListener( new Button.ClickListener()
                {
                    @Override
                    public void buttonClick( Button.ClickEvent clickEvent )
                    {
                        boolean exists = false;
                        List<Role> roles = identityManager.getIdentityDataService().getAllRoles();
                        Long newId = new Long( 1 );
                        for ( Role r : roles )
                        {
                            ++newId;
                            if ( r.getName().equals( newName.getValue() ) )
                            {
                                exists = true;
                                break;
                            }
                        }
                        if ( !exists )
                        {
                            identityManager.getIdentityDataService()
                                           .persistRoleByName( newName.getValue(), ( int ) newType.getValue() );
                            Role newRole = identityManager.getIdentityDataService().getRole( newId );
                            beans.addBean( newRole );
                            subWindow.close();
                            Notification notif = new Notification( "Role successfully added" );
                            notif.setDelayMsec( 2000 );
                            notif.show( Page.getCurrent() );
                        }
                        else
                        {
                            Notification notif = new Notification( "Role with such name already exists" );
                            notif.setDelayMsec( 2000 );
                            notif.show( Page.getCurrent() );
                        }
                    }
                } );
                HorizontalLayout buttonGrid = new HorizontalLayout();
                buttonGrid.setSpacing( true );
                buttonGrid.addComponent( close );
                buttonGrid.addComponent( create );
                buttonGrid.setComponentAlignment( close, Alignment.BOTTOM_CENTER );
                buttonGrid.setComponentAlignment( create, Alignment.BOTTOM_CENTER );
                content.addComponent( buttonGrid );
                content.setComponentAlignment( buttonGrid, Alignment.BOTTOM_CENTER );
                UI.getCurrent().addWindow( subWindow );
                // TODO: switch to the method of invoking window shown below
                //refreshControls( FormState.STATE_NEW_ENTITY );


                // Make the form a bit nicer
                //this is an example for future how to improve UI
                //form.setRole( newPermission, true );
            }
        } );


        vlayout.setSpacing( true );
        vlayout.setMargin( true );
        vlayout.addComponent( newBean );
        vlayout.addComponent( rolesTable );

        setCompositionRoot( vlayout );
    }


    //make centralized modification and controls updates
    //state:
    //  0: existing entity selected in table
    //  1: on save event existing one
    //  2: on save event new entity
    //  3: on remove event
    //  4: on new entity button click
    private void refreshControls( FormState state )
    {
        switch ( state )
        {
            case STATE_EXISTING_ENTITY_SELECTED:
                form = new RoleForm( this );
                form.setRole( setRole );
                newBean.setEnabled( false );
                UI.getCurrent().addWindow( form );
                rolesTable.setEnabled( false );
                break;
            case STATE_SAVE_EXISTING_ENTITY:
            case STATE_SAVE_NEW_ENTITY:
                newBean.setEnabled( true );
                rolesTable.setEnabled( true );
                form.close();
                break;
            case STATE_REMOVE_ENTITY:
                newBean.setEnabled( true );
                form.close();
                rolesTable.setEnabled( true );
                break;
            case STATE_NEW_ENTITY:
                form = new RoleForm( this );
                form.setRole( setRole );
                UI.getCurrent().addWindow( form );
                newBean.setEnabled( false );
                rolesTable.setEnabled( false );
                break;
            case STATE_CANCEL:
                form.close();
                newBean.setEnabled( true );
                rolesTable.setEnabled( true );
                break;
        }
    }


    @Override
    public void saveOperation( final BeanItem<Role> value, final boolean newValue )
    {
        identityManager.updateRole( value.getBean() );
        if ( newValue )
        {
            beans.addBean( value.getBean() );
            refreshControls( FormState.STATE_SAVE_NEW_ENTITY );
        }
        else
        {
            refreshControls( FormState.STATE_SAVE_EXISTING_ENTITY );
        }
    }


    @Override
    public void removeOperation( final BeanItem<Role> value, final boolean newValue )
    {
        if ( !newValue )
        {
            identityManager.removeRole( value.getBean().getId() );
            beans.removeItem( value.getBean() );
        }
        refreshControls( FormState.STATE_REMOVE_ENTITY );
    }


    @Override
    public void cancelOperation()
    {
        refreshControls( FormState.STATE_CANCEL );
    }


    public IdentityManager getIdentityManager()
    {
        return this.identityManager;
    }
}
