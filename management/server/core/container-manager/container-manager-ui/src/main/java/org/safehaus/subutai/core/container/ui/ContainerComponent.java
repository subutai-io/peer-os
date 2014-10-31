package org.safehaus.subutai.core.container.ui;


import java.util.concurrent.ExecutorService;

import javax.naming.NamingException;

import org.safehaus.subutai.common.protocol.Disposable;
import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.container.ui.clone.Cloner;
import org.safehaus.subutai.core.container.ui.manage.Manager;
import org.safehaus.subutai.core.lxc.quota.api.QuotaManager;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.strategy.api.StrategyManager;
import org.safehaus.subutai.server.ui.component.AgentTree;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;


public class ContainerComponent extends CustomComponent implements Disposable
{

    private static final String MANAGER_TAB_CAPTION = "Manage";
    private final AgentTree agentTree;
    private final ContainerTree containerTree;


    public ContainerComponent( ExecutorService executorService, ServiceLocator serviceLocator ) throws NamingException
    {

        //        final ContainerManager containerManager = serviceLocator.getService( ContainerManager.class );
        final AgentManager agentManager = serviceLocator.getService( AgentManager.class );
        final QuotaManager quotaManager = serviceLocator.getService( QuotaManager.class );
        final StrategyManager strategyManager = serviceLocator.getService( StrategyManager.class );
        final PeerManager peerManager = serviceLocator.getService( PeerManager.class );

        setHeight( 100, Unit.PERCENTAGE );

        HorizontalSplitPanel horizontalSplit = new HorizontalSplitPanel();
        horizontalSplit.setStyleName( Runo.SPLITPANEL_SMALL );
        horizontalSplit.setSplitPosition( 200, Unit.PIXELS );


        agentTree = new AgentTree( agentManager );
        containerTree = new ContainerTree( peerManager.getLocalPeer() );

        VerticalLayout treeLayout = new VerticalLayout();
        treeLayout.addComponent( agentTree );
        treeLayout.addComponent( containerTree );

        horizontalSplit.setFirstComponent( treeLayout );

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing( true );
        verticalLayout.setSizeFull();
        TabSheet commandsSheet = new TabSheet();
        commandsSheet.setStyleName( Runo.TABSHEET_SMALL );
        commandsSheet.setSizeFull();
        final Manager manager = new Manager( executorService, agentManager, quotaManager, peerManager );
        commandsSheet.addTab( new Cloner( peerManager.getLocalPeer(), strategyManager, containerTree ), "Clone" );
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


    public void dispose()
    {
        agentTree.dispose();
    }
}
