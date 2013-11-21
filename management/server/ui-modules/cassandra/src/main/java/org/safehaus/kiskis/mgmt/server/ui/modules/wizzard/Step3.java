/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.wizzard;

import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.*;

/**
 *
 * @author bahadyr
 */
public class Step3 extends Panel {

    public Step3(final CassandraWizard aThis) {
        setCaption("Installation");
        setSizeFull();

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSizeFull();
        verticalLayout.setHeight(600, Sizeable.UNITS_PIXELS);
        verticalLayout.setMargin(true);

        GridLayout grid = new GridLayout(6, 10);
        grid.setSpacing(true);
        grid.setSizeFull();

        Panel panel = new Panel();
        Label menu = new Label("Cluster Install Wizard<br>" +
                " 1) Welcome<br>" +
                " 2) List nodes<br>" +
                " 3) <font color=\"#f14c1a\"><strong>Installation</strong></font><br>" +
                " 4) Configuration");
        menu.setContentMode(Label.CONTENT_XHTML);
        panel.addComponent(menu);

        grid.addComponent(menu, 0, 0, 1, 5);
        grid.setComponentAlignment(panel, Alignment.TOP_CENTER);


        Label label = new Label("Please enter the list of hosts to be included in the cluster");
        label.setContentMode(Label.CONTENT_XHTML);

        grid.addComponent(label, 2, 0, 5, 0);
        grid.setComponentAlignment(label, Alignment.TOP_CENTER);

        Label label1 = new Label("<strong>Status of nodes</strong><br>" +
                "<br>");
        label1.setContentMode(Label.CONTENT_XHTML);

        grid.addComponent(label1, 2, 1, 5, 1);
        grid.setComponentAlignment(label1, Alignment.TOP_CENTER);


        HorizontalLayout horizontalLayoutHost1 = new HorizontalLayout();
        Label labelHost1 = new Label("cassandra-node1");
        Button buttonHost1 = new Button("Successful");
        buttonHost1.setIcon(new ThemeResource(
                "icons/16/ok.png"));
        horizontalLayoutHost1.addComponent(labelHost1);
        horizontalLayoutHost1.setComponentAlignment(labelHost1, Alignment.MIDDLE_LEFT);
        horizontalLayoutHost1.addComponent(buttonHost1);
        horizontalLayoutHost1.setComponentAlignment(buttonHost1, Alignment.MIDDLE_RIGHT);
        grid.addComponent(horizontalLayoutHost1, 2, 2, 5, 2);
        grid.setComponentAlignment(horizontalLayoutHost1, Alignment.TOP_LEFT);

        HorizontalLayout horizontalLayoutHost2 = new HorizontalLayout();
        Label labelHost2 = new Label("cassandra-node2");
        Button buttonHost2 = new Button("Successful");
        buttonHost2.setIcon(new ThemeResource(
                "icons/16/ok.png"));
        horizontalLayoutHost2.addComponent(labelHost2);
        horizontalLayoutHost2.setComponentAlignment(labelHost2, Alignment.MIDDLE_LEFT);
        horizontalLayoutHost2.addComponent(buttonHost2);
        horizontalLayoutHost2.setComponentAlignment(buttonHost2, Alignment.MIDDLE_RIGHT);
        grid.addComponent(horizontalLayoutHost2, 2, 3, 5, 3);
        grid.setComponentAlignment(horizontalLayoutHost2, Alignment.TOP_LEFT);

        HorizontalLayout horizontalLayoutHost3 = new HorizontalLayout();
        Label labelHost3 = new Label("cassandra-node3");
        Button buttonHost3 = new Button("Successful");
        buttonHost3.setIcon(new ThemeResource(
                "icons/16/ok.png"));
        horizontalLayoutHost3.addComponent(labelHost3);
        horizontalLayoutHost3.setComponentAlignment(labelHost3, Alignment.MIDDLE_LEFT);
        horizontalLayoutHost3.addComponent(buttonHost3);
        horizontalLayoutHost3.setComponentAlignment(buttonHost3, Alignment.MIDDLE_RIGHT);
        grid.addComponent(horizontalLayoutHost3, 2, 4, 5, 4);
        grid.setComponentAlignment(horizontalLayoutHost3, Alignment.TOP_LEFT);

        HorizontalLayout horizontalLayoutHost4 = new HorizontalLayout();
        Label labelHost4 = new Label("cassandra-node4");
        Button buttonHost4 = new Button("Successful");
        buttonHost4.setIcon(new ThemeResource(
                "icons/16/ok.png"));
        horizontalLayoutHost4.addComponent(labelHost4);
        horizontalLayoutHost4.setComponentAlignment(labelHost4, Alignment.MIDDLE_LEFT);
        horizontalLayoutHost4.addComponent(buttonHost4);
        horizontalLayoutHost4.setComponentAlignment(buttonHost4, Alignment.MIDDLE_RIGHT);
        grid.addComponent(horizontalLayoutHost4, 2, 5, 5, 5);
        grid.setComponentAlignment(horizontalLayoutHost4, Alignment.TOP_LEFT);

        Button next = new Button("Next");
        next.addListener(new Button.ClickListener() {

            public void buttonClick(Button.ClickEvent event) {
                aThis.showNext();
            }
        });
        Button back = new Button("Back");
        back.addListener(new Button.ClickListener() {

            public void buttonClick(Button.ClickEvent event) {
                aThis.showBack();
            }
        });

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.addComponent(back);
        horizontalLayout.addComponent(next);

        verticalLayout.addComponent(grid);
        verticalLayout.addComponent(horizontalLayout);

        addComponent(verticalLayout);
    }

}