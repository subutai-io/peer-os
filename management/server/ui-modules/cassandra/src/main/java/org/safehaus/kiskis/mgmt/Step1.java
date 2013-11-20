/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt;

import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;

/**
 *
 * @author bahadyr
 */
public class Step1 extends FormLayout {

    public Step1(final Terminal.ModuleComponent aThis) {
        setCaption("Welcome");
        setMargin(true);

        Label l = new Label();
        l.setCaption("Welcome Cassandra installation wizard. Please follow the steps carefully. When you are finished the wizard, you will have complete working cassandra servers. ");
        addComponent(l);
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
