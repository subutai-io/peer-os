package org.safehaus.subutai.plugin.shark.ui.wizard;


import com.google.common.base.Strings;
import com.vaadin.data.Property;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import java.util.List;
import org.safehaus.subutai.plugin.spark.api.Spark;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;


public class ConfigurationStep extends Panel
{

    public ConfigurationStep( Spark spark, final Wizard wizard )
    {

        setSizeFull();

        GridLayout content = new GridLayout( 1, 2 );
        content.setSizeFull();
        content.setSpacing( true );
        content.setMargin( true );

        ComboBox sparkClusters = new ComboBox( "Spark cluster" );

        sparkClusters.setImmediate( true );
        sparkClusters.setTextInputAllowed( false );
        sparkClusters.setRequired( true );
        sparkClusters.setNullSelectionAllowed( false );

        List<SparkClusterConfig> clusters = spark.getClusters();
        if ( !clusters.isEmpty() )
        {
            for ( SparkClusterConfig info : clusters )
            {
                sparkClusters.addItem( info );
                sparkClusters.setItemCaption( info, info.getClusterName() );
            }
        }

        sparkClusters.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                if ( event.getProperty().getValue() != null )
                {
                    SparkClusterConfig config = ( SparkClusterConfig ) event.getProperty().getValue();
                    wizard.getConfig().setClusterName( config.getClusterName() );
                    wizard.getConfig().setNodes( config.getAllNodes() );
                }
            }


        } );


        if ( Strings.isNullOrEmpty( wizard.getConfig().getClusterName() ) )
        {
            if ( !clusters.isEmpty() )
            {
                sparkClusters.setValue( clusters.iterator().next() );
            }
        }
        else
        {
            SparkClusterConfig info = spark.getCluster( wizard.getConfig().getClusterName() );
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


        Button next = new Button( "Next" );
        next.addStyleName( "default" );
        next.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                if ( Strings.isNullOrEmpty( wizard.getConfig().getClusterName() ) )
                {
                    show( "Please, select Spark cluster" );
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

        content.addComponent( sparkClusters );
        content.addComponent( buttons );

        setContent( layout );
    }


    private void show( String notification )
    {
        Notification.show( notification );
    }


}

