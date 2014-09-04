package org.safehaus.subutai.plugin.hive.ui.manager;


import com.google.common.base.Strings;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import org.safehaus.subutai.plugin.hive.api.HiveConfig;
import org.safehaus.subutai.common.tracker.ProductOperationState;
import org.safehaus.subutai.common.tracker.ProductOperationView;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.plugin.hive.ui.HiveUI;

import java.util.Set;
import java.util.UUID;


class AddNodeWindow extends Window
{

    private final TextArea outputTxtArea;
    private final Button ok;
    private final Label indicator;
    private volatile boolean track = true;


    public AddNodeWindow( final HiveConfig config, Set<Agent> nodes )
    {
        super( "Add New Node" );
        setModal( true );

        setWidth( 600, Unit.PIXELS );
        setHeight( 400, Unit.PIXELS );

        GridLayout content = new GridLayout( 1, 3 );
        content.setSizeFull();
        content.setMargin( true );
        content.setSpacing( true );

        HorizontalLayout topContent = new HorizontalLayout();
        topContent.setSpacing( true );

        content.addComponent( topContent );
        topContent.addComponent( new Label( "Nodes:" ) );

        final ComboBox hadoopNodes = new ComboBox();
        hadoopNodes.setImmediate( true );
        hadoopNodes.setTextInputAllowed( false );
        hadoopNodes.setNullSelectionAllowed( false );
        hadoopNodes.setRequired( true );
        hadoopNodes.setWidth( 60, Unit.PERCENTAGE );
        for ( Agent node : nodes )
        {
            hadoopNodes.addItem( node );
            hadoopNodes.setItemCaption( node, node.getHostname() );
        }
        hadoopNodes.setValue( nodes.iterator().next() );

        topContent.addComponent( hadoopNodes );

        final Button addNodeBtn = new Button( "Add" );
        addNodeBtn.addStyleName( "default" );
        topContent.addComponent( addNodeBtn );

        addNodeBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                addNodeBtn.setEnabled( false );
                showProgress();
                Agent agent = ( Agent ) hadoopNodes.getValue();
                final UUID trackID = HiveUI.getManager().addNode(
                    config.getClusterName(), agent.getHostname() );
                HiveUI.getExecutor().execute( new Runnable()
                {

                    public void run()
                    {
                        while ( track )
                        {
                            ProductOperationView po = HiveUI.getTracker().getProductOperation(
                                HiveConfig.PRODUCT_KEY, trackID );
                            if ( po != null )
                            {
                                setOutput( po.getDescription() + "\nState: "
                                    + po.getState() + "\nLogs:\n" + po.getLog() );
                                if ( po.getState() != ProductOperationState.RUNNING )
                                {
                                    hideProgress();
                                    break;
                                }
                            }
                            else
                            {
                                setOutput( "Product operation not found. Check logs" );
                                break;
                            }
                            try
                            {
                                Thread.sleep( 1000 );
                            }
                            catch ( InterruptedException ex )
                            {
                                break;
                            }
                        }
                    }
                } );
            }
        } );

        outputTxtArea = new TextArea( "Operation output" );
        outputTxtArea.setRows( 10 );
        outputTxtArea.setWidth( 80, Unit.PERCENTAGE );
        outputTxtArea.setImmediate( true );
        outputTxtArea.setWordwrap( true );

        content.addComponent( outputTxtArea );

        indicator = new Label();
        indicator.setIcon( new ThemeResource( "img/spinner.gif" ) );
        indicator.setContentMode( ContentMode.HTML );
        indicator.setHeight( 11, Unit.PIXELS );
        indicator.setWidth( 50, Unit.PIXELS );
        indicator.setVisible( false );

        ok = new Button( "Ok" );
        ok.addStyleName( "default" );
        ok.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                //close window
                track = false;
                close();
            }
        } );

        HorizontalLayout bottomContent = new HorizontalLayout();
        bottomContent.addComponent( indicator );
        bottomContent.setComponentAlignment( indicator, Alignment.MIDDLE_RIGHT );
        bottomContent.addComponent( ok );

        content.addComponent( bottomContent );
        content.setComponentAlignment( bottomContent, Alignment.MIDDLE_RIGHT );

        setContent( content );
    }


    private void showProgress()
    {
        indicator.setVisible( true );
        ok.setEnabled( false );
    }


    private void setOutput( String output )
    {
        if ( !Strings.isNullOrEmpty( output ) )
        {
            outputTxtArea.setValue( output );
            outputTxtArea.setCursorPosition( outputTxtArea.getValue().length() - 1 );
        }
    }


    private void hideProgress()
    {
        indicator.setVisible( false );
        ok.setEnabled( true );
    }


    @Override
    public void close()
    {
        super.close();
        track = false;
    }

}
