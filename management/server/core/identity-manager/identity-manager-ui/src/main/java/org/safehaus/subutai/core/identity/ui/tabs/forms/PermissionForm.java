package org.safehaus.subutai.core.identity.ui.tabs.forms;


import java.util.EnumSet;

import org.safehaus.subutai.core.identity.api.Permission;
import org.safehaus.subutai.core.identity.api.PermissionGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;


public class PermissionForm extends VerticalLayout
{

    private static final Logger LOGGER = LoggerFactory.getLogger( PermissionForm.class );

    BeanFieldGroup<Permission> fieldGroup = new BeanFieldGroup<>( Permission.class );
    private Permission permission;
    private TextField name = new TextField( "Permission name" );
    private TextField description = new TextField( "Permission Description" );
    private ComboBox permissionGroup;
    private Button saveButton;
    private Button cancelButton;
    private Button resetButton;


    public PermissionForm()
    {
        init();
    }


    private void init()
    {
        saveButton = new Button( "Save permission", saveListener );
        cancelButton = new Button( "Cancel", cancelListener );
        resetButton = new Button( "Reset changes", resetListener );

        BeanContainer<String, PermissionGroup> container = new BeanContainer<>( PermissionGroup.class );
        container.setBeanIdProperty( "name" );
        container.addAll( EnumSet.allOf( PermissionGroup.class ) );
        permissionGroup = new PermissionGroupComboBox( "" );
        permissionGroup.setContainerDataSource( container );
        permissionGroup.setItemCaptionPropertyId( "name" );
        permissionGroup.setNullSelectionAllowed( false );
        permissionGroup.setImmediate( true );
        permissionGroup.setTextInputAllowed( false );
        permissionGroup.setRequired( true );
        //        Converter converter = new PermissionGroupConverter();
        //        permissionGroup.setConverter( converter );


        fieldGroup.bind( name, "name" );
        //        fieldGroup.bind( permissionGroup, "permissionGroup" );
        fieldGroup.bind( description, "description" );

        if ( permission != null )
        {
            permissionGroup.select( permission.getPermissionGroup().getName() );
        }

        addComponents( name, permissionGroup, description );
        HorizontalLayout buttons = new HorizontalLayout( saveButton, cancelButton, resetButton );
        buttons.setSpacing( true );
        addComponents( buttons );
        saveButton.setStyleName( Reindeer.BUTTON_DEFAULT );
        setSpacing( true );
    }


    public void setPermission( final Permission permission )
    {
        this.permission = permission;
        fieldGroup.setItemDataSource( permission );
        permissionGroup.select( permission.getPermissionGroup().getName() );
    }


    public void discard()
    {
        fieldGroup.discard();
    }


    private Button.ClickListener saveListener = new Button.ClickListener()
    {
        @Override
        public void buttonClick( final Button.ClickEvent event )
        {
            try
            {
                PermissionForm.this.fieldGroup.commit();
            }
            catch ( FieldGroup.CommitException e )
            {
                LOGGER.error( "Error commit permission changes", e );
            }
        }
    };

    private Button.ClickListener resetListener = new Button.ClickListener()
    {
        @Override
        public void buttonClick( final Button.ClickEvent event )
        {
            PermissionForm.this.fieldGroup.discard();
        }
    };

    private Button.ClickListener cancelListener = new Button.ClickListener()
    {
        @Override
        public void buttonClick( final Button.ClickEvent event )
        {
            PermissionForm.this.fieldGroup.discard();
        }
    };
}
