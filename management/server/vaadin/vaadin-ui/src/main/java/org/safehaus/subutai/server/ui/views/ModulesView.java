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

import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.event.Transferable;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.shared.ui.label.ContentMode;
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

        /*editors.setCloseHandler(new TabSheet.CloseHandler() {
            @Override
            public void onTabClose(TabSheet tabsheet, final Component tabContent) {

            }
        });*/

        final VerticalLayout center = new VerticalLayout();
        center.setSizeFull();
        center.setCaption("All Drafts");
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

        CssLayout draftThumb = new CssLayout();
        draftThumb.addStyleName("draft-thumb");
        Image draft = new Image(null, new ThemeResource(
                "img/draft-report-thumb.png"));
        draftThumb.addComponent(draft);
        Label draftTitle = new Label(
                "Monthly revenue<br><span>Last modified 1 day ago</span>",
                ContentMode.HTML);
        draftTitle.setSizeUndefined();
        draftThumb.addComponent(draftTitle);
        drafts.addComponent(draftThumb);
        // TODO layout bug, we need to set the alignment also for the first
        // child
        drafts.setComponentAlignment(draftThumb, Alignment.MIDDLE_CENTER);

        final Button delete = new Button("×");
        delete.setPrimaryStyleName("delete-button");
        draftThumb.addComponent(delete);
        delete.addClickListener(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                Notification.show("Not implemented in this demo");
            }
        });

        draftThumb.addLayoutClickListener(new LayoutClickListener() {
            @Override
            public void layoutClick(LayoutClickEvent event) {
                if (event.getButton() == MouseButton.LEFT
                        && event.getChildComponent() != delete) {
                    editors.addTab(createEditorInstance(1, null, null))
                            .setClosable(true);
                    editors.setSelectedTab(editors.getComponentCount() - 1);
                }
            }
        });
        draft.setDescription("Click to edit");
        delete.setDescription("Delete draft");

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

