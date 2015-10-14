package io.subutai.core.identity.ui.tabs;


import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.Permission;
import io.subutai.core.identity.ui.tabs.subviews.PermissionForm;

import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;


public class PermissionsTab extends CustomComponent implements TabCallback<BeanItem<Permission>>
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
    private Table permissionsTable;
    private BeanItemContainer<Permission> beans;
    private Button newBean;
    private PermissionForm form;


    public PermissionsTab( final IdentityManager identityManager )
    {
        this.identityManager = identityManager;
        editorForm();
    }


    void editorForm()
    {
        VerticalLayout vlayout = new VerticalLayout();

        // Create a container for such beans
        // Add some beans to it
        beans = new BeanItemContainer<>( Permission.class );

        //beans.addAll( identityManager.getAllPermissions() );
        beans.addNestedContainerProperty( "permissionGroup.name" );

        // A layout for the table and form
        HorizontalLayout layout = new HorizontalLayout();

        // Bind a table to it
        permissionsTable = new Table( "Permissions", beans );
        permissionsTable.setVisibleColumns( new Object[] { "name", "permissionGroup.name", "description" } );
        permissionsTable.setPageLength( 7 );
        permissionsTable.setColumnHeader( "name", "Name" );
        permissionsTable.setColumnHeader( "permissionGroup.name", "PermissionGroup" );
        permissionsTable.setColumnHeader( "description", "Description" );
        permissionsTable.setBuffered( false );

        // Create a form for editing a selected or new item.
        // It is invisible until actually used.
        form = new PermissionForm( this );
        form.setVisible( false );

        // When the user selects an item, show it in the form
        permissionsTable.addValueChangeListener( new Property.ValueChangeListener()
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
                BeanItem<Permission> permission = beans.getItem( permissionsTable.getValue() );
                form.setPermission( permission, false );
                refreshControls( FormState.STATE_EXISTING_ENTITY_SELECTED );
                //                permissionsTable.select( null );
            }
        } );
        permissionsTable.setSelectable( true );
        permissionsTable.setImmediate( true );

        // Creates a new bean for editing in the form before adding
        // it to the table. Adding is handled after committing
        // the form.
        newBean = new Button( "New Permission" );
        newBean.addClickListener( new Button.ClickListener()
        {
            public void buttonClick( Button.ClickEvent event )
            {

                // Create a new item; this will create a new bean
                //BeanItem<Permission> newPermission = new BeanItem<>(
                        //identityManager.createPermission( "", PermissionGroup.DEFAULT_PERMISSIONS, "" ) );

                // The form was opened for editing a new item
                refreshControls( FormState.STATE_NEW_ENTITY );


                // Make the form a bit nicer
                //this is an example for future how to improve UI
                //form.setPermission( newPermission, true );
            }
        } );

        layout.addComponent( permissionsTable );
        layout.addComponent( form );

        layout.setSpacing( true );

        vlayout.addComponent( layout );
        vlayout.addComponent( newBean );

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
                newBean.setEnabled( false );
                form.setVisible( true );
                break;
            case STATE_SAVE_EXISTING_ENTITY:
                newBean.setEnabled( false );
                break;
            case STATE_SAVE_NEW_ENTITY:
                newBean.setEnabled( true );
                permissionsTable.setEnabled( true );
                form.setVisible( false );
                break;
            case STATE_REMOVE_ENTITY:
                newBean.setEnabled( true );
                form.setVisible( false );
                break;
            case STATE_NEW_ENTITY:
                form.setVisible( true );
                newBean.setEnabled( false );
                permissionsTable.setEnabled( false );
                //                permissionsTable.select( null );
                break;
            case STATE_CANCEL:
                form.setVisible( false );
                newBean.setEnabled( true );
                permissionsTable.setEnabled( true );
                break;
        }
    }


    @Override
    public void saveOperation( final BeanItem<Permission> value, final boolean newValue )
    {
        //identityManager.updatePermission( value.getBean() );
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
    public void removeOperation( final BeanItem<Permission> value, final boolean newValue )
    {
        if ( !newValue )
        {
            //identityManager.deletePermission( value.getBean() );
            beans.removeItem( value.getBean() );
        }
        refreshControls( FormState.STATE_REMOVE_ENTITY );
    }


    @Override
    public void cancelOperation()
    {
        refreshControls( FormState.STATE_CANCEL );
    }
}
