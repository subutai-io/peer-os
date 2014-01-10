/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.wizard;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import org.safehaus.kiskis.mgmt.shared.protocol.Task;

/**
 *
 * @author dilshat
 */
public class StepSetDirectories extends Panel {

    private Task task;

    public StepSetDirectories(final CassandraWizard wizard) {

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSizeFull();
        verticalLayout.setHeight(100, Sizeable.UNITS_PERCENTAGE);
        verticalLayout.setMargin(true);

        GridLayout grid = new GridLayout(10, 10);
        grid.setSpacing(true);
        grid.setSizeFull();

        Panel panel = new Panel();
        Label menu = new Label("Cluster Installation Wizard");

        menu.setContentMode(Label.CONTENT_XHTML);
        panel.addComponent(menu);
        grid.addComponent(menu, 0, 0, 2, 1);
        grid.setComponentAlignment(panel, Alignment.TOP_CENTER);

        VerticalLayout verticalLayoutForm = new VerticalLayout();
        verticalLayoutForm.setSizeFull();
        verticalLayoutForm.setSpacing(true);

        final TextField dataDirTxtFld = new TextField("Enter Data directory");
        dataDirTxtFld.setInputPrompt("/var/lib/cassandra/data");
        dataDirTxtFld.setRequired(true);
        dataDirTxtFld.setMaxLength(40);
        dataDirTxtFld.setWidth("250px");
        verticalLayoutForm.addComponent(dataDirTxtFld);

        final TextField commitLogTxtFld = new TextField("Enter Commitlog directory");
        commitLogTxtFld.setInputPrompt("/var/lib/cassandra/commitlog");
        commitLogTxtFld.setRequired(true);
        commitLogTxtFld.setMaxLength(40);
        commitLogTxtFld.setWidth("250px");
        verticalLayoutForm.addComponent(commitLogTxtFld);

        final TextField savedCachesTxtFld = new TextField("Enter Saved Caches directory");
        savedCachesTxtFld.setInputPrompt("/var/lib/cassandra/saved_caches");
        savedCachesTxtFld.setRequired(true);
        savedCachesTxtFld.setMaxLength(40);
        savedCachesTxtFld.setWidth("250px");
        verticalLayoutForm.addComponent(savedCachesTxtFld);

        grid.addComponent(verticalLayoutForm, 3, 0, 9, 9);
        grid.setComponentAlignment(verticalLayoutForm, Alignment.TOP_CENTER);

        Button next = new Button("Next");
        next.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                wizard.getConfig().setCommitLogDirectory(commitLogTxtFld.getValue().toString().trim());
                wizard.getConfig().setDataDirectory(dataDirTxtFld.getValue().toString().trim());
                wizard.getConfig().setSavedCachesDirectory(savedCachesTxtFld.getValue().toString().trim());
                wizard.next();
            }
        });

        Button back = new Button("Back");
        back.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                wizard.back();
            }
        });

        verticalLayout.addComponent(grid);

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.addComponent(back);
        horizontalLayout.addComponent(next);
        verticalLayout.addComponent(horizontalLayout);

        addComponent(verticalLayout);

        //set values if this is a second visit
        dataDirTxtFld.setValue(wizard.getConfig().getDataDirectory());
        savedCachesTxtFld.setValue(wizard.getConfig().getSavedCachesDirectory());
        commitLogTxtFld.setValue(wizard.getConfig().getCommitLogDirectory());

    }

    private void show(String notification) {
        getWindow().showNotification(notification);
    }

}
