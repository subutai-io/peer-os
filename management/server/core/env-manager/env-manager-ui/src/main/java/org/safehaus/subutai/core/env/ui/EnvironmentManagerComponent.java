package org.safehaus.subutai.core.env.ui;


import org.safehaus.subutai.core.env.api.EnvironmentManager;
import org.safehaus.subutai.core.env.ui.forms.BlueprintForm;
import org.safehaus.subutai.core.env.ui.forms.EnvironmentForm;
import org.safehaus.subutai.core.env.ui.tabs.EnvironmentContainersQuotaTab;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;


public class EnvironmentManagerComponent extends CustomComponent
{
    private EnvironmentManager environmentManager;
    private PeerManager peerManager;
    private TemplateRegistry templateRegistry;


    public EnvironmentManagerComponent( final EnvironmentManager environmentManager, final PeerManager peerManager,
                                        final TemplateRegistry templateRegistry )
    {
        this.environmentManager = environmentManager;
        this.peerManager = peerManager;
        this.templateRegistry = templateRegistry;
        init();
    }


    private void init()
    {
        setHeight( 100, Unit.PERCENTAGE );

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing( true );
        verticalLayout.setSizeFull();

        final TabSheet formSheet = new TabSheet();
        formSheet.setStyleName( Runo.TABSHEET_SMALL );
        formSheet.setSizeFull();

        BlueprintForm blueprintForm = new BlueprintForm( environmentManager, peerManager, templateRegistry );
        formSheet.addTab( blueprintForm.getContentRoot(), "Blueprints" );
        formSheet.getTab( 0 ).setId( "Blueprints" );

        final EnvironmentForm environmentForm = new EnvironmentForm( environmentManager, peerManager );
        formSheet.addTab( environmentForm.getContentRoot(), "Environments" );
        formSheet.getTab( 1 ).setId( "Environments" );

        final EnvironmentContainersQuotaTab envContainersTab = new EnvironmentContainersQuotaTab( this );
        formSheet.addTab( envContainersTab, "Environment Container Quota" ).setId( "EnvironmentContainersTab" );

        verticalLayout.addComponent( formSheet );

        setCompositionRoot( verticalLayout );
    }


    public EnvironmentManager getEnvironmentManager()
    {
        return environmentManager;
    }
}
