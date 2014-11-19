package org.safehaus.subutai.plugin.shark.ui.manager;


import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import org.safehaus.subutai.common.tracker.OperationState;
import org.safehaus.subutai.common.tracker.TrackerOperationView;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.shark.api.Shark;
import org.safehaus.subutai.plugin.shark.api.SharkClusterConfig;

import com.google.common.base.Strings;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.Window;


public class AddNodeWindow extends Window
{

    private final TextArea outputTxtArea;
    private final Label indicator;
    private Button ok;
    private volatile boolean track = true;


    public AddNodeWindow( final Shark shark, final ExecutorService executorService, final Tracker tracker,
                          final SharkClusterConfig config, Set<ContainerHost> nodes )
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

        final ComboBox cmbNodes = new ComboBox();
        cmbNodes.setId( "SharkHadoopNodesCb" );
        cmbNodes.setImmediate( true );
        cmbNodes.setTextInputAllowed( false );
        cmbNodes.setNullSelectionAllowed( false );
        cmbNodes.setRequired( true );
        cmbNodes.setWidth( 80, Unit.PERCENTAGE );
        for ( ContainerHost node : nodes )
        {
            cmbNodes.addItem( node );
            cmbNodes.setItemCaption( node, node.getHostname() );
        }
        cmbNodes.setValue( nodes.iterator().next() );

        topContent.addComponent( cmbNodes );

        final Button addNodeBtn = new Button( "Add" );
        addNodeBtn.setId( "SharkAddSelectedNodeBtn" );
        addNodeBtn.addStyleName( "default" );
        topContent.addComponent( addNodeBtn );

        ok = new Button( "Ok" );
        ok.setId( "btnOk" );
        ok.addStyleName( "default" );
        ok.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                track = false;
                close();
            }
        } );

        addNodeBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                addNodeBtn.setEnabled( false );
                showProgress();
                ContainerHost node = ( ContainerHost ) cmbNodes.getValue();
                final UUID trackID = shark.addNode( config.getClusterName(), node.getHostname() );
                executorService.execute( new Runnable()
                {
                    @Override
                    public void run()
                    {
                        while ( track )
                        {
                            TrackerOperationView po =
                                    tracker.getTrackerOperation( SharkClusterConfig.PRODUCT_KEY, trackID );
                            if ( po != null )
                            {
                                setOutput(
                                        po.getDescription() + "\nState: " + po.getState() + "\nLogs:\n" + po.getLog() );
                                if ( po.getState() != OperationState.RUNNING )
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
        outputTxtArea.setId( "outputTxtArea" );
        outputTxtArea.setRows( 13 );
        outputTxtArea.setColumns( 43 );
        outputTxtArea.setImmediate( true );
        outputTxtArea.setWordwrap( true );

        content.addComponent( outputTxtArea );

        indicator = new Label();
        indicator.setId( "indicator" );
        indicator.setIcon( new ThemeResource( "img/spinner.gif" ) );
        indicator.setContentMode( ContentMode.HTML );
        indicator.setHeight( 11, Unit.PIXELS );
        indicator.setWidth( 50, Unit.PIXELS );
        indicator.setVisible( false );

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
        track = false;
        super.close();
    }
}

