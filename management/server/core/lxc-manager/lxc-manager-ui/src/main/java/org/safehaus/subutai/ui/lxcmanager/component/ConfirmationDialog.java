package org.safehaus.subutai.ui.lxcmanager.component;

import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.ui.*;

/**
 * Created by daralbaev on 08.06.14.
 */
public class ConfirmationDialog {
    private Window alert;
    private Button discard, cancel, ok;
    private VerticalLayout l;

    public ConfirmationDialog() {
        l = new VerticalLayout();
        l.setWidth("400px");
        l.setMargin(true);
        l.setSpacing(true);

        alert = new Window("Unsaved Changes", l);
        alert.setModal(true);
        alert.setResizable(false);
        alert.setDraggable(false);
        alert.addStyleName("dialog");
        alert.setClosable(false);
    }

    public Window getAlert(){
        Label message = new Label(
                "You have not saved this report. Do you want to save or discard any changes you've made to this report?");
        l.addComponent(message);

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setWidth("100%");
        buttons.setSpacing(true);
        l.addComponent(buttons);

        discard = new Button("Don't Save");
        discard.addStyleName("small");
        discard.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                alert.close();
            }
        });
        buttons.addComponent(discard);
        buttons.setExpandRatio(discard, 1);

        cancel = new Button("Cancel");
        cancel.addStyleName("small");
        cancel.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                alert.close();
            }
        });
        buttons.addComponent(cancel);

        ok = new Button("Save");
        ok.addStyleName("default");
        ok.addStyleName("small");
        ok.addStyleName("wide");
        ok.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                alert.close();
            }
        });
        buttons.addComponent(ok);
        ok.focus();

        alert.addShortcutListener(new ShortcutListener("Cancel",
                ShortcutAction.KeyCode.ESCAPE, null) {
            @Override
            public void handleAction(Object sender, Object target) {
                alert.close();
            }
        });

        return alert;
    }
}
