/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.wizard;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Button;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.exec.Installer;
import org.safehaus.kiskis.mgmt.shared.protocol.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.CommandJson;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;

/**
 *
 * @author dilshat
 */
public class Step4 extends Panel {

    private final TextArea outputTxtArea;

    public Step4(final MongoWizard mongoWizard) {

        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        content.setHeight(100, Sizeable.UNITS_PERCENTAGE);
        content.setMargin(true);

        outputTxtArea = new TextArea("Installation output");
        outputTxtArea.setRows(20);
        outputTxtArea.setColumns(100);
        outputTxtArea.setImmediate(true);
        outputTxtArea.setWordwrap(true);

        content.addComponent(outputTxtArea);

        Button ok = new Button("OK");
        ok.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                mongoWizard.init();
            }
        });

        content.addComponent(ok);
        addComponent(content);

        Installer installer = new Installer(mongoWizard);
        installer.start();
    }

    private void show(String notification) {
        getWindow().showNotification(notification);
    }

    protected void onResponse(Response response) {
        String output = outputTxtArea.getValue() + "\n"
                + CommandJson.getJson(new Command(response));
        outputTxtArea.setValue(output);
        outputTxtArea.setCursorPosition(output.length() - 1);
    }

}
