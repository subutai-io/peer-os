package org.safehaus.subutai.core.identity.ui.tabs;


import org.safehaus.subutai.core.identity.api.IdentityManager;
import org.safehaus.subutai.core.identity.api.Permission;
import org.safehaus.subutai.core.identity.api.PermissionGroup;
import org.safehaus.subutai.core.identity.ui.tabs.subviews.PermissionForm;

import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;


/**
 * Created by talas on 1/26/15.
 */
public class PermissionsTab extends CustomComponent implements TabCallback<BeanItem<Permission>>
{
    private IdentityManager identityManager;
    private Table permissionsTable;


    public PermissionsTab( final IdentityManager identityManager )
    {
        this.identityManager = identityManager;
        editorForm();
        // TODO still need some modifications
    }


    void editorForm()
    {
        VerticalLayout vlayout = new VerticalLayout();
        Form form = new Form();
        form.commit();

        // Create a container for such beans
        // Add some beans to it
        //TODO need to retrieve all permissions from db.
        final BeanItemContainer<Permission> beans = new BeanItemContainer<>( Permission.class );

        beans.addBean( identityManager
                .createPermission( "Some permission", PermissionGroup.ENVIRONMENT_PERMISSIONS, "some description" ) );

        // A layout for the table and form
        HorizontalLayout layout = new HorizontalLayout();

        // Bind a table to it
        permissionsTable = new Table( "Permissions", beans );
        permissionsTable.setVisibleColumns( new Object[] { "name", "permissionGroup", "description" } );
        permissionsTable.setPageLength( 7 );
        permissionsTable.setBuffered( false );

        // Create a form for editing a selected or new item.
        // It is invisible until actually used.
        final PermissionForm permissionForm = new PermissionForm( identityManager, this );
        permissionForm.setVisible( false );

        // When the user selects an item, show it in the form
        permissionsTable.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                // Close the form if the item is deselected
                if ( event.getProperty().getValue() == null )
                {
                    permissionForm.setVisible( false );
                    return;
                }
                permissionForm.setVisible( true );
                BeanItem<Permission> permission = beans.getItem( permissionsTable.getValue() );
                permissionForm.setPermission( permission );
                permissionsTable.setData( null );
            }
        } );
        permissionsTable.setSelectable( true );
        permissionsTable.setImmediate( true );

        // Creates a new bean for editing in the form before adding
        // it to the table. Adding is handled after committing
        // the form.
        final Button newBean = new Button( "New Permission" );
        newBean.addClickListener( new Button.ClickListener()
        {
            public void buttonClick( Button.ClickEvent event )
            {

                // Create a new item; this will create a new bean
                BeanItem<Permission> newPermission = new BeanItem<Permission>( identityManager
                        .createPermission( "New permission", PermissionGroup.DEFAULT_PERMISSIONS,
                                "Default permissions" ) );

                // The form was opened for editing a new item
                permissionsTable.select( null );
                permissionsTable.setEnabled( false );
                newBean.setEnabled( false );
                //
                // Make the form a bit nicer
                //this is an example for future how to improve UI
                //                // form.setVisibleItemProperties( new Object[] { "name" } );
                //                form.setItemDataSource( beans.getItem( newPermission ) );
                //                form.setVisible( true );
                permissionForm.setPermission( newPermission );
                permissionForm.setVisible( true );
            }
        } );

        // When OK button is clicked, commit the form to the bean
        final Button submit = new Button( "Save" );
        submit.addClickListener( new Button.ClickListener()
        {
            public void buttonClick( Button.ClickEvent event )
            {
                //                form.commit();
                //                form.setVisible( false ); // and close it
                //
                //                // New items have to be added to the container
                //                // Commit the addition
                //                table.commit();
                //
                //                table.setEnabled( true );
                //                newBean.setEnabled( true );
            }
        } );

        // Make modification to enable/disable the Save button
        //        form.setFormFieldFactory( new DefaultFieldFactory()
        //        {
        //            @Override
        //            public Field createField( Item item, Object propertyId, Component uiContext )
        //            {
        //                final AbstractField field = ( AbstractField ) super.createField( item, propertyId,
        // uiContext );
        //                field.addValueChangeListener( new Property.ValueChangeListener()
        //                {
        //                    @Override
        //                    public void valueChange( Property.ValueChangeEvent event )
        //                    {
        //                        submit.setEnabled( form.isModified() );
        //                    }
        //                } );
        //                if ( field instanceof TextField )
        //                {
        //                    final TextField tf = ( TextField ) field;
        //                    tf.addTextChangeListener( new FieldEvents.TextChangeListener()
        //                    {
        //                        @Override
        //                        public void textChange( FieldEvents.TextChangeEvent event )
        //                        {
        //                            if ( form.isModified() || !event.getText().equals( tf.getValue() ) )
        //                            {
        //                                submit.setEnabled( true );
        //
        //                                // Not needed after first event unless
        //                                // want to detect also changes back to
        //                                // unmodified value.
        //                                tf.removeTextChangeListener( this );
        //
        //                                // Has to be reset because the
        //                                // removeListener() setting causes
        //                                // updating the field value from the
        //                                // server-side.
        //                                tf.setValue( event.getText() );
        //                            }
        //                        }
        //                    } );
        //                }
        //                field.setImmediate( true );
        //
        //                return field;
        //            }
        //        } );

        Button cancel = new Button( "Cancel" );
        cancel.addClickListener( new Button.ClickListener()
        {
            public void buttonClick( Button.ClickEvent event )
            {
                //                form.discard();  // Not really necessary
                //                form.setVisible( false ); // and close it
                //                table.discard(); // Discards possible addItem()
                //                table.setEnabled( true );
                //                if ( table.getData() != null )
                //                {
                //                    beans.removeItem( table.getData() );
                //                }
                //                newBean.setEnabled( true );
            }
        } );

        layout.addComponent( permissionsTable );
        layout.addComponent( permissionForm );

        //        form.getFooter().addComponent( submit );
        //        form.getFooter().addComponent( cancel );

        layout.setSpacing( true );

        vlayout.addComponent( layout );
        vlayout.addComponent( newBean );

        setCompositionRoot( vlayout );
    }


    @Override
    public void savePermission( final BeanItem<Permission> value )
    {
        //TODO populate data to table
        identityManager.updatePermission( value.getBean() );
    }


    @Override
    public void removeOperation( final BeanItem<Permission> value )
    {
        //TODO remove data from table
        identityManager.deletePermission( value.getBean() );
    }


    @Override
    public void updatePermission( final BeanItem<Permission> value )
    {
        //TODO refresh data from table
        identityManager.updatePermission( value.getBean() );
    }
}
