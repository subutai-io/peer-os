/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.oozie.ui.wizard;


import org.safehaus.subutai.plugin.oozie.api.OozieClusterConfig;
import org.safehaus.subutai.plugin.oozie.ui.OozieUI;

import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;

/**
 * @author dilshat
 */
public class Wizard {

	private final VerticalLayout vlayout;
	private int step = 1;
	private OozieClusterConfig config = new OozieClusterConfig();
    private OozieUI oozieUI;

	public Wizard(OozieUI oozieUI) {
        this.oozieUI = oozieUI;
		vlayout = new VerticalLayout();
		vlayout.setSizeFull();
		vlayout.setMargin(true);
		putForm();
	}

	public Component getContent() {
		return vlayout;
	}

	public void next() {
		step++;
		putForm();
	}

	public void back() {
		step--;
		putForm();
	}

	public void cancel() {
		step = 1;
		putForm();
	}

	public void init() {
		step = 1;
		config = new OozieClusterConfig();
		putForm();
	}

	public OozieClusterConfig getConfig() {
		return config;
	}


    public OozieUI getOozieUI() {
        return oozieUI;
    }


    public void setOozieUI( final OozieUI oozieUI ) {
        this.oozieUI = oozieUI;
    }


    private void putForm() {
		vlayout.removeAllComponents();
		switch (step) {
			case 1: {
				vlayout.addComponent(new StepStart(this));
				break;
			}
			case 2: {
				vlayout.addComponent(new ConfigurationStep(this));
				break;
			}
			case 3: {
				vlayout.addComponent(new StepSetConfig(this));
				break;
			}
			case 4: {
				vlayout.addComponent(new VerificationStep(this));
				break;
			}
			default: {
				step = 1;
				vlayout.addComponent(new StepStart(this));
				break;
			}
		}
	}

}
