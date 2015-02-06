package org.safehaus.subutai.core.identity.ui.tabs.subviews;


import java.util.EnumSet;

import org.safehaus.subutai.core.identity.api.Permission;
import org.safehaus.subutai.core.identity.api.PermissionGroup;
import org.safehaus.subutai.core.identity.ui.tabs.TabCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Field;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;


public class PermissionForm extends VerticalLayout
{

    TabCallback<BeanItem<Permission>> callback;
    private static final Logger LOGGER = LoggerFactory.getLogger( PermissionForm.class );

    private boolean newValue;
    private TextField name = new TextField()
    {
        {
            setInputPrompt( "Permission name" );
            setEnabled( false );
        }
    };
    private TextField description = new TextField()
    {
        {
            setInputPrompt( "Description" );
        }
    };
    private ComboBox permissionGroup;
    private BeanFieldGroup<Permission> permissionFieldGroup = new BeanFieldGroup<>( Permission.class );


    public PermissionForm( TabCallback<BeanItem<Permission>> callback )
    {
        init();
        this.callback = callback;
    }


    private void init()
    {
        final Button saveButton = new Button( "Save permission", saveListener );
        final Button cancelButton = new Button( "Cancel", cancelListener );
        final Button removeButton = new Button( "Remove permission", resetListener );
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

        HorizontalLayout buttons = new HorizontalLayout( saveButton, cancelButton, removeButton );
        buttons.setSpacing( true );

        final FormLayout form = new FormLayout();
        form.addComponents( name, permissionGroup, description );

        addComponents( form, buttons );

        setSpacing( true );
    }


    public void setPermission( final BeanItem<Permission> permission, boolean newValue )
    {
        this.newValue = newValue;
        if ( permission != null )
        {
            permissionFieldGroup.setItemDataSource( permission );
            //            Field<?> name = permissionFieldGroup.buildAndBind( "Name", "name" );

            permissionFieldGroup.bind( name, "name" );
            permissionFieldGroup.bind( description, "description" );
            permissionFieldGroup.bind( permissionGroup, "permissionGroup" );

            if ( !newValue )
            {
                permissionFieldGroup.setReadOnly( true );
                Field<?> description = permissionFieldGroup.getField( "description" );
                description.setReadOnly( false );
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
            }
            catch ( FieldGroup.CommitException e )
            {
                LOGGER.error( "Error commit permission fieldGroup changes" );
            }
            if ( callback != null )
            {
                callback.saveOperation( permissionFieldGroup.getItemDataSource(), newValue );
            }
        }
    };

    private Button.ClickListener resetListener = new Button.ClickListener()
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
    };

    private Button.ClickListener cancelListener = new Button.ClickListener()
    {
        @Override
        public void buttonClick( final Button.ClickEvent event )
        {
            permissionFieldGroup.discard();
            PermissionForm.this.setVisible( false );
            if ( callback != null )
            {
                callback.cancelOperation();
            }
        }
    };
}
