package org.safehaus.subutai.core.environment.ui.manage;


import com.vaadin.ui.Button;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;


@SuppressWarnings("serial")
public class BlueprintLoadForm {

    private final VerticalLayout contentRoot;
    private TextArea blueprintTxtArea;
    private EnvironmentManager environmentManager;


    public BlueprintLoadForm(EnvironmentManager environmentManager) {
        this.environmentManager = environmentManager;

        contentRoot = new VerticalLayout();

        contentRoot.setSpacing(true);
        contentRoot.setMargin(true);

        blueprintTxtArea = getTextArea();

        Button loadBlueprintButton = new Button("Load blueprint");

        loadBlueprintButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(final Button.ClickEvent clickEvent) {
                uploadBlueprint();
            }
        });

        contentRoot.addComponent(blueprintTxtArea);
        contentRoot.addComponent(loadBlueprintButton);
    }


    private TextArea getTextArea() {
        blueprintTxtArea = new TextArea("Blueprint");
        blueprintTxtArea.setSizeFull();
        blueprintTxtArea.setRows(20);
        blueprintTxtArea.setImmediate(true);
        blueprintTxtArea.setWordwrap(false);
        return blueprintTxtArea;
    }


    private void uploadBlueprint() {
        environmentManager.saveBlueprint(blueprintTxtArea.getValue().toString().trim());
    }


    public VerticalLayout getContentRoot() {
        return this.contentRoot;
    }
}
