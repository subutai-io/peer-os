package io.subutai.core.identity.ui.tabs;


import com.vaadin.ui.*;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.identity.ui.tabs.subviews.UserForm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;


public class UsersTab extends CustomComponent implements TabCallback<BeanItem<User>>
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
    private Table usersTable;
    private BeanItemContainer<User> beans;
    private Button newBean;
    private UserForm form;

    private static final Logger LOGGER = LoggerFactory.getLogger( UsersTab.class );


    public UsersTab( final IdentityManager identityManager )
    {
        this.identityManager = identityManager;
        editorForm();
    }


    void editorForm()
    {
        VerticalLayout vlayout = new VerticalLayout();

        // Create a container for such beans
        // Add some beans to it
        beans = new BeanItemContainer<>( User.class );
        beans.addAll( identityManager.getAllUsers() );

        // A layout for the table and form
        HorizontalLayout layout = new HorizontalLayout();

        // Bind a table to it
        usersTable = new Table( "Users", beans );
        usersTable.setVisibleColumns( new Object[] { "id","userName", "fullName", "email", "typeName" ,"statusName" } );
        usersTable.setPageLength( 7 );
        usersTable.setColumnHeader( "id", "id" );
        usersTable.setColumnHeader( "userName", "Username" );
        usersTable.setColumnHeader( "fullName", "Full name" );
        usersTable.setColumnHeader( "email", "Email" );
        usersTable.setColumnHeader( "typeName", "Type" );
        usersTable.setColumnHeader( "statusName", "Status" );
        usersTable.setBuffered( false );


        // When the user selects an item, show it in the form
        usersTable.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                // Close the form if the item is deselected
                if ( event.getProperty().getValue() == null )
                {
                    return;
                }
                BeanItem<User> userBean = beans.getItem( usersTable.getValue() );
                refreshControls( FormState.STATE_EXISTING_ENTITY_SELECTED, userBean);
                //                usersTable.select( null );
            }
        } );
        usersTable.setSelectable( true );
        usersTable.setImmediate( true );

        // Creates a new bean for editing in the form before adding
        // it to the table. Adding is handled after committing
        // the form.
        newBean = new Button( "New User" );
        newBean.addClickListener( new Button.ClickListener()
        {
            public void buttonClick( Button.ClickEvent event )
            {

                // Create a new item; this will create a new bean
                //BeanItem<User> newUser = new BeanItem<>( identityManager.createMockUser( "", "", "", "" ) );

                // The form was opened for editing a new item
                refreshControls( FormState.STATE_NEW_ENTITY, null );


                // Make the form a bit nicer
                //this is an example for future how to improve UI
                //form.setUser( newUser, true );
            }
        } );

        vlayout.addComponent( newBean );
        vlayout.setSpacing (true);
        vlayout.setMargin (true);
        layout.addComponent( usersTable );

        layout.setSpacing( true );

        vlayout.addComponent( layout );

        setCompositionRoot( vlayout );
    }


    //make centralized modification and controls updates
    //state:
    //  0: existing entity selected in table
    //  1: on save event existing one
    //  2: on save event new entity
    //  3: on remove event
    //  4: on new entity button click
    private void refreshControls( FormState state, BeanItem<User> userBean )
    {
        switch ( state )
        {
            case STATE_EXISTING_ENTITY_SELECTED:
                form = new UserForm (this, identityManager);
                form.setUser (userBean, false);
                UI.getCurrent().addWindow (form);
                newBean.setEnabled(false);
                usersTable.setEnabled( false );
                break;
            case STATE_SAVE_EXISTING_ENTITY:
            case STATE_SAVE_NEW_ENTITY:
                newBean.setEnabled( true );
                usersTable.setEnabled( true );
                form.close();
                break;
            case STATE_REMOVE_ENTITY:
                newBean.setEnabled( true );
                form.close();
                usersTable.setEnabled(true);
                break;
            case STATE_NEW_ENTITY:
                form = new UserForm (this, identityManager);
                form.setUser (userBean, false);
                UI.getCurrent().addWindow(form);
                newBean.setEnabled( false );
                usersTable.setEnabled( false );
                break;
            case STATE_CANCEL:
                form.close();
                newBean.setEnabled( true );
                usersTable.setEnabled( true );
                break;
        }
    }


    @Override
    public void saveOperation( final BeanItem<User> value, final boolean newValue )
    {
        identityManager.updateUser( value.getBean() );

        if ( newValue )
        {
            beans.removeAllItems();
            beans.addAll( identityManager.getAllUsers() );
            refreshControls( FormState.STATE_SAVE_NEW_ENTITY, null );
        }
        else
        {
            refreshControls( FormState.STATE_SAVE_EXISTING_ENTITY, null );
        }
    }


    @Override
    public void removeOperation( final BeanItem<User> value, final boolean newValue )
    {
        if ( !newValue )
        {
            identityManager.removeUser( value.getBean().getId() );
            beans.removeItem( value.getBean() );
        }
        refreshControls( FormState.STATE_REMOVE_ENTITY, null );
    }


    @Override
    public void cancelOperation()
    {
        refreshControls( FormState.STATE_CANCEL, null );
    }
}
