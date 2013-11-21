/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt;

import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Panel;

/**
 *
 * @author bahadyr
 */
public class Step1 extends FormLayout {

    public Step1(final Terminal.ModuleComponent aThis) {
        setCaption("Welcome");
        setMargin(true);

        Panel panel = new Panel();
        panel.setWidth("300px");
        panel.setCaption("Welcome Cassandra installation wizard. "
                + "Please follow the steps carefully. "
                + "When you are finished the wizard, you will have complete "
                + "working cassandra servers. ");
        
        addComponent(panel);
        Button next = new Button("Next");
        next.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                aThis.showNext();
            }
        });
        addComponent(next);

    }


}
