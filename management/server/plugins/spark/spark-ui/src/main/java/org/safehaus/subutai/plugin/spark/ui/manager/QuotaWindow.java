package org.safehaus.subutai.plugin.spark.ui.manager;


import org.safehaus.subutai.core.peer.api.ContainerHost;

import com.vaadin.server.Sizeable;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;


/**
 * Window for managing container resource quotas
 */
public class QuotaWindow extends Window
{

    private ContainerHost host;
    private TextField cpuTxt;
    private TextField ramTxt;
    private TextField diskTxt;


    public QuotaWindow( ContainerHost host )
    {
        super( "Quotas" );

        this.host = host;

        setModal( true );
        setWidth( 350, Sizeable.Unit.PIXELS );
        setHeight( 250, Sizeable.Unit.PIXELS );

        GridLayout content = new GridLayout( 3, 4 );
        content.setSizeFull();
        content.setMargin( true );
        content.setSpacing( true );
        //ram
        Label ramLbl = new Label( "RAM" );
        ramLbl.addStyleName( "v-label-dark" );
        ramTxt = new TextField();
        Button ramBtn = new Button( "Set" );
        //cpu
        Label cpuLbl = new Label( "CPU" );
        cpuLbl.addStyleName( "v-label-dark" );
        cpuTxt = new TextField();
        Button cpuBtn = new Button( "Set" );
        //disk
        Label diskLbl = new Label( "DISK" );
        diskLbl.addStyleName( "v-label-dark" );
        diskTxt = new TextField();
        Button diskBtn = new Button( "Set" );
        //close
        Button closeBtn = new Button( "Close" );

        content.addComponent( ramLbl );
        content.addComponent( ramTxt );
        content.addComponent( ramBtn );

        content.addComponent( cpuLbl );
        content.addComponent( cpuTxt );
        content.addComponent( cpuBtn );

        content.addComponent( diskLbl );
        content.addComponent( diskTxt );
        content.addComponent( diskBtn );
        content.addComponent( closeBtn, 2, 3 );

        //TODO implement click handlers for each quota type  after QuotaManager is completed

        ramBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent event )
            {
                Notification.show( "RAM" );
            }
        } );
        cpuBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent event )
            {
                Notification.show( "CPU" );
            }
        } );
        diskBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent event )
            {
                Notification.show( "DISK" );
            }
        } );
        closeBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent event )
            {
                close();
            }
        } );

        setContent( content );

        readQuotas( host );
    }


    private void readQuotas( ContainerHost host )
    {
        //TODO read current quotas and fill-in relevant input components
        ramTxt.setValue( "1G" );
        cpuTxt.setValue( "50%" );
        diskTxt.setValue( "40G" );
    }
}
