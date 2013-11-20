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
public class Step3 extends Panel {

    public Step3() {
        super("Installation");
        addStyleName("panelexample");

        setWidth(Sizeable.UNITS_PERCENTAGE, 100);

        final FormLayout form = new FormLayout();

        form.setMargin(true);

        form.addComponent(new TextField("Name"));
        form.addComponent(new TextField("Email"));
        Button next = new Button("Next");
        form.addComponent(next);
        setContent(form);
    }

}
