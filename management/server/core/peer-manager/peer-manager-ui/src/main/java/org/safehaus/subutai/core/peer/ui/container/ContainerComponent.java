package org.safehaus.subutai.core.peer.ui.container;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.naming.NamingException;

import org.safehaus.subutai.common.protocol.Disposable;
import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.peer.ui.PeerManagerPortalModule;
import org.safehaus.subutai.core.peer.ui.container.clone.Cloner;
import org.safehaus.subutai.core.peer.ui.container.manage.Manager;
import org.safehaus.subutai.core.strategy.api.StrategyManager;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;


public class ContainerComponent extends CustomComponent implements Disposable
{

    private static final String MANAGER_TAB_CAPTION = "Manage";
    private ExecutorService executorService;


    public ContainerComponent( PeerManagerPortalModule peerManagerPortalModule ) throws NamingException
    {
        final ServiceLocator serviceLocator = new ServiceLocator();
        executorService = Executors.newFixedThreadPool( 5 );
        final StrategyManager strategyManager = serviceLocator.getService( StrategyManager.class );
        final PeerManager peerManager = serviceLocator.getService( PeerManager.class );

        setHeight( 100, Unit.PERCENTAGE );

        HorizontalSplitPanel horizontalSplit = new HorizontalSplitPanel();
        horizontalSplit.setStyleName( Runo.SPLITPANEL_SMALL );
        horizontalSplit.setSplitPosition( 200, Unit.PIXELS );


        final ContainerTree containerTree =
                new ContainerTree( peerManager.getLocalPeer(), peerManagerPortalModule.getHostRegistry() );

        VerticalLayout treeLayout = new VerticalLayout();
        treeLayout.addComponent( containerTree );

        horizontalSplit.setFirstComponent( treeLayout );

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing( true );
        verticalLayout.setSizeFull();
        TabSheet commandsSheet = new TabSheet();
        commandsSheet.setStyleName( Runo.TABSHEET_SMALL );
        commandsSheet.setSizeFull();
        final Manager manager = new Manager( executorService, peerManager );
        commandsSheet.addTab(
                new Cloner( peerManagerPortalModule.getRegistry(), peerManager.getLocalPeer(), strategyManager,
                        containerTree ), "Clone" );
        commandsSheet.addTab( manager, MANAGER_TAB_CAPTION );
        commandsSheet.addSelectedTabChangeListener( new TabSheet.SelectedTabChangeListener()
        {
            @Override
            public void selectedTabChange( TabSheet.SelectedTabChangeEvent event )
            {
                TabSheet tabsheet = event.getTabSheet();
                String caption = tabsheet.getTab( event.getTabSheet().getSelectedTab() ).getCaption();
                if ( caption.equals( MANAGER_TAB_CAPTION ) )
                {
                    manager.getContainerInfo();
                }
            }
        } );
        verticalLayout.addComponent( commandsSheet );

        horizontalSplit.setSecondComponent( verticalLayout );
        setCompositionRoot( horizontalSplit );

        addDetachListener( new DetachListener()
        {
            @Override
            public void detach( final DetachEvent event )
            {
                executorService.shutdown();
            }
        } );
    }


    public void dispose()
    {
        executorService.shutdown();
    }
}
