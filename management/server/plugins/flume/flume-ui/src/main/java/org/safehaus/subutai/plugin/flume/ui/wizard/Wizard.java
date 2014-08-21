package org.safehaus.subutai.plugin.flume.ui.wizard;

import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import org.safehaus.subutai.plugin.flume.api.FlumeConfig;

public class Wizard {

	private final GridLayout grid;
	private int step = 1;
    private FlumeConfig config = new FlumeConfig();

	public Wizard() {
		grid = new GridLayout(1, 20);
		grid.setMargin(true);
		grid.setSizeFull();

		putForm();

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
        config = new FlumeConfig();
		putForm();
	}

    public FlumeConfig getConfig() {
		return config;
	}

	private void putForm() {
		grid.removeComponent(0, 1);
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
			grid.addComponent(component, 0, 1, 0, 19);
		}
	}

}
