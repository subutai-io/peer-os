package io.subutai.core.identity.ui.tabs.subviews;


import java.util.*;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItem;
import com.vaadin.server.Page;
import com.vaadin.ui.*;

import io.subutai.core.identity.api.model.Permission;
import io.subutai.core.identity.api.model.Role;
import io.subutai.core.identity.api.model.RolePermission;

import io.subutai.core.identity.ui.tabs.RolesTab;


// TODO: use identity manager methods only (no data service)
public class RoleForm extends Window
{
    private Table permTable = new Table( "Assigned permissions" );
    private Table allPerms = new Table( "All permissions" );
    private List<RolePermission> perms = new ArrayList<>();
    private BeanItem<Role> currentRole;
    private RolesTab callback;


    private void addRow( final RolePermission p )
    {
        Object newItemId = permTable.addItem();
        Item row = permTable.getItem( newItemId );
        row.getItemProperty( "Permission" ).setValue( p.getObjectName() );

        ComboBox scopes = new ComboBox();
        scopes.setNullSelectionAllowed( false );
        scopes.setTextInputAllowed( false );
        for ( int i = 1; i < 4; ++i )
        {
            scopes.addItem( i );
            switch ( i )
            {
                case ( 1 ):
                {
                    scopes.setItemCaption( 1, "All Objects" );
                    break;
                }
                case ( 2 ):
                {
                    scopes.setItemCaption( 2, "Child Objects" );
                    break;
                }
                case ( 3 ):
                {
                    scopes.setItemCaption( 3, "Owner Objects" );
                    break;
                }
            }
        }
        scopes.setValue( p.getScope() );
        scopes.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                int scope = ( int ) event.getProperty().getValue();
                p.setScope( scope );
                for ( RolePermission perm : perms )
                {
                    if ( p.getPermissionId() == perm.getPermissionId() )
                    {
                        perm.setScope( scope );

                        callback.getIdentityManager().getIdentityDataService().updateRolePermission( perm );
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
                for ( RolePermission perm : perms )
                {
                    if ( p.getPermissionId() == perm.getPermissionId() )
                    {
                        perm.setRead( read.getValue() );
                        callback.getIdentityManager().getIdentityDataService().updateRolePermission( perm );
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
                for ( RolePermission perm : perms )
                {
                    if ( p.getPermissionId() == perm.getPermissionId() )
                    {
                        perm.setWrite( write.getValue() );
                        callback.getIdentityManager().getIdentityDataService().updateRolePermission( perm );
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
                for ( RolePermission perm : perms )
                {
                    if ( p.getPermissionId() == perm.getPermissionId() )
                    {
                        perm.setUpdate( update.getValue() );
                        callback.getIdentityManager().getIdentityDataService().updateRolePermission( perm );
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
                for ( RolePermission perm : perms )
                {
                    if ( p.getPermissionId() == perm.getPermissionId() )
                    {
                        perm.setDelete( delete.getValue() );
                        callback.getIdentityManager().getIdentityDataService().updateRolePermission( perm );
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
                    if ( p.getPermissionId() == perms.get( i ).getPermissionId() )
                    {
                        callback.getIdentityManager().getIdentityDataService().removeRolePermission( perms.get( i ) );
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
        for ( final Permission p : callback.getIdentityManager().getAllPermissions() )
        {
            final Object newItemId = allPerms.addItem();
            final Item row = allPerms.getItem( newItemId );

            row.getItemProperty( "Permission" ).setValue( p.getObjectName() );

            Button add = new Button( "Add" );
            add.addClickListener( new Button.ClickListener()
            {
                @Override
                public void buttonClick( Button.ClickEvent event )
                {
                    boolean exists = false;
                    for ( RolePermission perm : perms )
                    {
                        if ( p.getId() == perm.getPermissionId() )
                        {
                            exists = true;
                            break;
                        }
                    }
                    if ( !exists )
                    {
                        RolePermission newRP = callback.getIdentityManager().getIdentityDataService()
                                                       .persistRolePermission( currentRole.getBean().getId(), p );
                        //callback.getIdentityManager ().assignRolePermission (currentRole.getBean ().getId (), p);
                        //TODO: perms.add(p);
                        addRow( newRP );
                    }
                    else
                    {
                        Notification notif = new Notification( "Permission is already added" );
                        notif.setDelayMsec( 2000 );
                        notif.show( Page.getCurrent() );
                    }
                }
            } );
            row.getItemProperty( "Add" ).setValue( add );
        }
    }


    public void setRole( BeanItem<Role> role )
    {
        this.currentRole = role;
        this.perms.clear();
        for ( RolePermission rp : callback.getIdentityManager().getIdentityDataService()
                                          .getAllRolePermissions( currentRole.getBean().getId() ) )
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

        permTable.setColumnWidth( "Scope", 70 );
        permTable.setColumnWidth( "Read", 35 );
        permTable.setColumnWidth( "Write", 35 );
        permTable.setColumnWidth( "Update", 35 );
        permTable.setColumnWidth( "Delete", 35 );

        allPerms.addContainerProperty( "Permission", String.class, null );
        allPerms.addContainerProperty( "Add", Button.class, null );

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
        Button save = new Button( "Save" );
        save.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {

                //TODO: currentRole.getBean().setPermissions (perms);
                callback.saveOperation( currentRole, false );
            }
        } );
        HorizontalLayout buttonGrid = new HorizontalLayout();
        buttonGrid.setSpacing( true );
        buttonGrid.addComponent( close );
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