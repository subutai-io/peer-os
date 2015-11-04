package io.subutai.core.identity.ui.tabs.subviews;


import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.vaadin.ui.*;
import io.subutai.common.security.objects.UserStatus;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.Role;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.identity.ui.ErrorUtils;
import io.subutai.core.identity.ui.tabs.TabCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.validator.AbstractStringValidator;
import com.vaadin.data.validator.EmailValidator;
import com.vaadin.ui.themes.Reindeer;

public class UserForm extends Window
{

    private final IdentityManager identityManager;
    private TabCallback<BeanItem<User>> callback;

    private static final Logger LOGGER = LoggerFactory.getLogger( UserForm.class );

    private BeanFieldGroup<User> userFieldGroup = new BeanFieldGroup<>( User.class );

    private FormLayout form;

    private boolean newValue;

    private Button removeButton;
    private TextField userName;
    private TextField fullName;
    private TextField email;
    private PasswordField password;
    private PasswordField confirmPassword;
    private ComboBox status = new ComboBox ("Status");;
    private TwinColSelect rolesSelector;
    private final BeanContainer<String, Role> permissionsContainer;


    public UserForm( TabCallback<BeanItem<User>> callback, IdentityManager identityManager )
    {
		this.setClosable (false);
		this.addStyleName ("default");
		this.center();
        init();
        this.identityManager = identityManager;
        permissionsContainer = new BeanContainer<>( Role.class );
        permissionsContainer.setBeanIdProperty( "name" );
        //permissionsContainer.addAll( roles );
        rolesSelector.setContainerDataSource( permissionsContainer );
        rolesSelector.setItemCaptionPropertyId( "name" );

        this.callback = callback;
    }


    private void init()
    {
        initControls();
        // When OK button is clicked, commit the form to the bean
        Button.ClickListener saveListener = new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent event )
            {
                // New items have to be added to the container
                // Commit the addition
                try
                {
                    userFieldGroup.commit();
                    Collection<String> selectedRoleNames = ( Collection<String> ) rolesSelector.getValue();
                    if ( selectedRoleNames.isEmpty() )
                    {
                        Notification.show( "Please select at least one role." );
                        return;
                    }
                    if ( callback != null )
                    {
                        User user = userFieldGroup.getItemDataSource().getBean();
                        //user.removeAllRoles();
                        for ( final String roleName : selectedRoleNames )
                        {
                            BeanItem beanItem = ( BeanItem ) rolesSelector.getItem( roleName );
                            //user.addRole( ( Role ) beanItem.getBean() );
                        }
                        callback.saveOperation( userFieldGroup.getItemDataSource(), newValue );
                        Notification.show( "Successfully saved." );
                    }
                }
                catch ( FieldGroup.CommitException e )
                {
                    ErrorUtils.showComponentErrors( userFieldGroup.getFields() );
                }
            }
        };

        Button.ClickListener cancelListener = new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent event )
            {
                userFieldGroup.discard();
                UserForm.this.setVisible( false );
                if ( callback != null )
                {
                    callback.cancelOperation();
                }
            }
        };

        final Button saveButton = new Button( "Save user", saveListener );
        final Button cancelButton = new Button( "Close", cancelListener );
        saveButton.setStyleName( Reindeer.BUTTON_DEFAULT );

		for (int i = 1; i < 3; ++i)
		{
			status.addItem (i);
			switch (i)
			{
				case (1):
				{
					status.setItemCaption (1, "Active");
					break;
				}
				case (2):
				{
					status.setItemCaption (2, "Disabled");
					break;
				}
			}
		}

        HorizontalLayout buttons = new HorizontalLayout( saveButton, cancelButton, removeButton );
        buttons.setSpacing( true );

        form = new FormLayout();
        form.addComponents( userName, fullName, email, password, confirmPassword, status, rolesSelector );

		VerticalLayout content = new VerticalLayout();
		content.setSpacing (true);
		content.setMargin (true);
        content.addComponents(buttons, form);

		this.setContent (content);
    }


    private void initControls()
    {
        removeButton = new Button( "Delete user", new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent event )
            {
                userFieldGroup.discard();
                if ( callback != null )
                {
                    callback.removeOperation( userFieldGroup.getItemDataSource(), newValue );
                }
            }
        } );

        userName = new TextField( "Username" );
        userName.setRequired( true );
        userName.setInputPrompt( "Username" );
        userName.setRequiredError( "Please enter username." );
        userName.setEnabled( false );

        fullName = new TextField( "Full name" );
        fullName.setRequired( true );
        fullName.setInputPrompt( "Full name" );
        fullName.setRequiredError( "Please enter full name." );

        email = new TextField( "Email" );
        email.setRequired( true );
        email.setRequiredError( "Please enter e-mail address." );
        email.setInputPrompt( "Email" );
        email.addValidator( new EmailValidator( "Incorrect email format!!!" ) );

        password = new PasswordField( "Password" );
        password.setRequired( true );
        password.setRequiredError( "Please enter password." );
        password.setInputPrompt( "Password" );
        password.addValidator( new AbstractStringValidator( "Passwords do not match." )
        {
            @Override
            protected boolean isValidValue( final String value )
            {
                return value.equals( confirmPassword.getValue() );
            }
        } );


        confirmPassword = new PasswordField( "Confirm password" );
        confirmPassword.setRequired( true );
        confirmPassword.setRequiredError( "Please enter password confirm." );
        confirmPassword.setInputPrompt( "Confirm password" );

		status.setNullSelectionAllowed (false);
		status.setTextInputAllowed (false);

        ComboBox cbUserStatus = new ComboBox("Some caption");
        cbUserStatus.setNullSelectionAllowed(false);

        cbUserStatus.addItem( UserStatus.Active.getName());
        cbUserStatus.addItem(UserStatus.Disabled.getName());


        rolesSelector = new TwinColSelect( "User roles" );
        rolesSelector.setWidth ("500px");
        rolesSelector.setHeight ("200px");
        rolesSelector.setItemCaptionMode(AbstractSelect.ItemCaptionMode.PROPERTY);
        rolesSelector.setItemCaptionPropertyId( "name" );
    }


    public void setUser( final BeanItem<User> user, boolean newValue )
    {
        this.newValue = newValue;
        if ( user != null )
        {
            List<Role> roles = identityManager.getAllRoles();
            permissionsContainer.removeAllItems();
            permissionsContainer.addAll( roles );
            userFieldGroup.setItemDataSource( user );

            userFieldGroup.bind( userName, "userName" );
            userFieldGroup.bind( fullName, "fullName" );
            userFieldGroup.bind( email, "email" );
            userFieldGroup.bind( password, "password" );
            confirmPassword.setValue( user.getBean().getPassword() );
			status.setValue (user.getBean().getStatus());
            if ( newValue )
            {
                password.setValue( "" );
                confirmPassword.setValue( "" );
            }
            // Pre-select user roles
            User userBean = user.getBean();
            Set<String> roleNames = new HashSet<>();
            for ( final Role role : userBean.getRoles() )
            {
                roleNames.add( role.getName() );
            }
            rolesSelector.setValue( roleNames );

            if ( !newValue )
            {
                userName.setReadOnly( true );
                removeButton.setVisible( true );
            }
            else
            {
                userName.setReadOnly( false );
                removeButton.setVisible( false );
            }
        }
    }
}
