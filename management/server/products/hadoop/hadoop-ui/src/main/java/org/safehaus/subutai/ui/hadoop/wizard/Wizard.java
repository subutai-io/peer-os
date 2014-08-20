package org.safehaus.subutai.ui.hadoop.wizard;

import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;
import org.safehaus.subutai.api.hadoop.Config;

/**
 * Created by daralbaev on 09.04.14.
 */
public class Wizard {
	private final VerticalLayout grid;
	private int step = 1;
	private Config config = new Config();

	public Wizard() {
		grid = new VerticalLayout();
		grid.setMargin(true);
		grid.setSizeFull();

		putForm();
	}

	private void putForm() {
		grid.removeAllComponents();
		Component component = null;
		switch (step) {
			case 1: {
				component = new WelcomeStep(this);
				break;
			}
			case 2: {
				component = new ConfigurationStep(this);
				break;
			}
			case 3: {
				component = new VerificationStep(this);
				break;
			}
			default: {
				break;
			}
		}

		if (component != null) {
			grid.addComponent(component);
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

	protected void init() {
		step = 1;
		config = new Config();
		putForm();
	}

	public Config getConfig() {
		return config;
	}
}
