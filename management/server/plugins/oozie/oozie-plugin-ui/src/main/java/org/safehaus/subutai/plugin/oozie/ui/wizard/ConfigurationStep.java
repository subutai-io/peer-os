/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.oozie.ui.wizard;


import java.util.List;

import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;

import com.google.common.base.Strings;
import com.vaadin.data.Property;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;


/**
 * @author dilshat
 */
public class ConfigurationStep extends Panel
{

    private final ComboBox hadoopClusters;
    private TextField oozieClusterName;


    public ConfigurationStep( final Wizard wizard )
    {

        setSizeFull();

        GridLayout content = new GridLayout( 1, 2 );
        content.setSizeFull();
        content.setSpacing( true );
        content.setMargin( true );

        hadoopClusters = new ComboBox( "Hadoop cluster" );
        oozieClusterName = new TextField();
        oozieClusterName.setCaption( "Oozie cluster name" );

        hadoopClusters.setImmediate( true );
        hadoopClusters.setTextInputAllowed( false );
        hadoopClusters.setRequired( true );
        hadoopClusters.setNullSelectionAllowed( false );

        List<HadoopClusterConfig> clusters = wizard.getOozieUI().getHadoopManager().getClusters();
        if ( !clusters.isEmpty() )
        {
            for ( HadoopClusterConfig config : clusters )
            {
                hadoopClusters.addItem( config );
                hadoopClusters.setItemCaption( config, config.getClusterName() );
            }
        }

        HadoopClusterConfig info =
                wizard.getOozieUI().getHadoopManager().getCluster( wizard.getConfig().getClusterName() );

        if ( info != null )
        {
            hadoopClusters.setValue( info );
        }
        else if ( !clusters.isEmpty() )
        {
            hadoopClusters.setValue( clusters.iterator().next() );
        }

        if ( hadoopClusters.getValue() != null )
        {
            HadoopClusterConfig hadoopInfo = ( HadoopClusterConfig ) hadoopClusters.getValue();
            wizard.getConfig().setClusterName( hadoopInfo.getClusterName() );
        }

        hadoopClusters.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                if ( event.getProperty().getValue() != null )
                {
                    HadoopClusterConfig hadoopInfo = ( HadoopClusterConfig ) event.getProperty().getValue();
                    wizard.getConfig().setClusterName( oozieClusterName.getValue() );
                    wizard.getConfig().setHadoopClusterName( hadoopInfo.getClusterName() );
                }
            }
        } );


        Button next = new Button( "Next" );
        next.addStyleName( "default" );
        next.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {

                if ( Strings.isNullOrEmpty( wizard.getConfig().getClusterName() ) )
                {
                    show( "Please, select Hadoop cluster" );
                }
                else if ( Strings.isNullOrEmpty( wizard.getConfig().getHadoopClusterName() ) )
                {
                    show( "Please, provide oozie cluster name" );
                }
                else
                {
                    wizard.next();
                }
            }
        } );

        Button back = new Button( "Back" );
        back.addStyleName( "default" );
        back.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                wizard.back();
            }
        } );

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing( true );
        layout.addComponent( new Label( "Please, specify installation settings" ) );
        layout.addComponent( content );

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.addComponent( back );
        buttons.addComponent( next );

        content.addComponent( hadoopClusters );
        content.addComponent( oozieClusterName );
        content.addComponent( buttons );

        setContent( layout );
    }


    private void show( String notification )
    {
        Notification.show( notification );
    }
}
