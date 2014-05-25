/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.ui.hbase.wizard;


import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import org.safehaus.subutai.shared.protocol.Agent;

import java.util.ArrayList;
import java.util.UUID;


/**
 * @author dilshat
 */
public class StepSetMaster extends Panel
{

    public StepSetMaster( final Wizard wizard )
    {
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSizeFull();
        verticalLayout.setHeight( 100, Sizeable.UNITS_PERCENTAGE );
        verticalLayout.setMargin( true );

        GridLayout grid = new GridLayout( 10, 10 );
        grid.setSpacing( true );
        grid.setSizeFull();

        Panel panel = new Panel();
        Label menu = new Label( "Cluster Installation Wizard" );

        menu.setContentMode( Label.CONTENT_XHTML );
        panel.addComponent( menu );
        grid.addComponent( menu, 0, 0, 2, 1 );
        grid.setComponentAlignment( panel, Alignment.TOP_CENTER );

        VerticalLayout verticalLayoutForm = new VerticalLayout();
        verticalLayoutForm.setSizeFull();
        verticalLayoutForm.setSpacing( true );

        Label configServersLabel = new Label( "<strong>Choose hosts that will act as Master" );
        configServersLabel.setContentMode( Label.CONTENT_XHTML );
        verticalLayoutForm.addComponent( configServersLabel );

        final TwinColSelect selectMaster = new TwinColSelect( "", new ArrayList<Agent>() );
        selectMaster.setItemCaptionPropertyId( "hostname" );
        selectMaster.setRows( 7 );
        selectMaster.setNullSelectionAllowed( true );
        selectMaster.setMultiSelect( false );
        selectMaster.setImmediate( true );
        selectMaster.setLeftColumnCaption( "Available Nodes" );
        selectMaster.setRightColumnCaption( "Master" );
        selectMaster.setWidth( 100, Sizeable.UNITS_PERCENTAGE );
        selectMaster.setRequired( true );

        verticalLayoutForm.addComponent( selectMaster );

        grid.addComponent( verticalLayoutForm, 3, 0, 9, 9 );
        grid.setComponentAlignment( verticalLayoutForm, Alignment.TOP_CENTER );

        Button next = new Button( "Next" );
        next.addListener( new Button.ClickListener()
        {

            @Override
            public void buttonClick( Button.ClickEvent event )
            {
                wizard.getConfig().setMaster( ( UUID ) selectMaster.getValue() );

                if ( wizard.getConfig().getMaster() == null )
                {
                    show( "Please add master servers" );
                }
                else
                {
                    wizard.next();
                }
            }
        } );

        Button back = new Button( "Back" );
        back.addListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent event )
            {
                wizard.back();
            }
        } );

        verticalLayout.addComponent( grid );

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.addComponent( back );
        horizontalLayout.addComponent( next );
        verticalLayout.addComponent( horizontalLayout );

        addComponent( verticalLayout );

        selectMaster
            .setContainerDataSource( new BeanItemContainer<UUID>( UUID.class, wizard.getConfig().getNodes() ) );
        selectMaster.setValue( wizard.getConfig().getMaster() );
    }


    private void show( String notification )
    {
        getWindow().showNotification( notification );
    }

}
