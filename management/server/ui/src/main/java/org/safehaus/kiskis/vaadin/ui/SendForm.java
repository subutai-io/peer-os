/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.vaadin.ui;

import com.vaadin.ui.Button;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextArea;

/**
 * @author daralbaev
 */
public class SendForm extends Form {

    private Button send = new Button("Send");
    private TextArea console = new TextArea();

    public SendForm() {
        console.setWidth("95%");
        console.setHeight("500px");
        addField("exit_code", console);

        HorizontalLayout footer = new HorizontalLayout();
        footer.setSpacing(true);
        footer.addComponent(send);
        setFooter(footer);
    }

    public void initIp() {
        send.setEnabled(true);
    }
}
