/**
 * DISCLAIMER
 * 
 * The quality of the code is such that you should not copy any of it as best
 * practice how to build Vaadin applications.
 * 
 * @author jouni@vaadin.com
 * 
 */

package org.safehaus.subutai.server.ui.views;

import com.vaadin.event.Transferable;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import org.safehaus.subutai.server.ui.MainUI;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

public class ModulesView extends VerticalLayout implements View {

    private static final Logger LOG = Logger.getLogger(MainUI.class.getName());
    private TabSheet editors;

    @Override
    public void enter(ViewChangeEvent event) {
        setSizeFull();
        addStyleName("reports");

        addComponent(buildDraftsView());
    }

    private Component buildDraftsView() {
        editors = new TabSheet();
        editors.setSizeFull();
        editors.addStyleName("borderless");
        editors.addStyleName("editors");

        final VerticalLayout center = new VerticalLayout();
        center.setSizeFull();
        center.setCaption("Modules");
        editors.addComponent(center);

        VerticalLayout titleAndDrafts = new VerticalLayout();
        titleAndDrafts.setSizeUndefined();
        titleAndDrafts.setSpacing(true);
        titleAndDrafts.addStyleName("drafts");
        center.addComponent(titleAndDrafts);
        center.setComponentAlignment(titleAndDrafts, Alignment.MIDDLE_CENTER);

        Label draftsTitle = new Label("Drafts");
        draftsTitle.addStyleName("h1");
        draftsTitle.setSizeUndefined();
        titleAndDrafts.addComponent(draftsTitle);
        titleAndDrafts.setComponentAlignment(draftsTitle, Alignment.TOP_CENTER);

        HorizontalLayout drafts = new HorizontalLayout();
        drafts.setSpacing(true);
        titleAndDrafts.addComponent(drafts);

        VerticalLayout createBox = new VerticalLayout();
        createBox.setWidth(null);
        createBox.addStyleName("create");
        Button create = new Button("Create New");
        create.addStyleName("default");
        createBox.addComponent(create);
        createBox.setComponentAlignment(create, Alignment.MIDDLE_CENTER);
        drafts.addComponent(createBox);
        create.addClickListener(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                autoCreate(0, null, null);
            }
        });

        return editors;
    }

    private HorizontalLayout createEditorInstance(int which,
                                                  Transferable items, Table table) {
        HorizontalLayout editor = new HorizontalLayout();
        editor.setSizeFull();

        SimpleDateFormat df = new SimpleDateFormat();
        df.applyPattern("M/dd/yyyy");
        editor.setCaption((df.format(new Date())));

        return editor;
    }

    public void autoCreate(int which, Transferable items, Table table) {
        editors.addTab(createEditorInstance(which, items, table)).setClosable(
                true);
        editors.setSelectedTab(editors.getComponentCount() - 1);
    };

}

