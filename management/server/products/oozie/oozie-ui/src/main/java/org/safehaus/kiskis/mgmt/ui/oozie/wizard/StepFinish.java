/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.ui.oozie.wizard;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;

/**
 * @author dilshat
 */
public class StepFinish extends Panel {

    private TextArea terminal;

    public StepFinish(final Wizard wizard) {

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSizeFull();
        verticalLayout.setHeight(100, Sizeable.UNITS_PERCENTAGE);
        verticalLayout.setMargin(true);

        GridLayout grid = new GridLayout(10, 10);
        grid.setSpacing(true);
        grid.setSizeFull();

        VerticalLayout verticalLayoutForm = new VerticalLayout();
        verticalLayoutForm.setSizeFull();
        verticalLayoutForm.setSpacing(true);
        terminal = new TextArea();
        terminal.setRows(20);
        terminal.setColumns(60);
        terminal.setSizeFull();
        verticalLayout.addComponent(terminal);

        grid.addComponent(verticalLayoutForm, 3, 0, 9, 9);
        grid.setComponentAlignment(verticalLayoutForm, Alignment.TOP_CENTER);

        final Button back = new Button("Cancel");
        back.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                wizard.cancel();
            }
        });

        final Button next = new Button("Next");
        next.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                next.setCaption("Installing...");
                back.setEnabled(false);
                next.setEnabled(false);
            }
        });


        verticalLayout.addComponent(grid);

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.addComponent(back);
        horizontalLayout.addComponent(next);
        verticalLayout.addComponent(horizontalLayout);

        addComponent(verticalLayout);

    }


    private void show(String notification) {
        getWindow().showNotification(notification);
    }

}
