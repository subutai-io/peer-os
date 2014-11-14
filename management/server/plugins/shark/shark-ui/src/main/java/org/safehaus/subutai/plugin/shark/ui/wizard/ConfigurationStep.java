package org.safehaus.subutai.plugin.shark.ui.wizard;


import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.shark.api.SetupType;
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
                if ( config.getSetupType() == SetupType.WITH_HADOOP_SPARK )
                {
                    config.setSparkClusterName( val );
                }
            }
        } );
        nameTxt.setValue( config.getClusterName() );
        content.addComponent( nameTxt );

        if ( config.getSetupType() == SetupType.OVER_SPARK )
        {
            addOverSparkComponents( content, spark, config );
        }
        else if ( config.getSetupType() == SetupType.WITH_HADOOP_SPARK )
        {
            addWithHadoopComponents( content, config, wizard.getHadoopConfig() );
        }

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


    private void addWithHadoopComponents( ComponentContainer parent, final SharkClusterConfig config,
                                          final HadoopClusterConfig hadoopConfig )
    {
        Collection<Integer> col = Arrays.asList( 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 );

        final TextField txtHadoopClusterName = new TextField( "Hadoop cluster name" );
        txtHadoopClusterName.setRequired( true );
        txtHadoopClusterName.setMaxLength( 20 );
        if ( hadoopConfig.getClusterName() != null )
        {
            txtHadoopClusterName.setValue( hadoopConfig.getClusterName() );
        }
        txtHadoopClusterName.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                String name = event.getProperty().getValue().toString().trim();
                config.setHadoopClusterName( name );
                hadoopConfig.setClusterName( name );
            }
        } );

        ComboBox cmbSlaveNodes = new ComboBox( "Number of Hadoop slave nodes", col );
        cmbSlaveNodes.setImmediate( true );
        cmbSlaveNodes.setTextInputAllowed( false );
        cmbSlaveNodes.setNullSelectionAllowed( false );
        cmbSlaveNodes.setValue( hadoopConfig.getCountOfSlaveNodes() );
        cmbSlaveNodes.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                hadoopConfig.setCountOfSlaveNodes( ( Integer ) event.getProperty().getValue() );
            }
        } );

        ComboBox cmbReplFactor = new ComboBox( "Replication factor for Hadoop slave nodes", col );
        cmbReplFactor.setImmediate( true );
        cmbReplFactor.setTextInputAllowed( false );
        cmbReplFactor.setNullSelectionAllowed( false );
        cmbReplFactor.setValue( hadoopConfig.getReplicationFactor() );
        cmbReplFactor.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                hadoopConfig.setReplicationFactor( ( Integer ) event.getProperty().getValue() );
            }
        } );

        TextField txtHadoopDomain = new TextField( "Hadoop cluster domain name" );
        txtHadoopDomain.setInputPrompt( hadoopConfig.getDomainName() );
        txtHadoopDomain.setValue( hadoopConfig.getDomainName() );
        txtHadoopDomain.setMaxLength( 20 );
        txtHadoopDomain.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                String val = event.getProperty().getValue().toString().trim();
                if ( !val.isEmpty() )
                {
                    hadoopConfig.setDomainName( val );
                }
            }
        } );

        parent.addComponent( new Label( "Hadoop settings" ) );
        parent.addComponent( txtHadoopClusterName );
        parent.addComponent( cmbSlaveNodes );
        parent.addComponent( cmbReplFactor );
        parent.addComponent( txtHadoopDomain );
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
                return;
            }
            if ( config.getSetupType() == SetupType.OVER_SPARK )
            {
                if ( Strings.isNullOrEmpty( config.getSparkClusterName() ) )
                {
                    show( "Select Spark cluster" );
                }
                else
                {
                    wizard.next();
                }
            }
            else if ( config.getSetupType() == SetupType.WITH_HADOOP_SPARK )
            {
                HadoopClusterConfig hc = wizard.getHadoopConfig();
                if ( Strings.isNullOrEmpty( hc.getClusterName() ) )
                {
                    show( "Enter Hadoop cluster name" );
                }
                else if ( hc.getCountOfSlaveNodes() <= 0 )
                {
                    show( "Invalid number of Hadoop slave nodes" );
                }
                else if ( hc.getReplicationFactor() <= 0 )
                {
                    show( "Invalid replication factor" );
                }
                else if ( Strings.isNullOrEmpty( hc.getDomainName() ) )
                {
                    show( "Enter Hadoop domain name" );
                }
                else
                {
                    wizard.next();
                }
            }
            else
            {
                show( "Installation type not supported" );
            }
        }
    }
}

