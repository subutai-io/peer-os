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
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Field;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;


public class PermissionForm extends VerticalLayout
{

    private TabCallback<BeanItem<Permission>> callback;

    private static final Logger LOGGER = LoggerFactory.getLogger( PermissionForm.class );

    private boolean newValue;

    private BeanFieldGroup<Permission> permissionFieldGroup = new BeanFieldGroup<>( Permission.class );

    private TextField name = new TextField()
    {
        {
            setInputPrompt( "Permission name" );
            setEnabled( false );
            setRequired( true );
            addValidator(
                    new StringLengthValidator( "Permission name must be at least 4 chars length", 4, 255, false ) );
        }
    };

    private TextField description = new TextField()
    {
        {
            setInputPrompt( "Description" );
        }
    };

    Button removeButton = new Button( "Remove permission", new Button.ClickListener()
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

    private ComboBox permissionGroup =
            new ComboBox( "", new BeanItemContainer<>( PermissionGroup.class, EnumSet.allOf( PermissionGroup.class ) ) )
            {
                {
                    setItemCaptionMode( ItemCaptionMode.PROPERTY );
                    setItemCaptionPropertyId( "name" );
                    setNullSelectionAllowed( false );
                    setImmediate( true );
                    setTextInputAllowed( false );
                    setRequired( true );
                }
            };





    public PermissionForm( TabCallback<BeanItem<Permission>> callback )
    {
        init();
        this.callback = callback;
    }


    private void init()
    {
        final Button saveButton = new Button( "Save permission", saveListener );
        final Button cancelButton = new Button( "Cancel", cancelListener );
        saveButton.setStyleName( Reindeer.BUTTON_DEFAULT );

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

            permissionFieldGroup.bind( name, "name" );
            permissionFieldGroup.bind( description, "description" );
            permissionFieldGroup.bind( permissionGroup, "permissionGroup" );

            if ( !newValue )
            {
                permissionFieldGroup.setReadOnly( true );
                Field<?> description = permissionFieldGroup.getField( "description" );
                description.setReadOnly( false );
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
                    callback.saveOperation( permissionFieldGroup.getItemDataSource(), newValue );
                }
            }
            catch ( FieldGroup.CommitException e )
            {
                LOGGER.error( "Error commit permission fieldGroup changes", e );
                Notification.show( e.getMessage(), Notification.Type.WARNING_MESSAGE );
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
