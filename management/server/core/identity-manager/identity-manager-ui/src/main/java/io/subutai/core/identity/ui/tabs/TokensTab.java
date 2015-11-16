package io.subutai.core.identity.ui.tabs;


import com.vaadin.data.Item;
import com.vaadin.server.Page;
import com.vaadin.ui.*;

import io.subutai.common.security.objects.PermissionObject;
import io.subutai.common.security.objects.PermissionOperation;
import io.subutai.common.security.objects.PermissionScope;
import io.subutai.common.security.objects.TokenType;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.identity.api.model.UserToken;
import io.subutai.server.ui.component.ConfirmationDialog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Calendar;


public class TokensTab extends Panel
{
    private static final Logger LOGGER = LoggerFactory.getLogger( TokensTab.class );
    private static final String USER_NAME = "User name";
    private static final String TOKEN_NAME = "Token name";
    private static final String CREATE_DATE = "Create date";
    private static final String STATUS = "Status";

    private IdentityManager identityManager;

    private GridLayout installationControls;
    private HorizontalLayout rangeButtons;
    private TextField validityPeriodTxtFld;
    private TextField tokenNameTxtFld;
    private TextField editValidityPeriodTxtFld;
    private TextField editTokenTxtFld;
    private TextField statusTxtFld;
    private Table tokenTable;
    private ComboBox userCombo;
    private Button generateTokenBtn;
    private Button editSubBtn;
    private Window subWindow;


    public TokensTab( final IdentityManager identityManager )
    {
        this.identityManager = identityManager;

        installationControls = new GridLayout( 1, 2 );
        installationControls.setSizeFull();
        installationControls.setSpacing( true );
        installationControls.setMargin( true );

        validityPeriodTxtFld = new TextField( "Enter validity period (hours)" );
        validityPeriodTxtFld.setId( "ValidityPeriod" );
        validityPeriodTxtFld.setInputPrompt( "Validity Period" );
        validityPeriodTxtFld.setValue( Integer.toString( 10 ) );
        validityPeriodTxtFld.setRequired( true );

        tokenNameTxtFld = new TextField( "Enter token name" );
        tokenNameTxtFld.setId( "Token" );
        tokenNameTxtFld.setInputPrompt( TOKEN_NAME );
        tokenNameTxtFld.setRequired( true );
        tokenNameTxtFld.setReadOnly( false );
        tokenNameTxtFld.setWidth( 280, Unit.PIXELS );

        statusTxtFld = new TextField( STATUS );
        statusTxtFld.setId( STATUS );
        statusTxtFld.setReadOnly( false );
        statusTxtFld.setRequired( false );


        userCombo = new ComboBox( "Users" );
        userCombo.setId( "UserCombo" );
        userCombo.setImmediate( true );
        userCombo.setNullSelectionAllowed( false );
        userCombo.setTextInputAllowed( false );
        userCombo.setWidth( 200, Unit.PIXELS );
        boolean set = false;

        User activeUser = identityManager.getActiveUser();
        boolean viewAllList = checkUserPermission(activeUser);

        for ( User u : identityManager.getAllUsers() )
        {
            if(viewAllList || u.getId() == activeUser.getId())
            {
                userCombo.addItem( u );
                userCombo.setItemCaption( u, u.getUserName() );
                if ( !set )
                {
                    userCombo.setValue( u );
                    set = true;
                }
            }
        }


        editValidityPeriodTxtFld = new TextField( "Enter validity period (hours)" );
        editValidityPeriodTxtFld.setId( "EditValidityPeriod" );
        editValidityPeriodTxtFld.setInputPrompt( "Validity Period" );
        editValidityPeriodTxtFld.setRequired( true );

        editTokenTxtFld = new TextField( "Enter token name" );
        editTokenTxtFld.setId( "EditToken" );
        editTokenTxtFld.setInputPrompt( TOKEN_NAME );
        editTokenTxtFld.setRequired( true );
        editTokenTxtFld.setReadOnly( false );
        editTokenTxtFld.setWidth( 280, Unit.PIXELS );

        rangeButtons = new HorizontalLayout();
        rangeButtons.setSpacing( true );
        rangeButtons.addComponent( tokenNameTxtFld );
        rangeButtons.addComponent( validityPeriodTxtFld );
        rangeButtons.addComponent( userCombo );

        tokenTable = new Table( "Token table" );
        // Define two columns for the built-in container
        tokenTable.addContainerProperty( "Username", String.class, null );
        tokenTable.addContainerProperty( "Token Name", String.class, null );
        tokenTable.addContainerProperty( "TTL/Date", String.class, null );
        tokenTable.addContainerProperty( "Type", String.class, null );
        tokenTable.addContainerProperty( "Hash Algo", String.class, null );
        tokenTable.addContainerProperty( "Issuer", String.class, null );
        tokenTable.addContainerProperty( "Token", Button.class, null );
        tokenTable.addContainerProperty( "Edit", Button.class, null );
        tokenTable.addContainerProperty( "Delete", Button.class, null );
        tokenTable.setHeight( "500px" );

        generateTokenBtn = new Button( "Add Token" );
        generateTokenBtn.setId( "AddToken" );
        generateTokenBtn.addStyleName( "default" );
        generateTokenBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent )
            {
                boolean exists = false;
                List<UserToken> userTokens = identityManager.getAllUserTokens();
                for ( UserToken ut : userTokens )
                {
                    if ( ut.getToken().equals( tokenNameTxtFld.getValue() ) )
                    {
                        exists = true;
                        break;
                    }
                }
                if ( !exists )
                {
                    Timestamp datetime = new Timestamp( System.currentTimeMillis() );
                    Date newDate = new Date( datetime.getTime() );
                    java.util.Calendar cal = Calendar.getInstance();
                    cal.setTime( newDate );
                    cal.add( Calendar.HOUR_OF_DAY, Integer.parseInt( validityPeriodTxtFld.getValue() ) );
                    newDate = new Date( cal.getTime().getTime() );
                    final UserToken userToken = identityManager
                            .createUserToken( ( User ) userCombo.getValue(), tokenNameTxtFld.getValue(), null, "subutai.io",
                                    2, newDate ); // TODO: fix issuer and type
                    addRow( userToken );
                }
                else
                {
                    Notification notif = new Notification( "Token with such name already exists" );
                    notif.setDelayMsec( 2000 );
                    notif.show( Page.getCurrent() );
                }
            }
        } );

        // Show exactly the currently contained rows (items)
        tokenTable.setPageLength( tokenTable.size() );

        populateTable();
        // listener for edit button
        init();
    }


    private boolean checkUserPermission(User user)
    {


        if ( identityManager.isUserPermitted( user,
                PermissionObject.IdentityManagement,
                PermissionScope.ALL_SCOPE,
                PermissionOperation.Read) )
        {
            return  true;
        }
        else
        {
            return false;
        }
    }


    private void populateTable()
    {
        User activeUser = identityManager.getActiveUser();
        boolean viewAllList = checkUserPermission(activeUser);

        List<UserToken> userTokens = identityManager.getAllUserTokens();

        for ( UserToken ut : userTokens )
        {
            if(viewAllList || ut.getUserId() == activeUser.getId())
            {
                addRow( ut );
            }
        }
    }


    private void addRow( final UserToken userToken )
    {
        final Object newItemId = tokenTable.addItem();
        Item row = tokenTable.getItem( newItemId );
        row.getItemProperty( "Username" ).setValue( identityManager.getUser( userToken.getUserId() ).getUserName() );
        row.getItemProperty( "Token Name" ).setValue( userToken.getToken() );
        String date = new SimpleDateFormat( "dd/MM/yyyy HH:mm:ss" ).format( userToken.getValidDate() );
        row.getItemProperty( "TTL/Date" ).setValue( date );
        row.getItemProperty( "Type" ).setValue( TokenType.values()[userToken.getType()-1].getName());
        row.getItemProperty( "Hash Algo" ).setValue( userToken.getHashAlgorithm() );
        row.getItemProperty( "Issuer" ).setValue( userToken.getIssuer() );
        Button generate = new Button( "Show Token" );
        generate.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent event )
            {
                final Window window = new Window( "Full token" );
                window.center();
                window.setClosable( false );
                window.addStyleName( "default" );
                VerticalLayout content = new VerticalLayout();
                content.setSpacing( true );
                content.setMargin( true );
                Label token = new Label( userToken.getFullToken() );
                Button close = new Button( "Close" );
                close.addClickListener( new Button.ClickListener()
                {
                    @Override
                    public void buttonClick( Button.ClickEvent event )
                    {
                        window.close();
                    }
                } );
                content.addComponent( token );
                content.addComponent( close );
                content.setComponentAlignment( token, Alignment.BOTTOM_CENTER );
                content.setComponentAlignment( close, Alignment.BOTTOM_CENTER );
                window.setContent( content );
                UI.getCurrent().addWindow( window );
            }
        } );
        row.getItemProperty( "Token" ).setValue( generate );
        final Button edit = new Button( "Edit" );
        edit.setData( newItemId );
        edit.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent event )
            {
                final Window window = new Window( "Edit token info" );
                window.center();
                window.setClosable( false );
                window.addStyleName( "default" );
                VerticalLayout content = new VerticalLayout();
                content.setSpacing( true );
                content.setMargin( true );
                HorizontalLayout fieldGrid = new HorizontalLayout();
                fieldGrid.setSpacing( true );
                final TextField editTokenName = new TextField( "Edit token name" );
                editTokenName.setValue(
                        tokenTable.getItem( edit.getData() ).getItemProperty( "Token Name" ).getValue().toString() );

                final ComboBox editUser = new ComboBox( "Switch user" );
                editUser.setNullSelectionAllowed( false );
                editUser.setTextInputAllowed( false );

                User activeUser = identityManager.getActiveUser();
                boolean viewAllList = checkUserPermission(activeUser);

                boolean set = false;
                for ( User u : identityManager.getAllUsers() )
                {
                    if(viewAllList || u.getId() == activeUser.getId())
                    {
                        editUser.addItem( u );
                        editUser.setItemCaption( u, u.getUserName() );
                        if ( u.getUserName() == tokenTable.getItem( edit.getData() ).getItemProperty( "Username" )
                                                          .getValue() )
                        {
                            set = true;
                            editUser.setValue( u );
                        }
                    }
                }
                if ( !set )
                {
                    editUser.setValue( identityManager.getAllUsers().get( 0 ) );
                }
                fieldGrid.addComponent( editTokenName );
                fieldGrid.addComponent( editUser );
                HorizontalLayout buttonsGrid = new HorizontalLayout();
                buttonsGrid.setSpacing( true );
                Button close = new Button( "Close" );
                close.addClickListener( new Button.ClickListener()
                {
                    @Override
                    public void buttonClick( Button.ClickEvent event )
                    {
                        window.close();
                    }
                } );
                final Button save = new Button( "Save" );
                save.addStyleName( "default" );
                save.setData( newItemId );
                save.addClickListener( new Button.ClickListener()
                {
                    @Override
                    public void buttonClick( Button.ClickEvent event )
                    {
                        boolean exists = false;
                        List<UserToken> userTokens = identityManager.getAllUserTokens();
                        for ( UserToken ut : userTokens )
                        {
                            if ( ut.getToken().equals( editTokenName.getValue() ) )
                            {
                                exists = true;
                                break;
                            }
                        }
                        if ( !exists )
                        {
                            String date = tokenTable.getItem( save.getData() ).getItemProperty( "TTL/Date" ).getValue()
                                                    .toString();
                            SimpleDateFormat format = new SimpleDateFormat( "dd/MM/yyyy HH:mm:ss", Locale.ENGLISH );
                            try
                            {
                                java.util.Date editDate = format.parse( date );
                                Date sqlDate = new Date( editDate.getTime() );
                                identityManager.updateUserToken(
                                        tokenTable.getItem( save.getData() ).getItemProperty( "Token Name" ).getValue()
                                                  .toString(), ( User ) editUser.getValue(), editTokenName.getValue(),
                                        null, "issuer", 1, sqlDate );
                                tokenTable.getItem( save.getData() ).getItemProperty( "Token Name" )
                                          .setValue( editTokenName.getValue() );
                                tokenTable.getItem( save.getData() ).getItemProperty( "Username" )
                                          .setValue( ( ( User ) editUser.getValue() ).getUserName() );
                                window.close();
                                Notification notif = new Notification( "Token successfully edited" );
                                notif.setDelayMsec( 2000 );
                                notif.show( Page.getCurrent() );
                            }
                            catch ( ParseException e )
                            {
                            }
                            // TODO: save in db
                        }
                        else
                        {
                            Notification notif = new Notification( "Token already exists" );
                            notif.setDelayMsec( 2000 );
                            notif.show( Page.getCurrent() );
                        }
                    }
                } );
                buttonsGrid.addComponent( close );
                buttonsGrid.addComponent( save );
                buttonsGrid.setComponentAlignment( close, Alignment.BOTTOM_CENTER );
                buttonsGrid.setComponentAlignment( save, Alignment.BOTTOM_CENTER );
                content.addComponent( fieldGrid );
                content.addComponent( buttonsGrid );
                content.setComponentAlignment( fieldGrid, Alignment.BOTTOM_CENTER );
                content.setComponentAlignment( buttonsGrid, Alignment.BOTTOM_CENTER );
                window.setContent( content );
                UI.getCurrent().addWindow( window );
            }
        } );
        row.getItemProperty( "Edit" ).setValue( edit );
        final Button delete = new Button( "Delete" );
        delete.setData( newItemId );
        delete.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent event )
            {
                identityManager.removeUserToken(
                        tokenTable.getItem( delete.getData() ).getItemProperty( "Token Name" ).getValue().toString() );
                tokenTable.removeItem( delete.getData() );
            }
        } );
        row.getItemProperty( "Delete" ).setValue( delete );

    }


    private void init()
    {
        installationControls.addComponent( rangeButtons );
        installationControls.addComponent( generateTokenBtn );
        installationControls.addComponent( tokenTable );
        setContent( installationControls );
    }

}