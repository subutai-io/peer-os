package io.subutai.core.identity.ui.tabs.subviews;


import java.util.*;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.*;

import io.subutai.common.security.objects.PermissionObject;
import io.subutai.common.security.objects.PermissionScope;
import io.subutai.core.identity.api.model.Permission;
import io.subutai.core.identity.api.model.Role;

import io.subutai.core.identity.ui.tabs.RolesTab;


// TODO: use identity manager methods only (no data service)
public class RoleForm extends Window
{
    private Table permTable = new Table( "Assigned permissions" );
    private Table allPerms = new Table( "All permissions" );
    private List<Permission> perms = new ArrayList<>();
    private BeanItem<Role> currentRole;
    private RolesTab callback;


    private void addRow( final Permission p )
    {
        Object newItemId = permTable.addItem();
        Item row = permTable.getItem( newItemId );
        row.getItemProperty( "Permission" ).setValue( p.getObjectName() );

        ComboBox scopes = new ComboBox();
        scopes.setNullSelectionAllowed( false );


        for (int i = 0; i < PermissionScope.values().length; ++i)
        {
            scopes.addItem(i+1);
            scopes.setItemCaption( i + 1, PermissionScope.values()[i].getName() );
        }
        scopes.setValue( p.getScope() );
        scopes.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                int scope = ( int ) event.getProperty().getValue();
                p.setScope( scope );
                for ( Permission perm : perms )
                {
                    if ( p.getId() == perm.getId() )
                    {
                        perm.setScope( scope );

                        callback.getIdentityManager().updatePermission( perm );
                    }
                }
            }
        } );
        row.getItemProperty( "Scope" ).setValue( scopes );

        final CheckBox read = new CheckBox();
        read.setValue( p.isRead() );
        read.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                p.setRead( read.getValue() );
                for ( Permission perm : perms )
                {
                    if ( p.getId() == perm.getId() )
                    {
                        perm.setRead( read.getValue() );

                        callback.getIdentityManager().updatePermission( perm );
                    }
                }
            }
        } );
        row.getItemProperty( "Read" ).setValue( read );

        final CheckBox write = new CheckBox();
        write.setValue( p.isWrite() );
        write.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                p.setWrite( write.getValue() );
                for ( Permission perm : perms )
                {
                    if ( p.getId() == perm.getId() )
                    {
                        perm.setWrite( write.getValue() );
                        callback.getIdentityManager().updatePermission( perm );
                    }
                }
            }
        } );
        row.getItemProperty( "Write" ).setValue( write );

        final CheckBox update = new CheckBox();
        update.setValue( p.isUpdate() );
        update.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                p.setUpdate( update.getValue() );
                for ( Permission perm : perms )
                {
                    if ( p.getId() == perm.getId() )
                    {
                        perm.setUpdate( update.getValue() );
                        callback.getIdentityManager().updatePermission( perm );
                    }
                }
            }
        } );
        row.getItemProperty( "Update" ).setValue( update );

        final CheckBox delete = new CheckBox();
        delete.setValue( p.isDelete() );
        delete.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                p.setDelete( delete.getValue() );
                for ( Permission perm : perms )
                {
                    if ( p.getId() == perm.getId() )
                    {
                        perm.setDelete( delete.getValue() );
                        callback.getIdentityManager().updatePermission( perm );
                    }
                }
            }
        } );
        row.getItemProperty( "Delete" ).setValue( delete );


        final Button remove = new Button( "Remove" );
        remove.setData( newItemId );
        remove.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent event )
            {
                for ( int i = 0; i < perms.size(); ++i )
                {
                    if ( p.getId() == perms.get( i ).getId() )
                    {
                        callback.getIdentityManager().removePermission( perms.get( i ).getId() );
                        perms.remove( i );
                        break;
                    }
                }
                permTable.removeItem( remove.getData() );
            }
        } );
        row.getItemProperty( "Remove" ).setValue( remove );
    }


    private void addPerms()
    {
        for (int a=0; a< PermissionObject.values().length;a++)
        {
            final Object newItemId = allPerms.addItem();
            final Item row = allPerms.getItem( newItemId );

            row.getItemProperty ("Permission").setValue (PermissionObject.values()[a].getName());

            Button add = new Button( "Add" );
            add.addClickListener( new Button.ClickListener()
            {
                @Override
                public void buttonClick( Button.ClickEvent event )
                {

                    Permission newRP = callback.getIdentityManager().createPermission((int)newItemId,PermissionScope.ALL_SCOPE.getId(),true,true,true,true );

                    callback.getIdentityManager().assignRolePermission( currentRole.getBean().getId(), newRP  );
                    addRow( newRP );
                }
            });
            row.getItemProperty( "Add" ).setValue( add );
        }
    }


    public void setRole( BeanItem<Role> role )
    {
        this.currentRole = role;
        this.perms.clear();
        for ( Permission rp : callback.getIdentityManager().getRole( currentRole.getBean().getId() ).getPermissions() )
        {
            this.perms.add( rp );
            this.addRow( rp );
        }
    }


    public RoleForm( final RolesTab callback )
    {
        this.callback = callback;
        this.setClosable( false );
        this.addStyleName( "default" );
        this.center();
        VerticalLayout content = new VerticalLayout();
        content.setSpacing( true );
        content.setMargin( true );
        permTable.addContainerProperty( "Permission", String.class, null );
        permTable.addContainerProperty( "Scope", ComboBox.class, null );
        permTable.addContainerProperty( "Read", CheckBox.class, null );
        permTable.addContainerProperty( "Write", CheckBox.class, null );
        permTable.addContainerProperty( "Update", CheckBox.class, null );
        permTable.addContainerProperty( "Delete", CheckBox.class, null );
        permTable.addContainerProperty( "Remove", Button.class, null );

        permTable.setColumnWidth( "Scope", 85 );
        permTable.setColumnWidth( "Read", 35 );
        permTable.setColumnWidth( "Write", 35 );
        permTable.setColumnWidth( "Update", 35 );
        permTable.setColumnWidth( "Delete", 35 );

        allPerms.addContainerProperty( "Permission", String.class, null );
        allPerms.addContainerProperty( "Add", Button.class, null );
        allPerms.setHeight( "280px" );
        permTable.setHeight( "280px" );

        addPerms();
        Button close = new Button( "Close" );
        close.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent event )
            {
                callback.cancelOperation();
            }
        } );

        Button removeRole = new Button( "Remove Role" );
        removeRole.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent event )
            {
                callback.removeOperation( currentRole, false );
            }
        } );

        Button save = new Button( "Save" );
        save.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                callback.saveOperation( currentRole, false );
            }
        } );
        HorizontalLayout buttonGrid = new HorizontalLayout();
        buttonGrid.setSpacing( true );
        buttonGrid.addComponent( close );
        buttonGrid.addComponent( removeRole );
        buttonGrid.addComponent( save );
        HorizontalLayout tableGrid = new HorizontalLayout();
        tableGrid.setSpacing( true );
        tableGrid.addComponent( permTable );
        tableGrid.addComponent( allPerms );
        content.addComponent( buttonGrid );
        content.addComponent( tableGrid );
        this.setContent( content );
    }
}