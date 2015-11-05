package io.subutai.core.identity.ui.tabs;


import com.vaadin.data.Item;
import com.vaadin.ui.*;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.identity.api.model.UserToken;
import io.subutai.server.ui.component.ConfirmationDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Calendar;

public class TokensTab extends Panel
{
    private static final Logger LOGGER = LoggerFactory.getLogger( TokensTab.class );
    private static final String USER_NAME = "User name";
    private static final String TOKEN_NAME = "Token name";
   /* private static final String IP_RANGE = "Ip range";
    private static final String TTL = "TTL";*/
    private static final String CREATE_DATE = "Create date";
    private static final String STATUS = "Status";

    private IdentityManager identityManager;

    private GridLayout installationControls;
    private HorizontalLayout rangeButtons;
/*
    private TextField ipRangeStartTxtFld;
    private TextField ipRangeEndTxtFld;
*/
    private TextField validityPeriodTxtFld;
    private TextField tokenNameTxtFld;
/*    private TextField editIpRangeStartTxtFld;
    private TextField editIpRangeEndTxtFld;*/
    private TextField editValidityPeriodTxtFld;
    private TextField editTokenTxtFld;
    private TextField statusTxtFld;
    private Table tokenTable;
    private ComboBox userCombo;
    private Button generateTokenBtn;
    private Button editSubBtn;
    private Window subWindow;


    public TokensTab(final IdentityManager identityManager)
    {
        this.identityManager = identityManager;

        installationControls = new GridLayout( 1, 2 );
        installationControls.setSizeFull();
        installationControls.setSpacing( true );
        installationControls.setMargin( true );

/*        ipRangeStartTxtFld = new TextField( "Enter IP range start" );
        ipRangeStartTxtFld.setId( "IpRangeStart" );
        ipRangeStartTxtFld.setInputPrompt( "IP range start" );
        ipRangeStartTxtFld.setRequired( true );
        ipRangeStartTxtFld.setValue( "*" );

        ipRangeEndTxtFld = new TextField( "Enter IP range end" );
        ipRangeEndTxtFld.setId( "IpRangeEnd" );
        ipRangeEndTxtFld.setInputPrompt( "IP range end" );
        ipRangeEndTxtFld.setRequired( true );
        ipRangeEndTxtFld.setValue( "*" );*/

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
        for (User u : identityManager.getAllUsers())
		{
			userCombo.addItem (u);
			userCombo.setItemCaption (u, u.getUserName());
			if (!set)
			{
				userCombo.setValue (u);
				set = true;
			}
		}
/*        userCombo.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                User selectedUser = (User) event.getProperty().getValue();
                //userName = selectedUser.getUsername();
                //userId = selectedUser.getId();
            }
        } );*/
        //----------------------------------------------------------------------------------------------
        /*
        if ( user.isAdmin() )
        {
            userCombo.setVisible( true );

            List<User> userList = identityManager.getAllUsers();

            for ( User user : userList )
            {
                userCombo.addItem( user );
                userCombo.setItemCaption( user, user.getUsername() );
            }
        }
        else
        {
            userCombo.setVisible( false );
        }*/
        //----------------------------------------------------------------------------------------------



/*        editIpRangeStartTxtFld = new TextField( "Enter IP range start" );
        editIpRangeStartTxtFld.setId( "EditIpRangeStart" );
        editIpRangeStartTxtFld.setInputPrompt( "IP range start" );
        editIpRangeStartTxtFld.setRequired( true );


        editIpRangeEndTxtFld = new TextField( "Enter IP range end" );
        editIpRangeEndTxtFld.setId( "EditIpRangeEnd" );
        editIpRangeEndTxtFld.setInputPrompt( "IP range end" );
        editIpRangeEndTxtFld.setRequired( true );*/


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
        rangeButtons.setSpacing (true);
        rangeButtons.addComponent( tokenNameTxtFld );
        /*rangeButtons.addComponent( ipRangeStartTxtFld );
        rangeButtons.addComponent( ipRangeEndTxtFld );*/
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
				// TODO: check if already exists or not
				Timestamp datetime = new Timestamp( System.currentTimeMillis() );
				final String uuid = UUID.randomUUID().toString();
				Date newDate = new Date (datetime.getTime());
				java.util.Calendar cal = Calendar.getInstance();
				cal.setTime (newDate);
				cal.add (Calendar.HOUR_OF_DAY, Integer.parseInt(validityPeriodTxtFld.getValue()));
				newDate = new Date (cal.getTime().getTime());
				final UserToken userToken = identityManager.createUserToken ((User) userCombo.getValue(), tokenNameTxtFld.getValue(), null, "issuer", 1, newDate); // TODO: fix issuer and type
				// TODO: add token to db
				Object newItemId = tokenTable.addItem();
				Item row = tokenTable.getItem (newItemId);
				row.getItemProperty ("Username").setValue (userToken.getUser().getUserName());
				row.getItemProperty ("Token Name").setValue (userToken.getToken());
				String date = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format (userToken.getValidDate());
				row.getItemProperty ("TTL/Date").setValue (date);
				row.getItemProperty ("Type").setValue (String.valueOf (userToken.getType()));
				row.getItemProperty ("Hash Algo").setValue (userToken.getHashAlgorithm());
				row.getItemProperty ("Issuer").setValue (userToken.getIssuer());
				Button generate = new Button ("Show Token");
				generate.addClickListener (new Button.ClickListener()
				{
					@Override
					public void buttonClick (Button.ClickEvent event)
					{
						final Window window = new Window ("Full token");
						window.center();
						window.setClosable (false);
						window.addStyleName ("default");
						VerticalLayout content = new VerticalLayout();
						content.setSpacing (true);
						content.setMargin (true);
						Label token = new Label (userToken.getFullToken());
						Button close = new Button ("Close");
						close.addClickListener (new Button.ClickListener()
						{
							@Override
							public void buttonClick (Button.ClickEvent event)
							{
								window.close();
							}
						});
						content.addComponent (token);
						content.addComponent (close);
						content.setComponentAlignment (token, Alignment.BOTTOM_CENTER);
						content.setComponentAlignment (close, Alignment.BOTTOM_CENTER);
						window.setContent (content);
						UI.getCurrent().addWindow (window);
					}
				});
				row.getItemProperty ("Token").setValue (generate);
				final Button edit = new Button ("Edit");
				edit.setData (newItemId);
				edit.addClickListener (new Button.ClickListener()
				{
					@Override
					public void buttonClick (Button.ClickEvent event)
					{
						final Window window = new Window ("Edit token info");
						window.center();
						window.setClosable (false);
						window.addStyleName ("default");
						VerticalLayout content = new VerticalLayout();
						content.setSpacing (true);
						content.setMargin (true);
						HorizontalLayout fieldGrid = new HorizontalLayout();
						fieldGrid.setSpacing (true);
						final TextField editTokenName = new TextField ("Edit token name");
						editTokenName.setValue (tokenTable.getItem (edit.getData()).getItemProperty ("Token Name").getValue().toString());

						final ComboBox editUser = new ComboBox ("Switch user");
						boolean set = false;
						for (User u : identityManager.getAllUsers())
						{
							editUser.addItem (u);
							editUser.setItemCaption (u, u.getUserName());
							if (u.getUserName() == tokenTable.getItem (edit.getData()).getItemProperty ("Username").getValue())
							{
								set = true;
								editUser.setValue (u);
							}
						}
						if (!set)
						{
							editUser.setValue (identityManager.getAllUsers().get (0));
						}
						fieldGrid.addComponent (editTokenName);
						fieldGrid.addComponent (editUser);
						Button close = new Button ("Close");
						close.addClickListener (new Button.ClickListener()
						{
							@Override
							public void buttonClick (Button.ClickEvent event)
							{
								window.close();
							}
						});
						Button save = new Button ("Save");
						save.addStyleName ("default");
						save.addClickListener (new Button.ClickListener()
						{
							@Override
							public void buttonClick (Button.ClickEvent event)
							{
								// TODO: add checks
								userToken.setUser ((User) editUser.getValue());
								userToken.setToken (editTokenName.getValue());
								// TODO: save in db
							}
						});
						content.addComponent (fieldGrid);
						content.addComponent (close);
						content.setComponentAlignment (fieldGrid, Alignment.BOTTOM_CENTER);
						content.setComponentAlignment (close, Alignment.BOTTOM_CENTER);
						window.setContent (content);
						UI.getCurrent().addWindow (window);
					}
				});
				row.getItemProperty ("Edit").setValue (edit);
				final Button delete = new Button ("Delete");
				delete.setData (newItemId);
				delete.addClickListener (new Button.ClickListener()
				{
					@Override
					public void buttonClick (Button.ClickEvent event)
					{
						// TODO: delete token from where it is stored
						tokenTable.removeItem (delete.getData());
					}
				});
				row.getItemProperty ("Delete").setValue (delete);
				/*
				IUserChannelToken userChannelToken = channelManager.getChannelTokenManager().createUserChannelToken();

                userChannelToken.setUserId( userId );
                userChannelToken.setToken( uuid );
                userChannelToken.setTokenName( tokenNameTxtFld.getValue() );
                userChannelToken.setValidPeriod( Short.valueOf( validityPeriodTxtFld.getValue() ) );
                userChannelToken.setIpRangeStart( ipRangeStartTxtFld.getValue() );
                userChannelToken.setIpRangeEnd( ipRangeEndTxtFld.getValue() );
                userChannelToken.setStatus( ( short ) 1 );
                userChannelToken.setDate( datetime );

                channelManager.getChannelTokenManager().saveUserChannelToken( userChannelToken );

                setUserChannelList();
                */
			}
		} );



        //--------------------------------------------------------------------------------

        //userName = user.getUsername();
        //userId = user.getId();
        setUserChannelList();
        //--------------------------------------------------------------------------------

        //--------------------------------------------------------------------------------

        // Show exactly the currently contained rows (items)
        tokenTable.setPageLength( tokenTable.size() );

        // listener for edit button
        init();
    }


    private Table createTokensTable ( String caption )
    {
        Table table = new Table( caption );
        table.addContainerProperty( USER_NAME, String.class, null );
        table.addContainerProperty( TOKEN_NAME, String.class, null );
//        table.addContainerProperty( IP_RANGE, String.class, null );
       // table.addContainerProperty( TTL, String.class, null );
        table.addContainerProperty( CREATE_DATE, String.class, null );
        table.addContainerProperty( STATUS, String.class, null );
        table.addContainerProperty( "", Button.class, null );
        table.addContainerProperty( "", Button.class, null );
        table.setPageLength( 10 );
        table.setSelectable( false );
        table.setEnabled( true );
        table.setImmediate( true );
        table.setSizeFull();
        return table;
    }


    private void init()
    {
        installationControls.addComponent( rangeButtons );
        installationControls.addComponent( generateTokenBtn );
        installationControls.addComponent( tokenTable );
        setContent( installationControls );
    }


    private void openEditWindow( final Button.ClickEvent clickEvent )
    {
        //final IUserChannelToken userChannelToken = ( IUserChannelToken ) clickEvent.getButton().getData();

        subWindow = new Window();

        editSubBtn = new Button( "Save" );
        editSubBtn.setId( "EditSubBtn" );
        editSubBtn.addStyleName( "default" );


        VerticalLayout content = new VerticalLayout();
        content.setMargin( true );

        //editTokenTxtFld.setValue( userChannelToken.getTokenName() );
        //editIpRangeStartTxtFld.setValue( userChannelToken.getIpRangeStart() );
        //editIpRangeEndTxtFld.setValue( userChannelToken.getIpRangeEnd() );
        //editValidityPeriodTxtFld.setValue( Short.toString( userChannelToken.getValidPeriod() ) );

		content.setSpacing (true);
		content.setMargin (true);
        content.addComponent( editTokenTxtFld );
/*        content.addComponent( editIpRangeStartTxtFld );
        content.addComponent( editIpRangeEndTxtFld );*/
        content.addComponent( editValidityPeriodTxtFld );
        content.addComponent( editSubBtn );

        editSubBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent)
            {
                Timestamp datetime = new Timestamp( System.currentTimeMillis() );
                /*
                userChannelToken.setDate( datetime );
                userChannelToken.setTokenName( editTokenTxtFld.getValue() );
                userChannelToken.setIpRangeStart( editIpRangeStartTxtFld.getValue() );
                userChannelToken.setIpRangeEnd( editIpRangeEndTxtFld.getValue() );
                userChannelToken.setValidPeriod( Short.valueOf( editValidityPeriodTxtFld.getValue() ) );
                channelManager.getChannelTokenManager().saveUserChannelToken( userChannelToken );

                Item rowItem = tokenTable.getItem( userChannelToken );
                Property property1 = rowItem.getItemProperty( "Token Name" );
                Property property2 = rowItem.getItemProperty( IP_RANGE );
                Property property3 = rowItem.getItemProperty("TTL");
                Property property4 = rowItem.getItemProperty( CREATE_DATE );

                property1.setValue(userChannelToken.getTokenName());
                property2.setValue(userChannelToken.getIpRangeStart()+"-"+userChannelToken.getIpRangeEnd());
                property3.setValue(Short.toString( userChannelToken.getValidPeriod()));
                property4.setValue(userChannelToken.getDate().toString());
                */
                subWindow.close();
            }
        } );

        subWindow.center();
        subWindow.setModal( true );
        subWindow.setImmediate( true );
        subWindow.setContent( content );
        UI.getCurrent().addWindow( subWindow );
    }


    private void setUserChannelList()
    {
        //List<IUserChannelToken> userChannelTokenList = null;

       // if ( user.isAdmin() )
        {
            //userChannelTokenList = channelManager.getChannelTokenManager().getAllUserChannelTokenData();
        }
        //else
        {
            //userChannelTokenList = channelManager.getChannelTokenManager().getUserChannelTokenData( user.getId() );
        }

        //if ( userChannelTokenList != null )
        {
            tokenTable.removeAllItems();

            //for ( IUserChannelToken userChannelToken : userChannelTokenList )
            {
                Button editBtn = new Button( "Edit" );
                editBtn.setId( "EditBtn" );
                editBtn.addStyleName( "default" );
                //editBtn.setData( userChannelToken );


                Button removeBtn = new Button( "Remove" );
                removeBtn.setId( "RemoveBtn" );
                removeBtn.addStyleName( "default" );
                //removeBtn.setData( userChannelToken );

                editBtn.addClickListener( new Button.ClickListener()
                {
                    @Override
                    public void buttonClick( final Button.ClickEvent clickEvent )
                    {
                        openEditWindow( clickEvent );
                    }
                } );

                removeBtn.addClickListener( new Button.ClickListener()
                {
                    @Override
                    public void buttonClick( final Button.ClickEvent clickEvent )
                    {
                        final Button.ClickEvent clickEventCustom = clickEvent;

                        ConfirmationDialog alert = new ConfirmationDialog( String.format( "Do you want to remove token?"),"Yes", "No" );
                        alert.getOk().addClickListener( new Button.ClickListener()
                        {
                            @Override
                            public void buttonClick( Button.ClickEvent clickEvent )
                            {
                                //-----------------------------------------------------------
                                //String token = ( ( IUserChannelToken ) clickEventCustom.getButton().getData() ).getToken();
                                //channelManager.getChannelTokenManager().removeUserChannelToken( token );
                                setUserChannelList();
                                //-----------------------------------------------------------
                            }
                        });
                        UI.getCurrent().addWindow( alert.getAlert() );
                    }
                } );

                //tokenTable.addItem( new Object[] {
                        //identityManager.getUser( userChannelToken.getUserId() ).getUsername(),
                        //userChannelToken.getTokenName(),
                       // userChannelToken.getIpRangeStart() + "-" + userChannelToken.getIpRangeEnd(),
                       // Short.toString( userChannelToken.getValidPeriod() ), userChannelToken.getDate().toString(),
                        //( userChannelToken.getValidPeriod() > 0 ) ? "Valid" : "Expired", userChannelToken.getToken(),
                       // editBtn, removeBtn
               // }, userChannelToken);
            }
        }
    }
}