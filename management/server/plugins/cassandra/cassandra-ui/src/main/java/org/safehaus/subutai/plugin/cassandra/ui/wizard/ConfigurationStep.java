package org.safehaus.subutai.plugin.cassandra.ui.wizard;


import java.util.Arrays;

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

    public ConfigurationStep( final Wizard wizard )
    {

        setSizeFull();

        GridLayout content = new GridLayout( 1, 3 );
        content.setSizeFull();
        content.setSpacing( true );
        content.setMargin( true );

        final TextField clusterNameTxtFld = new TextField( "Enter cluster name" );
        clusterNameTxtFld.setId( "CassClusterNameTxtField" );
        clusterNameTxtFld.setInputPrompt( "Cluster name" );
        clusterNameTxtFld.setRequired( true );
        clusterNameTxtFld.setValue( wizard.getConfig().getClusterName() );
        clusterNameTxtFld.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                wizard.getConfig().setClusterName( event.getProperty().getValue().toString().trim() );
            }
        } );

        final TextField domainNameTxtFld = new TextField( "Enter domain name" );
        domainNameTxtFld.setId( "CassDomainNameTxtField" );
        domainNameTxtFld.setInputPrompt( "Domain name" );
        domainNameTxtFld.setInputPrompt( "intra.lan" );
        domainNameTxtFld.setRequired( true );
        domainNameTxtFld.setValue( wizard.getConfig().getClusterName() );
        domainNameTxtFld.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                wizard.getConfig().setDomainName( event.getProperty().getValue().toString().trim() );
            }
        } );

        final TextField dataDirectoryTxtFld = new TextField( "Data directory" );
        dataDirectoryTxtFld.setId( "CassDirectoryTxtField" );
        dataDirectoryTxtFld.setInputPrompt( "/var/lib/cassandra/data" );
        dataDirectoryTxtFld.setRequired( true );
        dataDirectoryTxtFld.setValue( wizard.getConfig().getClusterName() );
        dataDirectoryTxtFld.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                wizard.getConfig().setDataDirectory( event.getProperty().getValue().toString().trim() );
            }
        } );

        final TextField commitLogDirectoryTxtFld = new TextField( "Commit log directory" );
        commitLogDirectoryTxtFld.setId( "CassCommitLogDirectoryTxtField" );
        commitLogDirectoryTxtFld.setInputPrompt( "/var/lib/cassandra/commitlog" );
        commitLogDirectoryTxtFld.setRequired( true );
        commitLogDirectoryTxtFld.setValue( wizard.getConfig().getClusterName() );
        commitLogDirectoryTxtFld.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                wizard.getConfig().setCommitLogDirectory( event.getProperty().getValue().toString().trim() );
            }
        } );

        final TextField savedCachesDirectoryTxtFld = new TextField( "Saved caches directory" );
        savedCachesDirectoryTxtFld.setId( "CassSavedCachesDirectoryTxtFld" );
        savedCachesDirectoryTxtFld.setInputPrompt( "/var/lib/cassandra/saved_caches" );
        savedCachesDirectoryTxtFld.setRequired( true );
        savedCachesDirectoryTxtFld.setValue( wizard.getConfig().getClusterName() );
        savedCachesDirectoryTxtFld.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                wizard.getConfig().setSavedCachesDirectory( event.getProperty().getValue().toString().trim() );
            }
        } );

        final ComboBox nodesCountCombo =
                new ComboBox( "Choose number of nodes in cluster", Arrays.asList( 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 ) );
        nodesCountCombo.setId( "CassNodesCountCombo" );
        nodesCountCombo.setImmediate( true );
        nodesCountCombo.setImmediate( true );
        nodesCountCombo.setTextInputAllowed( false );
        nodesCountCombo.setNullSelectionAllowed( false );
        nodesCountCombo.setValue( wizard.getConfig() );

        nodesCountCombo.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                wizard.getConfig().setNumberOfNodes( ( Integer ) event.getProperty().getValue() );
            }
        } );

        final ComboBox seedsCountCombo =
                new ComboBox( "Choose number of seeds", Arrays.asList( 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 ) );
        seedsCountCombo.setId( "CassSeedsCountCombo" );
        seedsCountCombo.setImmediate( true );
        seedsCountCombo.setImmediate( true );
        seedsCountCombo.setTextInputAllowed( false );
        seedsCountCombo.setNullSelectionAllowed( false );
        seedsCountCombo.setValue( wizard.getConfig() );

        seedsCountCombo.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                wizard.getConfig().setNumberOfSeeds( ( Integer ) event.getProperty().getValue() );
            }
        } );

        Button next = new Button( "Next" );
        next.setId( "CassConfNextBtn" );
        next.addStyleName( "default" );
        next.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                if ( Strings.isNullOrEmpty( wizard.getConfig().getClusterName() ) )
                {
                    show( "Please provide cluster name !" );
                }
                else if ( Strings.isNullOrEmpty( wizard.getConfig().getDomainName() ) )
                {
                    show( "Please provide domain name !" );
                }
                else if ( nodesCountCombo.getValue() == null || seedsCountCombo.getValue() == null )
                {
                    show( "Please provide number of nodes and seeds !" );
                }
                else if ( ( int ) nodesCountCombo.getValue() < ( int ) seedsCountCombo.getValue() )
                {
                    show( "Number of seeds should be smaller than total number nodes in the cluster !" );
                }
                else
                {
                    wizard.next();
                }
            }
        } );

        Button back = new Button( "Back" );
        back.setId( "CassConfBackBtn" );
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
        content.addComponent( domainNameTxtFld );
        content.addComponent( dataDirectoryTxtFld );
        content.addComponent( commitLogDirectoryTxtFld );
        content.addComponent( savedCachesDirectoryTxtFld );
        content.addComponent( nodesCountCombo );
        content.addComponent( seedsCountCombo );
        content.addComponent( buttons );

        addComponent( layout );
    }


    private void show( String notification )
    {
        Notification.show( notification );
    }
}
