package io.subutai.core.channel.ui.tabs;


import org.safehaus.subutai.common.settings.ChannelSettings;
import io.subutai.core.channel.api.ChannelManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;


public class PortManager extends Panel
{
    private static final Logger LOGGER = LoggerFactory.getLogger( UserTokenManager.class );

    private ChannelManager channelManager;

    private GridLayout installationControls;
    private TextField specialPortTxtFld;
    private TextField specialSecurePortTxtFld;
    private TextField securePort1TxtFld;
    private TextField securePort2TxtFld;
    private TextField securePort3TxtFld;
    private Button saveBtn;


    public PortManager( final ChannelManager channelManager )
    {
        this.channelManager = channelManager;

        installationControls = new GridLayout( 1, 4 );
        installationControls.setSizeFull();
        installationControls.setSpacing( true );
        installationControls.setMargin( true );

        specialPortTxtFld = new TextField( "Enter special port" );
        specialPortTxtFld.setId( "IpRangeStart" );
        specialPortTxtFld.setInputPrompt( ChannelSettings.SPECIAL_PORT_X1 );
        specialPortTxtFld.setRequired( true );

        specialSecurePortTxtFld = new TextField( "Enter special secure port" );
        specialSecurePortTxtFld.setId( "IpRangeStart" );
        specialSecurePortTxtFld.setInputPrompt( ChannelSettings.SPECIAL_SECURE_PORT_X1 );
        specialSecurePortTxtFld.setRequired( true );

        securePort1TxtFld = new TextField( "Enter secure port 1" );
        securePort1TxtFld.setId( "IpRangeEnd" );
        securePort1TxtFld.setInputPrompt( ChannelSettings.SECURE_PORT_X1 );
        securePort1TxtFld.setRequired( true );

        securePort2TxtFld = new TextField( "Enter secure port 2" );
        securePort2TxtFld.setId( "ValidityPeriod" );
        securePort2TxtFld.setInputPrompt( ChannelSettings.SECURE_PORT_X2 );
        securePort2TxtFld.setRequired( true );

        securePort3TxtFld = new TextField( "Enter secure port 3" );
        securePort3TxtFld.setId( "Token" );
        securePort3TxtFld.setInputPrompt( ChannelSettings.SECURE_PORT_X3 );
        securePort3TxtFld.setRequired( true );


        saveBtn = new Button( "Save" );
        saveBtn.setId( "Save" );
        saveBtn.addStyleName( "default" );

        init();
    }


    private void init()
    {
        installationControls.addComponent( specialPortTxtFld );
        installationControls.addComponent( specialSecurePortTxtFld );
        installationControls.addComponent( securePort1TxtFld );
        installationControls.addComponent( securePort2TxtFld );
        installationControls.addComponent( securePort3TxtFld );
        installationControls.addComponent( saveBtn );

        setContent( installationControls );
    }
}