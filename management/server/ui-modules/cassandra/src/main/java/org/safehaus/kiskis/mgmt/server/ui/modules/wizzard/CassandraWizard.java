package org.safehaus.kiskis.mgmt.server.ui.modules.wizzard;


import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;


public class CassandraWizard extends Window {

    VerticalLayout verticalLayout;
    int step = 1;

    public CassandraWizard() {
        setModal(true);
        setCaption("Cassandra Wizard");

        verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing(true);
        verticalLayout.setHeight(500, Sizeable.UNITS_PIXELS);
        verticalLayout.setWidth(800, Sizeable.UNITS_PIXELS);

        putForm();

        setContent(verticalLayout);
    }

    public void showNext() {
        step++;
        putForm();
    }

    public void showBack() {
        step--;
        putForm();
    }

    private void putForm() {
        verticalLayout.removeAllComponents();
        switch (step) {
            case 1: {
                Step1 step1 = new Step1(this);
                verticalLayout.addComponent(step1);
                break;
            }
            case 2: {
                Step2 step2 = new Step2(this);
                verticalLayout.addComponent(step2);
                break;
            }
            case 3: {
                Step3 step3 = new Step3(this);
                verticalLayout.addComponent(step3);
                break;
            }
            case 4: {
                Step41 step41 = new Step41(this);
                verticalLayout.addComponent(step41);
                break;
            }
            case 5: {
                Step42 step42 = new Step42(this);
                verticalLayout.addComponent(step42);
                break;
            }
            case 6: {
                Step43 step43 = new Step43(this);
                verticalLayout.addComponent(step43);
                break;
            }
            default: {
                step = 1;
                Step1 step1 = new Step1(this);
                verticalLayout.addComponent(step1);
                break;
            }
        }
    }
}