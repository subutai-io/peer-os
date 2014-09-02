package org.safehaus.subutai.ui.sqoop.wizard;


import com.google.common.base.Strings;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.*;
import org.safehaus.subutai.api.hadoop.Config;
import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.ui.sqoop.SqoopUI;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class NodeSelectionStep extends Panel
{

    private final ComboBox hadoopClusters;
    private final TwinColSelect select;


    public NodeSelectionStep( final Wizard wizard )
    {

        setSizeFull();

        GridLayout content = new GridLayout( 1, 2 );
        content.setSizeFull();
        content.setSpacing( true );
        content.setMargin( true );

        hadoopClusters = new ComboBox( "Hadoop cluster" );
        hadoopClusters.setImmediate( true );
        hadoopClusters.setTextInputAllowed( false );
        hadoopClusters.setRequired( true );
        hadoopClusters.setNullSelectionAllowed( false );
        hadoopClusters.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent e )
            {
                select.setValue( null );
                if ( e.getProperty().getValue() != null )
                {
                    Config hadoopInfo = ( Config ) e.getProperty().getValue();
                    select.setContainerDataSource( new BeanItemContainer<>(
                            Agent.class, hadoopInfo.getAllNodes() )
                    );
                    // do select if values exist
                    if ( !CollectionUtil.isCollectionEmpty( wizard.getConfig().getNodes() ) )
                    {
                        select.setValue( wizard.getConfig().getNodes() );
                    }

                    wizard.getConfig().setClusterName( hadoopInfo.getClusterName() );
                }
            }
        } );

        List<Config> clusters = SqoopUI.getHadoopManager().getClusters();
        if ( clusters.size() > 0 )
        {
            for ( Config hci : clusters )
            {
                hadoopClusters.addItem( hci );
                hadoopClusters.setItemCaption( hci, hci.getClusterName() );
            }
        }
        // select cluster if config has cluster name set
        if ( wizard.getConfig().getClusterName() != null )
        {
            Config info = SqoopUI.getHadoopManager().getCluster( wizard.getConfig().getClusterName() );
            if ( info != null )
            {
                hadoopClusters.setValue( info );
            }
        }

        select = makeClientNodeSelector( wizard );

        Button next = new Button( "Next" );
        next.addStyleName( "default" );
        next.addClickListener( new Button.ClickListener()
        {

            @Override
            public void buttonClick( Button.ClickEvent event )
            {

                if ( Strings.isNullOrEmpty( wizard.getConfig().getClusterName() ) )
                {
                    show( "Select Hadoop cluster" );
                }
                else if ( CollectionUtil.isCollectionEmpty( wizard.getConfig().getNodes() ) )
                {
                    show( "Select client nodes" );
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

        content.addComponent( hadoopClusters );
        content.addComponent( select );
        content.addComponent( buttons );

        setContent( layout );
    }


    private TwinColSelect makeClientNodeSelector( final Wizard wizard )
    {
        TwinColSelect tcs = new TwinColSelect( "Nodes" );
        tcs.setItemCaptionPropertyId( "hostname" );
        tcs.setRows( 7 );
        tcs.setMultiSelect( true );
        tcs.setImmediate( true );
        tcs.setLeftColumnCaption( "Available Nodes" );
        tcs.setRightColumnCaption( "Selected Nodes" );
        tcs.setWidth( 100, Unit.PERCENTAGE );
        tcs.setRequired( true );
        if ( !CollectionUtil.isCollectionEmpty( wizard.getConfig().getNodes() ) )
        {
            tcs.setValue( wizard.getConfig().getNodes() );
        }
        tcs.addValueChangeListener( new Property.ValueChangeListener()
        {

            public void valueChange( Property.ValueChangeEvent event )
            {
                if ( event.getProperty().getValue() != null )
                {
                    Set<Agent> clients = new HashSet();
                    clients.addAll( ( Collection ) event.getProperty().getValue() );
                    wizard.getConfig().setNodes( clients );
                }
            }
        } );
        return tcs;
    }


    private void show( String notification )
    {
        Notification.show( notification );
    }

}
