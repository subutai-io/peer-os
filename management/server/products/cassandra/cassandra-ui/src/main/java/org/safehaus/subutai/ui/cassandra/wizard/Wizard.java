/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.ui.cassandra.wizard;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.VerticalLayout;
import org.safehaus.subutai.api.cassandra.Config;

/**
 * @author dilshat
 */
public class Wizard {

	private static final int MAX_STEPS = 3;
	private final VerticalLayout verticalLayout;
	GridLayout grid;
	private int step = 1;
	private Config config = new Config();

	public Wizard() {
		verticalLayout = new VerticalLayout();
		verticalLayout.setSizeFull();
		grid = new GridLayout(1, 1);
		grid.setMargin(true);
		grid.setSizeFull();
		grid.addComponent(verticalLayout);
		grid.setComponentAlignment(verticalLayout, Alignment.TOP_CENTER);

		putForm();

	}

	private void putForm() {
		verticalLayout.removeAllComponents();
		switch (step) {
			case 1: {
				verticalLayout.addComponent(new StepStart(this));
				break;
			}
			case 2: {
				verticalLayout.addComponent(new ConfigurationStep(this));
				break;
			}
			case 3: {
				verticalLayout.addComponent(new VerificationStep(this));
				break;
			}
			default: {
				step = 1;
				verticalLayout.addComponent(new StepStart(this));
				break;
			}
		}
	}

	public Component getContent() {
		return grid;
	}

	protected void next() {
		step++;
		putForm();
	}

	protected void back() {
		step--;
		putForm();
	}

	protected void cancel() {
		step = 1;
		putForm();
	}

	public Config getConfig() {
		return config;
	}

	public void init() {
		step = 1;
		config = new Config();
		putForm();
	}

}
