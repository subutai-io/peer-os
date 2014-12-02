package org.safehaus.subutai.plugin.shark.ui.wizard;


import java.util.List;

import org.safehaus.subutai.plugin.shark.api.SharkClusterConfig;
import org.safehaus.subutai.plugin.spark.api.Spark;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;

import com.google.common.base.Strings;
import com.vaadin.data.Property;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;


public class ConfigurationStep extends Panel
{

    public ConfigurationStep( Spark spark, final Wizard wizard )
    {

        setSizeFull();

        GridLayout content = new GridLayout( 1, 2 );
        content.setSizeFull();
        content.setSpacing( true );
        content.setMargin( true );

        final SharkClusterConfig config = wizard.getConfig();

        TextField nameTxt = new TextField( "Cluster name" );
        nameTxt.setId( "SharkClusterName" );
        nameTxt.setRequired( true );
        nameTxt.addValueChangeListener( new Property.ValueChangeListener()
        {

            @Override
            public void valueChange( Property.ValueChangeEvent e )
            {
                String val = e.getProperty().getValue().toString().trim();
                config.setClusterName( val );
            }
        } );
        nameTxt.setValue( config.getClusterName() );
        content.addComponent( nameTxt );

        addOverSparkComponents( content, spark, config );


        Button next = new Button( "Next" );
        next.setId( "SharkNext" );
        next.addStyleName( "default" );
        next.addClickListener( new NextClickHandler( wizard ) );

        Button back = new Button( "Back" );
        back.setId( "SharkBack" );
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

        content.addComponent( buttons );

        setContent( layout );
    }


    private void addOverSparkComponents( ComponentContainer parent, Spark spark, final SharkClusterConfig config )
    {
        ComboBox sparkClusters = new ComboBox( "Spark cluster" );
        sparkClusters.setId( "sharkSparkClustersCb" );
        sparkClusters.setImmediate( true );
        sparkClusters.setTextInputAllowed( false );
        sparkClusters.setRequired( true );
        sparkClusters.setNullSelectionAllowed( false );
        sparkClusters.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                if ( event.getProperty().getValue() != null )
                {
                    SparkClusterConfig sparkConfig = ( SparkClusterConfig ) event.getProperty().getValue();
                    config.setSparkClusterName( sparkConfig.getClusterName() );
                }
            }
        } );

        List<SparkClusterConfig> clusters = spark.getClusters();
        if ( !clusters.isEmpty() )
        {
            for ( SparkClusterConfig info : clusters )
            {
                sparkClusters.addItem( info );
                sparkClusters.setItemCaption( info, info.getClusterName() );
            }
        }

        if ( Strings.isNullOrEmpty( config.getClusterName() ) )
        {
            if ( !clusters.isEmpty() )
            {
                sparkClusters.setValue( clusters.iterator().next() );
            }
        }
        else
        {
            SparkClusterConfig info = spark.getCluster( config.getClusterName() );
            if ( info != null )
            {
                //restore cluster
                sparkClusters.setValue( info );
            }
            else if ( !clusters.isEmpty() )
            {
                sparkClusters.setValue( clusters.iterator().next() );
            }
        }

        parent.addComponent( sparkClusters );
    }


    private void show( String notification )
    {
        Notification.show( notification );
    }


    private class NextClickHandler implements Button.ClickListener
    {

        private final Wizard wizard;


        public NextClickHandler( Wizard wizard )
        {
            this.wizard = wizard;
        }


        @Override
        public void buttonClick( Button.ClickEvent event )
        {
            SharkClusterConfig config = wizard.getConfig();
            if ( Strings.isNullOrEmpty( config.getClusterName() ) )
            {
                show( "Enter cluster name" );
            }
            else if ( Strings.isNullOrEmpty( config.getSparkClusterName() ) )
            {
                show( "Select Spark cluster" );
            }
            else
            {
                wizard.next();
            }
        }
    }
}

