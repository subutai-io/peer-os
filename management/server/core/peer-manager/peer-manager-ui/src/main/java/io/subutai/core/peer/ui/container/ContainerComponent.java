package io.subutai.core.peer.ui.container;


import javax.naming.NamingException;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;

import io.subutai.common.util.ServiceLocator;
import io.subutai.core.metric.api.Monitor;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.peer.ui.PeerManagerPortalModule;
import io.subutai.core.peer.ui.container.clone.Cloner;
import io.subutai.core.peer.ui.container.manage.Manager;
import io.subutai.core.strategy.api.StrategyManager;


public class ContainerComponent extends CustomComponent
{

    private static final String MANAGER_TAB_CAPTION = "Manage";


    public ContainerComponent( PeerManagerPortalModule peerManagerPortalModule ) throws NamingException
    {
        final ServiceLocator serviceLocator = new ServiceLocator();

        final StrategyManager strategyManager = serviceLocator.getService( StrategyManager.class );
        final PeerManager peerManager = serviceLocator.getService( PeerManager.class );
        final Monitor monitor = serviceLocator.getService( Monitor.class );

        setHeight( 100, Unit.PERCENTAGE );

        HorizontalSplitPanel horizontalSplit = new HorizontalSplitPanel();
        horizontalSplit.setStyleName( Runo.SPLITPANEL_SMALL );
        horizontalSplit.setSplitPosition( 200, Unit.PIXELS );


        final ContainerTree containerTree =
                new ContainerTree( peerManager.getLocalPeer(), peerManagerPortalModule.getHostRegistry(), monitor );

        VerticalLayout treeLayout = new VerticalLayout();
        treeLayout.addComponent( containerTree );

        horizontalSplit.setFirstComponent( treeLayout );

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing( true );
        verticalLayout.setSizeFull();
        TabSheet commandsSheet = new TabSheet();
        commandsSheet.setStyleName( Runo.TABSHEET_SMALL );
        commandsSheet.setSizeFull();
        final Manager manager = new Manager( peerManager );
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
    }
}
