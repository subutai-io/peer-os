package org.safehaus.subutai.plugin.zookeeper.ui;


import java.util.concurrent.ExecutorService;

import javax.naming.NamingException;

import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.plugin.zookeeper.ui.manager.Manager;
import org.safehaus.subutai.plugin.zookeeper.ui.wizard.Wizard;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;


public class ZookeeperComponent extends CustomComponent
{

    public ZookeeperComponent( ExecutorService executorService, ServiceLocator serviceLocator ) throws NamingException
    {
        setSizeFull();

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing( true );
        verticalLayout.setSizeFull();

        TabSheet zookeeperSheet = new TabSheet();
        zookeeperSheet.setSizeFull();
        final Manager manager = new Manager( executorService, serviceLocator );
        Wizard wizard = new Wizard( executorService, serviceLocator );
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
