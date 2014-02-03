package org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.view;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.action.manage.ChainManager;
import org.safehaus.kiskis.mgmt.server.ui.modules.sqoop.common.chain.Chain;

public class ManageLayoutBuilder {

    public static Layout get() {

        AbsoluteLayout layout = new AbsoluteLayout();

        TextArea textArea = getTextArea();
        UILogger logger = new UILogger(textArea);

        ChainManager chainManager = new ChainManager(logger);
        Chain statusChain = chainManager.getStatusChain();
        Chain installChain = chainManager.getInstallChain();
        Chain removeChain = chainManager.getRemoveChain();

        layout.addComponent(getButton("Check Status", statusChain), "left: 20px; top: 30px;");
        layout.addComponent(getButton("Install", installChain), "left: 20px; top: 70px;");
        layout.addComponent(getButton("Remove", removeChain), "left: 20px; top: 110px;");
        layout.addComponent(textArea, "left: 180px; top: 30px;");

        return layout;
    }

    private static Button getButton(String name, final Chain chain) {

        Button button = new Button(name);
        button.setWidth(120, Sizeable.UNITS_PIXELS);

        button.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                ChainManager.run(chain);
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
