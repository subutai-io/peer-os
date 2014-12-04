package org.safehaus.subutai.plugin.storm.ui.wizard;


import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.storm.api.StormClusterConfiguration;
import org.safehaus.subutai.plugin.zookeeper.api.Zookeeper;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;

import com.google.common.base.Strings;
import com.vaadin.data.Property;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;


public class NodeSelectionStep extends Panel
{

    public NodeSelectionStep( final Zookeeper zookeeper, final Wizard wizard,
                              final EnvironmentManager environmentManager )
    {

        setSizeFull();

        GridLayout content = new GridLayout( 1, 2 );
        content.setSizeFull();
        content.setSpacing( true );
        content.setMargin( true );

        TextField clusterNameTxt = new TextField( "Cluster name" );
        clusterNameTxt.setId( "StormConfClusterName" );
        clusterNameTxt.setRequired( true );
        clusterNameTxt.addValueChangeListener( new Property.ValueChangeListener()
        {

            @Override
            public void valueChange( Property.ValueChangeEvent e )
            {
                wizard.getConfig().setClusterName( e.getProperty().getValue().toString().trim() );
            }
        } );

        Component nimbusElem;
        if ( wizard.getConfig().isExternalZookeeper() )
        {
            ComboBox zkClustersCombo = new ComboBox( "Zookeeper cluster" );
            zkClustersCombo.setId( "StormConfClusterCombo" );
            final ComboBox masterNodeCombo = makeMasterNodeComboBox( wizard );
            masterNodeCombo.setId( "StormMasterNodeCombo" );

            zkClustersCombo.setImmediate( true );
            zkClustersCombo.setTextInputAllowed( false );
            zkClustersCombo.setRequired( true );
            zkClustersCombo.setNullSelectionAllowed( false );
            zkClustersCombo.addValueChangeListener( new Property.ValueChangeListener()
            {
                @Override
                public void valueChange( Property.ValueChangeEvent e )
                {
                    masterNodeCombo.removeAllItems();
                    if ( e.getProperty().getValue() != null )
                    {
                        ZookeeperClusterConfig zookeeperClusterConfig = ( ZookeeperClusterConfig ) e.getProperty().getValue();
                        Environment zookeeperEnvironment =
                                environmentManager.getEnvironmentByUUID( zookeeperClusterConfig.getEnvironmentId() );
                        Set<ContainerHost> zookeeperNodes =
                                zookeeperEnvironment.getContainerHostsByIds( zookeeperClusterConfig.getNodes() );
                        for ( ContainerHost containerHost : zookeeperNodes )
                        {
                            masterNodeCombo.addItem( containerHost );
                            masterNodeCombo.setItemCaption( containerHost, containerHost.getHostname() );
                        }
                        // do select if values exist
                        if ( wizard.getConfig().getNimbus() != null )
                        {
                            masterNodeCombo.setValue( wizard.getConfig().getNimbus() );
                        }

                        wizard.setZookeeperClusterConfig( zookeeperClusterConfig );

                        wizard.getConfig().setZookeeperClusterName( zookeeperClusterConfig.getClusterName() );
                    }
                }
            } );
            List<ZookeeperClusterConfig> zk_list = zookeeper.getClusters();
            for ( ZookeeperClusterConfig zkc : zk_list )
            {
                zkClustersCombo.addItem( zkc );
                zkClustersCombo.setItemCaption( zkc, zkc.getClusterName() );
                if ( zkc.getClusterName().equals( wizard.getConfig().getZookeeperClusterName() ) )
                {
                    zkClustersCombo.setValue( zkc );
                }
            }
            if ( wizard.getConfig().getNimbus() != null )
            {
                masterNodeCombo.setValue( wizard.getConfig().getNimbus() );
            }

            HorizontalLayout hl = new HorizontalLayout( zkClustersCombo, masterNodeCombo );
            nimbusElem = new Panel( "Nimbus node", hl );
            nimbusElem.setSizeUndefined();
            nimbusElem.setStyleName( "default" );
        }
        else
        {
            String s = "<b>A new nimbus node will be created with Zookeeper instance installed</b>";
            nimbusElem = new Label( s, ContentMode.HTML );
        }

        ComboBox nodesCountCmb =
                new ComboBox( "Number of supervisor nodes", Arrays.asList( 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 ) );

        nodesCountCmb.setId( "StormConfNumSupervisorNodes" );
        nodesCountCmb.setImmediate( true );
        nodesCountCmb.setRequired( true );
        nodesCountCmb.setTextInputAllowed( false );
        nodesCountCmb.setNullSelectionAllowed( false );
        nodesCountCmb.setValue( wizard.getConfig().getSupervisorsCount() );
        nodesCountCmb.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                wizard.getConfig().setSupervisorsCount( ( Integer ) event.getProperty().getValue() );
            }
        } );

        // set selected values
        if ( wizard.getConfig().getClusterName() != null )
        {
            clusterNameTxt.setValue( wizard.getConfig().getClusterName() );
        }
        if ( wizard.getConfig().getSupervisorsCount() > 0 )
        {
            nodesCountCmb.setValue( wizard.getConfig().getSupervisorsCount() );
        }

        Button next = new Button( "Next" );
        next.setId( "StormConfNext" );
        next.addStyleName( "default" );
        next.addClickListener( new Button.ClickListener()
        {

            @Override
            public void buttonClick( Button.ClickEvent event )
            {
                StormClusterConfiguration config = wizard.getConfig();
                if ( Strings.isNullOrEmpty( config.getClusterName() ) )
                {
                    show( "Enter cluster name" );
                }
                else if ( config.isExternalZookeeper() && config.getNimbus() == null )
                {
                    show( "Select master node" );
                }
                else if ( config.getSupervisorsCount() <= 0 )
                {
                    show( "Select supervisor nodes count" );
                }
                else
                {
                    wizard.next();
                }
            }
        } );

        Button back = new Button( "Back" );
        back.setId( "StormConfBack" );
        back.addStyleName( "default" );
        back.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent event )
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

        content.addComponent( clusterNameTxt );
        content.addComponent( nimbusElem );
        content.addComponent( nodesCountCmb );
        content.addComponent( buttons );

        setContent( layout );
    }


    private ComboBox makeMasterNodeComboBox( final Wizard wizard )
    {
        ComboBox cb = new ComboBox( "Nodes" );

        cb.setId( "StormConfMasterNodes" );
        cb.setImmediate( true );
        cb.setTextInputAllowed( false );
        cb.setRequired( true );
        cb.setNullSelectionAllowed( false );
        cb.addValueChangeListener( new Property.ValueChangeListener()
        {

            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                ContainerHost serverNode = ( ContainerHost ) event.getProperty().getValue();
                wizard.getConfig().setNimbus( serverNode.getId() );
            }
        } );
        return cb;
    }


    private void show( String notification )
    {
        Notification.show( notification );
    }
}
