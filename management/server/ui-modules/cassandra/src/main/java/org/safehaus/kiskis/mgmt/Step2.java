/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt;

import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;

/**
 *
 * @author bahadyr
 */
public class Step2 extends FormLayout {

    public Step2(final Terminal.ModuleComponent aThis) {
        setCaption("List nodes");
        setMargin(true);

        addComponent(new Label("Please enter the list of hosts to be included in the cluster"));
        addComponent(new Label("Target hosts"));
        addComponent(new Label("Enter a list of hosts "));
        
        
        
        Button next = new Button("Next");
        next.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                aThis.showNext();
            }
        });
        Button back = new Button("Back");
        back.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                aThis.showBack();
            }
        });
        addComponent(back);
        addComponent(next);

    }

}
