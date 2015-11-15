package io.subutai.core.environment.ui;


import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.environment.ui.forms.BlueprintForm;
import io.subutai.core.environment.ui.forms.EnvironmentForm;
import io.subutai.core.environment.ui.tabs.EnvironmentContainersQuotaTab;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.registry.api.TemplateRegistry;
import io.subutai.core.strategy.api.StrategyManager;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;


public class EnvironmentManagerComponent extends CustomComponent
{
    private final StrategyManager strategyManager;
    private EnvironmentManager environmentManager;
    private PeerManager peerManager;
    private TemplateRegistry templateRegistry;


    public EnvironmentManagerComponent( final EnvironmentManager environmentManager, final PeerManager peerManager,
                                        final TemplateRegistry templateRegistry, final StrategyManager strategyManager )
    {
        this.environmentManager = environmentManager;
        this.peerManager = peerManager;
        this.templateRegistry = templateRegistry;
        this.strategyManager = strategyManager;
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

        BlueprintForm blueprintForm = new BlueprintForm( environmentManager, peerManager, templateRegistry, strategyManager );
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
