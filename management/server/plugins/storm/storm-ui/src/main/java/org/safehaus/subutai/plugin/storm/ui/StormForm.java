package org.safehaus.subutai.plugin.storm.ui;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import org.safehaus.subutai.plugin.storm.ui.manager.Manager;
import org.safehaus.subutai.plugin.storm.ui.wizard.Wizard;

public class StormForm extends CustomComponent {

    private final Wizard wizard;
    private final Manager manager;

    public StormForm() {
        manager = new Manager();
        wizard = new Wizard();

        setSizeFull();
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing(true);
        verticalLayout.setSizeFull();

        TabSheet tabSheet = new TabSheet();
        tabSheet.setSizeFull();
        tabSheet.addTab(wizard.getContent(), "Install");
        tabSheet.addTab(manager.getContent(), "Manage");

        verticalLayout.addComponent(tabSheet);
        setCompositionRoot(verticalLayout);

        manager.refreshClustersInfo();
    }
}
