package org.safehaus.subutai.plugin.zookeeper.ui;


import java.util.concurrent.ExecutorService;

import javax.naming.NamingException;

import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.zookeeper.api.Zookeeper;
import org.safehaus.subutai.plugin.zookeeper.ui.manager.Manager;
import org.safehaus.subutai.plugin.zookeeper.ui.wizard.Wizard;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;


public class ZookeeperComponent extends CustomComponent
{

    public ZookeeperComponent( ExecutorService executorService, Zookeeper zookeeper, Hadoop hadoop, Tracker tracker, EnvironmentManager environmentManager ) throws NamingException
    {
        setSizeFull();

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing( true );
        verticalLayout.setSizeFull();

        TabSheet zookeeperSheet = new TabSheet();
        zookeeperSheet.setSizeFull();
        final Manager manager = new Manager( executorService, zookeeper, hadoop, tracker, environmentManager );
        Wizard wizard = new Wizard( executorService, zookeeper, hadoop, tracker, environmentManager );
        zookeeperSheet.addTab( wizard.getContent(), "Install" );
        zookeeperSheet.getTab( 0 ).setId( "ZookeeperInstallTab" );
        zookeeperSheet.addTab( manager.getContent(), "Manage" );
        zookeeperSheet.getTab( 1 ).setId( "ZookeeperManageTab" );
        zookeeperSheet.addSelectedTabChangeListener( new TabSheet.SelectedTabChangeListener()
        {
            @Override
            public void selectedTabChange( TabSheet.SelectedTabChangeEvent event )
            {
                TabSheet tabsheet = event.getTabSheet();
                String caption = tabsheet.getTab( event.getTabSheet().getSelectedTab() ).getCaption();
                if ( caption.equals( "Manage" ) )
                {
                    manager.refreshClustersInfo();
                }
            }
        } );
        verticalLayout.addComponent( zookeeperSheet );

        setCompositionRoot( verticalLayout );
        manager.refreshClustersInfo();
    }
}
