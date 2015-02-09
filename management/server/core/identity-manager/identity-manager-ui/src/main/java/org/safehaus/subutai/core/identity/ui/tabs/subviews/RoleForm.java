package org.safehaus.subutai.core.identity.ui.tabs.subviews;


import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.safehaus.subutai.core.identity.api.Permission;
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


    private TwinColSelect permissionsSelector = new TwinColSelect()
    {
        {
            setItemCaptionMode( ItemCaptionMode.PROPERTY );
            setItemCaptionPropertyId( "name" );
            setImmediate( true );
            setSpacing( true );
            setRequired( true );
            setNullSelectionAllowed( false );
        }
    };


    public RoleForm( TabCallback<BeanItem<Role>> callback, Set<Permission> permissions )
    {
        init();
        BeanContainer<String, Permission> permissionsContainer = new BeanContainer<>( Permission.class );
        permissionsContainer.setBeanIdProperty( "name" );
        permissionsContainer.addAll( permissions );
        permissionsSelector.setContainerDataSource( permissionsContainer );
        permissionsSelector.setItemCaptionPropertyId( "name" );
        this.callback = callback;
    }


    private void init()
    {
        final Button saveButton = new Button( "Save role", saveListener );
        final Button cancelButton = new Button( "Cancel", cancelListener );
        final Button removeButton = new Button( "Remove role", resetListener );
        saveButton.setStyleName( Reindeer.BUTTON_DEFAULT );

        HorizontalLayout buttons = new HorizontalLayout( saveButton, cancelButton, removeButton );
        buttons.setSpacing( true );

        final FormLayout form = new FormLayout();
        form.addComponents( name, permissionsSelector );

        addComponents( form, buttons );

        setSpacing( true );
    }


    public void setPermission( final BeanItem<Role> role, boolean newValue )
    {
        this.newValue = newValue;
        if ( role != null )
        {
            permissionFieldGroup.setItemDataSource( role );

            permissionFieldGroup.bind( name, "name" );
            //                        permissionFieldGroup.bind( permissionsSelector, "permissions" );
            Role roleBean = role.getBean();
            Set<String> permissionNames = new HashSet<>();

            for ( final Permission permission : roleBean.getPermissions() )
            {
                permissionNames.add( permission.getName() );
            }
            permissionsSelector.setValue( permissionNames );
            if ( !newValue )
            {
                permissionFieldGroup.setReadOnly( true );
                //                Field<?> permissionsField = permissionFieldGroup.getField( "permissions" );
                //                permissionsField.setReadOnly( false );
            }
            else
            {
                permissionFieldGroup.setReadOnly( false );
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
                    Collection<String> selectedPermissions = ( Collection<String> ) permissionsSelector.getValue();
                    Role role = permissionFieldGroup.getItemDataSource().getBean();

                    for ( final String permissionId : selectedPermissions )
                    {
                        BeanItem beanItem = ( BeanItem ) permissionsSelector.getItem( permissionId );
                        role.addPermission( ( Permission ) beanItem.getBean() );
                    }
                    //
                    //                    for ( final Permission selectedPermission : selectedPermissions )
                    //                    {
                    //                        role.addPermission( selectedPermission );
                    //                    }
                    //                    permissionFieldGroup.getItemDataSource().getBean().addPermission(  );
                    callback.saveOperation( permissionFieldGroup.getItemDataSource(), newValue );
                }
            }
            catch ( FieldGroup.CommitException e )
            {
                LOGGER.error( "Error commit role fieldGroup changes", e );
                Notification.show( "Verify for fields correctness", Notification.Type.WARNING_MESSAGE );
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
            RoleForm.this.setVisible( false );
            if ( callback != null )
            {
                callback.cancelOperation();
            }
        }
    };
}
