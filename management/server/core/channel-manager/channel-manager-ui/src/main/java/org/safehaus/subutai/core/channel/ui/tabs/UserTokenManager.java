package org.safehaus.subutai.core.channel.ui.tabs;


import org.safehaus.subutai.core.channel.api.ChannelManager;
import org.safehaus.subutai.core.channel.api.entity.IUserChannelToken;
import org.safehaus.subutai.core.channel.api.token.ChannelTokenManager;
import org.safehaus.subutai.core.identity.api.IdentityManager;
import org.safehaus.subutai.core.identity.api.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;

import java.sql.Timestamp;
import java.util.UUID;


/**
 * Created by ermek on 3/4/15.
 */
public class UserTokenManager extends Panel
{
    private static final Logger LOGGER = LoggerFactory.getLogger( UserTokenManager.class );

    private ChannelManager  channelManager;
    private IdentityManager identityManager;

    private GridLayout installationControls;
    private TextField ipRangeStartTxtFld;
    private TextField ipRangeEndTxtFld;
    private TextField validityPeriodTxtFld;
    private TextField tokenTxtFld;
    private TextField statusTxtFld;
    private TextField dateTxtFld;
    private Button generateTokenBtn;
    private HorizontalLayout rangeButtons;
    private User user;


    public UserTokenManager( final ChannelManager channelManager , final IdentityManager identityManager)
    {
        this.channelManager  = channelManager;
        this.identityManager = identityManager;

        user = identityManager.getUser();
        IUserChannelToken userChannelToken = channelManager.getChannelTokenManager().getUserChannelTokenData(user.getId() );



        installationControls = new GridLayout( 1, 6 );
        installationControls.setSizeFull();
        installationControls.setSpacing( true );
        installationControls.setMargin( true );

        ipRangeStartTxtFld = new TextField( "Enter IP range start" );
        ipRangeStartTxtFld.setId( "IpRangeStart" );
        ipRangeStartTxtFld.setInputPrompt( "IP range start" );
        ipRangeStartTxtFld.setRequired( true );


        ipRangeEndTxtFld = new TextField( "Enter IP range end" );
        ipRangeEndTxtFld.setId( "IpRangeEnd" );
        ipRangeEndTxtFld.setInputPrompt( "IP range end" );
        ipRangeEndTxtFld.setRequired( true );


        validityPeriodTxtFld = new TextField( "Enter validity period (hours)" );
        validityPeriodTxtFld.setId( "ValidityPeriod" );
        validityPeriodTxtFld.setInputPrompt( "Validity Period" );
        validityPeriodTxtFld.setRequired( true );

        dateTxtFld = new TextField( "Date" );
        dateTxtFld.setId( "TokenDate" );
        dateTxtFld.setInputPrompt( "Date" );
        dateTxtFld.setRequired( false );
        dateTxtFld.setWidth( 180, Unit.PIXELS );

        tokenTxtFld = new TextField( "Token" );
        tokenTxtFld.setId( "Token" );
        tokenTxtFld.setRequired( false );
        tokenTxtFld.setReadOnly( false );
        tokenTxtFld.setWidth( 280,Unit.PIXELS );

        statusTxtFld = new TextField( "Status" );
        statusTxtFld.setId( "Status" );
        statusTxtFld.setReadOnly( false );
        statusTxtFld.setRequired( false );

        generateTokenBtn = new Button( "Generate Token" );
        generateTokenBtn.setId( "GenerateToken" );
        generateTokenBtn.addStyleName( "default" );
        generateTokenBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent )
            {
                IUserChannelToken userChannelToken =  channelManager.getChannelTokenManager().createUserChannelToken();

                userChannelToken.setUserId(user.getId());
                userChannelToken.setToken( UUID.randomUUID().toString() );
                userChannelToken.setValidPeriod( Short.valueOf( validityPeriodTxtFld.getValue() ) );
                userChannelToken.setIpRangeStart( ipRangeStartTxtFld.getValue() );
                userChannelToken.setIpRangeEnd( ipRangeEndTxtFld.getValue() );
                userChannelToken.setStatus( (short)1 );
                userChannelToken.setDate( new Timestamp( System.currentTimeMillis() ));


                channelManager.getChannelTokenManager().saveUserChannelToken( userChannelToken );


                setValues(userChannelToken);
            }
        } );



        rangeButtons = new HorizontalLayout();
        rangeButtons.addComponent( ipRangeStartTxtFld );
        rangeButtons.addComponent( ipRangeEndTxtFld );


        setValues(userChannelToken);


        init();
    }


    private void init()
    {
        installationControls.addComponent( rangeButtons );
        installationControls.addComponent( validityPeriodTxtFld );
        installationControls.addComponent( statusTxtFld );
        installationControls.addComponent( dateTxtFld );
        installationControls.addComponent( tokenTxtFld );
        installationControls.addComponent( generateTokenBtn );

        setContent( installationControls );
    }
    private void setValues(IUserChannelToken userChannelToken)
    {
        if(userChannelToken != null)
        {
            ipRangeStartTxtFld.setValue( userChannelToken.getIpRangeStart() );
            ipRangeEndTxtFld.setValue( userChannelToken.getIpRangeEnd() );
            tokenTxtFld.setValue( userChannelToken.getToken() );
            validityPeriodTxtFld.setValue( Short.toString( userChannelToken.getValidPeriod() ));
            dateTxtFld.setValue(userChannelToken.getDate().toString());

            if(userChannelToken.getStatus() == 1)
            {
                statusTxtFld.setValue( "Token is Valid" );
            }
            else
            {
                statusTxtFld.setValue( "Token is Expired" );
            }


        }
    }
}