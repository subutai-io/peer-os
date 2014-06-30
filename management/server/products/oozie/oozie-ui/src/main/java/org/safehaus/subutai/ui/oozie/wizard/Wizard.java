/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.ui.oozie.wizard;

import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;
import org.safehaus.subutai.api.oozie.Config;

/**
 * @author dilshat
 */
public class Wizard {

	private final VerticalLayout vlayout;
	private int step = 1;
	private Config config = new Config();

	public Wizard() {
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
		config = new Config();
		putForm();
	}

	public Config getConfig() {
		return config;
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
