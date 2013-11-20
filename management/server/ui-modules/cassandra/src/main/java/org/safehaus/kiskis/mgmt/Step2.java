/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;

/**
 *
 * @author bahadyr
 */
public class Step2 extends FormLayout {

    public Step2() {
        setCaption("List nodes");
        setMargin(true);

        addComponent(new TextField("Name"));
        addComponent(new TextField("Email"));
        Button next = new Button("Next");
        next.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
            }
        });
        addComponent(next);

    }

}
