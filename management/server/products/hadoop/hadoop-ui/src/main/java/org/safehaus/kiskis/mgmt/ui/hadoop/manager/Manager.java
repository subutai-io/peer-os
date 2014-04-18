package org.safehaus.kiskis.mgmt.ui.hadoop.manager;

import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.*;

/**
 * Created by daralbaev on 12.04.14.
 */
public class Manager extends Panel {
    private HorizontalLayout horizontalLayout;
    private Label indicator;
    private Button refreshButton;
    private HadoopTable table;

    public Manager() {
        setSizeFull();

        horizontalLayout = new HorizontalLayout();
        horizontalLayout.setMargin(true);
        horizontalLayout.setSpacing(true);

        horizontalLayout.addComponent(getIndicator());
        horizontalLayout.setComponentAlignment(indicator, Alignment.MIDDLE_LEFT);
        horizontalLayout.addComponent(getButtonRefresh());
        horizontalLayout.setComponentAlignment(refreshButton, Alignment.MIDDLE_LEFT);

        addComponent(horizontalLayout);
        addComponent(getHadoopTable());
    }

    private Button getButtonRefresh() {
        refreshButton = new Button("Refresh");
        refreshButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                table.refreshDataSource();
            }
        });

        return refreshButton;
    }

    private Label getIndicator() {
        indicator = new Label("Label");
        indicator.setIcon(new ThemeResource("icons/indicator.gif"));
        indicator.setContentMode(Label.CONTENT_XHTML);
        indicator.setHeight(11, Sizeable.UNITS_PIXELS);
        indicator.setWidth(50, Sizeable.UNITS_PIXELS);
        indicator.setVisible(true);

        return indicator;
    }

    private HadoopTable getHadoopTable() {
        if (table == null) {
            table = new HadoopTable("Hadoop Clusters", indicator);
        }

        return table;
    }
}
