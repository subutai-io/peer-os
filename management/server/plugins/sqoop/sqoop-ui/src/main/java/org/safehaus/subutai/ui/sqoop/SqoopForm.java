package org.safehaus.subutai.ui.sqoop;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import org.safehaus.subutai.ui.sqoop.manager.ExportPanel;
import org.safehaus.subutai.ui.sqoop.manager.ImportExportBase;
import org.safehaus.subutai.ui.sqoop.manager.ImportPanel;
import org.safehaus.subutai.ui.sqoop.manager.Manager;
import org.safehaus.subutai.ui.sqoop.wizard.Wizard;

public class SqoopForm extends CustomComponent {

    private final Wizard wizard;
    private final Manager manager;

    private final TabSheet tabSheet;

    public SqoopForm() {
        manager = new Manager();
        wizard = new Wizard();

        setSizeFull();
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing(true);
        verticalLayout.setSizeFull();

        tabSheet = new TabSheet();
        tabSheet.setSizeFull();
        tabSheet.addTab(wizard.getContent(), "Install");
        tabSheet.addTab(manager.getContent(), "Manage");

        verticalLayout.addComponent(tabSheet);
        setCompositionRoot(verticalLayout);
        manager.refreshClustersInfo();
    }

    public void addTab(ImportExportBase component) {
        TabSheet.Tab tab = tabSheet.addTab(component);
        if(component instanceof ExportPanel)
            tab.setCaption("Export");
        else if(component instanceof ImportPanel)
            tab.setCaption("Import");
        tabSheet.setSelectedTab(component);
    }

}
