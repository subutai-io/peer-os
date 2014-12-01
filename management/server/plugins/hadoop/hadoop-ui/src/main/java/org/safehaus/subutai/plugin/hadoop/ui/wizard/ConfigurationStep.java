/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.hadoop.ui.wizard;


import java.util.ArrayList;
import java.util.List;

import org.safehaus.subutai.core.hostregistry.api.HostRegistry;

import com.google.common.base.Strings;
import com.vaadin.data.Property;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;


public class ConfigurationStep extends VerticalLayout
{

    private static final int MAX_NUMBER_OF_NODES_PER_SERVER = 5;
    private static final String SUGGESTED_NUMBER_OF_NODES_CAPTION = " (Suggested)";


    public ConfigurationStep( final Wizard wizard, HostRegistry hostRegistry )
    {
        setSizeFull();
        GridLayout content = new GridLayout( 2, 7 );
        content.setSizeFull();
        content.setSpacing( true );
        content.setMargin( true );

        final TextField clusterNameTxtFld = new TextField( "Enter cluster name" );
        clusterNameTxtFld.setId( "HadoopClusterNameTxtField" );
        clusterNameTxtFld.setInputPrompt( "Cluster name" );
        clusterNameTxtFld.setRequired( true );
        clusterNameTxtFld.setMaxLength( 20 );
        if ( !Strings.isNullOrEmpty( wizard.getHadoopClusterConfig().getClusterName() ) )
        {
            clusterNameTxtFld.setValue( wizard.getHadoopClusterConfig().getClusterName() );
        }
        clusterNameTxtFld.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                wizard.getHadoopClusterConfig().setClusterName( event.getProperty().getValue().toString().trim() );
            }
        } );

        //configuration servers number
        List<String> slaveNodeCountList = new ArrayList<String>();
        //TODO please do not count only local resource hosts since environments can span multiple peers
        //remove host registry usage once this fix is applied
        int connected_fai_count = hostRegistry.getResourceHostsInfo().size() - 1;
        for ( int i = 1; i <= ( connected_fai_count ) * MAX_NUMBER_OF_NODES_PER_SERVER; i++ )
        {
            if ( i == connected_fai_count )
            {
                slaveNodeCountList.add( i + SUGGESTED_NUMBER_OF_NODES_CAPTION );
            }
            else
            {
                slaveNodeCountList.add( i + "" );
            }
        }

        ComboBox slaveNodesComboBox = new ComboBox( "Choose number of slave nodes", slaveNodeCountList );
        slaveNodesComboBox.setId( "HadoopSlavesNodeComboBox" );
        //        slaveNodesComboBox.setMultiSelect(false);
        slaveNodesComboBox.setImmediate( true );
        slaveNodesComboBox.setTextInputAllowed( false );
        slaveNodesComboBox.setNullSelectionAllowed( false );
        slaveNodesComboBox.setValue( wizard.getHadoopClusterConfig().getCountOfSlaveNodes() );

        // parse count of slave nodes input field
        slaveNodesComboBox.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                String slaveNodeComboBoxSelection = event.getProperty().getValue().toString();
                int slaveNodeCount;
                int suggestedNumberOfNodesCaptionStart =
                        slaveNodeComboBoxSelection.indexOf( SUGGESTED_NUMBER_OF_NODES_CAPTION.charAt( 0 ) );
                if ( suggestedNumberOfNodesCaptionStart < 0 )
                {
                    slaveNodeCount = Integer.parseInt( slaveNodeComboBoxSelection );
                }
                else
                {
                    slaveNodeCount = Integer.parseInt(
                            slaveNodeComboBoxSelection.substring( 0, suggestedNumberOfNodesCaptionStart ) );
                }
                wizard.getHadoopClusterConfig().setCountOfSlaveNodes( slaveNodeCount );
            }
        } );

        slaveNodeCountList.remove( connected_fai_count - 1 );
        slaveNodeCountList.add( connected_fai_count - 1, connected_fai_count + "" );
        //configuration replication factor
        ComboBox replicationFactorComboBox =
                new ComboBox( "Choose replication factor for slave nodes", slaveNodeCountList );
        replicationFactorComboBox.setId( "HadoopReplicationFactorComboBox" );
        //        replicationFactorComboBox.setMultiSelect(false);
        replicationFactorComboBox.setImmediate( true );
        replicationFactorComboBox.setTextInputAllowed( false );
        replicationFactorComboBox.setNullSelectionAllowed( false );
        replicationFactorComboBox.setValue( wizard.getHadoopClusterConfig().getReplicationFactor() );

        replicationFactorComboBox.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                wizard.getHadoopClusterConfig()
                      .setReplicationFactor( Integer.parseInt( ( String ) event.getProperty().getValue() ) );
            }
        } );

        TextField domain = new TextField( "Enter domain name" );
        domain.setId( "HadoopDomainTxttField" );
        domain.setInputPrompt( wizard.getHadoopClusterConfig().getDomainName() );
        domain.setValue( wizard.getHadoopClusterConfig().getDomainName() );
        domain.setMaxLength( 20 );
        domain.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                String value = event.getProperty().getValue().toString().trim();
                if ( !Strings.isNullOrEmpty( value ) )
                {
                    wizard.getHadoopClusterConfig().setDomainName( value );
                }
            }
        } );

        Button next = new Button( "Next" );
        next.setId( "HadoopBtnNext" );
        next.addStyleName( "default" );
        next.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                if ( Strings.isNullOrEmpty( wizard.getHadoopClusterConfig().getClusterName() ) )
                {
                    show( "Please provide cluster name" );
                }
                else
                {
                    wizard.next();
                }
            }
        } );

        Button back = new Button( "Back" );
        back.setId( "HadoopConfigBack" );
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

        content.addComponent( clusterNameTxtFld );
        content.addComponent( domain );
        content.addComponent( slaveNodesComboBox );
        content.addComponent( replicationFactorComboBox );
        content.addComponent( buttons );

        addComponent( layout );
    }


    private void show( String notification )
    {
        Notification.show( notification );
    }
}
