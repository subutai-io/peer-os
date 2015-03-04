package org.safehaus.subutai.core.channel.ui.tabs;


import org.safehaus.subutai.core.channel.api.ChannelManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;


/**
 * Created by ermek on 3/4/15.
 */
public class UserTokenManager extends Panel
{
    private static final Logger LOGGER = LoggerFactory.getLogger( UserTokenManager.class );

    private ChannelManager channelManager;

    private GridLayout installationControls;
    private TextField ipRangeStartTxtFld;
    private TextField ipRangeEndTxtFld;
    private TextField validityPeriodTxtFld;
    private TextField tokenTxtFld;
    private Button generateTokenBtn;
    private HorizontalLayout rangeButtons;


    public UserTokenManager( final ChannelManager channelManager )
    {
        this.channelManager = channelManager;

        installationControls = new GridLayout( 1, 4 );
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


        validityPeriodTxtFld = new TextField( "Enter validity period" );
        validityPeriodTxtFld.setId( "ValidityPeriod" );
        validityPeriodTxtFld.setInputPrompt( "Validity Period" );
        validityPeriodTxtFld.setRequired( true );

        tokenTxtFld = new TextField( "Token" );
        tokenTxtFld.setId( "Token" );
        tokenTxtFld.setRequired( false );
        tokenTxtFld.setReadOnly( true );



        generateTokenBtn = new Button( "Generate Token" );
        generateTokenBtn.setId( "GenerateToken" );
        generateTokenBtn.addStyleName( "default" );


        rangeButtons = new HorizontalLayout();
        rangeButtons.addComponent( ipRangeStartTxtFld );
        rangeButtons.addComponent( ipRangeEndTxtFld );

        init();
    }


    private void init()
    {
        installationControls.addComponent( rangeButtons );
        installationControls.addComponent( validityPeriodTxtFld );
        installationControls.addComponent( tokenTxtFld );
        installationControls.addComponent( generateTokenBtn );

        setContent( installationControls );
    }

}
