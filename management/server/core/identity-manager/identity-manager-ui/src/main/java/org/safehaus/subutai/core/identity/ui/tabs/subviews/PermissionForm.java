package org.safehaus.subutai.core.identity.ui.tabs.subviews;


import java.util.EnumSet;

import org.safehaus.subutai.core.identity.api.IdentityManager;
import org.safehaus.subutai.core.identity.api.Permission;
import org.safehaus.subutai.core.identity.api.PermissionGroup;
import org.safehaus.subutai.core.identity.ui.tabs.TabCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;


public class PermissionForm extends VerticalLayout
{

    TabCallback callback;
    private static final Logger LOGGER = LoggerFactory.getLogger( PermissionForm.class );

    private Permission permission;
    private IdentityManager identityManager;
    private TextField name = new TextField( "Permission name" );
    private TextField description = new TextField( "Permission Description" );
    private ComboBox permissionGroup;


    public PermissionForm( IdentityManager identityManager )
    {
        init();
        this.identityManager = identityManager;
    }


    private void init()
    {
        final Button saveButton = new Button( "Save permission", saveListener );
        final Button cancelButton = new Button( "Cancel", cancelListener );
        final Button resetButton = new Button( "Reset changes", resetListener );
        saveButton.setStyleName( Reindeer.BUTTON_DEFAULT );

        BeanItemContainer<PermissionGroup> container = new BeanItemContainer<>( PermissionGroup.class );
        container.addAll( EnumSet.allOf( PermissionGroup.class ) );
        permissionGroup = new ComboBox( "", container );
        permissionGroup.setItemCaptionMode( AbstractSelect.ItemCaptionMode.PROPERTY );
        permissionGroup.setItemCaptionPropertyId( "name" );
        permissionGroup.setNullSelectionAllowed( false );
        permissionGroup.setImmediate( true );
        permissionGroup.setTextInputAllowed( false );
        permissionGroup.setRequired( true );

        HorizontalLayout buttons = new HorizontalLayout( saveButton, cancelButton, resetButton );
        buttons.setSpacing( true );

        addComponents( name, permissionGroup, description );
        addComponents( buttons );

        setSpacing( true );
    }


    public void setPermission( final Permission permission )
    {
        this.permission = permission;
        if ( permission != null )
        {
            name.setValue( permission.getName() );
            description.setValue( permission.getDescription() );
            permissionGroup.select( permission.getPermissionGroup() );
        }
        else
        {
            name.setValue( "" );
            description.setValue( "" );
            permissionGroup.setValue( null );
        }
    }


    private Button.ClickListener saveListener = new Button.ClickListener()
    {
        @Override
        public void buttonClick( final Button.ClickEvent event )
        {
            if ( permission != null )
            {
                permission.setDescription( description.getValue() );
                permission.setName( name.getValue() );
                permission.setPermissionGroup( ( PermissionGroup ) permissionGroup.getValue() );
                identityManager.updatePermission( permission );
            }
            else
            {
                permission = identityManager
                        .createPermission( name.getValue(), ( PermissionGroup ) permissionGroup.getValue(),
                                description.getValue() );
                identityManager.updatePermission( permission );
            }
        }
    };

    private Button.ClickListener resetListener = new Button.ClickListener()
    {
        @Override
        public void buttonClick( final Button.ClickEvent event )
        {
            PermissionForm.this.setVisible( false );
            setPermission( permission );
        }
    };

    private Button.ClickListener cancelListener = new Button.ClickListener()
    {
        @Override
        public void buttonClick( final Button.ClickEvent event )
        {
            setPermission( permission );
        }
    };
}
