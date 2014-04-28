package org.safehaus.kiskis.mgmt.ui.sqoop;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;
import org.safehaus.kiskis.mgmt.ui.sqoop.manager.Manager;
import org.safehaus.kiskis.mgmt.ui.sqoop.wizard.Wizard;

public class SqoopForm extends CustomComponent {

    private final Wizard wizard;
    private final Manager manager;

    public SqoopForm() {
        setSizeFull();
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing(true);
        verticalLayout.setSizeFull();
        TabSheet mongoSheet = new TabSheet();
        mongoSheet.setStyleName(Runo.TABSHEET_SMALL);
        mongoSheet.setSizeFull();
        manager = new Manager();
        wizard = new Wizard();
        mongoSheet.addTab(wizard.getContent(), "Install");
        mongoSheet.addTab(manager.getContent(), "Manage");
        verticalLayout.addComponent(mongoSheet);
        setCompositionRoot(verticalLayout);
        manager.refreshClustersInfo();
    }

}
