package org.safehaus.subutai.plugin.elasticsearch.ui;


import java.util.concurrent.ExecutorService;

import javax.naming.NamingException;

import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.elasticsearch.api.Elasticsearch;
import org.safehaus.subutai.plugin.elasticsearch.ui.manager.Manager;
import org.safehaus.subutai.plugin.elasticsearch.ui.wizard.Wizard;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;


public class ElasticsearchComponent extends CustomComponent
{
    private final Wizard wizard;
    private final Manager manager;


    public ElasticsearchComponent( ExecutorService executorService, Elasticsearch elasticsearch, Tracker tracker, EnvironmentManager environmentManager )
            throws NamingException
    {
        setSizeFull();

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing( true );
        verticalLayout.setSizeFull();

        setCompositionRoot( verticalLayout );
        TabSheet sheet = new TabSheet();
        sheet.setSizeFull();
        manager = new Manager( executorService, elasticsearch, tracker, environmentManager );
        wizard = new Wizard( executorService, elasticsearch, tracker );
        sheet.addTab( wizard.getContent(), "Install" );
        sheet.getTab( 0 ).setId( "ElasticSearchInstallTab" );
        sheet.addTab( manager.getContent(), "Manage" );
        sheet.getTab( 1 ).setId( "ElasticSearchManageTab" );
        sheet.addSelectedTabChangeListener( new TabSheet.SelectedTabChangeListener()
        {
            @Override
            public void selectedTabChange( TabSheet.SelectedTabChangeEvent event )
            {
                TabSheet tabsheet = event.getTabSheet();
                String caption = tabsheet.getTab( event.getTabSheet().getSelectedTab() ).getCaption();
                if ( "Manage".equals( caption ) )
                {
                    manager.refreshClustersInfo();
                }
            }
        } );
        verticalLayout.addComponent( sheet );
        manager.refreshClustersInfo();
    }
}
