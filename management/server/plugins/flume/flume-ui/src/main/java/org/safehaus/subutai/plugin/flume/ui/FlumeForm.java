package org.safehaus.subutai.plugin.flume.ui;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import org.safehaus.subutai.plugin.flume.ui.manager.Manager;
import org.safehaus.subutai.plugin.flume.ui.wizard.Wizard;

public class FlumeForm extends CustomComponent {

    private final Wizard wizard;
    private final Manager manager;

    public FlumeForm() {
        setSizeFull();

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing(true);
        verticalLayout.setSizeFull();

        TabSheet tabSheet = new TabSheet();
        tabSheet.setSizeFull();
        manager = new Manager();
        wizard = new Wizard();
        tabSheet.addTab(wizard.getContent(), "Install");
        tabSheet.addTab(manager.getContent(), "Manage");

        verticalLayout.addComponent(tabSheet);
        setCompositionRoot(verticalLayout);
        manager.refreshClustersInfo();
    }

}
