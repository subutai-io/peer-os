package org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.view;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Layout;
import com.vaadin.ui.TextArea;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.action.export.ExportChainManager;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.common.chain.Chain;

public class ExportLayoutBuilder {

    public static Layout get() {

        AbsoluteLayout layout = new AbsoluteLayout();

        TextArea textArea = getTextArea();
        UILogger logger = new UILogger(textArea);

        ExportChainManager chainManager = new ExportChainManager(logger);

        layout.addComponent(getButton("Export", chainManager.getStatusChain()), "left: 20px; top: 30px;");
        layout.addComponent(textArea, "left: 180px; top: 30px;");

        return layout;
    }

    private static Button getButton(String name, final Chain chain) {

        Button button = new Button(name);
        button.setWidth(120, Sizeable.UNITS_PIXELS);

        button.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                ExportChainManager.run(chain);
            }
        });

        return button;
    }

    private static TextArea getTextArea() {

        TextArea textArea = new TextArea("Log:");
        textArea.setWidth(600, Sizeable.UNITS_PIXELS);
        textArea.setHeight(600, Sizeable.UNITS_PIXELS);
        textArea.setWordwrap(false);

        return textArea;
    }



}
